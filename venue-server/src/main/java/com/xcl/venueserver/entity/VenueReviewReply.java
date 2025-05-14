package com.xcl.venueserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评价回复实体类
 */
@Data
@TableName("venue_review_reply")
public class VenueReviewReply {

    /**
     * 回复ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 评价ID
     */
    private Long reviewId;

    /**
     * 回复用户ID
     */
    private Long userId;

    /**
     * 回复内容
     */
    private String content;

    /**
     * 是否管理员回复：0-否，1-是
     */
    private Integer isAdmin;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
} 