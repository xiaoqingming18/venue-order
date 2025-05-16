package com.xcl.venueserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 会员等级实体类
 */
@Data
@TableName("member_levels")
public class MemberLevel {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 等级名称
     */
    private String levelName;

    /**
     * 等级值
     */
    private Integer levelValue;

    /**
     * 所需积分阈值
     */
    private Integer pointThreshold;

    /**
     * 折扣率
     */
    private BigDecimal discountRate;

    /**
     * 等级描述
     */
    private String description;

    /**
     * 等级图标URL
     */
    private String iconUrl;

    /**
     * 状态：0-禁用 1-启用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
} 