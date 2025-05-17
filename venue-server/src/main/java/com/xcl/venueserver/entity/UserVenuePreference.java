package com.xcl.venueserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户场馆偏好实体类
 */
@Data
@TableName("user_venue_preference")
public class UserVenuePreference {

    /**
     * 记录ID
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
     * 偏好分数(0-10)
     */
    private BigDecimal preferenceScore;

    /**
     * 浏览次数
     */
    private Integer browseCount;

    /**
     * 预订次数
     */
    private Integer orderCount;

    /**
     * 最后交互时间
     */
    private LocalDateTime lastInteraction;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
} 