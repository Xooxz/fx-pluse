package com.xooxz.notification.infrastructure.persistence

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * 환율 알림 조건(alert_condition) 테이블
 */
interface AlertConditionR2dbcRepository
    : ReactiveCrudRepository<AlertConditionEntity, Long> {

    /**
     * 회원별 다음 알림 순번 조회
     */
    fun findTopByMbrKeyOrderByAlertSeqDesc(
        mbrKey: String
    ): Mono<AlertConditionEntity>

    /**
     * 통화 코드별 사용 중인 알림 조건 조회
     */
    fun findBySymbolAndUseYn(
        symbol: String,
        useYn: Boolean
    ): Flux<AlertConditionEntity>
}