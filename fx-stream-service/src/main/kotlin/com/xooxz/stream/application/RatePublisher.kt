package com.xooxz.stream.application

import com.fasterxml.jackson.databind.ObjectMapper
import com.xooxz.stream.domain.model.CurrencyPair
import com.xooxz.stream.domain.service.RateGenerator
import com.xooxz.stream.infrastructure.kafka.RateEventProducer
import com.xooxz.stream.infrastructure.redis.CachedRate
import com.xooxz.stream.presentation.dto.RateResponse
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.stereotype.Component
import reactor.core.Disposable
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Duration
import java.time.LocalDateTime

/**
 * 실시간 환율 발행 컴포넌트
 */
@Component
class RatePublisher(
    private val rateGenerator: RateGenerator,
    private val redisTemplate: ReactiveStringRedisTemplate,
    private val rateEventProducer: RateEventProducer,
    private val objectMapper: ObjectMapper
) : ApplicationRunner {

    companion object {
        private val log = LoggerFactory.getLogger(RatePublisher::class.java)
    }

    private var disposable: Disposable? = null

    override fun run(args: ApplicationArguments) {
        start()
    }

    /**
     * 환율 생성 및 발행을 시작
     */
    fun start() {
        if (isRunning()) {
            log.info("RatePublisher is already running")
            return
        }

        disposable = Flux.interval(Duration.ofSeconds(1))
            .flatMap {
                Flux.fromIterable(CurrencyPair.entries)
            }
            .flatMap { currency ->
                val rate = rateGenerator.createDummyRate(currency.symbol)

                saveLatestRate(rate)
                    .then(rateEventProducer.send(rate))
                    .thenReturn(rate)
            }
            .doOnNext { rate ->
                log.debug("published rate: {}", rate)
            }
            .doOnError { ex ->
                log.error("RatePublisher error", ex)
            }
            .subscribe()

        log.info("RatePublisher started")
    }

    /**
     * 환율 생성 및 발행을 중지
     */
    fun stop() {
        disposable?.dispose()
        log.info("RatePublisher stopped")
    }

    /**
     * Publisher의 실행 여부를 반환
     * @return true[실행 중], false[실행 중X]
     */
    fun isRunning(): Boolean {
        return disposable?.isDisposed == false
    }

    /**
     * 최신 환율을 Redis에 저장
     * @param rate 저장할 환율 정보
     * @return 저장 성공 여부
     */
    private fun saveLatestRate(rate: RateResponse): Mono<Boolean> {
        val key = "rate:${rate.symbol}"

        return redisTemplate.opsForValue()
            .get(key)
            .map { json ->
                objectMapper.readValue(json, CachedRate::class.java)
            }
            .defaultIfEmpty(
                CachedRate(
                    symbol = rate.symbol,
                    price = rate.price,
                    previousPrice = null,
                    change = BigDecimal.ZERO,
                    changeRate = BigDecimal.ZERO,
                    updatedAt = LocalDateTime.now()
                )
            )
            .flatMap { previous ->
                val change = rate.price.subtract(previous.price)
                val changeRate =
                    if (previous.price.compareTo(BigDecimal.ZERO) == 0) {
                        BigDecimal.ZERO
                    } else {
                        change
                            .divide(previous.price, 6, RoundingMode.HALF_UP)
                            .multiply(BigDecimal("100"))
                            .setScale(2, RoundingMode.HALF_UP)
                    }

                val cachedRate = CachedRate(
                    symbol = rate.symbol,
                    price = rate.price,
                    previousPrice = previous.price,
                    change = change.setScale(2, RoundingMode.HALF_UP),
                    changeRate = changeRate,
                    updatedAt = LocalDateTime.now()
                )

                redisTemplate.opsForValue()
                    .set(key, objectMapper.writeValueAsString(cachedRate))
            }
    }

}