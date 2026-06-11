package com.xooxz.stream.application

import com.xooxz.stream.domain.RateGenerator
import com.xooxz.stream.infrastructure.RateEventProducer
import com.xooxz.stream.presentation.RateResponse
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.Duration
import java.time.LocalDateTime

@Service
class RateStreamService(
    private val rateGenerator: RateGenerator,
    private val redisTemplate: ReactiveStringRedisTemplate,
    private val rateEventProducer: RateEventProducer
) {

    companion object {
        private val log = LoggerFactory.getLogger(RateStreamService::class.java)
    }

    /**
     * 통화쌍에 대한 최신 환율 조회
     * @param symbol 통화쌍 코드
     * @return 최신 환율 정보
     */
    fun getRate(symbol: String): Mono<RateResponse> {
        val key = "rate:$symbol"

        return redisTemplate.opsForValue()
            .get(key)
            .switchIfEmpty(
                Mono.error(
                    IllegalArgumentException(
                        "Rate not found : $symbol"
                    )
                )
            )
            .map { price ->
                log.debug("getRate - symbol: $symbol, price: $price")
                RateResponse(symbol, BigDecimal(price), LocalDateTime.now())
            }
    }

    /**
     * 실시간 환율 스트림 생성
     * @return 실시간 환율 스트림
     */
    fun streamRates(symbol: String): Flux<RateResponse> {
        return Flux.interval(Duration.ofSeconds(1))
            .map {
                rateGenerator.createDummyRate(symbol)
            }
            .flatMap { rate ->
                saveLatestRate(rate)
                    .then(rateEventProducer.send(rate))
                    .thenReturn(rate)
            }
    }

    /**
     * 최신 환율 Redis 저장
     * @param rate 저장할 환율 정보
     * @return 저장 성공 여부
     */
    private fun saveLatestRate(rate: RateResponse): Mono<Boolean> {
        val key = "rate:${rate.symbol}"

        log.debug("saveLatestRate - symbol: {}, price: {}", rate.symbol, rate.price)
        return redisTemplate.opsForValue().set(key, rate.price.toString(), Duration.ofMinutes(10))
    }

}