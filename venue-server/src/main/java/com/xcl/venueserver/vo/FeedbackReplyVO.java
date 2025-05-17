package com.xcl.venueserver.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 反馈回复VO
 */
@Data
public class FeedbackReplyVO {
    
    /**
     * 回复ID
     */
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
     * 管理员用户名
     */
    private String adminUsername;
    
    /**
     * 管理员昵称
     */
    private String adminNickname;
    
    /**
     * 回复内容
     */
    private String content;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;
} 