package com.xcl.venueserver.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

/**
 * 场馆数据传输对象
 */
@Data
public class VenueDTO {
    
    /**
     * 场馆ID
     */
    private Long id;
    
    /**
     * 场馆名称
     */
    @NotBlank(message = "场馆名称不能为空")
    private String name;
    
    /**
     * 场馆类型ID
     */
    @NotNull(message = "场馆类型不能为空")
    @Positive(message = "场馆类型ID必须为正数")
    private Integer venueTypeId;
    
    /**
     * 基准价格
     */
    @NotNull(message = "基准价格不能为空")
    @Positive(message = "基准价格必须大于0")
    private BigDecimal basePrice;
    
    /**
     * 可容纳人数
     */
    @NotNull(message = "可容纳人数不能为空")
    @Positive(message = "可容纳人数必须大于0")
    private Integer capacity;
    
    /**
     * 场馆地址
     */
    @NotBlank(message = "场馆地址不能为空")
    private String address;
    
    /**
     * 场馆描述
     */
    private String description;
    
    /**
     * 营业时间
     */
    private String businessHours;
    
    /**
     * 联系电话
     */
    private String contactPhone;
    
    /**
     * 场馆封面图片URL
     */
    private String coverImage;
    
    /**
     * 状态：0-关闭，1-开放
     */
    @NotNull(message = "状态不能为空")
    @PositiveOrZero(message = "状态值错误")
    private Integer status;
} 