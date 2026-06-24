package com.xooxz.stream.infrastructure.redis

import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Redis에 저장되는 최신 환율 캐시 정보
 * @param symbol    통화쌍 코드
 * @param price     현재 환율
 * @param updatedAt 마지막으로 갱신된 시각
 */
data class CachedRate(
    val symbol: String,
    val price: BigDecimal,
    val updatedAt: LocalDateTime
)