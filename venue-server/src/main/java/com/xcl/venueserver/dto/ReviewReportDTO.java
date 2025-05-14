package com.xcl.venueserver.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 评价举报DTO
 */
@Data
public class ReviewReportDTO {

    /**
     * 举报原因
     */
    @NotBlank(message = "举报原因不能为空")
    @Size(max = 100, message = "举报原因不能超过100个字符")
    private String reason;

    /**
     * 举报详细说明
     */
    @Size(max = 500, message = "详细说明不能超过500个字符")
    private String reasonDetail;
} 