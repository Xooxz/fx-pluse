package com.xooxz.stream.infrastructure.kafka

import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 환율 변경 이벤트
 * @param symbol    통화쌍 코드 (ex: USD-KRW)
 * @param price     현재 환율
 * @param createdAt 환율 생성 시각
 */
data class RateUpdatedEvent(
    val symbol: String,
    val price: BigDecimal,
    val createdAt: LocalDateTime
)