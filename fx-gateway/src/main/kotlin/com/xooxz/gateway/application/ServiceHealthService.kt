package com.xooxz.gateway.application

import com.xooxz.gateway.presentation.ServiceHealthResponse
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.net.URI
import java.time.LocalDateTime

@Service
class ServiceHealthService(
    private val webClientBuilder: WebClient.Builder,
    private val routeLocator: RouteLocator
) {

    /**
     * Gateway에 등록된 서비스의 상태를 조회
     * @return 서비스 상태 목록
     */
    fun getServiceStatuses(): Flux<ServiceHealthResponse> {
        return routeLocator.routes
            .flatMap { route ->
                checkHealth(
                    serviceName = route.id,
                    url = "${route.uri}/actuator/health"
                )
            }
    }

    /**
     * 지정된 서비스의 Health 상태를 조회
     * @return 서비스 상태 목록
     */
    private fun checkHealth(
        serviceName: String,
        url: String
    ): Mono<ServiceHealthResponse> {
        val start = System.currentTimeMillis()
        val port = URI.create(url).port

        return webClientBuilder.build()
            .get()
            .uri(url)
            .retrieve()
            .bodyToMono(Map::class.java)
            .map { body ->
                val status = body["status"]?.toString() ?: "UNKNOWN"

                ServiceHealthResponse(
                    serviceName = serviceName,
                    port = port,
                    status = status,
                    responseTime = System.currentTimeMillis() - start,
                    checkedAt = LocalDateTime.now()
                )
            }
            .onErrorResume { _ ->
                Mono.just(
                    ServiceHealthResponse(
                        serviceName = serviceName,
                        port = port,
                        status = "DOWN",
                        responseTime = System.currentTimeMillis() - start,
                        checkedAt = LocalDateTime.now()
                    )
                )
            }
    }

}