package com.xcl.venueserver.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalTime;

/**
 * 时间段数据传输对象
 */
@Data
public class TimeSlotDTO {
    
    /**
     * 场馆ID
     */
    @NotNull(message = "场馆ID不能为空")
    private Long venueId;
    
    /**
     * 开始时间
     */
    @NotNull(message = "开始时间不能为空")
    private LocalTime startTime;
    
    /**
     * 结束时间
     */
    @NotNull(message = "结束时间不能为空")
    private LocalTime endTime;
    
    /**
     * 价格系数，默认为1
     */
    @DecimalMin(value = "0.1", message = "价格系数最小为0.1")
    @DecimalMax(value = "10.0", message = "价格系数最大为10.0")
    private BigDecimal priceRate = BigDecimal.ONE;
    
    /**
     * 状态：0-不可用，1-可用
     */
    private Integer status = 1;
} 