package com.xcl.venueserver.common.enums;

import lombok.Getter;

/**
 * 积分来源类型枚举
 */
@Getter
public enum PointSourceTypeEnum {
    
    /**
     * 签到
     */
    SIGN_IN(1, "签到"),
    
    /**
     * 预约
     */
    BOOKING(2, "预约"),
    
    /**
     * 评价
     */
    REVIEW(3, "评价"),
    
    /**
     * 消费
     */
    CONSUME(4, "消费"),
    
    /**
     * 积分抵扣
     */
    DISCOUNT(5, "积分抵扣"),
    
    /**
     * 过期
     */
    EXPIRED(6, "过期"),
    
    /**
     * 管理员调整
     */
    ADMIN(7, "管理员调整"),
    
    /**
     * 活动奖励
     */
    ACTIVITY(8, "活动奖励");
    
    public final Integer code;
    public final String desc;
    
    PointSourceTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    /**
     * 根据code获取枚举
     */
    public static PointSourceTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        
        for (PointSourceTypeEnum type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        
        return null;
    }
} 