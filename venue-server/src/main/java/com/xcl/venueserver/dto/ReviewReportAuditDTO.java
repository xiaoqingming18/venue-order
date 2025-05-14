package com.xcl.venueserver.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 评价举报审核DTO
 */
@Data
public class ReviewReportAuditDTO {

    /**
     * 审核状态：1-通过，2-拒绝
     */
    @NotNull(message = "审核状态不能为空")
    private Integer status;

    /**
     * 管理员备注
     */
    @Size(max = 500, message = "备注不能超过500个字符")
    private String adminNotes;
    
    /**
     * 是否同时封禁评价（当审核通过时有效）
     */
    private boolean banReview;
    
    /**
     * 封禁原因（当banReview为true时使用）
     */
    @Size(max = 500, message = "封禁原因不能超过500个字符")
    private String banReason;
} 