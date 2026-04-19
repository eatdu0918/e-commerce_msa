package com.ecommerce.gatewayservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * API 게이트웨이는 JwtAuthenticationFilter 로 JWT를 검증합니다.
 * React SPA 의 /admin 등 클라이언트 라우트는 세션 기반 hasRole 로 막지 않습니다.
 * 실제 관리자 권한은 각 서비스의 /api/admin/** 및 Bearer 토큰으로 검증됩니다.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
            .authorizeExchange(exchanges -> exchanges
                .pathMatchers(
                        "/",
                        "/admin/**",
                        "/static/**",
                        "/webjars/**",
                        "/login",
                        "/error"
                ).permitAll()
                .anyExchange().permitAll()
            )
            .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
            .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
            .logout(ServerHttpSecurity.LogoutSpec::disable)
            .csrf(ServerHttpSecurity.CsrfSpec::disable);
        return http.build();
    }
}
