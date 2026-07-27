package com.xooxz.auth.domain

/**
 * 저장된 Refresh Token이 없거나 유효하지 않은 경우 발생하는 예외
 */
class InvalidRefreshTokenException : RuntimeException(
    "유효하지 않은 Refresh Token입니다."
)