package com.xooxz.gateway.infrastructure.exception

import org.springframework.security.core.AuthenticationException

/**
 * Spring Security의 인증 실패 처리를 위해 사용하는 예외
 */
class JwtAuthenticationException(
    message: String,
    cause: Throwable? = null
) : AuthenticationException(message, cause)