package com.xcl.venueserver.dto;

import lombok.Data;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 预约订单数据传输对象
 */
@Data
public class BookingOrderDTO {
    
    /**
     * 场馆ID
     */
    @NotNull(message = "场馆ID不能为空")
    private Long venueId;
    
    /**
     * 场馆位置ID（已弃用）
     */
    private Long locationId;
    
    /**
     * 预约日期
     */
    @NotNull(message = "预约日期不能为空")
    @Future(message = "预约日期必须是未来日期")
    private LocalDate bookingDate;
    
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
     * 预约类型：1-包场预约，2-设施预约
     */
    @NotNull(message = "预约类型不能为空")
    private Integer bookingType;
    
    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;
    
    /**
     * 备注信息
     */
    @Size(max = 500, message = "备注信息不能超过500个字符")
    private String remarks;
    
    /**
     * 预约设施列表(预约类型为2时使用)
     */
    private List<BookingFacilityDTO> facilities;
} 