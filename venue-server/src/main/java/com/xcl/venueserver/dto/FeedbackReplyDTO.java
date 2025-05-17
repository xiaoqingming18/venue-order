package com.xcl.venueserver.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 反馈回复DTO
 */
@Data
public class FeedbackReplyDTO {
    
    /**
     * 反馈ID
     */
    @NotNull(message = "反馈ID不能为空")
    private Long feedbackId;
    
    /**
     * 回复内容
     */
    @NotBlank(message = "回复内容不能为空")
    @Size(max = 1000, message = "回复内容长度不能超过1000个字符")
    private String content;
} 