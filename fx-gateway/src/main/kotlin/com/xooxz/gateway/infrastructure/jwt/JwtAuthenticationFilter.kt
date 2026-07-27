package com.xooxz.gateway.infrastructure.jwt

import com.xooxz.common.exception.ExpiredTokenException
import com.xooxz.common.exception.InvalidTokenException
import com.xooxz.common.jwt.JwtTokenProvider
import com.xooxz.gateway.infrastructure.exception.JwtAuthenticationException
import com.xooxz.gateway.infrastructure.security.JwtAuthenticationEntryPoint
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

/**
 * Gateway에서 JWT를 검증하고 인증 정보를 생성하는 필터
 */
@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider,
    private val jwtAuthenticationEntryPoint: JwtAuthenticationEntryPoint
) : WebFilter {

    companion object {
        private const val BEARER_PREFIX = "Bearer "
        private const val MEMBER_KEY_HEADER = "X-Member-Key"
        private const val MEMBER_ROLE_HEADER = "X-Member-Role"
    }

    private val log = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)

    /**
     * Authorization 헤더의 JWT를 검증하고 인증 정보를 생성
     */
    override fun filter(
        exchange: ServerWebExchange,
        chain: WebFilterChain
    ): Mono<Void> {
        val accessToken = resolveToken(exchange)
            ?: return chain.filter(exchange)

        return try {
            val claims = jwtTokenProvider.getClaims(accessToken)

            val memberKey = jwtTokenProvider.getSubject(claims)
            val role = jwtTokenProvider.getRole(claims)

            val authentication = UsernamePasswordAuthenticationToken(
                memberKey,
                null,
                listOf(SimpleGrantedAuthority(role))
            )

            val mutatedExchange = exchange.mutate()
                .request { request ->
                    request.headers { headers ->
                        // 외부에서 전달한 내부 헤더 제거
                        headers.remove(MEMBER_KEY_HEADER)
                        headers.remove(MEMBER_ROLE_HEADER)

                        // Gateway가 검증한 사용자 정보 설정
                        headers.set(MEMBER_KEY_HEADER, memberKey)
                        headers.set(MEMBER_ROLE_HEADER, role)
                    }
                }
                .build()

            chain.filter(mutatedExchange)
                .contextWrite(
                    ReactiveSecurityContextHolder.withAuthentication(authentication)
                )
        } catch (e: ExpiredTokenException) {
            handleAuthenticationFailure(exchange, e)
        } catch (e: InvalidTokenException) {
            handleAuthenticationFailure(exchange, e)
        }
    }

    /**
     * Authorization 헤더에서 Bearer Access Token을 추출
     */
    private fun resolveToken(exchange: ServerWebExchange): String? {
        return exchange.request.headers
            .getFirst(HttpHeaders.AUTHORIZATION)
            ?.takeIf { it.startsWith(BEARER_PREFIX, ignoreCase = true) }
            ?.substring(BEARER_PREFIX.length)
            ?.trim()
            ?.takeIf { it.isNotBlank() }
    }

    /**
     * JWT 검증 실패 시 인증 실패 응답을 반환
     */
    private fun handleAuthenticationFailure(
        exchange: ServerWebExchange,
        exception: RuntimeException
    ): Mono<Void> {
        log.warn(exception.message)

        return jwtAuthenticationEntryPoint.commence(
            exchange,
            JwtAuthenticationException(
                message = exception.message ?: "인증에 실패했습니다.",
                cause = exception
            )
        )
    }

}