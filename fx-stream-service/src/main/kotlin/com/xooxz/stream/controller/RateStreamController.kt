package com.xooxz.stream.controller

import com.xooxz.stream.dto.RateResponse
import com.xooxz.stream.service.RateMockService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
class RateStreamController(
    private val rateMockService: RateMockService
) {

    /**
     * 단건 환율 조회
     * @param symbol 통화쌍 코드
     */
    @GetMapping("/{symbol}")
    fun getRate(@PathVariable symbol: String): Mono<RateResponse> {
        val result = rateMockService.getRate(symbol)
        return result
    }

    /**
     * 실시간 환율 스트리밍 조회
     * @param symbol 통화쌍 코드
     */
    @GetMapping(
        value = ["/stream/{symbol}"],
        produces = [MediaType.TEXT_EVENT_STREAM_VALUE]
    )
    fun streamRates(@PathVariable symbol: String): Flux<RateResponse> {
        return rateMockService.streamRates(symbol)
    }

}