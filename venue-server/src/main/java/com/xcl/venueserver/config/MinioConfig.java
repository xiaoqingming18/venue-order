package com.xcl.venueserver.config;

import io.minio.MinioClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO配置类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "minio")
public class MinioConfig {

    /**
     * MinIO服务地址
     */
    private String endpoint;

    /**
     * MinIO Access Key
     */
    private String accessKey;

    /**
     * MinIO Secret Key
     */
    private String secretKey;

    /**
     * 默认存储桶名称
     */
    private String bucketName;

    /**
     * 创建MinIO客户端
     */
    @Bean
    public MinioClient minioClient() {
        try {
            return new MinioClient(endpoint, accessKey, secretKey);
        } catch (Exception e) {
            throw new RuntimeException("初始化MinIO客户端失败: " + e.getMessage(), e);
        }
    }
} 