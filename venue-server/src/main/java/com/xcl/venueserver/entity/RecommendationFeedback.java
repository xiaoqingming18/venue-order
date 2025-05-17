package com.xcl.venueserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 推荐反馈实体类
 */
@Data
@TableName("recommendation_feedback")
public class RecommendationFeedback {

    /**
     * 反馈ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 场馆ID
     */
    private Long venueId;

    /**
     * 推荐记录ID
     */
    private Long recommendationId;

    /**
     * 反馈类型: 0-喜欢, 1-不感兴趣, 2-不再推荐此类场馆
     */
    private Integer feedbackType;

    /**
     * 反馈原因
     */
    private String reason;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
} 