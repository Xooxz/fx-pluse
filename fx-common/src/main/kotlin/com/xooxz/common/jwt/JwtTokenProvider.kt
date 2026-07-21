package com.xooxz.common.jwt

import com.xooxz.common.exception.ExpiredTokenException
import com.xooxz.common.exception.InvalidTokenException
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import javax.crypto.SecretKey

/**
 * JWT를 검증하고 Claims 정보를 제공하는 컴포넌트
 */
class JwtTokenProvider(
    secret: String
) {

    companion object {
        private const val ROLE_CLAIM = "role"
    }

    /**
     * JWT 서명 검증에 사용하는 Secret Key
     */
    private val signingKey: SecretKey =
        Keys.hmacShaKeyFor(secret.toByteArray(Charsets.UTF_8))

    /**
     * JWT를 검증하고 Claims를 반환
     * @throws ExpiredTokenException 토큰이 만료된 경우
     * @throws InvalidTokenException 토큰이 위조되었거나 형식이 올바르지 않은 경우
     */
    fun getClaims(token: String): Claims {
        return try {
            Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .payload
        } catch (_: ExpiredJwtException) {
            throw ExpiredTokenException()
        } catch (_: JwtException) {
            throw InvalidTokenException()
        } catch (_: IllegalArgumentException) {
            throw InvalidTokenException("토큰 값이 올바르지 않습니다.")
        }
    }

    /**
     * Claims에서 사용자 식별자를 조회
     */
    fun getSubject(claims: Claims): String {
        return claims.subject
            ?: throw InvalidTokenException("사용자 정보가 존재하지 않습니다.")
    }

    /**
     * Claims에서 사용자 권한을 조회
     */
    fun getRole(claims: Claims): String {
        return claims[ROLE_CLAIM] as? String
            ?: throw InvalidTokenException("권한 정보가 존재하지 않습니다.")
    }

}