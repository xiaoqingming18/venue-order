package com.xcl.venueserver.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.xcl.venueserver.entity.UserFeedback;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户反馈VO
 */
@Data
public class UserFeedbackVO {
    
    /**
     * 反馈ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 用户昵称
     */
    private String nickname;
    
    /**
     * 反馈类型：1-问题反馈，2-功能建议，3-投诉，4-其他
     */
    private Integer type;
    
    /**
     * 反馈类型名称
     */
    private String typeName;
    
    /**
     * 反馈标题
     */
    private String title;
    
    /**
     * 反馈内容
     */
    private String content;
    
    /**
     * 联系方式
     */
    private String contact;
    
    /**
     * 反馈图片URL列表
     */
    private List<String> images;
    
    /**
     * 状态：0-待处理，1-处理中，2-已回复，3-已关闭
     */
    private Integer status;
    
    /**
     * 状态名称
     */
    private String statusName;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updatedAt;
    
    /**
     * 回复列表
     */
    private List<FeedbackReplyVO> replies;
    
    /**
     * 获取反馈类型名称
     */
    public static String getTypeName(Integer type) {
        if (type == null) {
            return "";
        }
        switch (type) {
            case 1:
                return "问题反馈";
            case 2:
                return "功能建议";
            case 3:
                return "投诉";
            case 4:
                return "其他";
            default:
                return "未知类型";
        }
    }
    
    /**
     * 获取状态名称
     */
    public static String getStatusName(Integer status) {
        if (status == null) {
            return "";
        }
        switch (status) {
            case 0:
                return "待处理";
            case 1:
                return "处理中";
            case 2:
                return "已回复";
            case 3:
                return "已关闭";
            default:
                return "未知状态";
        }
    }
} 