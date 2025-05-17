package com.xcl.venueserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户偏好实体类
 */
@Data
@TableName("user_preference")
public class UserPreference {

    /**
     * 偏好ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 标签ID
     */
    private Long tagId;

    /**
     * 偏好分数(0-10)
     */
    private BigDecimal preferenceScore;

    /**
     * 来源: 0-系统计算, 1-用户设置, 2-推荐反馈
     */
    private Integer source;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
} 