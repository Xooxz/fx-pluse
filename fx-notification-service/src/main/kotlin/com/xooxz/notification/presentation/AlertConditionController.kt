package com.xooxz.notification.presentation

import com.xooxz.notification.application.AlertConditionService
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping(value = ["/alerts"])
class AlertConditionController (
        private val alertConditionService: AlertConditionService
    ){

    @PostMapping
    fun create(@RequestBody request: AlertConditionCreateRequest): Mono<Boolean> {
        return alertConditionService.create(request)
    }
}
