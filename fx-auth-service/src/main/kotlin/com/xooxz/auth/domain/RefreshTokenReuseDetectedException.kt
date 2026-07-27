package com.xooxz.auth.domain

/**
 * Refresh Token 재사용이 감지된 경우 발생하는 예외
 */
class RefreshTokenReuseDetectedException : RuntimeException(
    "Refresh Token 재사용이 감지되었습니다."
)