package com.xooxz.stream.presentation.controller

import com.xooxz.stream.application.RatePublisher
import com.xooxz.stream.presentation.dto.BaseResponse
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 실시간 환율 Publisher 제어 API
 */
@RestController
@RequestMapping("/publisher")
class PublisherController(
    private val ratePublisher: RatePublisher
) {

    companion object {
        private val log = LoggerFactory.getLogger(PublisherController::class.java)
    }

    /**
     * 환율 Publisher를 시작
     * @return Publisher 실행 여부
     */
    @PostMapping("/start")
    fun start(): BaseResponse<Boolean> {
        log.info("환율 Publisher 시작")
        ratePublisher.start()

        return BaseResponse.succeed(
            message = "RatePublisher started",
            data = ratePublisher.isRunning()
        )
    }

    /**
     * 환율 Publisher를 중지
     * @return Publisher 실행 여부
     */
    @PostMapping("/stop")
    fun stop(): BaseResponse<Boolean> {
        log.info("환율 Publisher 중지")
        ratePublisher.stop()

        return BaseResponse.succeed(
            message = "RatePublisher stopped",
            data = ratePublisher.isRunning()
        )
    }

    /**
     * 환율 Publisher의 실행 상태를 조회
     * @return Publisher 실행 여부
     */
    @GetMapping("/status")
    fun status(): BaseResponse<Boolean> {
        log.info("환율 Publisher 상태 조회")

        return BaseResponse.succeed(
            message = "RatePublisher status",
            data = ratePublisher.isRunning()
        )
    }
}