package com.xcl.venueserver.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xcl.venueserver.common.utils.CurrentUser;
import com.xcl.venueserver.common.utils.Result;
import com.xcl.venueserver.dto.BookingOrderDTO;
import com.xcl.venueserver.service.BookingOrderService;
import com.xcl.venueserver.vo.BookingOrderVO;
import com.xcl.venueserver.vo.BookingStatsVO;
import com.xcl.venueserver.vo.VenueAvailabilityVO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 预约订单控制器
 */
@RestController
@RequestMapping("/api/booking-orders")
public class BookingOrderController {
    
    @Resource
    private BookingOrderService bookingOrderService;
    
    @Resource
    private CurrentUser currentUser;
    
    /**
     * 分页查询预约订单
     */
    @GetMapping
    public Result<IPage<BookingOrderVO>> pageBookingOrders(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long venueId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer bookingType,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        
        // 只有管理员可以查询所有人的预约订单
        if (!currentUser.isAdmin()) {
            userId = currentUser.getUserId();
        }
        
        IPage<BookingOrderVO> pageData = bookingOrderService.pageBookingOrders(
                page, size, userId, venueId, status, bookingType, startDate, endDate);
        return Result.success(pageData);
    }
    
    /**
     * 获取当前用户的预约订单列表
     */
    @GetMapping("/my")
    public Result<List<BookingOrderVO>> getMyBookingOrders() {
        Long userId = currentUser.getUserId();
        List<BookingOrderVO> orders = bookingOrderService.getUserBookingOrders(userId);
        return Result.success(orders);
    }
    
    /**
     * 获取预约订单详情
     */
    @GetMapping("/{id}")
    public Result<BookingOrderVO> getBookingOrderDetails(@PathVariable Long id) {
        // 只有管理员或订单所有者可以查看详情
        BookingOrderVO order = bookingOrderService.getBookingOrderDetails(id);
        if (order == null) {
            return Result.error("预约订单不存在");
        }
        
        if (!currentUser.isAdmin() && !order.getUserId().equals(currentUser.getUserId())) {
            return Result.error("无权查看该预约订单");
        }
        
        return Result.success(order);
    }
    
    /**
     * 创建预约订单
     */
    @PostMapping
    public Result<BookingOrderVO> createBookingOrder(@Validated @RequestBody BookingOrderDTO dto) {
        try {
            Long userId = currentUser.getUserId();
            BookingOrderVO order = bookingOrderService.createBookingOrder(dto, userId);
            return Result.success(order);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 取消预约订单
     */
    @PutMapping("/{id}/cancel")
    public Result<Void> cancelBookingOrder(@PathVariable Long id) {
        try {
            Long userId = currentUser.getUserId();
            boolean success = bookingOrderService.cancelBookingOrder(id, userId);
            if (success) {
                return Result.success();
            } else {
                return Result.error("取消预约失败");
            }
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 检查场馆在指定日期和时间段的可用性
     */
    @GetMapping("/availability/venue/{venueId}")
    public Result<VenueAvailabilityVO> checkVenueAvailability(
            @PathVariable Long venueId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam @DateTimeFormat(pattern = "HH:mm:ss") LocalTime startTime,
            @RequestParam @DateTimeFormat(pattern = "HH:mm:ss") LocalTime endTime) {
        
        try {
            VenueAvailabilityVO availability = bookingOrderService.checkVenueAvailability(
                    venueId, date, startTime, endTime);
            return Result.success(availability);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 管理员完成预约订单
     */
    @PutMapping("/{id}/complete")
    public Result<Void> completeBookingOrder(@PathVariable Long id) {
        // 检查是否是管理员
        if (!currentUser.isAdmin()) {
            return Result.error("无权操作");
        }
        
        try {
            boolean success = bookingOrderService.completeBookingOrder(id);
            if (success) {
                return Result.success();
            } else {
                return Result.error("完成预约失败");
            }
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 管理员更新预约订单状态
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        
        // 检查是否是管理员
        if (!currentUser.isAdmin()) {
            return Result.error("无权操作");
        }
        
        try {
            boolean success = bookingOrderService.updateOrderStatus(id, status);
            if (success) {
                return Result.success();
            } else {
                return Result.error("更新状态失败");
            }
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取预约统计数据
     */
    @GetMapping("/stats")
    public Result<BookingStatsVO> getBookingStats() {
        // 检查是否是管理员
        if (!currentUser.isAdmin()) {
            return Result.error("无权操作");
        }
        
        BookingStatsVO stats = bookingOrderService.getBookingStats();
        return Result.success(stats);
    }
} 