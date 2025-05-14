package com.xcl.venueserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 预约订单实体类
 */
@Data
@TableName("booking_order")
public class BookingOrder {
    
    /**
     * 预约订单ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 订单编号
     */
    private String orderNo;
    
    /**
     * 预约用户ID
     */
    private Long userId;
    
    /**
     * 场馆ID
     */
    private Long venueId;
    
    /**
     * 场馆位置ID（已弃用）
     */
    private Long locationId;
    
    /**
     * 预约日期
     */
    private LocalDate bookingDate;
    
    /**
     * 开始时间
     */
    private LocalTime startTime;
    
    /**
     * 结束时间
     */
    private LocalTime endTime;
    
    /**
     * 预约类型：1-包场预约，2-设施预约
     */
    private Integer bookingType;
    
    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;
    
    /**
     * 订单状态：0-已取消，1-待支付，2-已预约，3-已完成
     */
    private Integer status;
    
    /**
     * 备注信息
     */
    private String remarks;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
} 