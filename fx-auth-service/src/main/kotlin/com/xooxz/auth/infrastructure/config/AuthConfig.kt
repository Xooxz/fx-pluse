package com.xooxz.auth.infrastructure.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(AuthTokenProperties::class, JwtProperties::class)
class AuthConfig