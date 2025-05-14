package com.xcl.venueserver.vo;

import com.xcl.venueserver.entity.BookingFacility;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 预约订单视图对象
 */
@Data
public class BookingOrderVO {
    
    /**
     * 预约订单ID
     */
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
     * 用户名
     */
    private String username;
    
    /**
     * 场馆ID
     */
    private Long venueId;
    
    /**
     * 场馆名称
     */
    private String venueName;
    
    /**
     * 场馆位置ID（已弃用）
     */
    private Long locationId;
    
    /**
     * 场馆位置名称（已弃用）
     */
    private String locationName;
    
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
     * 预约类型名称
     */
    private String bookingTypeName;
    
    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;
    
    /**
     * 订单状态：0-已取消，1-待支付，2-已预约，3-已完成
     */
    private Integer status;
    
    /**
     * 订单状态名称
     */
    private String statusName;
    
    /**
     * 备注信息
     */
    private String remarks;
    
    /**
     * A预约设施详情
     */
    private List<BookingFacilityVO> facilities;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
} 