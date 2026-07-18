package com.xooxz.notification.application

import com.xooxz.notification.domain.AlertInterval
import com.xooxz.notification.domain.AlertOperator
import com.xooxz.notification.domain.RateAlertCondition
import com.xooxz.notification.infrastructure.kafka.RateUpdatedEvent
import com.xooxz.notification.infrastructure.redis.AlertConditionRepository
import com.xooxz.notification.presentation.AlertConditionCreateRequest
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import com.xooxz.notification.domain.AlertTriggeredEvent
import com.xooxz.notification.infrastructure.redis.CooldownRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.LocalDateTime


@Service
class AlertConditionService(
    private val alertConditionRepository: AlertConditionRepository,
    private val cooldownRepository: CooldownRepository
) {

    private val log = KotlinLogging.logger {}

    /**
     * Kafka로 수신한 환율 이벤트 처리
     *
     * 1. 알림 조건 조회
     * 2. 발송 가능 여부 확인(Cooldown)
     * 3. 조건 충족 여부 판단
     * 4. 알림 이벤트 생성
     * 5. 마지막 발송 시간 및 Cooldown 갱신
     */
    fun handle(event: RateUpdatedEvent): Mono<Void> {
        return alertConditionRepository.findBySymbol(event.symbol)

            // 1. 알림 조건 조회
            .flatMap { condition ->

                return@flatMap cooldownRepository
                    .isCooldown(condition.mbrKey, condition.alertSeq)
                    .flatMap { cooldown ->
                        // 2. 발송 가능 여부 확인(Cooldown) : TTL 남아있으면 건너뜀
                        if (cooldown) {
                            return@flatMap Mono.just(false)
                        }

                        // 3-1. 조건 충족 여부 판단 : 현재 환율이 사용자가 등록한 조건을 만족하는 경우
                        if (isMatched(condition, event)) {
                            log.info(
                                """
                                ===============================
                                  *** 환율 알림 조건 충족 ***
                                   - mbrKey       : {}
                                   - alertSeq     : {}
                                   - symbol       : {}
                                   - operator     : {}
                                   - targetPrice  : {}
                                   - currentPrice : {}
                                ===============================
                                """.trimIndent(),
                                condition.mbrKey,
                                condition.alertSeq,
                                event.symbol,
                                condition.operator,
                                condition.targetPrice,
                                event.price
                            )

                            val now = LocalDateTime.now()
                            val isOnce = condition.interval == AlertInterval.ONCE

                            // 마지막 발송 시각 갱신
                            val updatedCondition = condition.copy(
                                lastSentAt = now,
                                useYn = if (isOnce) false else condition.useYn,
                                modifiedAt = now,
                                modifiedBy = "SYSTEM"
                            )

                            // 4. 알림 이벤트 생성
                            val alertEvent = createAlertEvent(updatedCondition, event)

                            log.info(
                                """
                                ======================================================
                                 *** 알림 이벤트 생성 ***
                                  - mbrKey : {} | alertSeq : {} | symbol : {}
                                  - message : {}
                                ======================================================
                                """.trimIndent(),
                                alertEvent.mbrKey,
                                alertEvent.alertSeq,
                                alertEvent.symbol,
                                alertEvent.message
                            )

                            // 5. 마지막 발송 시간 및 Cooldown 갱신
                            return@flatMap alertConditionRepository.update(updatedCondition)
                                .flatMap {
                                    if (isOnce) {
                                        log.info(
                                            "일회성 알림 종료 - alertConditionId : {}, mbrKey : {}, alertSeq : {}",
                                            updatedCondition.alertConditionId,
                                            updatedCondition.mbrKey,
                                            updatedCondition.alertSeq,
                                        )

                                        Mono.just(true)
                                    } else {
                                        log.info("Cooldown 시작 - alertConditionId={}, mbrKey={}, alertSeq={}, interval={}초",
                                            updatedCondition.alertConditionId,
                                            updatedCondition.mbrKey,
                                            updatedCondition.alertSeq,
                                            updatedCondition.interval.seconds
                                        )

                                        cooldownRepository.startCooldown(
                                            updatedCondition.mbrKey,
                                            updatedCondition.alertSeq,
                                            updatedCondition.interval.seconds
                                        )
                                    }
                                }

                        } else {
                            // 3-2. 조건 충족 여부 판단 : 조건 미충족
                            log.info(
                                """
                                ======================================================================
                                 !!! 알림 발행 미대상 !!! 
                                  - mbrKey : {} |  alertSeq : {} : symbol={}
                                  - targetPrice : {} | currentPrice : {} |  operator : {}
                                ======================================================================
                                """.trimIndent(),
                                condition.mbrKey,
                                condition.alertSeq,
                                event.symbol,
                                condition.targetPrice,
                                event.price,
                                condition.operator
                            )

                            return@flatMap Mono.just(false)
                        }
                    }
            }
            .then()
    }

    /**
     * 알림 조건 충족 여부 판단
     * */
    private fun isMatched(condition: RateAlertCondition, event: RateUpdatedEvent): Boolean {
        return when (condition.operator) {
            AlertOperator.GTE -> event.price >= condition.targetPrice
            AlertOperator.LTE -> event.price <= condition.targetPrice
        }
    }

    /**
     * 알림 발송 이벤트 객체 생성
     * */
    private fun createAlertEvent(
        condition: RateAlertCondition,
        event: RateUpdatedEvent
    ): AlertTriggeredEvent {
        return AlertTriggeredEvent(
            mbrKey = condition.mbrKey,
            alertSeq = condition.alertSeq,
            symbol = condition.symbol,
            targetPrice = condition.targetPrice,
            currentPrice = event.price,
            operator = condition.operator,
            message = "[환율 알림] ${condition.symbol} : ${event.price}원"
        )
    }


    /**
     * 새로운 환율 알림 조건 등록
     * - 알림 번호(alertSeq) : 회원별로 자동 채번
     * */
    fun create(request: AlertConditionCreateRequest): Mono<Boolean> {
        return alertConditionRepository.nextAlertSeq(request.mbrKey)
            .flatMap { nextSeq ->
                val condition = RateAlertCondition(
                    mbrKey = request.mbrKey,
                    alertSeq = nextSeq,
                    symbol = request.symbol,
                    targetPrice = request.targetPrice,
                    operator = request.operator,
                    interval = request.interval,
                    createdBy = request.mbrId
                )

                alertConditionRepository.save(condition)
            }
    }
}