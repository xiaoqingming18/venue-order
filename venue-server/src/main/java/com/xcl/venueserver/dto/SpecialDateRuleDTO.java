package com.xcl.venueserver.dto;

import lombok.Data;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 特殊日期规则数据传输对象
 */
@Data
public class SpecialDateRuleDTO {
    
    /**
     * 特殊日期
     */
    @NotNull(message = "特殊日期不能为空")
    private LocalDate specialDate;
    
    /**
     * 描述
     */
    @Size(max = 100, message = "描述长度不能超过100个字符")
    private String description;
    
    /**
     * 价格系数
     */
    @NotNull(message = "价格系数不能为空")
    @DecimalMin(value = "0.1", message = "价格系数最小为0.1")
    @DecimalMax(value = "10.0", message = "价格系数最大为10")
    private BigDecimal priceRate = BigDecimal.ONE;
    
    /**
     * 状态：0-不可预约，1-可预约
     */
    @Min(value = 0, message = "状态只能为0或1")
    @Max(value = 1, message = "状态只能为0或1")
    private Integer status = 1;
} 