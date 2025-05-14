package com.xcl.venueserver.common.utils;

import lombok.Data;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 当前用户信息工具类
 */
@Data
@Component
public class CurrentUser {

    private final JwtUtils jwtUtils;
    
    public CurrentUser(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    /**
     * 获取当前登录用户ID
     * @return 用户ID
     */
    public Long getUserId() {
        String token = getTokenFromRequest();
        if (token == null) {
            return null;
        }
        return jwtUtils.getUserIdFromToken(token);
    }

    /**
     * 获取当前登录用户名
     * @return 用户名
     */
    public String getUsername() {
        String token = getTokenFromRequest();
        if (token == null) {
            return null;
        }
        return jwtUtils.getUsernameFromToken(token);
    }
    
    /**
     * 获取当前用户角色
     * @return 用户角色
     */
    public Integer getUserRole() {
        String token = getTokenFromRequest();
        if (token == null) {
            return null;
        }
        return jwtUtils.getRoleFromToken(token);
    }
    
    /**
     * 判断当前用户是否为管理员
     * @return 是否为管理员
     */
    public boolean isAdmin() {
        Integer role = getUserRole();
        return role != null && role == 2;
    }
    
    /**
     * 判断当前是否已登录
     * @return 是否已登录
     */
    public boolean isAuthenticated() {
        String token = getTokenFromRequest();
        return token != null && jwtUtils.validateToken(token);
    }

    /**
     * 从请求中获取token
     * @return token字符串
     */
    private String getTokenFromRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        
        HttpServletRequest request = attributes.getRequest();
        return request.getHeader(jwtUtils.getHeaderName());
    }
} 