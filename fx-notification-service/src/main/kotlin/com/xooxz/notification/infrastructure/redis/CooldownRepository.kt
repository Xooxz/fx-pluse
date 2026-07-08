package com.xooxz.notification.infrastructure.redis

import reactor.core.publisher.Mono

/**
 * 알림 재발송 제한(Cooldown)
 * Redis TTL 사용
 */
interface CooldownRepository {

    /**
     * 재발송 제한(Cooldown) 여부 조회
     */
    fun isCooldown(
        mbrKey: String,
        alertSeq: Long
    ): Mono<Boolean>

    /**
     * 재발송 제한 시간(TTL) 시작
     */
    fun startCooldown(
        mbrKey: String,
        alertSeq: Long,
        seconds: Long
    ): Mono<Boolean>
}