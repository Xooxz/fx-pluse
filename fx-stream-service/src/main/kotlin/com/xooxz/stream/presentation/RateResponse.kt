package com.xooxz.stream.presentation

import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 환율 조회 응답 DTO
 * @param symbol    통화쌍 코드 (ex: USD-KRW)
 * @param price     현재 환율
 * @param createdAt 환율 생성 시각
 */
data class RateResponse(
    val symbol: String,
    val price: BigDecimal,
    val createdAt: LocalDateTime
)