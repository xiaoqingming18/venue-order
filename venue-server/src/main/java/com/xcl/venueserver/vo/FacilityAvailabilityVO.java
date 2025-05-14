package com.xcl.venueserver.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 设施可用性视图对象
 */
@Data
public class FacilityAvailabilityVO {
    
    /**
     * 设施ID
     */
    private Long facilityId;
    
    /**
     * 设施类型
     */
    private String facilityType;
    
    /**
     * 总数量
     */
    private Integer totalQuantity;
    
    /**
     * 可用数量
     */
    private Integer availableQuantity;
    
    /**
     * 日期
     */
    private LocalDate date;
    
    /**
     * 开始时间
     */
    private LocalTime startTime;
    
    /**
     * 结束时间
     */
    private LocalTime endTime;
    
    /**
     * 是否可用
     */
    private Boolean available;
} 