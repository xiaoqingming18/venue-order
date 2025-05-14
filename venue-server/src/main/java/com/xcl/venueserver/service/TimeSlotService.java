package com.xcl.venueserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xcl.venueserver.dto.TimeSlotDTO;
import com.xcl.venueserver.entity.TimeSlot;

import java.time.LocalTime;
import java.util.List;

/**
 * 时间段服务接口
 */
public interface TimeSlotService extends IService<TimeSlot> {
    
    /**
     * 获取场馆的时间段列表
     * @param venueId 场馆ID
     * @return 时间段列表
     */
    List<TimeSlot> getTimeSlotsByVenueId(Long venueId);
    
    /**
     * 创建时间段
     * @param dto 时间段信息
     * @return 创建的时间段
     */
    TimeSlot createTimeSlot(TimeSlotDTO dto);
    
    /**
     * 更新时间段
     * @param id 时间段ID
     * @param dto 时间段信息
     * @return 更新后的时间段
     */
    TimeSlot updateTimeSlot(Long id, TimeSlotDTO dto);
    
    /**
     * 删除时间段
     * @param id 时间段ID
     * @return 是否成功
     */
    boolean deleteTimeSlot(Long id);
    
    /**
     * 获取指定时间范围的时间段
     * @param venueId 场馆ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 时间段列表
     */
    List<TimeSlot> getTimeSlotsByTimeRange(Long venueId, LocalTime startTime, LocalTime endTime);
    
    /**
     * 获取指定ID的时间段
     * @param id 时间段ID
     * @return 时间段
     */
    TimeSlot getTimeSlotById(Long id);
} 