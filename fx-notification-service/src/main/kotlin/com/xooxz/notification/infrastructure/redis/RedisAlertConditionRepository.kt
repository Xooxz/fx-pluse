package com.xooxz.notification.infrastructure.redis

import com.fasterxml.jackson.databind.ObjectMapper
import com.xooxz.notification.domain.RateAlertCondition
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration

@Repository
class RedisAlertConditionRepository(
    private val redisTemplate: ReactiveStringRedisTemplate,
    private val objectMapper: ObjectMapper
) : AlertConditionRepository {

    companion object {
        private val log = LoggerFactory.getLogger(RedisAlertConditionRepository::class.java)

        private fun key(symbol: String): String =
            "alert:condition:$symbol"

        private fun field(condition: RateAlertCondition): String =
            "${condition.userId}:${condition.alertSeq}"

        private fun seqKey(userId: Long): String =
            "alert:seq:$userId"
    }

    override fun nextAlertSeq(userId: Long): Mono<Long> {
        return redisTemplate.opsForValue()
            .increment(seqKey(userId))
    }

    override fun save(condition: RateAlertCondition): Mono<Boolean> {
        val json = objectMapper.writeValueAsString(condition)

        log.info(
            "- 알림 조건 저장 userId={}, alertSeq={}, symbol={}, targetPrice={}, operator={}, interval={}",
            condition.userId,
            condition.alertSeq,
            condition.symbol,
            condition.targetPrice,
            condition.operator,
            condition.interval
        )

        return redisTemplate.opsForHash<String, String>()
            .put(key(condition.symbol), field(condition), json)
    }

    override fun findBySymbol(symbol: String): Flux<RateAlertCondition> {
        return redisTemplate.opsForHash<String, String>()
            .values(key(symbol))
            .map { json ->
                objectMapper.readValue(json, RateAlertCondition::class.java)
            }
    }

    override fun update(condition: RateAlertCondition): Mono<Boolean> {
        return save(condition)
    }

    /*알림 재발송 제한(TTL) Key*/
    private fun cooldownKey(
        userId: Long,
        alertSeq: Long
    ): String =
        "alert:cooldown:$userId:$alertSeq"

    override fun isCooldown(
        userId: Long,
        alertSeq: Long
    ): Mono<Boolean> {

        //TTL Key 존재 여부로 재발송 제한 상태를 판단
        return redisTemplate.hasKey(
            cooldownKey(userId, alertSeq)
        )
    }

    override fun startCooldown(
        userId: Long,
        alertSeq: Long,
        seconds: Long
    ): Mono<Boolean> {

        // TTL 만료 시 Redis가 Key를 자동 삭제
        return redisTemplate.opsForValue()
            .set(
                cooldownKey(userId, alertSeq),
                "1",
                Duration.ofSeconds(seconds)
            )
    }

}