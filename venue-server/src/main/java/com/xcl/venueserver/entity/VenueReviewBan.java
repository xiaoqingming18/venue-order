package com.xcl.venueserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评价封禁记录实体类
 */
@Data
@TableName("venue_review_ban")
public class VenueReviewBan {

    /**
     * 封禁ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 评价ID
     */
    private Long reviewId;

    /**
     * 操作管理员ID
     */
    private Long adminId;

    /**
     * 关联举报ID（可为空）
     */
    private Long reportId;

    /**
     * 封禁原因
     */
    private String reason;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
} 