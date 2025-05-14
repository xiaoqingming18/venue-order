package com.xcl.venueserver.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 预约规则数据传输对象
 */
@Data
public class BookingRuleDTO {
    
    /**
     * 规则类型
     */
    @NotBlank(message = "规则类型不能为空")
    @Size(max = 50, message = "规则类型长度不能超过50个字符")
    private String ruleType;
    
    /**
     * 规则键
     */
    @NotBlank(message = "规则键不能为空")
    @Size(max = 50, message = "规则键长度不能超过50个字符")
    private String ruleKey;
    
    /**
     * 规则值
     */
    @NotBlank(message = "规则值不能为空")
    @Size(max = 200, message = "规则值长度不能超过200个字符")
    private String ruleValue;
    
    /**
     * 规则描述
     */
    @Size(max = 200, message = "规则描述长度不能超过200个字符")
    private String description;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status = 1;
} 