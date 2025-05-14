package com.xcl.venueserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评价举报实体类
 */
@Data
@TableName("venue_review_report")
public class VenueReviewReport {

    /**
     * 举报ID
     */
    @TableId(value = "id", type = IdType.AUTO)
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
} 