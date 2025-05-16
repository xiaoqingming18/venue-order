package com.xcl.venueserver.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户收藏场馆视图对象
 */
@Data
public class UserFavoriteVenueVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 收藏ID
     */
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
     * 场馆名称
     */
    private String venueName;

    /**
     * 场馆封面图片
     */
    private String coverImage;

    /**
     * 场馆地址
     */
    private String address;

    /**
     * 场馆类型名称
     */
    private String venueTypeName;

    /**
     * 基准价格
     */
    private String basePrice;

    /**
     * 收藏备注
     */
    private String notes;

    /**
     * 收藏时间
     */
    private LocalDateTime createdAt;
} 