package com.xooxz.notification.infrastructure.kafka

import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class RateUpdatedConsumer {

    @KafkaListener(
        topics = ["rate.updated"],
        groupId = "fx-notification-service"
    )
    fun consume(event: RateUpdatedEvent) {
        println("환율 이벤트 수신: symbol=${event.symbol}, price=${event.price}, createdAt=${event.createdAt}")
    }
}