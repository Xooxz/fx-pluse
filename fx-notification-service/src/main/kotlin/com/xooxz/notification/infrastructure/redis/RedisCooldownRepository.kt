package com.xooxz.notification.infrastructure.redis

import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.time.Duration

@Repository
class RedisCooldownRepository(
    private val redisTemplate: ReactiveStringRedisTemplate
) : CooldownRepository {

    /*알림 재발송 제한(TTL) Key*/
    private fun cooldownKey(
        mbrKey: String,
        alertSeq: Long
    ): String =
        "alert:cooldown:$mbrKey:$alertSeq"

    override fun isCooldown(
        mbrKey: String,
        alertSeq: Long
    ): Mono<Boolean> {

        //TTL Key 존재 여부로 재발송 제한 상태를 판단
        return redisTemplate.hasKey(
            cooldownKey(mbrKey, alertSeq)
        )
    }

    override fun startCooldown(
        mbrKey: String,
        alertSeq: Long,
        seconds: Long
    ): Mono<Boolean> {

        // TTL 만료 시 Redis가 Key를 자동 삭제
        return redisTemplate.opsForValue()
            .set(
                cooldownKey(mbrKey, alertSeq),
                "1",
                Duration.ofSeconds(seconds)
            )
    }

}