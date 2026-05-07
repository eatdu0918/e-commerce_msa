package com.ecommerce.productservice.config;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.common.security.JwtAuthenticationFilter;
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

    // ?    ?   ??    ?(?  ?/   ?    ??   ????   ??   ??
    public static final String[] PUBLIC_GET_PATHS = {
            "/api/products",
            "/api/products/**",
            "/api/categories",
            "/api/categories/**",
            "/api/reviews/products/**",
            "/api/stocks",
            "/api/stocks/**"
    };

    // Swagger  ??    ?   ??
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
                        // Swagger  ??    ?   ??
                        .requestMatchers(SWAGGER_PATHS).permitAll()
                        // ?  ?    ??(GET)???    ?   ??
                        .requestMatchers(HttpMethod.GET, PUBLIC_GET_PATHS).permitAll()
                        // ?  ? ?   /??  /?????ADMIN ?   ??
                        .requestMatchers(HttpMethod.POST, "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")
                        // ??     ???    ?   
                        .anyRequest().authenticated()
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                // JWT ?    ?  ?
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // ??      ??
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json;charset=UTF-8");
                            ApiResponse<Void> errorResponse = ApiResponse.<Void>builder()
                                    .error(ApiResponse.Error.of("UNAUTHORIZED", "?   ???   ??  ??"))
                                    .build();
                            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json;charset=UTF-8");
                            ApiResponse<Void> errorResponse = ApiResponse.<Void>builder()
                                    .error(ApiResponse.Error.of("FORBIDDEN", "?        ????  ??  ."))
                                    .build();
                            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
                        })
                );

        return http.build();
    }
}
