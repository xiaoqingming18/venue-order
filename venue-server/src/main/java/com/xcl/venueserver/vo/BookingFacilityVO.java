package com.xcl.venueserver.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 预约设施视图对象
 */
@Data
public class BookingFacilityVO {
    
    /**
     * 预约设施ID
     */
    private Long id;
    
    /**
     * 关联的订单ID
     */
    private Long orderId;
    
    /**
     * 设施ID
     */
    private Long facilityId;
    
    /**
     * 设施类型
     */
    private String facilityType;
    
    /**
     * 预约数量
     */
    private Integer quantity;
    
    /**
     * 单价
     */
    private BigDecimal unitPrice;
    
    /**
     * 金额小计
     */
    private BigDecimal amount;
} 