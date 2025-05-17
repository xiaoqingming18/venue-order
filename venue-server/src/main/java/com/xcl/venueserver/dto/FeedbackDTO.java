package com.xcl.venueserver.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 用户反馈DTO
 */
@Data
public class FeedbackDTO {
    
    /**
     * 反馈类型：1-问题反馈，2-功能建议，3-投诉，4-其他
     */
    @NotNull(message = "反馈类型不能为空")
    private Integer type;
    
    /**
     * 反馈标题
     */
    @NotBlank(message = "反馈标题不能为空")
    @Size(max = 100, message = "标题长度不能超过100个字符")
    private String title;
    
    /**
     * 反馈内容
     */
    @NotBlank(message = "反馈内容不能为空")
    @Size(max = 1000, message = "内容长度不能超过1000个字符")
    private String content;
    
    /**
     * 联系方式
     */
    @Size(max = 50, message = "联系方式长度不能超过50个字符")
    private String contact;
    
    /**
     * 反馈图片URL列表
     */
    private List<String> images;
} 