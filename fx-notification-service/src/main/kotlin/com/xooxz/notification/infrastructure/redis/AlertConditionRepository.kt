package com.xooxz.notification.infrastructure.redis

import com.xooxz.notification.domain.RateAlertCondition

interface  AlertConditionRepository {
    fun save(condition: RateAlertCondition)
    fun findBySymbol(symbol: String): List<RateAlertCondition>
}