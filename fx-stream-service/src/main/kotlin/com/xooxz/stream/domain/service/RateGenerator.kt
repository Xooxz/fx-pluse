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
     * @param symbol 통화 코드
     * @return 생성된 현재 환율
     */
    fun createDummyRate(symbol: String): RateResponse {
        val randomPrice = ThreadLocalRandom.current()
            .nextDouble(1370.0, 1400.0)

        return RateResponse(
            symbol = symbol,
            price = BigDecimal.valueOf(randomPrice)
                .setScale(2, RoundingMode.HALF_UP),
            previousPrice = null,
            change = BigDecimal.ZERO,
            changeRate = BigDecimal.ZERO,
            updatedAt = LocalDateTime.now()
        )
    }
}