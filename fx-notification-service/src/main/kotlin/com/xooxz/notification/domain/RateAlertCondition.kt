package com.xooxz.notification.domain

import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 사용자가 등록한 환율 알림 설정 정보
 */
data class RateAlertCondition(
    val userId: Long,
    val alertSeq: Long,
    val symbol: String,
    val targetPrice: BigDecimal,
    val operator: AlertOperator,
    val interval: AlertInterval,
    val useYn: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now(),

    // 마지막 발송 성공 시각
    val lastSentAt: LocalDateTime? = null
)