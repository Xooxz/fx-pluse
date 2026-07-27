package com.xooxz.auth.domain

enum class RefreshTokenRotateResult {
    /** Refresh Token 교체 성공 */
    SUCCESS,

    /** Redis에 Refresh Token이 존재하지 않음 */
    NOT_FOUND,

    /** 저장된 Refresh Token과 요청 Refresh Token이 다름 */
    REUSED
}