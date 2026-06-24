package com.xooxz.stream.application

import com.fasterxml.jackson.databind.ObjectMapper
import com.xooxz.stream.infrastructure.redis.CachedRate
import com.xooxz.stream.domain.exception.RateNotFoundException
import com.xooxz.stream.domain.exception.UnsupportedCurrencyException
import com.xooxz.stream.domain.model.CurrencyPair
import com.xooxz.stream.domain.service.RateGenerator
import com.xooxz.stream.infrastructure.kafka.RateEventProducer
import com.xooxz.stream.presentation.dto.RateResponse
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration
import java.time.LocalDateTime

@Service
class RateStreamService(
    private val rateGenerator: RateGenerator,
    private val redisTemplate: ReactiveStringRedisTemplate,
    private val rateEventProducer: RateEventProducer,
    private val objectMapper: ObjectMapper
) {

    companion object {
        private val log = LoggerFactory.getLogger(RateStreamService::class.java)
        private val STALE_THRESHOLD = Duration.ofMinutes(1)
    }

    /**
     * 통화쌍에 대한 최신 환율 조회
     * @param symbol 통화쌍 코드
     * @return 최신 환율 정보
     */
    fun getRate(symbol: String): Mono<RateResponse> {
        if (!CurrencyPair.isValid(symbol)) {
            return Mono.error(
                UnsupportedCurrencyException(symbol)
            )
        }

        return redisTemplate.opsForValue()
            .get("rate:$symbol")
            .switchIfEmpty(
                Mono.error(
                    RateNotFoundException(symbol)
                )
            )
            .map { json ->
                objectMapper.readValue(json, CachedRate::class.java)
            }
            .flatMap { cachedRate ->
                log.debug("getRate: {}", cachedRate)

                if (cachedRate.updatedAt.isBefore(LocalDateTime.now().minus(STALE_THRESHOLD))) {
                    return@flatMap Mono.error(
                        RateNotFoundException(symbol)
                    )
                }

                Mono.just(
                    RateResponse(
                        symbol = cachedRate.symbol,
                        price = cachedRate.price,
                        createdAt = cachedRate.updatedAt
                    )
                )
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
        val cachedRate = CachedRate(
            symbol = rate.symbol,
            price = rate.price,
            updatedAt = LocalDateTime.now()
        )

        log.debug("saveLatestRate: {}", cachedRate)
        return redisTemplate.opsForValue()
            .set(
                "rate:${rate.symbol}",
                objectMapper.writeValueAsString(cachedRate)
            )
    }

}