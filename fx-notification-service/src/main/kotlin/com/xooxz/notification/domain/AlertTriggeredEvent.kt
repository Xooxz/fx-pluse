package com.xooxz.notification.domain

import java.math.BigDecimal
import java.time.LocalDateTime

/**
* 알림 조건이 충족됐을 때 생성되는 이벤트 정보
* */
data class AlertTriggeredEvent (
    val mbrKey: String,
    val alertSeq: Long,
    val symbol: String,
    val targetPrice: BigDecimal,
    val currentPrice: BigDecimal,
    val operator: AlertOperator,
    val message: String,
    val createdAt: LocalDateTime = LocalDateTime.now()
)