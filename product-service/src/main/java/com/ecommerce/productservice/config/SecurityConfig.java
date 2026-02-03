package com.ecommerce.productservice.config;

import com.ecommerce.productservice.response.ApiResponse;
import com.ecommerce.productservice.security.jwt.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    // 인증 불필요 경로 (상품 조회는 누구나 가능)
    public static final String[] PUBLIC_GET_PATHS = {
            "/api/products",
            "/api/products/**"
    };

    // Swagger 및 정적 리소스
    public static final String[] SWAGGER_PATHS = {
            "/public/**",
            "/api/swagger-ui/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/api/swagger-ui.html",
            "/api/v3/api-docs/**",
            "/v3/api-docs/**",
            "/favicon.ico",
            "/actuator/**",
            "/swagger-resources/**"
    };

    private final ObjectMapper objectMapper;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // Swagger 및 정적 리소스
                        .requestMatchers(SWAGGER_PATHS).permitAll()
                        // 상품 조회 (GET)는 인증 불필요
                        .requestMatchers(HttpMethod.GET, PUBLIC_GET_PATHS).permitAll()
                        // 상품 등록/수정/삭제는 ADMIN만 가능
                        .requestMatchers(HttpMethod.POST, "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")
                        // 나머지는 인증 필요
                        .anyRequest().authenticated()
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                // JWT 필터 추가
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // 예외 처리
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json;charset=UTF-8");
                            ApiResponse<Void> errorResponse = ApiResponse.<Void>builder()
                                    .error(ApiResponse.Error.of("UNAUTHORIZED", "인증이 필요합니다."))
                                    .build();
                            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json;charset=UTF-8");
                            ApiResponse<Void> errorResponse = ApiResponse.<Void>builder()
                                    .error(ApiResponse.Error.of("FORBIDDEN", "접근 권한이 없습니다."))
                                    .build();
                            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
                        })
                );

        return http.build();
    }
}
