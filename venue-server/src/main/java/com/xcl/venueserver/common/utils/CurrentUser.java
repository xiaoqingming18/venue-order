package com.xcl.venueserver.common.utils;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/**
 * 当前用户信息工具类
 */
@Data
@Slf4j
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
            log.warn("获取userId失败: token为空");
            return null;
        }
        
        try {
            Long userId = jwtUtils.getUserIdFromToken(token);
            log.debug("从token获取userId: {}", userId);
            return userId;
        } catch (Exception e) {
            log.error("从token解析userId失败", e);
            return null;
        }
    }

    /**
     * 获取当前登录用户名
     * @return 用户名
     */
    public String getUsername() {
        String token = getTokenFromRequest();
        if (token == null) {
            log.warn("获取username失败: token为空");
            return null;
        }
        
        try {
            String username = jwtUtils.getUsernameFromToken(token);
            log.debug("从token获取username: {}", username);
            return username;
        } catch (Exception e) {
            log.error("从token解析username失败", e);
            return null;
        }
    }
    
    /**
     * 获取当前用户角色
     * @return 用户角色
     */
    public Integer getUserRole() {
        String token = getTokenFromRequest();
        if (token == null) {
            log.warn("获取userRole失败: token为空");
            return null;
        }
        
        try {
            Integer role = jwtUtils.getRoleFromToken(token);
            log.debug("从token获取userRole: {}", role);
            return role;
        } catch (Exception e) {
            log.error("从token解析userRole失败", e);
            return null;
        }
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
            log.warn("获取RequestAttributes失败，可能是在非Web上下文中调用");
            return null;
        }
        
        HttpServletRequest request = attributes.getRequest();
        String token = request.getHeader(jwtUtils.getHeaderName());
        
        // 调试信息: 打印所有请求头
        log.debug("当前请求URL: {}", request.getRequestURI());
        log.debug("寻找请求头: {}", jwtUtils.getHeaderName());
        
        if (token == null) {
            log.debug("请求头中所有可用的头信息:");
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                log.debug("  {} = {}", headerName, request.getHeader(headerName));
            }
        } else {
            log.debug("找到token: {}", token.length() > 20 ? token.substring(0, 20) + "..." : token);
        }
        
        return token;
    }
    
    @Override
    public String toString() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "CurrentUser{requestAttributes=null}";
        }
        
        HttpServletRequest request = attributes.getRequest();
        StringBuilder sb = new StringBuilder("CurrentUser{");
        
        sb.append("uri='").append(request.getRequestURI()).append("'");
        
        Enumeration<String> headerNames = request.getHeaderNames();
        sb.append(", headers={");
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if (headerName.equalsIgnoreCase("authorization")) {
                String authHeader = request.getHeader(headerName);
                if (authHeader != null && authHeader.length() > 20) {
                    sb.append(headerName).append("='").append(authHeader.substring(0, 20)).append("...'");
                } else {
                    sb.append(headerName).append("='").append(authHeader).append("'");
                }
            } else {
                sb.append(headerName).append("='").append(request.getHeader(headerName)).append("'");
            }
            if (headerNames.hasMoreElements()) {
                sb.append(", ");
            }
        }
        sb.append("}}");
        
        return sb.toString();
    }
} 