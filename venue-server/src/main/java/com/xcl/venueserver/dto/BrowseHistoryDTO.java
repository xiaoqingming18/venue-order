package com.xcl.venueserver.dto;

import lombok.Data;

/**
 * 浏览历史DTO类
 */
@Data
public class BrowseHistoryDTO {

    /**
     * 用户ID (可选, 如果前端已登录可以不传)
     */
    private Long userId;

    /**
     * 场馆ID
     */
    private Long venueId;

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