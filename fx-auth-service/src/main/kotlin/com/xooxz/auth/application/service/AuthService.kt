package com.xooxz.auth.application.service

import com.xooxz.auth.application.port.out.AuthRepository
import com.xooxz.auth.application.port.out.TokenIssuer
import com.xooxz.auth.domain.InvalidRefreshTokenException
import com.xooxz.auth.domain.RefreshTokenReuseDetectedException
import com.xooxz.auth.domain.RefreshTokenRotateResult
import com.xooxz.auth.infrastructure.config.AuthTokenProperties
import com.xooxz.auth.infrastructure.security.TokenHashProvider
import com.xooxz.auth.presentation.dto.AuthDto
import com.xooxz.common.exception.InvalidTokenException
import com.xooxz.common.jwt.JwtTokenProvider
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val authRepository: AuthRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val tokenIssuer: TokenIssuer,
    private val tokenHashProvider: TokenHashProvider,
    private val authTokenProperties: AuthTokenProperties
) {

    companion object {
        private const val BEARER_PREFIX = "Bearer "
    }

    /**
     * JWT 유효성을 검증
     *
     * @param authorization Authorization 헤더
     */
    fun validateToken(authorization: String) {
        val accessToken = extractBearerToken(authorization)
        jwtTokenProvider.getClaims(accessToken)
    }

    /**
     * 유효한 Access Token에서 memberKey를 조회
     *
     * @param authorization Authorization 헤더
     * @return 사용자 고유 식별자
     */
    fun getMemberKey(authorization: String): String {
        val accessToken = extractBearerToken(authorization)
        val claims = jwtTokenProvider.getClaims(accessToken)

        return jwtTokenProvider.getSubject(claims)
    }

    /**
     * Access Token과 Refresh Token을 재발행
     *
     * @param dto AuthDto.Request
     * @return AuthDto.Response
     */
    fun reissue(dto: AuthDto.Request): AuthDto.Response {
        validateReissueRequest(dto)

        val accessToken = extractBearerToken(dto.accessToken)
        val claims = jwtTokenProvider.getClaimsAllowExpired(accessToken)
        val memberKey = jwtTokenProvider.getSubject(claims)

        val newRefreshToken = tokenIssuer.createRefreshToken()
        val requestedRefreshTokenHash = tokenHashProvider.hash(dto.refreshToken)
        val newRefreshTokenHash = tokenHashProvider.hash(newRefreshToken)

        /*
         * Redis Lua Script에서 기존 Refresh Token 검증과
         * 신규 Refresh Token 교체를 원자적으로 처리합니다.
         */
        val rotateResult = authRepository.rotateRefreshToken(
            memberKey = memberKey,
            currentRefreshTokenHash = requestedRefreshTokenHash,
            newRefreshTokenHash = newRefreshTokenHash,
            ttl = authTokenProperties.refreshTokenExpiration
        )

        when (rotateResult) {
            RefreshTokenRotateResult.SUCCESS -> Unit

            RefreshTokenRotateResult.NOT_FOUND -> {
                throw InvalidRefreshTokenException()
            }

            RefreshTokenRotateResult.REUSED -> {
                throw RefreshTokenReuseDetectedException()
            }
        }

        // Refresh Token 교체가 성공한 이후에 새로운 Access Token을 생성
        val newAccessToken = tokenIssuer.createAccessToken(memberKey)

        return AuthDto.Response(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken
        )
    }

    /**
     * 로그인 성공 후 Refresh Token을 Redis에 저장
     *
     * @param memberKey 사용자 고유 식별자
     * @param refreshToken Refresh Token 원문
     */
    fun saveRefreshToken(
        memberKey: String,
        refreshToken: String
    ) {
        if (memberKey.isBlank()) {
            throw InvalidTokenException(
                "memberKey는 비어 있을 수 없습니다."
            )
        }

        if (refreshToken.isBlank()) {
            throw InvalidTokenException(
                "Refresh Token은 비어 있을 수 없습니다."
            )
        }

        val refreshTokenHash = tokenHashProvider.hash(
            refreshToken
        )

        authRepository.saveRefreshToken(
            memberKey = memberKey,
            refreshTokenHash = refreshTokenHash,
            ttl = authTokenProperties.refreshTokenExpiration
        )
    }

    /**
     * 로그아웃 시 Refresh Token을 제거
     *
     * @param authorization Authorization 헤더
     */
    fun logout(authorization: String) {
        val memberKey = getMemberKey(authorization)
        authRepository.deleteRefreshToken(memberKey)
    }

    /**
     * Authorization 헤더에서 Bearer Token을 추출
     *
     * @param authorization Authorization 헤더
     * @return Bearer Prefix가 제거된 Access Token
     */
    private fun extractBearerToken(
        authorization: String
    ): String {
        if (authorization.isBlank()) {
            throw InvalidTokenException(
                "Authorization 헤더가 비어 있습니다."
            )
        }

        if (!authorization.startsWith(BEARER_PREFIX)) {
            throw InvalidTokenException(
                "Bearer 인증 형식이 아닙니다."
            )
        }

        return authorization
            .removePrefix(BEARER_PREFIX)
            .trim()
            .takeIf { it.isNotBlank() }
            ?: throw InvalidTokenException(
                "Access Token이 존재하지 않습니다."
            )
    }

    /**
     * 토큰 재발급 요청값을 검증
     *
     * @param dto AuthDto.Request
     */
    private fun validateReissueRequest(
        dto: AuthDto.Request
    ) {
        if (dto.accessToken.isBlank()) {
            throw InvalidTokenException(
                "Access Token은 비어 있을 수 없습니다."
            )
        }

        if (dto.refreshToken.isBlank()) {
            throw InvalidTokenException(
                "Refresh Token은 비어 있을 수 없습니다."
            )
        }
    }

}