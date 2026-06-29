package com.xooxz.gateway.presentation

import com.xooxz.gateway.application.ServiceHealthService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
@RequestMapping("/api/system")
class SystemStatusController(
    private val serviceHealthService: ServiceHealthService
) {

    /**
     * 전체 서비스의 상태를 조회
     * @return 서비스 상태 목록
     */
    @GetMapping("/status")
    fun getSystemStatus(): Flux<ServiceHealthResponse> {
        return serviceHealthService.getServiceStatuses()
    }

}