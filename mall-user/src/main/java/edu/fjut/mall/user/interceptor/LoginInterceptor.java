package edu.fjut.mall.user.interceptor;

import edu.fjut.mall.common.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录拦截器：从 Authorization 头解析 JWT，将 userId 放入 request 属性
 */
@Slf4j
@Component
public class LoginInterceptor implements HandlerInterceptor {

    /** Request 属性名 */
    public static final String USER_ID = "userId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(401);
            log.warn("请求缺少 Authorization 头: {}", request.getRequestURI());
            return false;
        }

        String token = authHeader.substring(7);
        try {
            if (JwtUtil.isExpired(token)) {
                response.setStatus(401);
                log.warn("Token 已过期: {}", request.getRequestURI());
                return false;
            }

            Long userId = JwtUtil.getUserId(token);
            request.setAttribute(USER_ID, userId);
            return true;
        } catch (Exception e) {
            response.setStatus(401);
            log.warn("Token 解析失败: {} - {}", request.getRequestURI(), e.getMessage());
            return false;
        }
    }
}
