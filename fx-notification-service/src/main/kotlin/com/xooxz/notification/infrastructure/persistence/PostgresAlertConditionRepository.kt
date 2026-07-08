package com.xooxz.notification.infrastructure.persistence

import com.xooxz.notification.domain.AlertInterval
import com.xooxz.notification.domain.AlertOperator
import com.xooxz.notification.domain.RateAlertCondition
import com.xooxz.notification.infrastructure.redis.AlertConditionRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
class PostgresAlertConditionRepository(
    private val r2dbcRepository: AlertConditionR2dbcRepository
) : AlertConditionRepository {

    private val log = KotlinLogging.logger {}

    /**
     * 회원별 다음 알림 순번 조회
     */
    override fun nextAlertSeq(mbrKey: String): Mono<Long> {
        return r2dbcRepository.findTopByMbrKeyOrderByAlertSeqDesc(mbrKey)
            .map { it.alertSeq + 1 }
            .defaultIfEmpty(1)
    }

    override fun save(condition: RateAlertCondition): Mono<Boolean> {

        log.info(
            "- 알림 조건 저장 mbrKey={}, mbrId={}, alertSeq={}, symbol={}, targetPrice={}, operator={}, interval={}",
            condition.mbrKey,
            condition.createdBy,
            condition.alertSeq,
            condition.symbol,
            condition.targetPrice,
            condition.operator,
            condition.interval
        )

        return r2dbcRepository.save(condition.toEntity())
            .map { true }
    }

    override fun findBySymbol(symbol: String): Flux<RateAlertCondition> {
        return r2dbcRepository.findBySymbolAndUseYn(symbol, true)
            .map { it.toDomain() }
    }

    override fun update(condition: RateAlertCondition): Mono<Boolean> {

        log.info(
            "- 알림 조건 갱신 alertConditionId={}, mbrKey={}, alertSeq={}, useYn={}, lastSentAt={}, modifiedAt={}, modifiedBy={}",
            condition.alertConditionId,
            condition.mbrKey,
            condition.alertSeq,
            condition.useYn,
            condition.lastSentAt,
            condition.modifiedAt,
            condition.modifiedBy
        )

        return r2dbcRepository.save(condition.toEntity())
            .map { true }
    }

    /**
     * Domain → Entity 변환
     */
    private fun RateAlertCondition.toEntity(): AlertConditionEntity {
        return AlertConditionEntity(
            alertConditionId = alertConditionId,
            mbrKey = mbrKey,
            alertSeq = alertSeq,
            symbol = symbol,
            targetPrice = targetPrice,
            operator = operator.name,
            interval = interval.name,
            lastSentAt = lastSentAt,
            useYn = useYn,
            createdAt = createdAt,
            createdBy = createdBy,
            modifiedAt = modifiedAt,
            modifiedBy = modifiedBy
        )
    }

    /**
     * Entity → Domain 변환
     */
    private fun AlertConditionEntity.toDomain(): RateAlertCondition {
        return RateAlertCondition(
            alertConditionId = alertConditionId,
            mbrKey = mbrKey,
            alertSeq = alertSeq,
            symbol = symbol,
            targetPrice = targetPrice,
            operator = AlertOperator.valueOf(operator),
            interval = AlertInterval.valueOf(interval),
            lastSentAt = lastSentAt,
            useYn = useYn,
            createdAt = createdAt,
            createdBy = createdBy,
            modifiedAt = modifiedAt,
            modifiedBy = modifiedBy
        )
    }
}