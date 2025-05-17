package com.xcl.venueserver.dto;

import lombok.Data;

/**
 * 推荐反馈DTO类
 */
@Data
public class RecommendationFeedbackDTO {

    /**
     * 推荐记录ID
     */
    private Long recommendationId;

    /**
     * 场馆ID
     */
    private Long venueId;

    /**
     * 反馈类型: 0-喜欢, 1-不感兴趣, 2-不再推荐此类场馆
     */
    private Integer feedbackType;

    /**
     * 反馈原因
     */
    private String reason;
} 