package com.xcl.venueserver.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 场馆评价数据传输对象
 */
@Data
public class VenueReviewDTO {

    /**
     * 订单ID
     */
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    /**
     * 评价内容
     */
    @Size(max = 1000, message = "评价内容不能超过1000个字符")
    private String content;

    /**
     * 环境评分(1-5)
     */
    @NotNull(message = "环境评分不能为空")
    @Min(value = 1, message = "评分最小为1")
    @Max(value = 5, message = "评分最大为5")
    private Integer environmentScore;

    /**
     * 设施评分(1-5)
     */
    @NotNull(message = "设施评分不能为空")
    @Min(value = 1, message = "评分最小为1")
    @Max(value = 5, message = "评分最大为5")
    private Integer facilityScore;

    /**
     * 服务评分(1-5)
     */
    @NotNull(message = "服务评分不能为空")
    @Min(value = 1, message = "评分最小为1")
    @Max(value = 5, message = "评分最大为5")
    private Integer serviceScore;

    /**
     * 性价比评分(1-5)
     */
    @NotNull(message = "性价比评分不能为空")
    @Min(value = 1, message = "评分最小为1")
    @Max(value = 5, message = "评分最大为5")
    private Integer costPerformanceScore;

    /**
     * 综合评分(1-5)
     */
    @NotNull(message = "综合评分不能为空")
    @Min(value = 1, message = "评分最小为1")
    @Max(value = 5, message = "评分最大为5")
    private Integer overallScore;

    /**
     * 评价图片URL列表
     */
    private List<String> images;
} 