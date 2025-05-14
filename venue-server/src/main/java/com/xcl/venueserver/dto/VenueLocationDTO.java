package com.xcl.venueserver.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.DecimalMin;
import java.math.BigDecimal;

/**
 * 场馆位置数据传输对象
 */
@Data
public class VenueLocationDTO {
    
    /**
     * 位置ID
     */
    private Long id;
    
    /**
     * 所属场馆ID
     */
    @NotNull(message = "场馆ID不能为空")
    @Positive(message = "场馆ID必须为正数")
    private Long venueId;
    
    /**
     * 位置名称
     */
    @NotBlank(message = "位置名称不能为空")
    private String name;
    
    /**
     * 位置类型
     */
    @NotBlank(message = "位置类型不能为空")
    private String type;
    
    /**
     * 状态：0-不可用，1-可用
     */
    @NotNull(message = "状态不能为空")
    @PositiveOrZero(message = "状态值错误")
    private Integer status;
    
    /**
     * 位置单独价格，为空则使用场馆基准价格
     */
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    private BigDecimal price;
    
    /**
     * 位置描述
     */
    private String description;
} 