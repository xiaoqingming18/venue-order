package com.xcl.venueserver.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评价举报VO
 */
@Data
public class VenueReviewReportVO {

    /**
     * 举报ID
     */
    private Long id;

    /**
     * 评价ID
     */
    private Long reviewId;

    /**
     * 举报人ID
     */
    private Long reporterId;

    /**
     * 举报人用户名
     */
    private String reporterUsername;

    /**
     * 举报人昵称
     */
    private String reporterNickname;

    /**
     * 举报原因
     */
    private String reason;

    /**
     * 举报详细说明
     */
    private String reasonDetail;

    /**
     * 状态：0-待处理，1-已通过，2-已拒绝
     */
    private Integer status;

    /**
     * 处理管理员ID
     */
    private Long adminId;

    /**
     * 管理员用户名
     */
    private String adminUsername;

    /**
     * 管理员处理备注
     */
    private String adminNotes;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 关联的评价信息
     */
    private VenueReviewVO review;
} 