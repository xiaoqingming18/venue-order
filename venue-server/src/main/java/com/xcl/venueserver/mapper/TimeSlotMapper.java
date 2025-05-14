package com.xcl.venueserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xcl.venueserver.entity.TimeSlot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalTime;
import java.util.List;

/**
 * 时间段Mapper接口
 */
@Mapper
public interface TimeSlotMapper extends BaseMapper<TimeSlot> {
    
    /**
     * 根据场馆ID获取时间段列表
     * @param venueId 场馆ID
     * @return 时间段列表
     */
    List<TimeSlot> getTimeSlotsByVenueId(@Param("venueId") Long venueId);
    
    /**
     * 查询指定时间范围的时间段
     * @param venueId 场馆ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 时间段列表
     */
    List<TimeSlot> getTimeSlotsByTimeRange(
            @Param("venueId") Long venueId,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );
} 