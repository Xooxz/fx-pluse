package com.xooxz.gateway.filter

import org.slf4j.MDC
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.util.UUID

@Component
class TraceIdGlobalFilter : GlobalFilter, Ordered {

    companion object {
        private const val TRACE_ID_HEADER = "X-Trace-Id"
        private const val MDC_TRACE_ID = "traceId"
    }

    override fun filter(
        exchange: ServerWebExchange,
        chain: GatewayFilterChain
    ): Mono<Void> {
        // 요청 Header에 TraceId가 존재하면 재사용
        val traceId = exchange.request.headers.getFirst(TRACE_ID_HEADER)
            ?.takeIf { it.isNotBlank() }
            ?: UUID.randomUUID().toString()

        // 하위 서비스로 전달할 Header 추가
        val mutatedRequest: ServerHttpRequest = exchange.request
            .mutate()
            .header(TRACE_ID_HEADER, traceId)
            .build()

        // 변경된 Request를 포함하는 Exchange 생성
        val mutatedExchange = exchange
            .mutate()
            .request(mutatedRequest)
            .build()

        return chain.filter(mutatedExchange)
            .doFirst { MDC.put(MDC_TRACE_ID, traceId) }
            .doFinally { MDC.clear() }
    }

    //  가장 먼저 실행되도록 최우선 순위 지정
    override fun getOrder(): Int = Ordered.HIGHEST_PRECEDENCE

}