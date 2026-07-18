package com.xooxz.gateway.infrastructure.exception

/**
 * Access Token의 유효기간이 만료된 경우 발생하는 예외
 */
class ExpiredTokenException(
    message: String = "만료된 토큰입니다."
) : RuntimeException(message)