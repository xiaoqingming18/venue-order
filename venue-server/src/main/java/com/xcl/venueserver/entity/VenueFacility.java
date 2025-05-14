package com.xcl.venueserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 场馆设施实体类
 */
@Data
@TableName("venue_facility")
public class VenueFacility {
    
    /**
     * 设施ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 场馆ID
     */
    private Long venueId;
    
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