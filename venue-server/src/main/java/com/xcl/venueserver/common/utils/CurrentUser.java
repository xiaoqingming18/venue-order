package com.xcl.venueserver.common.utils;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * 当前用户工具类
 */
@Component
public class CurrentUser {

    private static final String USER_ID_KEY = "current_user_id";
    private static final String USER_ROLE_KEY = "current_user_role";
    private static final String USER_NAME_KEY = "current_user_name";
    
    /**
     * 获取当前登录用户ID
     *
     * @return 用户ID，未登录返回null
     */
    public Long getCurrentUserId() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        
        HttpServletRequest request = attributes.getRequest();
        HttpSession session = request.getSession(false);
        
        if (session == null) {
            return null;
        }
        
        Object userId = session.getAttribute(USER_ID_KEY);
        return userId == null ? null : (Long) userId;
    }
    
    /**
     * 获取当前登录用户ID (兼容旧代码)
     *
     * @return 用户ID，未登录返回null
     */
    public Long getUserId() {
        return getCurrentUserId();
    }
    
    /**
     * 获取当前登录用户角色
     *
     * @return 用户角色ID，未登录返回null
     */
    public Integer getUserRole() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        
        HttpServletRequest request = attributes.getRequest();
        HttpSession session = request.getSession(false);
        
        if (session == null) {
            return null;
        }
        
        Object userRole = session.getAttribute(USER_ROLE_KEY);
        return userRole == null ? null : (Integer) userRole;
    }
    
    /**
     * 获取当前登录用户名
     *
     * @return 用户名，未登录返回null
     */
    public String getUsername() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        
        HttpServletRequest request = attributes.getRequest();
        HttpSession session = request.getSession(false);
        
        if (session == null) {
            return null;
        }
        
        Object username = session.getAttribute(USER_NAME_KEY);
        return username == null ? null : (String) username;
    }
    
    /**
     * 判断当前是否已登录
     *
     * @return 是否已登录
     */
    public boolean isAuthenticated() {
        return getCurrentUserId() != null;
    }
    
    /**
     * 设置当前用户ID到Session
     *
     * @param userId 用户ID
     */
    public void setCurrentUserId(Long userId) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            HttpSession session = request.getSession(true);
            session.setAttribute(USER_ID_KEY, userId);
        }
    }
    
    /**
     * 设置当前用户名到Session
     *
     * @param username 用户名
     */
    public void setCurrentUsername(String username) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            HttpSession session = request.getSession(true);
            session.setAttribute(USER_NAME_KEY, username);
        }
    }
    
    /**
     * 清除当前用户ID
     */
    public void clearCurrentUserId() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.removeAttribute(USER_ID_KEY);
            }
        }
    }
    
    /**
     * 判断当前用户是否为管理员
     *
     * @return 是否为管理员
     */
    public boolean isAdmin() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return false;
        }
        
        HttpServletRequest request = attributes.getRequest();
        HttpSession session = request.getSession(false);
        
        if (session == null) {
            return false;
        }
        
        Object userRole = session.getAttribute(USER_ROLE_KEY);
        if (userRole == null) {
            return false;
        }
        
        // 假设角色值为2表示管理员
        return Integer.valueOf(2).equals(userRole);
    }
    
    /**
     * 设置当前用户角色到Session
     *
     * @param roleId 角色ID (1-普通用户, 2-管理员)
     */
    public void setCurrentUserRole(Integer roleId) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            HttpSession session = request.getSession(true);
            session.setAttribute(USER_ROLE_KEY, roleId);
        }
    }
} 