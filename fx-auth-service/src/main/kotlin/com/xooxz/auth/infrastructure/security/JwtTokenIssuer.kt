package com.xooxz.auth.infrastructure.security

import com.xooxz.auth.application.port.out.TokenIssuer
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.time.Duration
import java.time.Instant
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey

class JwtTokenIssuer(
    secret: String,
    private val accessTokenExpiration: Duration
) : TokenIssuer {

    /**
     * JWT 서명에 사용하는 Secret Key
     */
    private val signingKey: SecretKey = Keys.hmacShaKeyFor(
        secret.toByteArray(Charsets.UTF_8)
    )

    init {
        require(secret.isNotBlank()) {
            "JWT secret은 비어 있을 수 없습니다."
        }

        require(
            !accessTokenExpiration.isZero &&
                    !accessTokenExpiration.isNegative
        ) {
            "Access Token 유효기간은 0보다 커야 합니다."
        }
    }

    /**
     * Access Token을 생성
     *
     * @param memberKey 사용자 고유 식별자
     * @return JWT Access Token
     */
    override fun createAccessToken(
        memberKey: String
    ): String {
        require(memberKey.isNotBlank()) {
            "memberKey는 비어 있을 수 없습니다."
        }

        val issuedAt = Instant.now()
        val expiration = issuedAt.plus(
            accessTokenExpiration
        )

        return Jwts.builder()
            .subject(memberKey)
            .issuedAt(Date.from(issuedAt))
            .expiration(Date.from(expiration))
            .signWith(signingKey)
            .compact()
    }

    /**
     * Refresh Token을 생성
     *
     * @return Refresh Token
     */
    override fun createRefreshToken(): String {
        return UUID.randomUUID().toString()
    }

}