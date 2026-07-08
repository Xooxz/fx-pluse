package com.xooxz.notification.domain

import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 사용자가 등록한 환율 알림 설정 정보
 */
data class RateAlertCondition(
    val alertConditionId: Long? = null,
    val mbrKey: String,
    val alertSeq: Long,
    val symbol: String,
    val targetPrice: BigDecimal,
    val operator: AlertOperator,
    val interval: AlertInterval,
    val lastSentAt: LocalDateTime? = null,  // 마지막 발송 성공 시각
    val useYn: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val createdBy: String,
    val modifiedAt: LocalDateTime? = null,
    val modifiedBy: String? = null
)