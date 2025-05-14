package com.xcl.venueserver.vo;

import com.xcl.venueserver.entity.VenueReviewReply;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 场馆评价视图对象
 */
@Data
public class VenueReviewVO {

    /**
     * 评价ID
     */
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
     * 用户名称
     */
    private String username;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 场馆ID
     */
    private Long venueId;

    /**
     * 场馆名称
     */
    private String venueName;

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
     * 评价图片URL列表
     */
    private List<String> images;

    /**
     * 状态：0-待审核，1-已通过，2-已拒绝
     */
    private Integer status;

    /**
     * 拒绝原因
     */
    private String rejectReason;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 回复列表
     */
    private List<VenueReviewReply> replies;
} 