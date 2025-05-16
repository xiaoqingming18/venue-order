package com.xcl.venueserver.common.enums;

import lombok.Getter;

/**
 * 订单状态枚举
 */
@Getter
public enum OrderStatus {
    
    /**
     * 待支付
     */
    PENDING_PAYMENT(1, "待支付"),
    
    /**
     * 已支付
     */
    PAID(2, "已支付"),
    
    /**
     * 已取消
     */
    CANCELLED(3, "已取消"),
    
    /**
     * 已完成
     */
    COMPLETED(4, "已完成"),
    
    /**
     * 已退款
     */
    REFUNDED(5, "已退款");
    
    public final Integer code;
    public final String desc;
    
    OrderStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    /**
     * 根据code获取枚举
     */
    public static OrderStatus getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        
        for (OrderStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        
        return null;
    }
} 