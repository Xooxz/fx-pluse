package com.xooxz.gateway.infrastructure.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.xooxz.gateway.infrastructure.dto.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.server.ServerAuthenticationEntryPoint
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

/**
 * 인증되지 않은 요청에 대해 401 Unauthorized 응답을 반환하는 EntryPoint
 */
@Component
class JwtAuthenticationEntryPoint(
    private val objectMapper: ObjectMapper
) : ServerAuthenticationEntryPoint {

    /**
     * 인증에 실패한 요청에 대해 401 응답을 반환
     */
    override fun commence(
        exchange: ServerWebExchange,
        ex: AuthenticationException
    ): Mono<Void> {
        val response = exchange.response
        if (response.isCommitted) {
            return Mono.empty()
        }

        response.statusCode = HttpStatus.UNAUTHORIZED
        response.headers.contentType = MediaType.APPLICATION_JSON

        val body = ErrorResponse(
            code = "UNAUTHORIZED",
            message = "인증이 필요합니다."
        )

        val bytes = objectMapper.writeValueAsBytes(body)
        val buffer = response.bufferFactory().wrap(bytes)

        return response.writeWith(Mono.just(buffer))
    }

}