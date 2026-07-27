package com.xooxz.auth.application.port.out

import com.xooxz.auth.domain.RefreshTokenRotateResult
import java.time.Duration

interface AuthRepository {

    /**
     * Refresh Token을 저장
     *
     * @param memberKey 사용자 고유 식별자
     * @param refreshTokenHash Refresh Token(암호화)
     * @param ttl 유효 기간
     */
    fun saveRefreshToken(
        memberKey: String,
        refreshTokenHash: String,
        ttl: Duration
    )

    /**
     * 현재 Refresh Token을 검증하고 새로운 Refresh Token으로 교체
     *
     * @param memberKey 사용자 고유 식별자
     * @param currentRefreshTokenHash 현재 Refresh Token(암호화)
     * @param newRefreshTokenHash 새로 발급한 Refresh Token(암호화)
     * @param ttl 유효 기간
     * @return Refresh Token 교체 결과
     */
    fun rotateRefreshToken(
        memberKey: String,
        currentRefreshTokenHash: String,
        newRefreshTokenHash: String,
        ttl: Duration
    ): RefreshTokenRotateResult

    /**
     * 회원의 Refresh Token을 삭제
     *
     * @param memberKey 사용자 고유 식별자
     * @return 삭제 성공 여부
     */
    fun deleteRefreshToken(memberKey: String): Boolean

    /**
     * Refresh Token 존재 여부를 확인
     *
     * @param memberKey 사용자 고유 식별자
     * @return Refresh Token 존재 여부
     */
    fun existsRefreshToken(memberKey: String): Boolean

}