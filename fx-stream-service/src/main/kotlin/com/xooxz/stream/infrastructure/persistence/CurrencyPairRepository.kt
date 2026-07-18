package com.xooxz.stream.infrastructure.persistence

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface CurrencyPairRepository : ReactiveCrudRepository<CurrencyPairEntity, Long> {

    /**
     * 지원하는 통화 여부를 조회한다.
     */
    fun existsBySymbolAndUseYnTrue(symbol: String): Mono<Boolean>

    /**
     * 사용 가능한 전체 통화 목록을 조회한다.
     */
    fun findAllByUseYnTrue(): Flux<CurrencyPairEntity>

}