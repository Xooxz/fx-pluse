package com.xooxz.stream.infrastructure.kafka

import com.xooxz.stream.presentation.dto.RateResponse
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class RateEventProducer(
    private val kafkaTemplate: KafkaTemplate<String, RateUpdatedEvent>
) {

    /**
     * 환율 변경 이벤트를 Kafka Topic에 발행
     * @param rate 발행할 환율 정보
     * @return 이벤트 발행 완료 Mono
     */
    fun send(rate: RateResponse): Mono<Void> {
        val event = RateUpdatedEvent(
            symbol = rate.symbol,
            price = rate.price,
            createdAt = rate.updatedAt
        )

        return Mono.fromFuture(
            kafkaTemplate.send("rate.updated", rate.symbol, event)
        ).then()
    }

}