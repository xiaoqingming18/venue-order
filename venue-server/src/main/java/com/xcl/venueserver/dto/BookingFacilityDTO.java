package com.xcl.venueserver.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 预约设施数据传输对象
 */
@Data
public class BookingFacilityDTO {
    
    /**
     * 设施ID
     */
    @NotNull(message = "设施ID不能为空")
    private Long facilityId;
    
    /**
     * 预约数量
     */
    @NotNull(message = "预约数量不能为空")
    @Min(value = 1, message = "预约数量必须大于0")
    private Integer quantity;
    
    /**
     * 单价
     */
    private BigDecimal unitPrice;
} 