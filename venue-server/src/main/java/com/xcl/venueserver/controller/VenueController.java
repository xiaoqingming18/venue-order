package com.xcl.venueserver.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xcl.venueserver.common.utils.Result;
import com.xcl.venueserver.dto.VenueDTO;
import com.xcl.venueserver.entity.Venue;
import com.xcl.venueserver.service.BookingOrderService;
import com.xcl.venueserver.service.MinioService;
import com.xcl.venueserver.service.VenueService;
import com.xcl.venueserver.vo.VenueAvailabilityVO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 场馆控制器
 */
@RestController
@RequestMapping("/api/venues")
@Validated
public class VenueController {
    
    @Resource
    private VenueService venueService;
    
    @Resource
    private MinioService minioService;
    
    @Resource
    private BookingOrderService bookingOrderService;
    
    /**
     * 分页查询场馆
     */
    @GetMapping
    public Result<IPage<Venue>> pageVenues(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer venueTypeId,
            @RequestParam(required = false) Integer status) {
        
        IPage<Venue> pageData = venueService.pageVenues(page, size, name, venueTypeId, status);
        return Result.success(pageData);
    }
    
    /**
     * 搜索场馆
     * 根据关键词搜索场馆名称、地址和描述字段
     * 该接口不需要登录认证
     */
    @GetMapping("/search")
    public Result<IPage<Venue>> searchVenues(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer venueTypeId,
            @RequestParam(required = false) Integer status) {
        
        IPage<Venue> pageData = venueService.searchVenues(page, size, keyword, venueTypeId, status);
        return Result.success(pageData);
    }
    
    /**
     * 根据ID获取场馆
     */
    @GetMapping("/{id}")
    public Result<Venue> getVenueById(@PathVariable Long id) {
        Venue venue = venueService.getVenueById(id);
        if (venue == null) {
            return Result.error("场馆不存在");
        }
        return Result.success(venue);
    }
    
    /**
     * 创建场馆
     */
    @PostMapping
    public Result<Venue> createVenue(@Validated @RequestBody VenueDTO dto) {
        try {
            Venue venue = venueService.saveVenue(dto);
            return Result.success(venue);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新场馆
     */
    @PutMapping("/{id}")
    public Result<Venue> updateVenue(
            @PathVariable Long id,
            @Validated @RequestBody VenueDTO dto) {
        
        try {
            Venue venue = venueService.updateVenue(id, dto);
            return Result.success(venue);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新场馆状态
     */
    @PutMapping("/{id}/status")
    public Result<Venue> updateVenueStatus(
            @PathVariable Long id,
            @RequestParam @Min(0) @Max(1) Integer status) {
        
        try {
            Venue venue = venueService.updateVenueStatus(id, status);
            return Result.success(venue);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除场馆
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteVenue(@PathVariable Long id) {
        try {
            boolean success = venueService.deleteVenue(id);
            if (success) {
                return Result.success();
            } else {
                return Result.error("删除场馆失败");
            }
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 上传场馆封面图片
     */
    @PostMapping("/{id}/cover")
    public Result<Map<String, String>> uploadCoverImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        try {
            // 仅当ID不为0时才进行场馆存在性检查
            if (id > 0) {
                // 检查场馆是否存在
                Venue venue = venueService.getVenueById(id);
                if (venue == null) {
                    return Result.error("场馆不存在");
                }
                
                // 上传图片到MinIO，使用简洁URL
                String fileUrl = minioService.uploadFile(file);
                
                // 更新场馆封面
                venue.setCoverImage(fileUrl);
                venueService.updateById(venue);
                
                // 构建返回结果
                Map<String, String> data = new HashMap<>(2);
                data.put("fileName", file.getOriginalFilename());
                data.put("fileUrl", fileUrl);
                
                return Result.success(data);
            } else {
                // ID=0表示新建场馆模式，仅上传文件并返回URL，不关联场馆
                String fileUrl = minioService.uploadFile(file);
                
                // 构建返回结果
                Map<String, String> data = new HashMap<>(2);
                data.put("fileName", file.getOriginalFilename());
                data.put("fileUrl", fileUrl);
                
                return Result.success(data);
            }
        } catch (Exception e) {
            return Result.error("上传封面失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取场馆可预约时间 
     * 该接口放在无需授权的URL下，不需要token验证
     */
    @GetMapping("/{id}/availability")
    public Result<VenueAvailabilityVO> getVenueAvailability(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        
        try {
            // 检查场馆是否存在
            Venue venue = venueService.getVenueById(id);
            if (venue == null) {
                return Result.error("场馆不存在");
            }
            
            // 设置默认时间范围（全天）
            LocalTime startTime = LocalTime.of(8, 0, 0);
            LocalTime endTime = LocalTime.of(22, 0, 0);
            
            VenueAvailabilityVO availability = bookingOrderService.checkVenueAvailability(
                    id, date, startTime, endTime);
            return Result.success(availability);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }
} 