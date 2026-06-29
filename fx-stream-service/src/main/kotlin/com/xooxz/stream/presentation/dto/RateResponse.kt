package com.xooxz.stream.presentation.dto

import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 환율 조회 응답 DTO
 * @param symbol         통화 코드
 * @param price          현재 환율
 * @param previousPrice  직전 환율
 * @param change         직전 환율 대비 변동 금액
 * @param changeRate     직전 환율 대비 변동률(%)
 * @param updatedAt      환율 마지막 갱신 시각
 */
data class RateResponse(
    val symbol: String,
    val price: BigDecimal,
    val previousPrice: BigDecimal?,
    val change: BigDecimal,
    val changeRate: BigDecimal,
    val updatedAt: LocalDateTime
)