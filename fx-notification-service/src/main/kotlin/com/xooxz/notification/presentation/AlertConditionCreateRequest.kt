package com.xooxz.notification.presentation

import com.xooxz.notification.domain.AlertInterval
import com.xooxz.notification.domain.AlertOperator
import java.math.BigDecimal

data class AlertConditionCreateRequest (
    val userId: Long,
    val symbol: String,
    val targetPrice: BigDecimal,
    val operator: AlertOperator,
    val interval: AlertInterval
)
