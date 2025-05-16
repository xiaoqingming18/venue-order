package com.xcl.venueserver.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分记录VO
 */
@Data
public class PointRecordVO {

    /**
     * 记录ID
     */
    private Long id;

    /**
     * 积分类型：1-获取 2-使用 3-过期 4-冻结 5-解冻
     */
    private Integer pointType;

    /**
     * 积分类型名称
     */
    private String pointTypeName;

    /**
     * 积分数量
     */
    private Integer points;

    /**
     * 积分变动后余额
     */
    private Integer balance;

    /**
     * 来源类型
     */
    private Integer sourceType;

    /**
     * 来源类型名称
     */
    private String sourceTypeName;

    /**
     * 来源ID
     */
    private String sourceId;

    /**
     * 描述
     */
    private String description;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
} 