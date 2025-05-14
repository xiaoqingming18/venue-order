package com.xcl.venueserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 预约规则实体类
 */
@Data
@TableName("booking_rule")
public class BookingRule {
    
    /**
     * 规则ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 规则类型
     */
    private String ruleType;
    
    /**
     * 规则键
     */
    private String ruleKey;
    
    /**
     * 规则值
     */
    private String ruleValue;
    
    /**
     * 规则描述
     */
    private String description;
    
    /**
     * 状态：0-禁用，1-启用
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