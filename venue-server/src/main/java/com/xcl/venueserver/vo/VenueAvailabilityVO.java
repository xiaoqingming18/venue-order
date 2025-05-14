package com.xcl.venueserver.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 场馆可用性视图对象
 */
@Data
public class VenueAvailabilityVO {
    
    /**
     * 场馆ID
     */
    private Long venueId;
    
    /**
     * 场馆名称
     */
    private String venueName;
    
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
     * 基准价格
     */
    private BigDecimal basePrice;
    
    /**
     * 当前时段价格
     */
    private BigDecimal currentPrice;
    
    /**
     * 是否可整体预约
     */
    private Boolean availableForAll;
    
    /**
     * 设施可用性列表
     */
    private List<FacilityAvailabilityVO> facilities;
} 