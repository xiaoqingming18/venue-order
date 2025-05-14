package com.xcl.venueserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 场馆类型实体类
 */
@Data
@TableName("venue_type")
public class VenueType {
    
    /**
     * 类型ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    
    /**
     * 类型名称
     */
    private String name;
    
    /**
     * 类型描述
     */
    private String description;
    
    /**
     * 基础价格
     */
    private BigDecimal basePrice;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
} 