package com.xcl.venueserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 场馆位置实体类
 */
@Data
@TableName("venue_location")
public class VenueLocation {
    
    /**
     * 位置ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 所属场馆ID
     */
    private Long venueId;
    
    /**
     * 位置名称
     */
    private String name;
    
    /**
     * 位置类型
     */
    private String type;
    
    /**
     * 状态：0-不可用，1-可用
     */
    private Integer status;
    
    /**
     * 位置单独价格，为空则使用场馆基准价格
     */
    private BigDecimal price;
    
    /**
     * 位置描述
     */
    private String description;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
} 