package edu.fjut.mall.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * GW-04: 请求日志记录过滤器
 * <p>
 * 记录每个请求的路径、方法、耗时、响应状态码。
 */
@Slf4j
@Component
public class AccessLogFilter implements GlobalFilter, Ordered {

    private static final String START_TIME_ATTR = "requestStartTime";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String method = request.getMethod().name();
        String path = request.getURI().getPath();
        String clientIp = request.getRemoteAddress() != null
                ? request.getRemoteAddress().getAddress().getHostAddress() : "unknown";

        // 记录开始时间
        exchange.getAttributes().put(START_TIME_ATTR, System.currentTimeMillis());

        log.info(">>> 请求开始: {} {} from {}", method, path, clientIp);

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            Long startTime = exchange.getAttribute(START_TIME_ATTR);
            if (startTime != null) {
                long duration = System.currentTimeMillis() - startTime;
                ServerHttpResponse response = exchange.getResponse();
                int statusCode = response.getStatusCode() != null
                        ? response.getStatusCode().value() : 0;
                log.info("<<< 请求结束: {} {} -> {} ({}ms)", method, path, statusCode, duration);
            }
        }));
    }

    /**
     * 最低优先级（最先执行，最后结束），保证能记录整个请求链的耗时
     */
    @Override
    public int getOrder() {
        return -200;
    }
}
