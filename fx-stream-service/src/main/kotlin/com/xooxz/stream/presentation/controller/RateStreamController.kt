package com.xooxz.stream.presentation.controller

import com.xooxz.stream.application.RateStreamService
import com.xooxz.stream.presentation.dto.RateResponse
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
class RateStreamController(
    private val rateStreamService: RateStreamService
) {

    companion object {
        private val log = LoggerFactory.getLogger(RateStreamController::class.java)
    }

    /**
     * 단건 환율 조회
     * @param symbol 통화 코드
     */
    @GetMapping("/{symbol}")
    fun getRate(@PathVariable symbol: String): Mono<RateResponse> {
        log.info("단건 환율 조회")
        return rateStreamService.getRate(symbol)
    }

    /**
     * 실시간 환율 스트리밍 조회
     * @param symbol 통화 코드
     */
    @GetMapping(
        value = ["/stream/{symbol}"],
        produces = [MediaType.TEXT_EVENT_STREAM_VALUE]
    )
    fun streamRates(@PathVariable symbol: String): Flux<RateResponse> {
        log.info("개별 환율 스트리밍 조회")
        return rateStreamService.getStreamRates(symbol)
    }

    /**
     * 전체 통화의 최신 환율을 SSE로 스트리밍한다.
     */
    @GetMapping(
        value = ["/stream"],
        produces = [MediaType.TEXT_EVENT_STREAM_VALUE]
    )
    fun streamRates(): Flux<List<RateResponse>> {
        log.info("전체 환율 스트리밍 조회")
        return rateStreamService.streamRates()
    }

}