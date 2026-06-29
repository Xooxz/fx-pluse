package com.xooxz.stream.domain.model

/**
 * 지원하는 통화 목록
 * @param symbol 통화 코드
 */
enum class CurrencyPair(
    val symbol: String
) {
    USD("USD-KRW"),
    EUR("EUR-KRW"),
    JPY("JPY-KRW"),
    GBP("GBP-KRW"),
    CNY("CNY-KRW"),
    HKD("HKD-KRW"),
    AUD("AUD-KRW"),
    CAD("CAD-KRW"),
    NZD("NZD-KRW"),
    CHF("CHF-KRW"),
    SGD("SGD-KRW"),
    THB("THB-KRW"),
    TWD("TWD-KRW"),
    VND("VND-KRW"),
    INR("INR-KRW"),
    IDR("IDR-KRW"),
    MYR("MYR-KRW"),
    PHP("PHP-KRW"),
    RUB("RUB-KRW"),
    AED("AED-KRW");

    companion object {
        /**
         * 지원하는 통화인지 검증
         * @param symbol 통화 코드
         * @return 지원 여부
         */
        fun isValid(symbol: String): Boolean =
            entries.any {
                it.symbol.equals(symbol, ignoreCase = true)
            }
    }

}