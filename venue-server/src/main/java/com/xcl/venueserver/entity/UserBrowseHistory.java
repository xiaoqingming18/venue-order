package com.xcl.venueserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户浏览历史实体类
 */
@Data
@TableName("user_browse_history")
public class UserBrowseHistory {

    /**
     * 历史记录ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 场馆ID
     */
    private Long venueId;

    /**
     * 浏览时间
     */
    private LocalDateTime browseTime;

    /**
     * 停留时长(秒)
     */
    private Integer stayDuration;

    /**
     * 来源: 0-首页推荐, 1-搜索结果, 2-分类浏览, 3-其他场馆跳转
     */
    private Integer source;

    /**
     * 设备信息
     */
    private String deviceInfo;
} 