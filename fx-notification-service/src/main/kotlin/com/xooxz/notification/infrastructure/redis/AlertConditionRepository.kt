package com.xooxz.notification.infrastructure.redis

import com.xooxz.notification.domain.RateAlertCondition
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface  AlertConditionRepository {
    fun nextAlertSeq(userId: Long): Mono<Long>
    fun save(condition: RateAlertCondition): Mono<Boolean>
    fun findBySymbol(symbol: String): Flux<RateAlertCondition>
    fun update(condition: RateAlertCondition): Mono<Boolean>

    /* 재발송 제한(Cooldown) 여부 조회*/
    fun isCooldown(userId: Long, alertSeq: Long): Mono<Boolean>

    /* 재발송 제한 시간(TTL) 시작*/
    fun startCooldown(
        userId: Long,
        alertSeq: Long,
        seconds: Long
    ): Mono<Boolean>
}