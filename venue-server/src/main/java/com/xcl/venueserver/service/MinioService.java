package com.xcl.venueserver.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * MinIO文件存储服务接口
 */
public interface MinioService {

    /**
     * 上传文件
     *
     * @param file 文件对象
     * @return 文件访问URL
     */
    String uploadFile(MultipartFile file);

    /**
     * 上传文件到指定桶
     *
     * @param bucketName 桶名称
     * @param file       文件对象
     * @return 文件访问URL
     */
    String uploadFile(String bucketName, MultipartFile file);

    /**
     * 上传文件到指定路径
     *
     * @param bucketName 桶名称
     * @param objectName 对象名称/文件路径
     * @param file       文件对象
     * @return 文件访问URL
     */
    String uploadFile(String bucketName, String objectName, MultipartFile file);

    /**
     * 获取文件流
     *
     * @param bucketName 桶名称
     * @param objectName 对象名称/文件路径
     * @return 文件流
     */
    InputStream getObject(String bucketName, String objectName);

    /**
     * 删除文件
     *
     * @param bucketName 桶名称
     * @param objectName 对象名称/文件路径
     * @return 是否删除成功
     */
    boolean removeObject(String bucketName, String objectName);

    /**
     * 获取文件外链
     *
     * @param bucketName 桶名称
     * @param objectName 对象名称/文件路径
     * @return 文件外链
     */
    String getObjectUrl(String bucketName, String objectName);

    /**
     * 上传文件到MinIO
     * @param bucketName 存储桶名称
     * @param fileName 文件名
     * @param inputStream 文件输入流
     * @return 文件URL
     */
    void uploadFile(String bucketName, String fileName, InputStream inputStream) throws Exception;

    /**
     * 获取文件URL
     * @param bucketName 存储桶名称
     * @param fileName 文件名
     * @return 文件URL
     */
    String getFileUrl(String bucketName, String fileName);
} 