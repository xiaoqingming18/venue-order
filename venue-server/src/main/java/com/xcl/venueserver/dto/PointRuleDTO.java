package com.xcl.venueserver.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

/**
 * 积分规则DTO
 */
@Data
public class PointRuleDTO {

    /**
     * 规则ID，新增时为null
     */
    private Integer id;

    /**
     * 规则类型
     */
    @NotNull(message = "规则类型不能为空")
    private Integer ruleType;

    /**
     * 规则名称
     */
    @NotEmpty(message = "规则名称不能为空")
    private String ruleName;

    /**
     * 积分值
     */
    @NotNull(message = "积分值不能为空")
    @Positive(message = "积分值必须为正数")
    private Integer pointValue;

    /**
     * 规则描述
     */
    private String ruleDescription;

    /**
     * 积分有效期(天)
     */
    @Positive(message = "积分有效期必须为正数")
    private Integer validityDays = 365;

    /**
     * 状态：0-禁用 1-启用
     */
    private Integer status = 1;
} 