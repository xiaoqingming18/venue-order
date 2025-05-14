package com.xcl.venueserver.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 评价审核数据传输对象
 */
@Data
public class ReviewAuditDTO {

    /**
     * 审核状态：1-通过，2-拒绝
     */
    @NotNull(message = "审核状态不能为空")
    @Min(value = 1, message = "状态值最小为1")
    @Max(value = 2, message = "状态值最大为2")
    private Integer status;

    /**
     * 拒绝原因，当status=2时必填
     */
    @Size(max = 200, message = "拒绝原因不能超过200个字符")
    private String rejectReason;
} 