package com.xcl.venueserver.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户消息VO
 */
@Data
public class UserMessageVO {
    
    /**
     * 消息ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 消息类型：1-系统通知，2-订单消息，3-反馈回复，4-其他
     */
    private Integer type;
    
    /**
     * 消息类型名称
     */
    private String typeName;
    
    /**
     * 消息标题
     */
    private String title;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 关联ID，根据消息类型关联到相应记录
     */
    private Long relatedId;
    
    /**
     * 是否已读：0-未读，1-已读
     */
    private Integer isRead;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;
    
    /**
     * 获取消息类型名称
     */
    public static String getTypeName(Integer type) {
        if (type == null) {
            return "";
        }
        switch (type) {
            case 1:
                return "系统通知";
            case 2:
                return "订单消息";
            case 3:
                return "反馈回复";
            case 4:
                return "其他";
            default:
                return "未知类型";
        }
    }
} 