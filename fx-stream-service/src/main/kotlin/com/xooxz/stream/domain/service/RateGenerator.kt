package com.xooxz.stream.domain.service

import com.xooxz.stream.presentation.dto.RateResponse
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import java.util.concurrent.ThreadLocalRandom

@Component
class RateGenerator {

    /**
     * 테스트용 현재 환율 생성
     * @param symbol      통화 코드
     * @param countryName 국가명
     * @param minRate     환율 최소값
     * @param maxRate     환율 최대값
     * @return 생성된 현재 환율
     */
    fun createDummyRate(
        symbol: String,
        countryName: String,
        minRate: Double,
        maxRate: Double
    ): RateResponse {
        val randomPrice = ThreadLocalRandom.current()
            .nextDouble(minRate, maxRate)

        return RateResponse(
            symbol = symbol,
            countryName = countryName,
            price = BigDecimal.valueOf(randomPrice).setScale(2, RoundingMode.HALF_UP),
            change = BigDecimal.ZERO,
            changeRate = BigDecimal.ZERO,
            updatedAt = LocalDateTime.now()
        )
    }

}