package com.xooxz.gateway.infrastructure.security

import com.xooxz.gateway.infrastructure.jwt.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository

@Configuration
@EnableWebFluxSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {

    /**
     * Gateway 보안 설정
     */
    @Bean
    fun securityWebFilterChain(
        http: ServerHttpSecurity
    ): SecurityWebFilterChain {
        return http
            .securityContextRepository(
                NoOpServerSecurityContextRepository.getInstance()
            )

            .csrf { it.disable() }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .logout { it.disable() }

            .authorizeExchange { exchanges ->
                exchanges
                    .pathMatchers(HttpMethod.OPTIONS, "/**")
                    .permitAll()

                    .pathMatchers(
                        "/api/auth/login",
                        "/api/auth/signup",
                        "/api/auth/reissue",

                        "/api/rates/**",
                        "/api/notification/**",
                    )
                    .permitAll()

                    .pathMatchers(
                        "/actuator/health",
                        "/actuator/info"
                    )
                    .permitAll()

                    .pathMatchers("/api/admin/**")
                    .hasRole("ADMIN")
                    .anyExchange()
                    .authenticated()
            }

            .addFilterAt(
                jwtAuthenticationFilter,
                SecurityWebFiltersOrder.AUTHENTICATION
            )
            .build()
    }
}