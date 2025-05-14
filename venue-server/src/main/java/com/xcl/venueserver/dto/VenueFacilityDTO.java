package com.xcl.venueserver.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.DecimalMin;
import java.math.BigDecimal;

/**
 * 场馆设施数据传输对象
 */
@Data
public class VenueFacilityDTO {
    
    /**
     * 设施ID
     */
    private Long id;
    
    /**
     * 场馆ID
     */
    @NotNull(message = "场馆ID不能为空")
    @Positive(message = "场馆ID必须为正数")
    private Long venueId;
    
    /**
     * 设施类型
     */
    @NotBlank(message = "设施类型不能为空")
    private String facilityType;
    
    /**
     * 设施数量
     */
    @NotNull(message = "设施数量不能为空")
    @Positive(message = "设施数量必须大于0")
    private Integer quantity;
    
    /**
     * 位置描述(JSON格式)
     */
    @NotBlank(message = "位置描述不能为空")
    private String positionDesc;
    
    /**
     * 设施单独价格，为空则使用场馆基准价格
     */
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    private BigDecimal price;
} 