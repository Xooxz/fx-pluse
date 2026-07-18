package com.xooxz.stream.application

import com.fasterxml.jackson.databind.ObjectMapper
import com.xooxz.stream.domain.exception.RateNotFoundException
import com.xooxz.stream.domain.exception.UnsupportedCurrencyException
import com.xooxz.stream.infrastructure.persistence.CurrencyPairRepository
import com.xooxz.stream.infrastructure.redis.CachedRate
import com.xooxz.stream.presentation.dto.RateResponse
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration
import java.time.LocalDateTime
import kotlin.String

@Service
class RateService(
    private val redisTemplate: ReactiveStringRedisTemplate,
    private val objectMapper: ObjectMapper,
    private val currencyPairRepository: CurrencyPairRepository
) {

    /**
     * 지원 통화 여부를 검증한 뒤 Redis에 저장된 최신 환율 정보를 조회
     * @param symbol 통화 코드
     * @return 최신 환율 정보
     */
    fun getRate(symbol: String): Mono<RateResponse> {
        return currencyPairRepository.existsBySymbolAndUseYnTrue(symbol)
            .flatMap { exists ->
                if (!exists) {
                    return@flatMap Mono.error(
                        UnsupportedCurrencyException(symbol)
                    )
                }

                getRateFromRedis(symbol)
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
     * @return 전체 통화 환율 스트림
     */
    fun streamRates(): Flux<List<RateResponse>> {
        return Flux.interval(Duration.ZERO, Duration.ofSeconds(1))
            .flatMap {
                currencyPairRepository.findAllByUseYnTrue().flatMap { currency ->
                    getRateFromRedis(currency.symbol).onErrorResume {
                        Mono.empty()
                    }
                }
                    .collectList()
            }
    }

    /**
     * Redis에서 최신 환율 정보를 조회
     * @param symbol 통화 코드
     * @return 최신 환율 정보
     */
    private fun getRateFromRedis(symbol: String): Mono<RateResponse> {
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
                        countryName = cachedRate.countryName,
                        price = cachedRate.price,
                        change = cachedRate.change,
                        changeRate = cachedRate.changeRate,
                        updatedAt = cachedRate.updatedAt
                    )
                )
            }
    }

    companion object {
        private val STALE_THRESHOLD = Duration.ofMinutes(1)
    }

}