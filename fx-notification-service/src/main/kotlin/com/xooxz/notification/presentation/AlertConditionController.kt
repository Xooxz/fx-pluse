package com.xooxz.notification.presentation

import com.xooxz.notification.domain.RateAlertCondition
import com.xooxz.notification.infrastructure.redis.AlertConditionRepository
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(value = ["/alerts"])
class AlertConditionController (
    private val alertConditionRepository: AlertConditionRepository
    ){

    @PostMapping
    fun create(
        @RequestBody request: AlertConditionCreateRequest
    ){

        val condition = RateAlertCondition(
            userId = request.userId,
            alertSeq = request.alertSeq,
            symbol = request.symbol,
            targetPrice = request.targetPrice,
            operator = request.operator,
            interval = request.interval
        )

        alertConditionRepository.save(condition)
    }
}
