package com.xooxz.notification.infrastructure.persistence

import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * 환율 알림 조건(alert_condition) 테이블
 */
interface AlertConditionR2dbcRepository
    : ReactiveCrudRepository<AlertConditionEntity, Long> {

    /**
     * 회원별 마지막 알림 순번 조회
     *
     * @param mbrKey 사용자고유키
     * @return 가장 큰 알림 순번
     */
    @Query(
        """
            SELECT COALESCE(MAX(alert_seq), 0)
              FROM alert_condition
             WHERE mbr_key = :mbrKey
            """
    )
    fun findMaxAlertSeq(
        mbrKey: String
    ): Mono<Long>

    /**
     * 통화 코드별 사용 중인 알림 조건 조회
     *
     * @param symbol 통화코드
     * @param useYn  사용여부
     * @return 조건에 해당하는 알림 조건 목록
     */
    fun findBySymbolAndUseYn(
        symbol: String,
        useYn: Boolean
    ): Flux<AlertConditionEntity>
}