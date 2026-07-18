package com.xooxz.notification.infrastructure.persistence

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime


/**
* 환율 알림 조건 테이블
* */
@Table("alert_condition")
data class AlertConditionEntity (

    /** 알림 조건 PK */
    @Id
    @Column("alert_condition_id")
    val alertConditionId: Long? = null,

    /** 회원 고유키 */
    @Column("mbr_key")
    val mbrKey: String,

    /** 회원별 알림 순번*/
    @Column("alert_seq")
    val alertSeq: Long,


    /** 통화 코드 */
    @Column("symbol")
    val symbol: String,

    /** 목표 환율 */
    @Column("target_price")
    val targetPrice: BigDecimal,

    /** 비교 조건(GTE/LTE) */
    @Column("operator")
    val operator: String,

    /** 재발송 주기 */
    @Column("interval")
    val interval: String,

    /** 마지막 알림 발송 시각 */
    @Column("last_sent_at")
    val lastSentAt: LocalDateTime?,

    /** 사용 여부 */
    @Column("use_yn")
    val useYn: Boolean,

    /** 등록 일시 */
    @Column("created_at")
    val createdAt: LocalDateTime,

    /** 등록자 */
    @Column("created_by")
    val createdBy: String,

    /** 수정 일시 */
    @Column("modified_at")
    val modifiedAt: LocalDateTime?,

    /** 수정자 */
    @Column("modified_by")
    val modifiedBy: String?
)