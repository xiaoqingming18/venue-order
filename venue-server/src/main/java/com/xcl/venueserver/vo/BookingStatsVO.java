package com.xcl.venueserver.vo;

import lombok.Data;

/**
 * 预约统计数据VO
 */
@Data
public class BookingStatsVO {
    
    /**
     * 总预约数
     */
    private Integer totalCount;
    
    /**
     * 今日预约数
     */
    private Integer todayCount;
    
    /**
     * 待支付预约数
     */
    private Integer pendingCount;
    
    /**
     * 已支付预约数
     */
    private Integer paidCount;
    
    /**
     * 已取消预约数
     */
    private Integer cancelledCount;
    
    /**
     * 已完成预约数
     */
    private Integer completedCount;
    
    /**
     * 已退款预约数
     */
    private Integer refundedCount;
    
    /**
     * 总金额
     */
    private Double totalAmount;
    
    /**
     * 今日金额
     */
    private Double todayAmount;
} 