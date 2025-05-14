package com.xcl.venueserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 特殊日期规则实体类
 */
@Data
@TableName("special_date_rule")
public class SpecialDateRule {
    
    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 特殊日期
     */
    private LocalDate specialDate;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 价格系数
     */
    private BigDecimal priceRate;
    
    /**
     * 状态：0-不可预约，1-可预约
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