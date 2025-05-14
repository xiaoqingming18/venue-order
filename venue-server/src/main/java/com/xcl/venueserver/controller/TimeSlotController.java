package com.xcl.venueserver.controller;

import com.xcl.venueserver.common.utils.CurrentUser;
import com.xcl.venueserver.common.utils.Result;
import com.xcl.venueserver.dto.TimeSlotDTO;
import com.xcl.venueserver.entity.TimeSlot;
import com.xcl.venueserver.service.TimeSlotService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 时间段控制器
 */
@RestController
@RequestMapping("/api/time-slots")
public class TimeSlotController {
    
    @Resource
    private TimeSlotService timeSlotService;
    
    @Resource
    private CurrentUser currentUser;
    
    /**
     * 获取场馆的时间段列表
     */
    @GetMapping("/venue/{venueId}")
    public Result<List<TimeSlot>> getTimeSlotsByVenueId(@PathVariable Long venueId) {
        List<TimeSlot> timeSlots = timeSlotService.getTimeSlotsByVenueId(venueId);
        return Result.success(timeSlots);
    }
    
    /**
     * 创建时间段
     */
    @PostMapping
    public Result<TimeSlot> createTimeSlot(@Validated @RequestBody TimeSlotDTO dto) {
        // 检查权限（只有管理员可以创建时间段）
        if (!currentUser.isAdmin()) {
            return Result.error("无权操作");
        }
        
        try {
            TimeSlot timeSlot = timeSlotService.createTimeSlot(dto);
            return Result.success(timeSlot);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新时间段
     */
    @PutMapping("/{id}")
    public Result<TimeSlot> updateTimeSlot(
            @PathVariable Long id,
            @Validated @RequestBody TimeSlotDTO dto) {
        
        // 检查权限（只有管理员可以更新时间段）
        if (!currentUser.isAdmin()) {
            return Result.error("无权操作");
        }
        
        try {
            TimeSlot timeSlot = timeSlotService.updateTimeSlot(id, dto);
            return Result.success(timeSlot);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除时间段
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteTimeSlot(@PathVariable Long id) {
        // 检查权限（只有管理员可以删除时间段）
        if (!currentUser.isAdmin()) {
            return Result.error("无权操作");
        }
        
        try {
            boolean success = timeSlotService.deleteTimeSlot(id);
            if (success) {
                return Result.success();
            } else {
                return Result.error("删除时间段失败");
            }
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取时间段详情
     */
    @GetMapping("/{id}")
    public Result<TimeSlot> getTimeSlotById(@PathVariable Long id) {
        TimeSlot timeSlot = timeSlotService.getTimeSlotById(id);
        if (timeSlot == null) {
            return Result.error("时间段不存在");
        }
        return Result.success(timeSlot);
    }
} 