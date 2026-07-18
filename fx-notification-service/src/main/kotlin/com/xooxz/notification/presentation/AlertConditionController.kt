package com.xooxz.notification.presentation

import com.xooxz.notification.application.AlertConditionService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping(value = ["/alerts"])
class AlertConditionController (
        private val alertConditionService: AlertConditionService
    ){

    private val log = KotlinLogging.logger {}

    /**
     * 환율 알림 조건 등록 API
     *
     * @param request 환율 알림 조건
     */
    @PostMapping
    fun create(@RequestBody request: AlertConditionCreateRequest): Mono<Boolean> {
        log.info { "환율 알림 조건 등록 요청" }
        return alertConditionService.create(request)
    }
}
