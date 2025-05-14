package com.xcl.venueserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xcl.venueserver.dto.TimeSlotDTO;
import com.xcl.venueserver.entity.TimeSlot;
import com.xcl.venueserver.entity.Venue;
import com.xcl.venueserver.mapper.TimeSlotMapper;
import com.xcl.venueserver.mapper.VenueMapper;
import com.xcl.venueserver.service.TimeSlotService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 时间段服务实现类
 */
@Service
public class TimeSlotServiceImpl extends ServiceImpl<TimeSlotMapper, TimeSlot> implements TimeSlotService {

    @Resource
    private TimeSlotMapper timeSlotMapper;
    
    @Resource
    private VenueMapper venueMapper;

    @Override
    public List<TimeSlot> getTimeSlotsByVenueId(Long venueId) {
        return timeSlotMapper.getTimeSlotsByVenueId(venueId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TimeSlot createTimeSlot(TimeSlotDTO dto) {
        // 1. 检查场馆是否存在
        Venue venue = venueMapper.selectById(dto.getVenueId());
        if (venue == null) {
            throw new IllegalArgumentException("场馆不存在");
        }
        
        // 2. 验证时间段
        if (dto.getStartTime().isAfter(dto.getEndTime())) {
            throw new IllegalArgumentException("开始时间不能晚于结束时间");
        }
        
        // 3. 检查时间段是否重叠
        List<TimeSlot> existingSlots = timeSlotMapper.getTimeSlotsByTimeRange(
                dto.getVenueId(), 
                dto.getStartTime(), 
                dto.getEndTime()
        );
        
        if (!existingSlots.isEmpty()) {
            throw new IllegalArgumentException("该时间段与现有时间段重叠");
        }
        
        // 4. 创建时间段
        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setVenueId(dto.getVenueId());
        timeSlot.setStartTime(dto.getStartTime());
        timeSlot.setEndTime(dto.getEndTime());
        timeSlot.setPriceRate(dto.getPriceRate());
        timeSlot.setStatus(dto.getStatus());
        timeSlot.setCreatedAt(LocalDateTime.now());
        timeSlot.setUpdatedAt(LocalDateTime.now());
        
        // 5. 保存时间段
        this.save(timeSlot);
        
        return timeSlot;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TimeSlot updateTimeSlot(Long id, TimeSlotDTO dto) {
        // 1. 检查时间段是否存在
        TimeSlot timeSlot = this.getById(id);
        if (timeSlot == null) {
            throw new IllegalArgumentException("时间段不存在");
        }
        
        // 2. 验证场馆ID是否匹配
        if (!timeSlot.getVenueId().equals(dto.getVenueId())) {
            throw new IllegalArgumentException("场馆ID不匹配");
        }
        
        // 3. 验证时间段
        if (dto.getStartTime().isAfter(dto.getEndTime())) {
            throw new IllegalArgumentException("开始时间不能晚于结束时间");
        }
        
        // 4. 检查时间段是否重叠(排除自身)
        List<TimeSlot> existingSlots = timeSlotMapper.getTimeSlotsByTimeRange(
                dto.getVenueId(), 
                dto.getStartTime(), 
                dto.getEndTime()
        );
        
        existingSlots.removeIf(slot -> slot.getId().equals(id));
        
        if (!existingSlots.isEmpty()) {
            throw new IllegalArgumentException("该时间段与现有时间段重叠");
        }
        
        // 5. 更新时间段
        timeSlot.setStartTime(dto.getStartTime());
        timeSlot.setEndTime(dto.getEndTime());
        timeSlot.setPriceRate(dto.getPriceRate());
        timeSlot.setStatus(dto.getStatus());
        timeSlot.setUpdatedAt(LocalDateTime.now());
        
        // 6. 保存更新
        this.updateById(timeSlot);
        
        return timeSlot;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTimeSlot(Long id) {
        // 1. 检查时间段是否存在
        TimeSlot timeSlot = this.getById(id);
        if (timeSlot == null) {
            throw new IllegalArgumentException("时间段不存在");
        }
        
        // 2. 删除时间段
        return this.removeById(id);
    }

    @Override
    public List<TimeSlot> getTimeSlotsByTimeRange(Long venueId, LocalTime startTime, LocalTime endTime) {
        return timeSlotMapper.getTimeSlotsByTimeRange(venueId, startTime, endTime);
    }

    @Override
    public TimeSlot getTimeSlotById(Long id) {
        return this.getById(id);
    }
} 