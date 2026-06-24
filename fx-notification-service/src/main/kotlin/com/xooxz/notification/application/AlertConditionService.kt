package com.xooxz.notification.application

import com.xooxz.notification.domain.AlertInterval
import com.xooxz.notification.domain.AlertOperator
import com.xooxz.notification.domain.RateAlertCondition
import com.xooxz.notification.infrastructure.kafka.RateUpdatedEvent
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class AlertConditionService {

    private val conditions = listOf(
        RateAlertCondition(
            userId = 1L,
            alertSeq = 1L,
            symbol = "USD-KRW",
            targetPrice = BigDecimal("1390"),
            operator = AlertOperator.GREATER_THAN_OR_EQUAL,
            interval = AlertInterval.TEN_MINUTES
        )
    )

    fun handle(event: RateUpdatedEvent) {
        conditions
            .filter { it.symbol == event.symbol }
            .forEach { condition ->
                if (isMatched(condition, event)) {
                    println("!!알림 발행 대상!! userId=${condition.userId}, rate=${event.price}")
                } else {
                    println("~알림 발행 미대상~ userId=${condition.userId}, rate=${event.price}");
                }
            }
    }

    private fun isMatched(condition: RateAlertCondition, event: RateUpdatedEvent): Boolean {
        return when (condition.operator) {
            AlertOperator.GREATER_THAN_OR_EQUAL -> event.price >= condition.targetPrice
            AlertOperator.LESS_THAN_OR_EQUAL -> event.price <= condition.targetPrice
        }
    }
}