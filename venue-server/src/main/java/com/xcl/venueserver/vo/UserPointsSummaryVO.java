package com.xcl.venueserver.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 用户积分概览VO
 */
@Data
public class UserPointsSummaryVO {
    
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
     * 即将过期积分
     */
    private Integer pointsExpiringSoon;
    
    /**
     * 会员等级信息
     */
    private MemberLevelInfoVO memberLevel;
    
    /**
     * 用户连续签到天数
     */
    private Integer consecutiveSignDays;
    
    /**
     * 今日是否已签到
     */
    private Boolean signedToday;
    
    /**
     * 会员等级VO
     */
    @Data
    public static class MemberLevelInfoVO {
        
        /**
         * 等级ID
         */
        private Long levelId;
        
        /**
         * 等级名称
         */
        private String levelName;
        
        /**
         * 等级值
         */
        private Integer levelValue;
        
        /**
         * 等级图标
         */
        private String icon;
        
        /**
         * 折扣率
         */
        private BigDecimal discount;
        
        /**
         * 下一等级名称
         */
        private String nextLevelName;
        
        /**
         * 下一等级所需积分
         */
        private Integer nextLevelPoints;
        
        /**
         * 升级还需积分
         */
        private Integer pointsForNextLevel;
    }
} 