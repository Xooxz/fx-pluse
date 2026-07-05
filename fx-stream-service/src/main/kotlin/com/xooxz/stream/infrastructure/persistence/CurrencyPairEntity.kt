package com.xooxz.stream.infrastructure.persistence

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

/**
 * 지원 통화 엔티티
 * @param id           식별자
 * @param symbol       통화쌍 코드
 * @param countryName  국가명
 * @param minRate      환율 최소값
 * @param maxRate      환율 최대값
 * @param period       환율 생성 주기
 * @param enabled      사용 여부
 * @param createdAt    생성 일시
 * @param updatedAt    수정 일시
 */
@Table("currency_pair")
data class CurrencyPairEntity(

    @Id
    val id: Long? = null,

    @Column("symbol")
    val symbol: String,

    @Column("country_name")
    val countryName: String,

    @Column("min_rate")
    val minRate: Double,

    @Column("max_rate")
    val maxRate: Double,

    @Column("period")
    val period: Int,

    @Column("enabled")
    val enabled: Boolean = true,

    @Column("created_at")
    val createdAt: LocalDateTime? = null,

    @Column("updated_at")
    val updatedAt: LocalDateTime? = null

)