package com.xooxz.gateway.presentation

import java.time.LocalDateTime

/**
 * 서비스 상태 조회 응답 DTO
 * @param serviceName   서비스 이름
 * @param port          서비스 포트
 * @param status        서비스 상태 (UP, DOWN)
 * @param responseTime  응답 시간
 * @param checkedAt     마지막 상태 확인 시각
 */
data class ServiceHealthResponse(
    val serviceName: String,
    val port: Int,
    val status: String,
    val responseTime: Long,
    val checkedAt: LocalDateTime,
)