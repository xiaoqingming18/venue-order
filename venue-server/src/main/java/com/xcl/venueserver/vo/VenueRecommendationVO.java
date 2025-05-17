package com.xcl.venueserver.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 场馆推荐VO类
 */
@Data
public class VenueRecommendationVO {

    /**
     * 推荐ID
     */
    private Long id;

    /**
     * 场馆ID
     */
    private Long venueId;

    /**
     * 场馆名称
     */
    private String venueName;

    /**
     * 场馆类型名称
     */
    private String venueTypeName;

    /**
     * 场馆封面图
     */
    private String coverImage;

    /**
     * 场馆地址
     */
    private String address;

    /**
     * 场馆基准价格
     */
    private BigDecimal basePrice;

    /**
     * 场馆描述
     */
    private String description;

    /**
     * 推荐分数(0-10)
     */
    private BigDecimal recommendationScore;

    /**
     * 推荐类型: 0-基于用户行为, 1-基于相似用户, 2-热门推荐, 3-新场馆推荐
     */
    private Integer recommendationType;

    /**
     * 推荐类型名称
     */
    private String recommendationTypeName;

    /**
     * 推荐原因
     */
    private String recommendationReason;
} 