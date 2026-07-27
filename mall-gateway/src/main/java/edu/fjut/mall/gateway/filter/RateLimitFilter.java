package edu.fjut.mall.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * GW-05: 简易令牌桶限流过滤器
 * <p>
 * 基于全局令牌桶实现，限制每秒最大请求数。
 * 适用于训练/演示场景，生产环境建议使用 Redis + Sentinel。
 */
@Slf4j
@Component
public class RateLimitFilter implements GlobalFilter, Ordered {

    @Value("${gateway.rate-limit.requests-per-second:20}")
    private int maxRequestsPerSecond;

    /** 令牌桶：按 IP 维度限流 */
    private final Map<String, TokenBucket> bucketMap = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String clientIp = exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : "unknown";

        TokenBucket bucket = bucketMap.computeIfAbsent(clientIp,
                k -> new TokenBucket(maxRequestsPerSecond));

        if (!bucket.tryAcquire()) {
            log.warn("限流触发 - IP: {}, path: {}", clientIp, exchange.getRequest().getURI().getPath());
            return tooManyRequests(exchange);
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        // 限流最先执行（在鉴权和日志之前）
        return -300;
    }

    private Mono<Void> tooManyRequests(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"code\":429,\"message\":\"请求过于频繁，请稍后再试\",\"data\":null,\"timestamp\":"
                + System.currentTimeMillis() + "}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    /**
     * 简易令牌桶实现
     */
    static class TokenBucket {
        private final int maxTokens;
        private final AtomicInteger tokens;
        private volatile long lastRefillTime;

        TokenBucket(int maxTokens) {
            this.maxTokens = maxTokens;
            this.tokens = new AtomicInteger(maxTokens);
            this.lastRefillTime = System.currentTimeMillis();
        }

        boolean tryAcquire() {
            refill();
            int current;
            do {
                current = tokens.get();
                if (current <= 0) {
                    return false;
                }
            } while (!tokens.compareAndSet(current, current - 1));
            return true;
        }

        private void refill() {
            long now = System.currentTimeMillis();
            long elapsed = now - lastRefillTime;
            if (elapsed >= 1000) {
                // 每秒补充全部令牌
                tokens.set(maxTokens);
                lastRefillTime = now;
            }
        }
    }
}
