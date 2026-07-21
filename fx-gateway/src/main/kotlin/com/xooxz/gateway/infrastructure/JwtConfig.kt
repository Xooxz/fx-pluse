package com.xooxz.gateway.infrastructure

import com.xooxz.common.jwt.JwtTokenProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JwtConfig {

    @Bean
    fun jwtTokenProvider(
        @Value("\${jwt.secret}") secret: String
    ): JwtTokenProvider {
        return JwtTokenProvider(secret)
    }

}