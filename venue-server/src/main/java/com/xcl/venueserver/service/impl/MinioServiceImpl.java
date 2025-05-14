package com.xcl.venueserver.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.xcl.venueserver.config.MinioConfig;
import com.xcl.venueserver.service.MinioService;
import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.PutObjectArgs;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.GetObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.SetBucketPolicyArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * MinIO文件存储服务实现类
 */
@Slf4j
@Service
public class MinioServiceImpl implements MinioService {

    @Resource
    private MinioClient minioClient;

    @Resource
    private MinioConfig minioConfig;

    /**
     * 初始化默认存储桶
     */
    @PostConstruct
    public void init() {
        try {
            // 检查默认存储桶是否存在，不存在则创建
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .build());
            if (!found) {
                // 创建默认存储桶
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(minioConfig.getBucketName())
                        .build());
                log.info("成功创建存储桶: {}", minioConfig.getBucketName());
                
                // 设置存储桶访问策略为公共读取
                String policy = "{\n" +
                        "    \"Version\": \"2012-10-17\",\n" +
                        "    \"Statement\": [\n" +
                        "        {\n" +
                        "            \"Effect\": \"Allow\",\n" +
                        "            \"Principal\": {\"AWS\": [\"*\"]},\n" +
                        "            \"Action\": [\"s3:GetObject\"],\n" +
                        "            \"Resource\": [\"arn:aws:s3:::" + minioConfig.getBucketName() + "/*\"]\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}";
                minioClient.setBucketPolicy(
                    SetBucketPolicyArgs.builder()
                        .bucket(minioConfig.getBucketName())
                        .config(policy)
                        .build()
                );
                log.info("成功设置存储桶{}的访问策略为公共读取", minioConfig.getBucketName());
            } else {
                // 存储桶已存在，确保其访问策略为公共读取
                String policy = "{\n" +
                        "    \"Version\": \"2012-10-17\",\n" +
                        "    \"Statement\": [\n" +
                        "        {\n" +
                        "            \"Effect\": \"Allow\",\n" +
                        "            \"Principal\": {\"AWS\": [\"*\"]},\n" +
                        "            \"Action\": [\"s3:GetObject\"],\n" +
                        "            \"Resource\": [\"arn:aws:s3:::" + minioConfig.getBucketName() + "/*\"]\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}";
                minioClient.setBucketPolicy(
                    SetBucketPolicyArgs.builder()
                        .bucket(minioConfig.getBucketName())
                        .config(policy)
                        .build()
                );
                log.info("确保存储桶{}的访问策略为公共读取", minioConfig.getBucketName());
            }
        } catch (Exception e) {
            log.error("初始化MinIO存储桶时发生错误", e);
            throw new RuntimeException("初始化MinIO存储桶失败", e);
        }
    }

    @Override
    public String uploadFile(MultipartFile file) {
        return this.uploadFile(minioConfig.getBucketName(), file);
    }

    @Override
    public String uploadFile(String bucketName, MultipartFile file) {
        // 生成文件路径：年/月/日/文件名
        String objectName = generateObjectName(file.getOriginalFilename());
        return this.uploadFile(bucketName, objectName, file);
    }

    @Override
    public String uploadFile(String bucketName, String objectName, MultipartFile file) {
        try {
            // 判断文件是否为空
            if (file.isEmpty()) {
                throw new IllegalArgumentException("上传文件不能为空");
            }

            // 检查存储桶是否存在，不存在则创建
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
                log.info("成功创建存储桶: {}", bucketName);
            }

            // 上传文件
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build()
            );
            log.info("文件上传成功: {}/{}", bucketName, objectName);

            // 返回文件URL
            return getObjectUrl(bucketName, objectName);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    public InputStream getObject(String bucketName, String objectName) {
        try {
            return minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build()
            );
        } catch (Exception e) {
            log.error("获取文件失败", e);
            throw new RuntimeException("获取文件失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean removeObject(String bucketName, String objectName) {
        try {
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build()
            );
            return true;
        } catch (Exception e) {
            log.error("删除文件失败", e);
            return false;
        }
    }

    @Override
    public String getObjectUrl(String bucketName, String objectName) {
        try {
            // 返回不带参数的简洁URL
            return String.format("%s/%s/%s", minioConfig.getEndpoint(), bucketName, objectName);
        } catch (Exception e) {
            log.error("获取文件URL失败", e);
            throw new RuntimeException("获取文件URL失败: " + e.getMessage(), e);
        }
    }

    /**
     * 为存储桶设置公共读取权限
     * @param bucketName 存储桶名称
     */
    private void setBucketPublicReadPolicy(String bucketName) {
        try {
            String policy = "{\n" +
                    "    \"Version\": \"2012-10-17\",\n" +
                    "    \"Statement\": [\n" +
                    "        {\n" +
                    "            \"Effect\": \"Allow\",\n" +
                    "            \"Principal\": {\"AWS\": [\"*\"]},\n" +
                    "            \"Action\": [\"s3:GetObject\"],\n" +
                    "            \"Resource\": [\"arn:aws:s3:::" + bucketName + "/*\"]\n" +
                    "        }\n" +
                    "    ]\n" +
                    "}";
            minioClient.setBucketPolicy(
                SetBucketPolicyArgs.builder()
                    .bucket(bucketName)
                    .config(policy)
                    .build()
            );
            log.info("设置存储桶{}的访问策略为公共读取", bucketName);
        } catch (Exception e) {
            log.error("设置存储桶{}的访问策略失败", bucketName, e);
        }
    }

    @Override
    public void uploadFile(String bucketName, String fileName, InputStream inputStream) throws Exception {
        try {
            // 检查存储桶是否存在，不存在则创建
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!bucketExists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                // 设置新创建的存储桶为公共读取
                setBucketPublicReadPolicy(bucketName);
            }

            // 上传文件
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .stream(inputStream, inputStream.available(), -1)
                    .contentType(getContentType(fileName))
                    .build()
            );
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new Exception("文件上传失败：" + e.getMessage());
        }
    }

    @Override
    public String getFileUrl(String bucketName, String fileName) {
        try {
            // 返回不带参数的简洁URL
            return String.format("%s/%s/%s", minioConfig.getEndpoint(), bucketName, fileName);
        } catch (Exception e) {
            log.error("获取文件URL失败", e);
            return null;
        }
    }

    /**
     * 根据文件名获取内容类型
     * @param fileName 文件名
     * @return 内容类型
     */
    private String getContentType(String fileName) {
        // 文件扩展名
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        switch (extension) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "pdf":
                return "application/pdf";
            case "doc":
                return "application/msword";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls":
                return "application/vnd.ms-excel";
            case "xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "txt":
                return "text/plain";
            default:
                return "application/octet-stream";
        }
    }

    /**
     * 生成MinIO对象名称
     * 格式：年/月/日/随机ID.扩展名
     *
     * @param originalFilename 原始文件名
     * @return MinIO对象名称
     */
    private String generateObjectName(String originalFilename) {
        // 获取当前日期
        LocalDateTime now = LocalDateTime.now();
        String datePath = String.format("%d/%02d/%02d", now.getYear(), now.getMonthValue(), now.getDayOfMonth());
        
        // 生成唯一文件名
        String fileExtension = FileUtil.extName(originalFilename);
        String fileName = IdUtil.fastSimpleUUID();
        
        // 如果有扩展名，添加扩展名
        if (StrUtil.isNotEmpty(fileExtension)) {
            fileName = fileName + "." + fileExtension;
        }
        
        // 返回完整对象路径
        return datePath + "/" + fileName;
    }
} 