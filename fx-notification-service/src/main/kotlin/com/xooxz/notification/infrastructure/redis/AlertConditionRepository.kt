package com.xooxz.notification.infrastructure.redis

import com.xooxz.notification.domain.RateAlertCondition
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface  AlertConditionRepository {
    fun nextAlertSeq(mbrKey: String): Mono<Long>
    fun save(condition: RateAlertCondition): Mono<Boolean>
    fun findBySymbol(symbol: String): Flux<RateAlertCondition>
    fun update(condition: RateAlertCondition): Mono<Boolean>
}