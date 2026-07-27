package com.xooxz.auth.infrastructure.persistence.repository

import com.xooxz.auth.domain.RefreshTokenRotateResult
import com.xooxz.auth.application.port.out.AuthRepository
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.stereotype.Repository
import java.time.Duration

@Repository
class RedisAuthRepository(
    private val redisTemplate: StringRedisTemplate
) : AuthRepository {

    companion object {
        private const val REFRESH_TOKEN_KEY_PREFIX = "auth:refresh"
        private const val ROTATE_SUCCESS = 1L
        private const val TOKEN_NOT_FOUND = 0L
        private const val TOKEN_REUSED = -1L

        /**
         * 반환값
         *
         *  1: 토큰 교체 성공
         *  0: 저장된 토큰 없음
         * -1: 기존 토큰 재사용 감지 및 토큰 삭제
         */
        private val ROTATE_REFRESH_TOKEN_SCRIPT =
            DefaultRedisScript<Long>().apply {
                setScriptText(
                    """
                    local currentRefreshToken = redis.call('GET', KEYS[1])

                    if not currentRefreshToken then
                        return 0
                    end

                    if currentRefreshToken ~= ARGV[1] then
                        redis.call('DEL', KEYS[1])
                        return -1
                    end

                    redis.call(
                        'SET',
                        KEYS[1],
                        ARGV[2],
                        'PX',
                        ARGV[3]
                    )

                    return 1
                    """.trimIndent()
                )

                resultType = Long::class.java
            }
    }

    /**
     * 로그인 성공 후 Refresh Token을 Redis에 저장
     *
     * @param memberKey 사용자 고유 식별자
     * @param refreshTokenHash Refresh Token
     * @param ttl 유효 기간
     */
    override fun saveRefreshToken(
        memberKey: String,
        refreshTokenHash: String,
        ttl: Duration
    ) {
        require(memberKey.isNotBlank()) {
            "memberKey는 비어 있을 수 없습니다."
        }

        require(refreshTokenHash.isNotBlank()) {
            "refreshTokenHash는 비어 있을 수 없습니다."
        }

        require(!ttl.isZero && !ttl.isNegative) {
            "TTL은 0보다 커야 합니다."
        }

        redisTemplate.opsForValue().set(
            createRefreshTokenKey(memberKey),
            refreshTokenHash,
            ttl
        )
    }

    /**
     * Refresh Token Rotation을 수행
     *
     * @param memberKey 사용자 고유 식별자
     * @param currentRefreshTokenHash 현재 Refresh Token(암호화)
     * @param newRefreshTokenHash 새로 발급한 Refresh Token(암호화)
     * @param ttl 유효 기간
     * @return Refresh Token 교체 결과
     */
    override fun rotateRefreshToken(
        memberKey: String,
        currentRefreshTokenHash: String,
        newRefreshTokenHash: String,
        ttl: Duration
    ): RefreshTokenRotateResult {
        require(memberKey.isNotBlank()) {
            "memberKey는 비어 있을 수 없습니다."
        }

        require(currentRefreshTokenHash.isNotBlank()) {
            "currentRefreshTokenHash는 비어 있을 수 없습니다."
        }

        require(newRefreshTokenHash.isNotBlank()) {
            "newRefreshTokenHash는 비어 있을 수 없습니다."
        }

        require(!ttl.isZero && !ttl.isNegative) {
            "TTL은 0보다 커야 합니다."
        }

        val result = redisTemplate.execute(
            ROTATE_REFRESH_TOKEN_SCRIPT,
            listOf(createRefreshTokenKey(memberKey)),
            currentRefreshTokenHash,
            newRefreshTokenHash,
            ttl.toMillis().toString()
        )

        return when (result) {
            ROTATE_SUCCESS -> RefreshTokenRotateResult.SUCCESS
            TOKEN_NOT_FOUND -> RefreshTokenRotateResult.NOT_FOUND
            TOKEN_REUSED -> RefreshTokenRotateResult.REUSED

            else -> throw IllegalStateException(
                "Refresh Token 교체 중 알 수 없는 Redis 응답이 발생했습니다. result=$result"
            )
        }
    }

    /**
     * 회원의 Refresh Token을 삭제
     *
     * @return 삭제 성공 여부
     */
    override fun deleteRefreshToken(memberKey: String): Boolean {
        require(memberKey.isNotBlank()) {
            "memberKey는 비어 있을 수 없습니다."
        }

        return redisTemplate.delete(
            createRefreshTokenKey(memberKey)
        )
    }

    /**
     * 회원의 Refresh Token 존재 여부를 조회
     *
     * @param memberKey 사용자 고유 식별자
     * @return Refresh Token 존재 여부
     */
    override fun existsRefreshToken(memberKey: String): Boolean {
        require(memberKey.isNotBlank()) {
            "memberKey는 비어 있을 수 없습니다."
        }

        return redisTemplate.hasKey(
            createRefreshTokenKey(memberKey)
        )
    }

    /**
     * 회원별 Refresh Token 저장 Key를 생성
     */
    private fun createRefreshTokenKey(memberKey: String): String {
        return "$REFRESH_TOKEN_KEY_PREFIX:$memberKey"
    }

}