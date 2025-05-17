package com.xcl.venueserver.filter;

import com.xcl.venueserver.common.utils.CurrentUser;
import com.xcl.venueserver.common.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * JWT认证过滤器
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final CurrentUser currentUser;
    
    // 不需要验证token的路径
    private static final List<String> EXCLUDE_PATHS = Arrays.asList(
            "/api/auth/login",
            "/api/auth/register",
            "/auth/login",
            "/auth/register",
            "/api/common/**",
            "/api/test/public",
            "/api/venues/*/availability",
            "/api/venue-facilities/venue/*",
            "/api/venues/search",  // 场馆搜索接口
            "/venues/search",      // 场馆搜索接口 - 不带/api前缀
            "/*/venues/search",    // 场馆搜索接口 - 通配符匹配前缀
            "/api/alipay/notify",  // 支付宝异步回调接口
            "/api/alipay/return",  // 支付宝同步回调接口
            "/api/alipay/test",    // 支付宝测试接口
            "/api/alipay/pay",     // 支付宝支付接口 - 仅用于测试环境
            "/api/alipay/mock-notify", // 模拟支付宝回调接口
            "/api/payment/test-update-status/**", // 测试更新订单状态接口
            "/api/payment/direct-update/**", // 直接SQL更新订单状态接口
            "/error"
    );
    
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JwtAuthenticationFilter(JwtUtils jwtUtils, CurrentUser currentUser) {
        this.jwtUtils = jwtUtils;
        this.currentUser = currentUser;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        // 获取请求路径
        String path = request.getRequestURI();
        log.debug("JWT过滤器处理请求路径: {}, 方法: {}", path, request.getMethod());
        
        // 如果是OPTIONS预检请求，直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            log.debug("OPTIONS预检请求，直接放行: {}", path);
            filterChain.doFilter(request, response);
            return;
        }
        
        // 如果是排除的路径，则直接放行
        if (isExcludePath(path)) {
            log.debug("路径{}在白名单中，无需验证token", path);
            filterChain.doFilter(request, response);
            return;
        }
        
        // 获取请求头中的token
        String token = request.getHeader(jwtUtils.getHeaderName());
        log.debug("从请求头中获取token: {} = {}", jwtUtils.getHeaderName(), token != null ? token.substring(0, Math.min(token != null ? token.length() : 0, 20)) + "..." : "null");
        
        // 如果token为空或无效，则返回未授权错误
        if (token == null || !jwtUtils.validateToken(token)) {
            String errorMsg = token == null ? "未提供token" : "token无效或已过期";
            log.warn("认证失败: {}, 路径: {}", errorMsg, path);
            
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"code\":401,\"message\":\"未授权，请先登录: " + errorMsg + "\",\"data\":null}");
            return;
        }
        
        // token有效，设置用户信息到CurrentUser中
        try {
            Long userId = jwtUtils.getUserIdFromToken(token);
            String username = jwtUtils.getUsernameFromToken(token);
            Integer role = jwtUtils.getRoleFromToken(token);
            
            // 将用户信息设置到CurrentUser中
            currentUser.setCurrentUserId(userId);
            currentUser.setCurrentUsername(username);
            currentUser.setCurrentUserRole(role);
            
            log.debug("JWT验证通过，已设置用户信息到CurrentUser: userId={}, username={}, role={}", userId, username, role);
        } catch (Exception e) {
            log.error("设置用户信息到CurrentUser失败", e);
        }
        
        // token有效，放行请求
        log.debug("token验证通过，继续处理请求: {}", path);
        filterChain.doFilter(request, response);
    }
    
    /**
     * 判断是否是排除的路径
     */
    private boolean isExcludePath(String path) {
        log.debug("开始判断路径[{}]是否在白名单中", path);
        
        // 去除前导斜杠后的路径，用于兼容不同格式的URL
        String normalizedPath = path;
        if (normalizedPath.startsWith("/")) {
            normalizedPath = normalizedPath.substring(1);
        }
        
        // 尝试增加和去除/api前缀的匹配
        String withApiPrefix = "/api/" + normalizedPath;
        String withoutApiPrefix = normalizedPath.startsWith("api/") ? "/" + normalizedPath.substring(4) : "/" + normalizedPath;
        
        log.debug("标准化路径：原始=[{}], 带API前缀=[{}], 不带API前缀=[{}]", path, withApiPrefix, withoutApiPrefix);
        
        // 首先尝试直接匹配
        for (String pattern : EXCLUDE_PATHS) {
            if (pathMatcher.match(pattern, path)) {
                log.debug("路径 {} 直接匹配白名单模式 {}", path, pattern);
                return true;
            }
            
            // 尝试匹配带API前缀的路径
            if (pathMatcher.match(pattern, withApiPrefix)) {
                log.debug("路径 {} (转换为 {}) 匹配白名单模式 {}", path, withApiPrefix, pattern);
                return true;
            }
            
            // 尝试匹配不带API前缀的路径
            if (pathMatcher.match(pattern, withoutApiPrefix)) {
                log.debug("路径 {} (转换为 {}) 匹配白名单模式 {}", path, withoutApiPrefix, pattern);
                return true;
            }
        }
        
        // 特殊处理场馆搜索接口
        if (path.endsWith("/venues/search") || path.contains("/venues/search?")) {
            log.debug("特殊处理：路径 {} 是场馆搜索接口，放行", path);
            return true;
        }
        
        log.debug("路径 {} 不在白名单中，需要验证token", path);
        return false;
    }
} 