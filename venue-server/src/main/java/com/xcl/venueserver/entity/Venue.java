package com.xcl.venueserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 场馆实体类
 */
@Data
@TableName("venue")
public class Venue {
    
    /**
     * 场馆ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 场馆名称
     */
    private String name;
    
    /**
     * 场馆类型ID
     */
    private Integer venueTypeId;
    
    /**
     * 基准价格
     */
    private BigDecimal basePrice;
    
    /**
     * 可容纳人数
     */
    private Integer capacity;
    
    /**
     * 场馆地址
     */
    private String address;
    
    /**
     * 场馆描述
     */
    private String description;
    
    /**
     * 营业时间
     */
    private String businessHours;
    
    /**
     * 联系电话
     */
    private String contactPhone;
    
    /**
     * 场馆封面图片URL
     */
    private String coverImage;
    
    /**
     * 状态：0-关闭，1-开放
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
} 