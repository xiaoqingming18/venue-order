package com.xcl.venueserver.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 评价封禁DTO
 */
@Data
public class ReviewBanDTO {

    /**
     * 封禁原因
     */
    @NotBlank(message = "封禁原因不能为空")
    @Size(max = 500, message = "封禁原因不能超过500个字符")
    private String reason;
} 