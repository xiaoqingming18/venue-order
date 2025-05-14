package com.xcl.venueserver.vo;

import lombok.Data;

/**
 * 场馆评价统计视图对象
 */
@Data
public class VenueReviewStatsVO {

    /**
     * 场馆ID
     */
    private Long venueId;

    /**
     * 场馆名称
     */
    private String venueName;

    /**
     * 评价总数
     */
    private Integer totalCount;

    /**
     * 待审核数量
     */
    private Integer pendingCount;

    /**
     * 已通过数量
     */
    private Integer approvedCount;

    /**
     * 已拒绝数量
     */
    private Integer rejectedCount;

    /**
     * 环境平均评分
     */
    private Double environmentAvgScore;

    /**
     * 设施平均评分
     */
    private Double facilityAvgScore;

    /**
     * 服务平均评分
     */
    private Double serviceAvgScore;

    /**
     * 性价比平均评分
     */
    private Double costPerformanceAvgScore;

    /**
     * 综合平均评分
     */
    private Double overallAvgScore;

    /**
     * 好评率（4-5分）百分比
     */
    private Double goodReviewRate;
} 