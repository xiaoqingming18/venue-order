package com.xcl.venueserver.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 场馆设施视图对象
 */
@Data
public class VenueFacilityVO {
    
    /**
     * 设施ID
     */
    private Long id;
    
    /**
     * 场馆ID
     */
    private Long venueId;
    
    /**
     * 场馆名称
     */
    private String venueName;
    
    /**
     * 设施类型
     */
    private String facilityType;
    
    /**
     * 设施数量
     */
    private Integer quantity;
    
    /**
     * 位置描述(JSON格式)
     */
    private String positionDesc;
    
    /**
     * 设施单独价格，为空则使用场馆基准价格
     */
    private BigDecimal price;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
} 