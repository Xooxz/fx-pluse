package com.xooxz.gateway.infrastructure.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.xooxz.gateway.infrastructure.dto.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

/**
 * 인증은 되었지만 권한이 없는 요청에 대해 403 Forbidden 응답을 반환하는 Handler
 */
@Component
class JwtAccessDeniedHandler(
    private val objectMapper: ObjectMapper
) : ServerAccessDeniedHandler {

    /**
     * 권한이 없는 요청에 대해 403 응답을 반환
     */
    override fun handle(
        exchange: ServerWebExchange,
        denied: AccessDeniedException
    ): Mono<Void> {
        val response = exchange.response
        if (response.isCommitted) {
            return Mono.empty()
        }

        response.statusCode = HttpStatus.FORBIDDEN
        response.headers.contentType = MediaType.APPLICATION_JSON

        val body = ErrorResponse(
            code = "ACCESS_DENIED",
            message = "접근 권한이 없습니다."
        )

        val bytes = objectMapper.writeValueAsBytes(body)
        val buffer = response.bufferFactory().wrap(bytes)

        return response.writeWith(Mono.just(buffer))
    }

}