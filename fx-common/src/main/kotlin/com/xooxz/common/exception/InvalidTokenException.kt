package com.xooxz.common.exception

/**
 * JWT가 위조되었거나 형식이 올바르지 않은 경우 발생하는 예외
 */
class InvalidTokenException(
    message: String = "유효하지 않은 토큰입니다."
) : RuntimeException(message)