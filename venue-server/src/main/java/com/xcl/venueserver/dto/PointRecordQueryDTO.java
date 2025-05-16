package com.xcl.venueserver.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分记录查询DTO
 */
@Data
public class PointRecordQueryDTO extends PageDTO {
    
    /**
     * 积分类型：1-获取 2-使用 3-过期 4-冻结 5-解冻
     */
    private Integer pointType;
    
    /**
     * 积分来源类型
     */
    private Integer sourceType;
    
    /**
     * 开始时间
     */
    private LocalDateTime startDate;
    
    /**
     * 结束时间
     */
    private LocalDateTime endDate;
} 