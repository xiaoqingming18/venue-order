package com.xcl.venueserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户积分账户实体类
 */
@Data
@TableName("user_points")
public class UserPoints {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 总积分
     */
    private Integer totalPoints;

    /**
     * 可用积分
     */
    private Integer availablePoints;

    /**
     * 冻结积分
     */
    private Integer frozenPoints;

    /**
     * 已过期积分
     */
    private Integer expiredPoints;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
} 