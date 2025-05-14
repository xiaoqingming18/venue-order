package com.xcl.venueserver.config;

import com.xcl.venueserver.filter.JwtAuthenticationFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.filter.CorsFilter;

import java.util.Collections;

/**
 * 过滤器配置类
 */
@Slf4j
@Configuration
public class FilterConfig {

    /**
     * 注册JWT认证过滤器
     *
     * @param filter JWT过滤器
     * @return 过滤器注册Bean
     */
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthenticationFilterRegistration(JwtAuthenticationFilter filter) {
        log.info("注册JWT认证过滤器...");
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setName("jwtAuthenticationFilter");
        registration.addUrlPatterns("/api/*");
        // 排除支付宝回调接口
        registration.setUrlPatterns(Collections.singletonList("/api/*"));
        registration.setInitParameters(Collections.singletonMap("exclusions", "/api/alipay/notify"));
        registration.setOrder(1);
        return registration;
    }
    
    /**
     * 配置CORS过滤器
     */
    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilterRegistration(CorsFilter corsFilter) {
        log.info("注册CORS过滤器...");
        FilterRegistrationBean<CorsFilter> registration = new FilterRegistrationBean<>(corsFilter);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);  // 最高优先级，确保CORS过滤器最先执行
        registration.addUrlPatterns("/*");
        registration.setName("corsFilter");
        registration.setEnabled(true);
        return registration;
    }
} 