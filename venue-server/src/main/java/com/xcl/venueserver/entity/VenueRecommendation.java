package com.xcl.venueserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 场馆推荐实体类
 */
@Data
@TableName("venue_recommendation")
public class VenueRecommendation {

    /**
     * 推荐ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 推荐场馆ID
     */
    private Long venueId;

    /**
     * 推荐类型: 0-基于用户行为, 1-基于相似用户, 2-热门推荐, 3-新场馆推荐
     */
    private Integer recommendationType;

    /**
     * 推荐分数(0-10)
     */
    private BigDecimal recommendationScore;

    /**
     * 是否已展示: 0-未展示, 1-已展示
     */
    private Integer isShown;

    /**
     * 是否已点击: 0-未点击, 1-已点击
     */
    private Integer isClicked;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
} 