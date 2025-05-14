package com.xcl.venueserver.controller;

import com.xcl.venueserver.common.utils.Result;
import com.xcl.venueserver.service.MinioService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 文件上传控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/files")
public class FileController {

    @Resource
    private MinioService minioService;

    /**
     * 上传文件
     */
    @PostMapping("/upload")
    public Result<String> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }

        try {
            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = UUID.randomUUID().toString() + extension;

            // 上传到MinIO
            minioService.uploadFile("venue", fileName, file.getInputStream());

            // 直接返回文件URL，不带参数的简洁URL
            String fileUrl = minioService.getFileUrl("venue", fileName);
            return Result.success(fileUrl);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return Result.error("文件上传失败：" + e.getMessage());
        }
    }

    /**
     * 删除文件
     */
    @DeleteMapping
    public Result<Void> deleteFile(@RequestParam("bucketName") String bucketName,
                               @RequestParam("objectName") String objectName) {
        boolean success = minioService.removeObject(bucketName, objectName);
        if (success) {
            return Result.success();
        } else {
            return Result.error("文件删除失败");
        }
    }
} 