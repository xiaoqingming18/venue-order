package com.xcl.venueserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 场馆评价实体类
 */
@Data
@TableName(value = "venue_review", autoResultMap = true)
public class VenueReview {

    /**
     * 评价ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联订单ID
     */
    private Long orderId;

    /**
     * 评价用户ID
     */
    private Long userId;

    /**
     * 场馆ID
     */
    private Long venueId;

    /**
     * 评价内容
     */
    private String content;

    /**
     * 环境评分(1-5)
     */
    private Integer environmentScore;

    /**
     * 设施评分(1-5)
     */
    private Integer facilityScore;

    /**
     * 服务评分(1-5)
     */
    private Integer serviceScore;

    /**
     * 性价比评分(1-5)
     */
    private Integer costPerformanceScore;

    /**
     * 综合评分(1-5)
     */
    private Integer overallScore;

    /**
     * 评价图片URL列表，JSON格式
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> images;

    /**
     * 状态：0-正常，1-已封禁
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
} 