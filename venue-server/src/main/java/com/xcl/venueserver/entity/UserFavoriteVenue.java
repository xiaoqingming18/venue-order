package com.xcl.venueserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户收藏场馆实体类
 */
@Data
@TableName("user_favorite_venue")
public class UserFavoriteVenue implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 收藏ID
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
     * 收藏备注
     */
    private String notes;

    /**
     * 收藏时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
} 