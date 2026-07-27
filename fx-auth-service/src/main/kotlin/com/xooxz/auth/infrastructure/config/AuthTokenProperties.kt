package com.xooxz.auth.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "auth.token")
data class AuthTokenProperties(
    val refreshTokenExpiration: Duration
)