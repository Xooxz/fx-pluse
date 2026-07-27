package com.xooxz.auth.application.port.out

/**
 * JWT 및 Refresh Token을 발급하는 컴포넌트
 */
interface TokenIssuer {

    /**
     * Access Token을 생성
     *
     * @param memberKey 사용자 고유 식별자
     */
    fun createAccessToken(memberKey: String): String

    /**
     * Refresh Token을 생성
     */
    fun createRefreshToken(): String

}