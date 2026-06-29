package com.xooxz.stream.application

import com.fasterxml.jackson.databind.ObjectMapper
import com.xooxz.stream.infrastructure.redis.CachedRate
import com.xooxz.stream.domain.exception.RateNotFoundException
import com.xooxz.stream.domain.exception.UnsupportedCurrencyException
import com.xooxz.stream.domain.model.CurrencyPair
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
    private val redisTemplate: ReactiveStringRedisTemplate,
    private val objectMapper: ObjectMapper
) {

    companion object {
        private val log = LoggerFactory.getLogger(RateStreamService::class.java)
        private val STALE_THRESHOLD = Duration.ofMinutes(1)
    }

    /**
     * Redis에 저장된 최신 환율 정보를 조회
     * @param symbol 통화 코드
     * @return 최신 환율 정보
     */
    fun getRate(symbol: String): Mono<RateResponse> {
        if (!CurrencyPair.isValid(symbol)) {
            return Mono.error(UnsupportedCurrencyException(symbol))
        }

        return redisTemplate.opsForValue()
            .get("rate:$symbol")
            .switchIfEmpty(Mono.error(RateNotFoundException(symbol)))
            .map { json ->
                objectMapper.readValue(json, CachedRate::class.java)
            }
            .flatMap { cachedRate ->
                if (cachedRate.updatedAt.isBefore(LocalDateTime.now().minus(STALE_THRESHOLD))) {
                    return@flatMap Mono.error(
                        RateNotFoundException(symbol)
                    )
                }

                Mono.just(
                    RateResponse(
                        symbol = cachedRate.symbol,
                        price = cachedRate.price,
                        previousPrice = cachedRate.previousPrice,
                        change = cachedRate.change,
                        changeRate = cachedRate.changeRate,
                        updatedAt = cachedRate.updatedAt
                    )
                )
            }
    }

    /**
     * 단일 통화의 최신 환율을 SSE(Server-Sent Events)로 스트리밍
     * @param symbol 통화 코드
     * @return 실시간 환율 스트림
     */
    fun getStreamRates(symbol: String): Flux<RateResponse> {
        return Flux.interval(Duration.ZERO, Duration.ofSeconds(1))
            .flatMap {
                getRate(symbol)
            }
    }

    /**
     * 지원하는 전체 통화의 최신 환율을 SSE로 스트리밍
     */
    fun streamRates(): Flux<List<RateResponse>> {
        return Flux.interval(Duration.ZERO, Duration.ofSeconds(1))
            .flatMap {
                Flux.fromIterable(CurrencyPair.entries)
                    .flatMap { currency ->
                        getRate(currency.symbol)
                            .onErrorResume {
                                Mono.empty()
                            }
                    }
                    .collectList()
            }
    }

}