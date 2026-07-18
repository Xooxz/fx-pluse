package com.xooxz.stream.domain.exception

/**
 * Redis에 환율 정보가 존재하지 않을 경우 발생하는 도메인 예외
 * @param symbol 통화 코드
 */
class RateNotFoundException(
    symbol: String
) : RuntimeException(
    "해당 통화의 환율 정보를 찾을 수 없습니다. ($symbol)"
)