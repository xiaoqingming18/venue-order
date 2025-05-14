package com.xcl.venueserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xcl.venueserver.dto.VenueLocationDTO;
import com.xcl.venueserver.entity.Venue;
import com.xcl.venueserver.entity.VenueLocation;
import com.xcl.venueserver.mapper.VenueLocationMapper;
import com.xcl.venueserver.mapper.VenueMapper;
import com.xcl.venueserver.service.VenueLocationService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 场馆位置服务实现类
 */
@Service
public class VenueLocationServiceImpl extends ServiceImpl<VenueLocationMapper, VenueLocation> implements VenueLocationService {

    @Resource
    private VenueMapper venueMapper;

    @Override
    public IPage<VenueLocation> pageLocations(Integer page, Integer size, Long venueId, String type, Integer status) {
        LambdaQueryWrapper<VenueLocation> queryWrapper = new LambdaQueryWrapper<>();
        
        // 根据场馆ID查询
        queryWrapper.eq(VenueLocation::getVenueId, venueId);
        
        // 根据位置类型查询
        if (StringUtils.hasText(type)) {
            queryWrapper.eq(VenueLocation::getType, type);
        }
        
        // 根据状态查询
        if (status != null) {
            queryWrapper.eq(VenueLocation::getStatus, status);
        }
        
        // 按创建时间降序排序
        queryWrapper.orderByDesc(VenueLocation::getCreatedAt);
        
        return page(new Page<>(page, size), queryWrapper);
    }

    @Override
    public List<VenueLocation> getLocationsByVenueId(Long venueId) {
        LambdaQueryWrapper<VenueLocation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VenueLocation::getVenueId, venueId);
        queryWrapper.orderByDesc(VenueLocation::getCreatedAt);
        
        return list(queryWrapper);
    }

    @Override
    public VenueLocation getLocationById(Long id) {
        return getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VenueLocation saveLocation(VenueLocationDTO dto) {
        // 检查场馆是否存在
        Venue venue = venueMapper.selectById(dto.getVenueId());
        if (venue == null) {
            throw new IllegalArgumentException("场馆不存在");
        }
        
        VenueLocation location = new VenueLocation();
        BeanUtils.copyProperties(dto, location);
        
        // 设置创建时间和更新时间
        LocalDateTime now = LocalDateTime.now();
        location.setCreatedAt(now);
        location.setUpdatedAt(now);
        
        // 保存场馆位置
        save(location);
        
        return location;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VenueLocation updateLocation(Long id, VenueLocationDTO dto) {
        // 检查场馆位置是否存在
        VenueLocation location = getById(id);
        if (location == null) {
            throw new IllegalArgumentException("场馆位置不存在");
        }
        
        // 检查场馆是否存在
        Venue venue = venueMapper.selectById(dto.getVenueId());
        if (venue == null) {
            throw new IllegalArgumentException("场馆不存在");
        }
        
        // 复制属性
        BeanUtils.copyProperties(dto, location);
        location.setId(id); // 确保ID不变
        location.setUpdatedAt(LocalDateTime.now()); // 更新时间
        
        // 更新场馆位置
        updateById(location);
        
        return location;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VenueLocation updateLocationStatus(Long id, Integer status) {
        // 检查场馆位置是否存在
        VenueLocation location = getById(id);
        if (location == null) {
            throw new IllegalArgumentException("场馆位置不存在");
        }
        
        // 检查状态值是否合法
        if (status != 0 && status != 1) {
            throw new IllegalArgumentException("状态值错误，只能为0或1");
        }
        
        // 更新状态
        location.setStatus(status);
        location.setUpdatedAt(LocalDateTime.now());
        updateById(location);
        
        return location;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteLocation(Long id) {
        // 检查场馆位置是否存在
        VenueLocation location = getById(id);
        if (location == null) {
            throw new IllegalArgumentException("场馆位置不存在");
        }
        
        // 删除场馆位置
        return removeById(id);
    }
} 