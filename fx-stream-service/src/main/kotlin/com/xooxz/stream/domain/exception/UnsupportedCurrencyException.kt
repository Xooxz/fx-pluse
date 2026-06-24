package com.xooxz.stream.domain.exception

/**
 * 지원하지 않는 통화 요청 시 발생하는 도메인 예외
 * @param symbol 통화쌍 코드
 */
class UnsupportedCurrencyException(
    symbol: String
) : RuntimeException(
    "Unsupported currency pair: $symbol"
)