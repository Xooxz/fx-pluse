package com.xooxz.notification.domain

/**
 * 알림 재발송 주기
 */
enum class AlertInterval(
    val seconds: Long
) {
    ONCE(0),
    //테스트용
    THIRTY_SECONDS(30),
    TEN_MINUTES(600),
    ONE_HOUR(3600),
    ONE_DAY(86400)
}