package com.xooxz.stream.application

import com.fasterxml.jackson.databind.ObjectMapper
import com.xooxz.stream.domain.service.RateGenerator
import com.xooxz.stream.infrastructure.kafka.RateEventProducer
import com.xooxz.stream.infrastructure.persistence.CurrencyPairEntity
import com.xooxz.stream.infrastructure.persistence.CurrencyPairRepository
import com.xooxz.stream.infrastructure.redis.CachedRate
import com.xooxz.stream.presentation.dto.RateResponse
import io.github.oshai.kotlinlogging.KotlinLogging
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
import java.util.concurrent.atomic.AtomicReference

/**
 * 실시간 환율 발행 컴포넌트
 */
@Component
class RatePublisher(
    private val rateGenerator: RateGenerator,
    private val redisTemplate: ReactiveStringRedisTemplate,
    private val rateEventProducer: RateEventProducer,
    private val objectMapper: ObjectMapper,
    private val currencyPairRepository: CurrencyPairRepository
) : ApplicationRunner {

    private val log = KotlinLogging.logger {}
    private var disposable: Disposable? = null

    /**
     * 발행 대상 통화 목록 캐시
     */
    private val currenciesRef =
        AtomicReference<List<CurrencyPairEntity>>(emptyList())

    /**
     * 애플리케이션 시작 시 통화 목록을 로딩한 뒤 Publisher 실행
     */
    override fun run(args: ApplicationArguments) {
        reloadCurrencies()
            .doOnSuccess {
                start()
            }
            .subscribe()
    }

    /**
     * DB에서 사용 가능한 통화 목록을 조회하여 메모리에 저장
     */
    fun reloadCurrencies(): Mono<Void> {
        return currencyPairRepository.findAllByUseYnTrue()
            .collectList()
            .doOnNext { loadedCurrencies ->
                currenciesRef.set(loadedCurrencies)
                log.info { "Currency pairs loaded. count=${loadedCurrencies.size}" }
            }
            .then()
    }

    /**
     * 환율 생성 및 발행을 시작
     */
    fun start() {
        if (isRunning()) {
            log.info { "RatePublisher is already running" }
            return
        }

        if (currenciesRef.get().isEmpty()) {
            log.warn { "RatePublisher cannot start. currency list is empty." }
            return
        }

        disposable = Flux.interval(Duration.ZERO, Duration.ofSeconds(1))
            .flatMapIterable { tick ->
                currenciesRef.get()
                    .filter { currency ->
                        val currentTick = tick + 1
                        currentTick % currency.period.toLong() == 0L
                    }
            }
            .flatMap { currency ->
                val rate = rateGenerator.createDummyRate(
                    symbol = currency.symbol,
                    countryName = currency.countryName,
                    minRate = currency.minRate,
                    maxRate = currency.maxRate
                )

                saveLatestRate(rate)
                    .then(rateEventProducer.send(rate))
                    .thenReturn(rate)
            }
            .doOnError { ex ->
                log.error(ex) { "RatePublisher error" }
            }
            .subscribe()

        log.info { "RatePublisher started" }
    }

    /**
     * 환율 생성 및 발행을 중지
     */
    fun stop() {
        disposable?.dispose()
        log.info { "RatePublisher stopped" }
    }

    /**
     * Publisher의 실행 여부를 반환
     * @return 실행 여부
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
                    countryName = rate.countryName,
                    price = rate.price,
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
                    countryName = rate.countryName,
                    price = rate.price,
                    change = change.setScale(2, RoundingMode.HALF_UP),
                    changeRate = changeRate,
                    updatedAt = LocalDateTime.now()
                )

                redisTemplate.opsForValue()
                    .set(key, objectMapper.writeValueAsString(cachedRate))
            }
    }
}