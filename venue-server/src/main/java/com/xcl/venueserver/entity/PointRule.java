package com.xcl.venueserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分规则实体类
 */
@Data
@TableName("point_rules")
public class PointRule {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 规则类型：1-预约 2-评价 3-签到 4-邀请 5-其他
     */
    private Integer ruleType;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 积分值
     */
    private Integer pointValue;

    /**
     * 规则描述
     */
    private String ruleDescription;

    /**
     * 积分有效期(天)
     */
    private Integer validityDays;

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