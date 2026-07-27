package com.xooxz.auth.infrastructure.security

import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

@Component
class TokenHashProvider {

    companion object {
        private const val ALGORITHM = "SHA-256"
    }

    /**
     * 토큰을 SHA-256으로 해시
     */
    fun hash(token: String): String {
        require(token.isNotBlank()) {
            "토큰은 비어 있을 수 없습니다."
        }

        val digest = MessageDigest.getInstance(ALGORITHM)
            .digest(token.toByteArray(StandardCharsets.UTF_8))

        return digest.joinToString(separator = "") { byte ->
            "%02x".format(byte.toInt() and 0xff)
        }
    }

}