package com.xcl.venueserver.common.utils;

import cn.hutool.core.util.RandomUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 密码工具类
 */
@Component
public class PasswordUtils {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * 生成随机盐值
     * @return 盐值字符串
     */
    public String generateSalt() {
        return RandomUtil.randomString(16);
    }

    /**
     * 加密密码
     * @param rawPassword 原始密码
     * @param salt 盐值
     * @return 加密后的密码
     */
    public String encryptPassword(String rawPassword, String salt) {
        return encoder.encode(rawPassword + salt);
    }

    /**
     * 校验密码
     * @param rawPassword 原始密码
     * @param salt 盐值
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    public boolean matches(String rawPassword, String salt, String encodedPassword) {
        return encoder.matches(rawPassword + salt, encodedPassword);
    }
} 