package com.ecommerce.gatewayservice.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Component
public class GlobalLoggingFilter implements GlobalFilter, Ordered {

    private static final String CORRELATION_ID = "X-Correlation-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String correlationId = UUID.randomUUID().toString().substring(0, 8);

        // 주의: Spring 6.x + Spring Cloud Gateway 의 GlobalFilter 단계에서는 request 의
        // HttpHeaders 가 ReadOnlyHttpHeaders 로 감싸져 있어 request.mutate().header(...)
        // 또는 .headers(h -> h.set(...)) 호출이 모두 UnsupportedOperationException 으로 깨진다.
        // 이 필터는 단순 로깅 목적이므로 헤더를 다운스트림으로 굳이 전파하지 않고
        // ServerWebExchange attribute 에만 보관한다. (응답 헤더가 필요하면 응답 단계에서 추가)
        exchange.getAttributes().put(CORRELATION_ID, correlationId);

        log.info("[{}] {} {} - Client: {}",
                correlationId,
                request.getMethod(),
                request.getURI().getPath(),
                request.getRemoteAddress());

        long startTime = System.currentTimeMillis();

        return chain.filter(exchange)
                .then(Mono.fromRunnable(() -> {
                    long duration = System.currentTimeMillis() - startTime;
                    log.info("[{}] {} {} - Status: {} - {}ms",
                            correlationId,
                            request.getMethod(),
                            request.getURI().getPath(),
                            exchange.getResponse().getStatusCode(),
                            duration);
                }));
    }

    @Override
    public int getOrder() {
        return -2;
    }
}
