package com.xcl.venueserver.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 会员等级DTO
 */
@Data
public class MemberLevelDTO {

    /**
     * 等级ID，新增时为null
     */
    private Integer id;

    /**
     * 等级名称
     */
    @NotEmpty(message = "等级名称不能为空")
    private String levelName;

    /**
     * 等级值
     */
    @NotNull(message = "等级值不能为空")
    private Integer levelValue;

    /**
     * 所需积分阈值
     */
    @NotNull(message = "所需积分阈值不能为空")
    private Integer pointThreshold;

    /**
     * 折扣率
     */
    @DecimalMin(value = "0.01", message = "折扣率不能小于0.01")
    @DecimalMax(value = "1.00", message = "折扣率不能大于1.00")
    private BigDecimal discountRate = BigDecimal.ONE;

    /**
     * 等级描述
     */
    private String description;

    /**
     * 等级图标URL
     */
    private String iconUrl;

    /**
     * 状态：0-禁用 1-启用
     */
    private Integer status = 1;
} 