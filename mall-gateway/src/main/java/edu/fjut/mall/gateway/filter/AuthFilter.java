package edu.fjut.mall.gateway.filter;

import edu.fjut.mall.common.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * GW-02: Token 鉴权全局过滤器
 * <p>
 * 拦截请求，校验 JWT Token 有效性；白名单路径直接放行。
 * 校验通过后将 userId 和 role 写入请求头传递给下游服务。
 */
@Slf4j
@Component
public class AuthFilter implements GlobalFilter, Ordered {

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Value("${gateway.auth.whitelist:}")
    private String whitelistStr;

    private List<String> whitelist;

    @jakarta.annotation.PostConstruct
    public void init() {
        if (whitelistStr != null && !whitelistStr.isBlank()) {
            whitelist = Arrays.asList(whitelistStr.split(","));
        } else {
            whitelist = Collections.emptyList();
        }
        log.info("网关白名单路径: {}", whitelist);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 1. 白名单放行
        if (isWhitelisted(path)) {
            return chain.filter(exchange);
        }

        // 2. 获取 Token
        String token = extractToken(request);
        if (token == null || token.isBlank()) {
            return unauthorized(exchange, "缺少 Authorization 请求头");
        }

        // 3. 校验 Token
        try {
            if (!JwtUtil.validate(token)) {
                return unauthorized(exchange, "Token 已过期或无效");
            }
            Long userId = JwtUtil.getUserId(token);
            Integer role = JwtUtil.getRole(token);

            // 4. 将用户信息传递给下游服务
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Id", String.valueOf(userId))
                    .header("X-User-Role", String.valueOf(role))
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        } catch (Exception e) {
            log.warn("Token 解析失败: {}", e.getMessage());
            return unauthorized(exchange, "Token 解析失败");
        }
    }

    /**
     * 优先级最高（在 AccessLogFilter 之后，保证日志能记录被拒绝的请求）
     */
    @Override
    public int getOrder() {
        return -100;
    }

    /**
     * 判断路径是否在白名单中
     */
    private boolean isWhitelisted(String path) {
        if (whitelist == null || whitelist.isEmpty()) {
            return false;
        }
        for (String pattern : whitelist) {
            if (pathMatcher.match(pattern.trim(), path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 从 Authorization 头中提取 Bearer Token
     */
    private String extractToken(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * 返回 401 未授权响应
     */
    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"code\":401,\"message\":\"" + message + "\",\"data\":null,\"timestamp\":" + System.currentTimeMillis() + "}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}
