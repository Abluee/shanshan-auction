package com.shanshan.auction.config;

import com.shanshan.auction.exception.UnauthorizedException;
import com.shanshan.auction.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationInterceptor implements HandlerInterceptor {
    private final JwtUtil jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 排除登录接口和其他公开接口
        if (isPublicPath(request.getRequestURI())) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            log.warn("Token is missing or invalid format: {}", token);
            throw new UnauthorizedException("未登录");
        }

        try {
            token = token.replace("Bearer ", "");
            log.debug("Validating token: {}", token);
            if (!jwtUtils.validateToken(token)) {
                log.warn("Token validation failed for token: {}", token);
                throw new UnauthorizedException("登录已过期");
            }
            // 将用户ID存入请求属性中
            Long userId = jwtUtils.getUserIdFromToken(token);
            log.debug("Token validated successfully for user: {}", userId);
            request.setAttribute("userId", userId);
            return true;
        } catch (Exception e) {
            log.error("Token validation failed", e);
            throw new UnauthorizedException("登录已过期: " + e.getMessage());
        }
    }

    private boolean isPublicPath(String path) {
        return path.equals("/auth/login") ||
               path.equals("/auth/register") ||
               path.startsWith("/error");
    }
} 