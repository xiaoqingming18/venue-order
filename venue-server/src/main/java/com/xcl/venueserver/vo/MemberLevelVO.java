package com.xcl.venueserver.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 会员等级VO
 */
@Data
public class MemberLevelVO {

    /**
     * 等级ID
     */
    private Integer levelId;

    /**
     * 等级名称
     */
    private String levelName;

    /**
     * 等级值
     */
    private Integer levelValue;

    /**
     * 图标URL
     */
    private String icon;

    /**
     * 折扣率
     */
    private BigDecimal discount;

    /**
     * 下一级所需积分
     */
    private Integer nextLevelPoints;

    /**
     * 下一级名称
     */
    private String nextLevelName;
} 