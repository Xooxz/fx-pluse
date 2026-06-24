package com.xooxz.stream.domain.exception


/**
 * Redis에 환율 정보가 존재하지 않을 경우 발생하는 도메인 예외
 * @param symbol 통화쌍 코드
 */
class RateNotFoundException(
    symbol: String
) : RuntimeException(
    "Rate not found: $symbol"
)