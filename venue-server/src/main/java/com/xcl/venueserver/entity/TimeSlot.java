package com.xcl.venueserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 时间段实体类
 */
@Data
@TableName("time_slot")
public class TimeSlot {
    
    /**
     * 时间段ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     *.场馆ID
     */
    private Long venueId;
    
    /**
     * 开始时间
     */
    private LocalTime startTime;
    
    /**
     * 结束时间
     */
    private LocalTime endTime;
    
    /**
     * 价格系数，默认为1
     */
    private BigDecimal priceRate;
    
    /**
     * 状态：0-不可用，1-可用
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