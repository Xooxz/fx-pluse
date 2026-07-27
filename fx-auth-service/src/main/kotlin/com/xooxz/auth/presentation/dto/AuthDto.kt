package com.xooxz.auth.presentation.dto

/**
 * 인증 관련 요청 및 응답 DTO
 */
class AuthDto {

    /**
     * Access Token 재발급 요청 DTO
     *
     * @param accessToken 기존 Access Token
     * @param refreshToken 기존 Refresh Token
     */
    data class Request(
        val accessToken: String,
        val refreshToken: String
    )

    /**
     * Access Token 재발급 응답 DTO
     *
     * @param accessToken 새로 발급된 Access Token
     * @param refreshToken 새로 발급된 Refresh Token
     */
    data class Response(
        val accessToken: String,
        val refreshToken: String
    )

}