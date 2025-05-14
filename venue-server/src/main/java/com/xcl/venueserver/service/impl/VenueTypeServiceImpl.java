package com.xcl.venueserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xcl.venueserver.dto.VenueTypeDTO;
import com.xcl.venueserver.entity.Venue;
import com.xcl.venueserver.entity.VenueType;
import com.xcl.venueserver.mapper.VenueMapper;
import com.xcl.venueserver.mapper.VenueTypeMapper;
import com.xcl.venueserver.service.VenueTypeService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

/**
 * 场馆类型服务实现类
 */
@Service
public class VenueTypeServiceImpl extends ServiceImpl<VenueTypeMapper, VenueType> implements VenueTypeService {

    @Resource
    private VenueMapper venueMapper;

    @Override
    public IPage<VenueType> pageVenueTypes(Integer page, Integer size, String name) {
        LambdaQueryWrapper<VenueType> queryWrapper = new LambdaQueryWrapper<>();
        
        // 根据名称模糊查询
        if (StringUtils.hasText(name)) {
            queryWrapper.like(VenueType::getName, name);
        }
        
        // 按创建时间降序排序
        queryWrapper.orderByDesc(VenueType::getCreatedAt);
        
        return page(new Page<>(page, size), queryWrapper);
    }

    @Override
    public VenueType getVenueTypeById(Integer id) {
        return getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VenueType saveVenueType(VenueTypeDTO dto) {
        VenueType venueType = new VenueType();
        BeanUtils.copyProperties(dto, venueType);
        
        // 保存场馆类型
        save(venueType);
        
        return venueType;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VenueType updateVenueType(Integer id, VenueTypeDTO dto) {
        // 检查场馆类型是否存在
        VenueType venueType = getById(id);
        if (venueType == null) {
            throw new IllegalArgumentException("场馆类型不存在");
        }
        
        // 复制属性
        BeanUtils.copyProperties(dto, venueType);
        venueType.setId(id); // 确保ID不变
        
        // 更新场馆类型
        updateById(venueType);
        
        return venueType;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteVenueType(Integer id) {
        // 检查该类型是否被场馆引用
        LambdaQueryWrapper<Venue> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Venue::getVenueTypeId, id);
        Long count = venueMapper.selectCount(queryWrapper);
        
        if (count > 0) {
            throw new IllegalStateException("该场馆类型正在被使用，无法删除");
        }
        
        // 删除场馆类型
        return removeById(id);
    }
} 