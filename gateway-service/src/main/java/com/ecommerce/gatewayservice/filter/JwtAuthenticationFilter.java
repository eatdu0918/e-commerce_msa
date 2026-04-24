package com.ecommerce.gatewayservice.filter;

import com.ecommerce.gatewayservice.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtProperties jwtProperties;
    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    private SecretKey secretKey;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String BLACKLIST_PREFIX = "blacklist:";
    private static final String TOKEN_TYPE = "type";
    private static final String TOKEN_TYPE_ACCESS = "ACCESS";
    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_ROLE = "role";

    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/auth/signup",
            "/api/auth/login",
            "/api/auth/refresh",
            "/api/auth/findUser/**",
            "/api/v3/api-docs/**",
            "/**/v3/api-docs/**",
            "/api/swagger-ui/**",
            "/api/swagger-ui.html",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/webjars/**",
            "/actuator/**"
    );

    private static final List<String> PUBLIC_GET_PATHS = List.of(
            "/api/products/**",
            "/api/categories/**",
            "/api/reviews/products/**",
            // 상품 상세·비로그인 조회용 재고 (프론트 getStock / checkStock)
            "/api/stocks/**"
    );

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(
                jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)
        );
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        HttpMethod method = request.getMethod();

        // OPTIONS 요청은 통과
        if (method == HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }

        // 공개 경로는 인증 불필요
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        // GET 공개 경로는 인증 불필요
        if (method == HttpMethod.GET && isPublicGetPath(path)) {
            return chain.filter(exchange);
        }

        // Authorization 헤더 추출
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return onError(exchange, HttpStatus.UNAUTHORIZED, "인증 토큰이 필요합니다.");
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        // JWT 검증
        Claims claims;
        try {
            claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            return onError(exchange, HttpStatus.UNAUTHORIZED, "만료된 토큰입니다.");
        } catch (Exception e) {
            return onError(exchange, HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다.");
        }

        // Access Token 확인
        String tokenType = claims.get(TOKEN_TYPE, String.class);
        if (!TOKEN_TYPE_ACCESS.equals(tokenType)) {
            return onError(exchange, HttpStatus.UNAUTHORIZED, "Access Token이 아닙니다.");
        }

        // Redis 블랙리스트 확인 후 헤더 전달
        String blacklistKey = BLACKLIST_PREFIX + token;
        return reactiveRedisTemplate.hasKey(blacklistKey)
                .flatMap(isBlacklisted -> {
                    if (Boolean.TRUE.equals(isBlacklisted)) {
                        return onError(exchange, HttpStatus.UNAUTHORIZED, "로그아웃된 토큰입니다.");
                    }

                    // 사용자 정보를 헤더에 추가하여 하위 서비스로 전달
                    String userId = claims.getSubject();
                    String email = claims.get(CLAIM_EMAIL, String.class);
                    String role = claims.get(CLAIM_ROLE, String.class);

                    log.debug("JWT 추출 정보 - userId: {}, email: {}, role: {}", userId, email, role);

                    if (userId == null || email == null) {
                        return onError(exchange, HttpStatus.UNAUTHORIZED, "토큰 정보가 부적절합니다.");
                    }

                    // ReadOnlyHttpHeaders 오류를 피하기 위해 Decorator를 사용하여 헤더를 교체합니다.
                    HttpHeaders writableHeaders = new HttpHeaders();
                    writableHeaders.addAll(exchange.getRequest().getHeaders());
                    writableHeaders.set("X-User-Id", userId);
                    writableHeaders.set("X-User-Email", email);
                    writableHeaders.set("X-User-Role", role);

                    org.springframework.http.server.reactive.ServerHttpRequest mutatedRequest = 
                        new org.springframework.http.server.reactive.ServerHttpRequestDecorator(exchange.getRequest()) {
                            @Override
                            public HttpHeaders getHeaders() {
                                return writableHeaders;
                            }
                        };

                    return chain.filter(exchange.mutate().request(mutatedRequest).build());
                });
    }

    private boolean isPublicPath(String path) {
        if (path.contains("v3/api-docs") || path.contains("swagger-ui") || path.contains("webjars")) {
            return true;
        }
        return PUBLIC_PATHS.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private boolean isPublicGetPath(String path) {
        return PUBLIC_GET_PATHS.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus status, String message) {
        log.warn("인증 실패: {} - {}", exchange.getRequest().getURI().getPath(), message);
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        String body = "{\"success\":false,\"error\":{\"errorCode\":\"UNAUTHORIZED\",\"errorMessage\":\"" + message + "\"}}";
        return response.writeWith(
                Mono.just(response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8)))
        );
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
