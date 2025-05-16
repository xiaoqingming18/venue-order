package com.xcl.venueserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分记录实体类
 */
@Data
@TableName("point_records")
public class PointRecord {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 积分类型：1-获取 2-使用 3-过期 4-冻结 5-解冻
     */
    private Integer pointType;

    /**
     * 积分变动数量
     */
    private Integer points;

    /**
     * 变动后积分余额
     */
    private Integer balance;

    /**
     * 来源类型：1-预约 2-评价 3-签到 4-邀请 5-兑换 6-抵扣 7-过期 8-其他
     */
    private Integer sourceType;

    /**
     * 来源ID，关联相应的订单ID等
     */
    private String sourceId;

    /**
     * 描述
     */
    private String description;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
} 