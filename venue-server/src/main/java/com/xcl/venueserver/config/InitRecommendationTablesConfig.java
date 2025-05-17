package com.xcl.venueserver.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * 初始化推荐系统相关表的配置类
 */
@Slf4j
@Configuration
public class InitRecommendationTablesConfig {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public InitRecommendationTablesConfig(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 初始化方法，创建必要的表结构
     */
    @PostConstruct
    public void initTables() {
        log.info("开始初始化推荐系统相关表...");
        try {
            // 读取SQL文件
            String sql = readSqlFile("sql/user_browse_history.sql");
            
            // 按分号分割SQL语句并执行
            String[] statements = sql.split(";");
            for (String statement : statements) {
                if (!statement.trim().isEmpty()) {
                    try {
                        jdbcTemplate.execute(statement);
                        log.info("执行SQL成功: {}", statement.substring(0, Math.min(100, statement.length())));
                    } catch (Exception e) {
                        log.error("执行SQL失败: {}", statement, e);
                    }
                }
            }
            
            log.info("推荐系统相关表初始化完成");
        } catch (Exception e) {
            log.error("初始化推荐系统相关表失败", e);
        }
    }

    /**
     * 从资源文件中读取SQL脚本
     *
     * @param resourcePath 资源路径
     * @return SQL脚本内容
     */
    private String readSqlFile(String resourcePath) throws IOException {
        ClassPathResource resource = new ClassPathResource(resourcePath);
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
} 