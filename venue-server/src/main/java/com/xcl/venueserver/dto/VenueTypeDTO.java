package com.xcl.venueserver.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * 场馆类型数据传输对象
 */
@Data
public class VenueTypeDTO {
    
    /**
     * 类型ID
     */
    private Integer id;
    
    /**
     * 类型名称
     */
    @NotBlank(message = "类型名称不能为空")
    private String name;
    
    /**
     * 类型描述
     */
    private String description;
    
    /**
     * 基础价格
     */
    @NotNull(message = "基础价格不能为空")
    @Positive(message = "基础价格必须大于0")
    private BigDecimal basePrice;
} 