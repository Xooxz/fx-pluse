package com.xooxz.stream.infrastructure.redis

import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Redis에 저장되는 최신 환율 캐시 정보
 * @param symbol        통화 코드
 * @param price         현재 환율
 * @param previousPrice 직전 환율
 * @param change        이전 환율 대비 변동 금액
 * @param changeRate    이전 환율 대비 변동률(%)
 * @param updatedAt     마지막 갱신 시각
 */
data class CachedRate(
    val symbol: String,
    val price: BigDecimal,
    val previousPrice: BigDecimal?,
    val change: BigDecimal,
    val changeRate: BigDecimal,
    val updatedAt: LocalDateTime
)