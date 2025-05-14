package com.xcl.venueserver.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 评价回复数据传输对象
 */
@Data
public class VenueReviewReplyDTO {

    /**
     * 回复内容
     */
    @NotBlank(message = "回复内容不能为空")
    @Size(max = 500, message = "回复内容不能超过500个字符")
    private String content;
} 