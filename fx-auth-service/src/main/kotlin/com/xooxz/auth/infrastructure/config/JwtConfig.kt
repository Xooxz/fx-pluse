package com.xooxz.auth.infrastructure.config

import com.xooxz.auth.application.port.out.TokenIssuer
import com.xooxz.auth.infrastructure.security.JwtTokenIssuer
import com.xooxz.common.jwt.JwtTokenProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JwtConfig(
    private val jwtProperties: JwtProperties
) {

    @Bean
    fun jwtTokenProvider(): JwtTokenProvider {
        return JwtTokenProvider(
            secret = jwtProperties.secret
        )
    }

    @Bean
    fun tokenIssuer(): TokenIssuer {
        return JwtTokenIssuer(
            secret = jwtProperties.secret,
            accessTokenExpiration = jwtProperties.accessTokenExpiration
        )
    }
}