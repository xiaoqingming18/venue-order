package com.xcl.venueserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xcl.venueserver.dto.VenueFacilityDTO;
import com.xcl.venueserver.entity.Venue;
import com.xcl.venueserver.entity.VenueFacility;
import com.xcl.venueserver.mapper.VenueFacilityMapper;
import com.xcl.venueserver.mapper.VenueMapper;
import com.xcl.venueserver.service.VenueFacilityService;
import com.xcl.venueserver.vo.VenueFacilityVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 场馆设施服务实现类
 */
@Service
public class VenueFacilityServiceImpl extends ServiceImpl<VenueFacilityMapper, VenueFacility> implements VenueFacilityService {

    @Resource
    private VenueFacilityMapper venueFacilityMapper;

    @Resource
    private VenueMapper venueMapper;

    @Override
    public IPage<VenueFacilityVO> pageFacilities(Integer page, Integer size, Long venueId, String facilityType) {
        Page<VenueFacility> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<VenueFacility> queryWrapper = new LambdaQueryWrapper<>();
        
        if (venueId != null) {
            queryWrapper.eq(VenueFacility::getVenueId, venueId);
        }
        
        if (!StringUtils.isEmpty(facilityType)) {
            queryWrapper.like(VenueFacility::getFacilityType, facilityType);
        }
        
        // 查询并转换结果
        IPage<VenueFacility> resultPage = this.page(pageParam, queryWrapper);
        return resultPage.convert(this::convertToVO);
    }

    @Override
    public List<VenueFacility> getFacilitiesByVenueId(Long venueId) {
        if (venueId == null) {
            return Collections.emptyList();
        }
        
        LambdaQueryWrapper<VenueFacility> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VenueFacility::getVenueId, venueId);
        
        return this.list(queryWrapper);
    }

    @Override
    public VenueFacility getFacilityById(Long id) {
        return this.getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VenueFacility createFacility(VenueFacility facility) {
        // 验证场馆是否存在
        Venue venue = venueMapper.selectById(facility.getVenueId());
        if (venue == null) {
            throw new IllegalArgumentException("场馆不存在");
        }
        
        // 设置创建和更新时间
        LocalDateTime now = LocalDateTime.now();
        facility.setCreatedAt(now);
        facility.setUpdatedAt(now);
        
        // 显式记录price是否为null
        boolean hasPriceField = facility.getPrice() != null;
        
        // 保存设施
        this.save(facility);
        
        // 如果价格为空，但前端有设置price字段，执行一次额外的更新以确保null被保存
        if (!hasPriceField) {
            facility.setPrice(null);
            this.updateById(facility);
        }
        
        return facility;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VenueFacility updateFacility(Long id, VenueFacility facility) {
        // 检查设施是否存在
        VenueFacility existingFacility = this.getById(id);
        if (existingFacility == null) {
            throw new IllegalArgumentException("设施不存在");
        }
        
        // 验证场馆是否存在
        Venue venue = venueMapper.selectById(facility.getVenueId());
        if (venue == null) {
            throw new IllegalArgumentException("场馆不存在");
        }
        
        // 保留原始设施数据的ID
        facility.setId(id);
        facility.setUpdatedAt(LocalDateTime.now());
        
        // 特别处理price字段，如果前端明确设置为null，表示要清除价格
        if (facility.getPrice() == null) {
            // 这里明确设置price为null，让数据库中的值被更新为null
            existingFacility.setPrice(null);
            existingFacility.setVenueId(facility.getVenueId());
            existingFacility.setFacilityType(facility.getFacilityType());
            existingFacility.setQuantity(facility.getQuantity());
            existingFacility.setPositionDesc(facility.getPositionDesc());
            existingFacility.setUpdatedAt(LocalDateTime.now());
            
            this.updateById(existingFacility);
            return existingFacility;
        } else {
            // 如果price不为null，则正常更新
            this.updateById(facility);
            return facility;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteFacility(Long id) {
        // 检查设施是否存在
        VenueFacility facility = this.getById(id);
        if (facility == null) {
            return false;
        }
        
        return this.removeById(id);
    }
    
    @Override
    public List<VenueFacility> getFacilitiesByIds(List<Long> facilityIds) {
        if (facilityIds == null || facilityIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        LambdaQueryWrapper<VenueFacility> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(VenueFacility::getId, facilityIds);
        
        return this.list(queryWrapper);
    }
    
    /**
     * 将实体对象转换为视图对象
     * @param entity 实体对象
     * @return 视图对象
     */
    private VenueFacilityVO convertToVO(VenueFacility entity) {
        if (entity == null) {
            return null;
        }
        
        VenueFacilityVO vo = new VenueFacilityVO();
        BeanUtils.copyProperties(entity, vo);
        
        // 获取场馆信息
        Venue venue = venueMapper.selectById(entity.getVenueId());
        if (venue != null) {
            vo.setVenueName(venue.getName());
        }
        
        return vo;
    }
} 