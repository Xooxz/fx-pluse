package com.xooxz.notification.application

import com.xooxz.notification.domain.AlertOperator
import com.xooxz.notification.domain.RateAlertCondition
import com.xooxz.notification.infrastructure.kafka.RateUpdatedEvent
import com.xooxz.notification.infrastructure.redis.AlertConditionRepository
import com.xooxz.notification.presentation.AlertConditionCreateRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import com.xooxz.notification.domain.AlertInterval
import java.time.LocalDateTime


@Service
class AlertConditionService(
    private val alertConditionRepository: AlertConditionRepository
    ) {

    companion object {
        private val log = LoggerFactory.getLogger(AlertConditionService::class.java)
    }

    fun handle(event: RateUpdatedEvent): Mono<Void> {
        return alertConditionRepository.findBySymbol(event.symbol)

            // 조회된 알림 조건을 하나씩 검사
            .flatMap { condition ->

                return@flatMap alertConditionRepository
                    .isCooldown(condition.userId, condition.alertSeq)
                    .flatMap { cooldown ->
                        // TTL 남아있으면 건너뜀
                        if (cooldown) {
                            return@flatMap Mono.just(false)
                        }

                        // 현재 환율이 사용자가 등록한 조건을 만족하는 경우
                        if (isMatched(condition, event)) {
                            log.info(
                                """
                                ====================================================
                                [환율 알림 조건 충족]
                                - userId       : {}
                                - alertSeq     : {}
                                - symbol       : {}
                                - operator     : {}
                                - targetPrice  : {}
                                - currentPrice : {}
                                ====================================================
                                """.trimIndent(),
                                condition.userId,
                                condition.alertSeq,
                                event.symbol,
                                condition.operator,
                                condition.targetPrice,
                                event.price
                            )

                            // Kotlin data class는 불변 객체이므로
                            // copy()로 active와 lastSentAt만 바꾼 새 객체를 만든다.
                            val updatedCondition = condition.copy(
                                lastSentAt = LocalDateTime.now()
                            )

                            // Reactive에서는 Mono를 반환해야 실제 실행 체인에 포함
                            return@flatMap alertConditionRepository.update(updatedCondition)
                                .flatMap {
                                    log.info("Cooldown 시작 - {}초", updatedCondition.interval.seconds)

                                    alertConditionRepository.startCooldown(
                                        updatedCondition.userId,
                                        updatedCondition.alertSeq,
                                        updatedCondition.interval.seconds
                                    )
                                }

                        } else {

                            log.info(
                                "!!알림 발행 미대상 userId={}, alertSeq={}, symbol={}, targetPrice={}, currentPrice={}, operator={}",
                                condition.userId,
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

    private fun isMatched(condition: RateAlertCondition, event: RateUpdatedEvent): Boolean {
        return when (condition.operator) {
            AlertOperator.GTE -> event.price >= condition.targetPrice
            AlertOperator.LTE -> event.price <= condition.targetPrice
        }
    }

    fun create(request: AlertConditionCreateRequest): Mono<Boolean> {
        return alertConditionRepository.nextAlertSeq(request.userId)
            .flatMap { nextSeq ->
                val condition = RateAlertCondition(
                    userId = request.userId,
                    alertSeq = nextSeq,
                    symbol = request.symbol,
                    targetPrice = request.targetPrice,
                    operator = request.operator,
                    interval = request.interval
                )

                alertConditionRepository.save(condition)
            }
    }
}