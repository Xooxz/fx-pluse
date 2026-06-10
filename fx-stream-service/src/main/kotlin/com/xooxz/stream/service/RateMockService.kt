package com.xooxz.stream.service

import com.xooxz.stream.dto.RateResponse
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.ThreadLocalRandom

@Service
class RateMockService(
    private val redisTemplate: ReactiveStringRedisTemplate
) {

    companion object {
        private val log = LoggerFactory.getLogger(RateMockService::class.java)
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
            .map { price ->
                log.info("테스트")
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
                createDummyRate(symbol)
            }
            .flatMap { rate ->
                saveLatestRate(rate).thenReturn(rate)
            }
    }

    /**
     * 최신 환율 Redis 저장
     * @param rate 저장할 환율 정보
     * @return 저장 성공 여부
     */
    private fun saveLatestRate(rate: RateResponse): Mono<Boolean> {
        val key = "rate:${rate.symbol}"

        return redisTemplate.opsForValue().set(key, rate.price.toString(), Duration.ofMinutes(10))
    }

    /**
     * 테스트용 환율 더미데이터 생성
     * @param symbol 통화쌍 코드
     * @return 생성된 환율 정보
     */
    private fun createDummyRate(symbol: String): RateResponse {
        val randomPrice = ThreadLocalRandom.current()
            .nextDouble(1370.0, 1400.0)

        return RateResponse(
            symbol,
            BigDecimal.valueOf(randomPrice).setScale(2, RoundingMode.HALF_UP),
            LocalDateTime.now()
        )
    }

}