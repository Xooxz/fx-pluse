package com.xooxz.stream.domain

import com.xooxz.stream.presentation.RateResponse
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import java.util.concurrent.ThreadLocalRandom

@Component
class RateGenerator {

    /**
     * 테스트용 환율 더미데이터 생성
     * @param symbol 통화쌍 코드
     * @return 생성된 환율 정보
     */
    fun createDummyRate(symbol: String): RateResponse {
        val randomPrice = ThreadLocalRandom.current()
            .nextDouble(1370.0, 1400.0)

        return RateResponse(
            symbol = symbol,
            price = BigDecimal.valueOf(randomPrice).setScale(2, RoundingMode.HALF_UP),
            createdAt = LocalDateTime.now()
        )
    }

}