package com.xcl.venueserver.interceptor;

import com.xcl.venueserver.common.utils.CurrentUser;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 管理员权限拦截器
 */
@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    private final CurrentUser currentUser;

    public AdminAuthInterceptor(CurrentUser currentUser) {
        this.currentUser = currentUser;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        // 检查用户是否已登录，并且是管理员
        if (!currentUser.isAuthenticated() || !currentUser.isAdmin()) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"code\":403,\"message\":\"权限不足，需要管理员权限\",\"data\":null}");
            return false;
        }
        return true;
    }
} 