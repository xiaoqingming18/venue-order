package com.xcl.venueserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 反馈回复实体类
 */
@Data
@TableName("feedback_reply")
public class FeedbackReply {

    /**
     * 回复ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 反馈ID
     */
    private Long feedbackId;

    /**
     * 管理员ID
     */
    private Long adminId;
    
    /**
     * 管理员名称
     */
    private String adminName;

    /**
     * 回复内容
     */
    private String content;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
} 