package com.xcl.venueserver.common.enums;

import lombok.Getter;

/**
 * 积分类型枚举
 */
@Getter
public enum PointTypeEnum {

    INCOME(1, "获取"),
    EXPENSE(2, "使用"),
    EXPIRE(3, "过期"),
    FREEZE(4, "冻结"),
    UNFREEZE(5, "解冻");

    public final Integer code;
    public final String desc;

    PointTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static String getDescByCode(Integer code) {
        for (PointTypeEnum type : values()) {
            if (type.code.equals(code)) {
                return type.desc;
            }
        }
        return "";
    }
} 