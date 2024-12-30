package com.shanshan.auction.interceptor;

import com.shanshan.auction.annotation.RequireAdmin;
import com.shanshan.auction.exception.UnauthorizedException;
import com.shanshan.auction.model.User;
import com.shanshan.auction.model.enums.UserRole;
import com.shanshan.auction.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@RequiredArgsConstructor
public class AdminAuthInterceptor implements HandlerInterceptor {
    private final UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RequireAdmin requireAdmin = handlerMethod.getMethodAnnotation(RequireAdmin.class);
        if (requireAdmin == null) {
            return true;
        }

        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getById(userId);
        
        if (user == null || user.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedException("需要管理员权限");
        }

        return true;
    }
} 