package com.xcl.venueserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xcl.venueserver.dto.VenueDTO;
import com.xcl.venueserver.entity.Venue;
import com.xcl.venueserver.entity.VenueType;
import com.xcl.venueserver.mapper.VenueFacilityMapper;
import com.xcl.venueserver.mapper.VenueLocationMapper;
import com.xcl.venueserver.mapper.VenueMapper;
import com.xcl.venueserver.mapper.VenueTypeMapper;
import com.xcl.venueserver.service.VenueService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 场馆服务实现类
 */
@Service
public class VenueServiceImpl extends ServiceImpl<VenueMapper, Venue> implements VenueService {

    @Resource
    private VenueTypeMapper venueTypeMapper;
    
    @Resource
    private VenueFacilityMapper venueFacilityMapper;
    
    @Resource
    private VenueLocationMapper venueLocationMapper;

    @Override
    public IPage<Venue> pageVenues(Integer page, Integer size, String name, Integer venueTypeId, Integer status) {
        LambdaQueryWrapper<Venue> queryWrapper = new LambdaQueryWrapper<>();
        
        // 根据名称模糊查询
        if (StringUtils.hasText(name)) {
            queryWrapper.like(Venue::getName, name);
        }
        
        // 根据场馆类型ID查询
        if (venueTypeId != null) {
            queryWrapper.eq(Venue::getVenueTypeId, venueTypeId);
        }
        
        // 根据状态查询
        if (status != null) {
            queryWrapper.eq(Venue::getStatus, status);
        }
        
        // 按创建时间降序排序
        queryWrapper.orderByDesc(Venue::getCreatedAt);
        
        return page(new Page<>(page, size), queryWrapper);
    }

    @Override
    public Venue getVenueById(Long id) {
        return getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Venue saveVenue(VenueDTO dto) {
        // 检查场馆类型是否存在
        VenueType venueType = venueTypeMapper.selectById(dto.getVenueTypeId());
        if (venueType == null) {
            throw new IllegalArgumentException("场馆类型不存在");
        }
        
        Venue venue = new Venue();
        BeanUtils.copyProperties(dto, venue);
        
        // 设置创建时间和更新时间
        LocalDateTime now = LocalDateTime.now();
        venue.setCreatedAt(now);
        venue.setUpdatedAt(now);
        
        // 保存场馆
        save(venue);
        
        return venue;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Venue updateVenue(Long id, VenueDTO dto) {
        // 检查场馆是否存在
        Venue venue = getById(id);
        if (venue == null) {
            throw new IllegalArgumentException("场馆不存在");
        }
        
        // 检查场馆类型是否存在
        VenueType venueType = venueTypeMapper.selectById(dto.getVenueTypeId());
        if (venueType == null) {
            throw new IllegalArgumentException("场馆类型不存在");
        }
        
        // 复制属性
        BeanUtils.copyProperties(dto, venue);
        venue.setId(id); // 确保ID不变
        venue.setUpdatedAt(LocalDateTime.now()); // 更新时间
        
        // 更新场馆
        updateById(venue);
        
        return venue;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Venue updateVenueStatus(Long id, Integer status) {
        // 检查场馆是否存在
        Venue venue = getById(id);
        if (venue == null) {
            throw new IllegalArgumentException("场馆不存在");
        }
        
        // 检查状态值是否合法
        if (status != 0 && status != 1) {
            throw new IllegalArgumentException("状态值错误，只能为0或1");
        }
        
        // 更新状态
        venue.setStatus(status);
        venue.setUpdatedAt(LocalDateTime.now());
        updateById(venue);
        
        return venue;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteVenue(Long id) {
        // 检查场馆是否存在
        Venue venue = getById(id);
        if (venue == null) {
            throw new IllegalArgumentException("场馆不存在");
        }
        
        // 删除关联的场馆设施
        venueFacilityMapper.delete(new LambdaQueryWrapper<com.xcl.venueserver.entity.VenueFacility>()
                .eq(com.xcl.venueserver.entity.VenueFacility::getVenueId, id));
        
        // 删除关联的场馆位置
        venueLocationMapper.delete(new LambdaQueryWrapper<com.xcl.venueserver.entity.VenueLocation>()
                .eq(com.xcl.venueserver.entity.VenueLocation::getVenueId, id));
        
        // 删除场馆
        return removeById(id);
    }
} 