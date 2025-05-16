package com.xcl.venueserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xcl.venueserver.entity.UserFavoriteVenue;
import com.xcl.venueserver.entity.Venue;
import com.xcl.venueserver.entity.VenueType;
import com.xcl.venueserver.mapper.UserFavoriteVenueMapper;
import com.xcl.venueserver.mapper.VenueMapper;
import com.xcl.venueserver.mapper.VenueTypeMapper;
import com.xcl.venueserver.service.UserFavoriteVenueService;
import com.xcl.venueserver.vo.UserFavoriteVenueVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户收藏场馆服务实现类
 */
@Service
public class UserFavoriteVenueServiceImpl extends ServiceImpl<UserFavoriteVenueMapper, UserFavoriteVenue> implements UserFavoriteVenueService {

    private final UserFavoriteVenueMapper userFavoriteVenueMapper;
    private final VenueMapper venueMapper;
    private final VenueTypeMapper venueTypeMapper;

    public UserFavoriteVenueServiceImpl(UserFavoriteVenueMapper userFavoriteVenueMapper, 
                                        VenueMapper venueMapper, 
                                        VenueTypeMapper venueTypeMapper) {
        this.userFavoriteVenueMapper = userFavoriteVenueMapper;
        this.venueMapper = venueMapper;
        this.venueTypeMapper = venueTypeMapper;
    }

    @Override
    public UserFavoriteVenue addFavorite(Long userId, Long venueId, String notes) {
        // 先检查是否已经收藏过
        if (isFavorite(userId, venueId)) {
            throw new RuntimeException("该场馆已收藏，请勿重复收藏");
        }

        // 检查场馆是否存在
        Venue venue = venueMapper.selectById(venueId);
        if (venue == null) {
            throw new RuntimeException("场馆不存在");
        }

        // 创建收藏记录
        UserFavoriteVenue favorite = new UserFavoriteVenue();
        favorite.setUserId(userId);
        favorite.setVenueId(venueId);
        favorite.setNotes(notes);
        favorite.setCreatedAt(LocalDateTime.now());
        favorite.setUpdatedAt(LocalDateTime.now());

        userFavoriteVenueMapper.insert(favorite);

        return favorite;
    }

    @Override
    public boolean removeFavorite(Long userId, Long venueId) {
        LambdaQueryWrapper<UserFavoriteVenue> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFavoriteVenue::getUserId, userId)
                .eq(UserFavoriteVenue::getVenueId, venueId);

        return userFavoriteVenueMapper.delete(wrapper) > 0;
    }

    @Override
    public boolean removeFavoriteById(Long userId, Long favoriteId) {
        LambdaQueryWrapper<UserFavoriteVenue> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFavoriteVenue::getUserId, userId)
                .eq(UserFavoriteVenue::getId, favoriteId);

        return userFavoriteVenueMapper.delete(wrapper) > 0;
    }

    @Override
    public boolean updateFavoriteNotes(Long userId, Long favoriteId, String notes) {
        UserFavoriteVenue favorite = userFavoriteVenueMapper.selectById(favoriteId);
        
        // 验证记录是否存在且属于当前用户
        if (favorite == null || !favorite.getUserId().equals(userId)) {
            return false;
        }

        favorite.setNotes(notes);
        favorite.setUpdatedAt(LocalDateTime.now());

        return userFavoriteVenueMapper.updateById(favorite) > 0;
    }

    @Override
    public boolean isFavorite(Long userId, Long venueId) {
        LambdaQueryWrapper<UserFavoriteVenue> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFavoriteVenue::getUserId, userId)
                .eq(UserFavoriteVenue::getVenueId, venueId);

        return userFavoriteVenueMapper.selectCount(wrapper) > 0;
    }

    @Override
    public List<UserFavoriteVenueVO> getUserFavorites(Long userId) {
        // 获取用户收藏的场馆列表
        LambdaQueryWrapper<UserFavoriteVenue> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFavoriteVenue::getUserId, userId)
                .orderByDesc(UserFavoriteVenue::getCreatedAt);

        List<UserFavoriteVenue> favorites = userFavoriteVenueMapper.selectList(wrapper);

        // 收集所有场馆ID
        List<Long> venueIds = favorites.stream()
                .map(UserFavoriteVenue::getVenueId)
                .collect(Collectors.toList());

        if (venueIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 批量查询场馆信息
        LambdaQueryWrapper<Venue> venueWrapper = new LambdaQueryWrapper<>();
        venueWrapper.in(Venue::getId, venueIds);
        List<Venue> venues = venueMapper.selectList(venueWrapper);
        Map<Long, Venue> venueMap = venues.stream()
                .collect(Collectors.toMap(Venue::getId, venue -> venue));

        // 收集所有场馆类型ID
        List<Integer> typeIds = venues.stream()
                .map(Venue::getVenueTypeId)
                .collect(Collectors.toList());

        if (typeIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 批量查询场馆类型信息
        LambdaQueryWrapper<VenueType> typeWrapper = new LambdaQueryWrapper<>();
        typeWrapper.in(VenueType::getId, typeIds);
        List<VenueType> types = venueTypeMapper.selectList(typeWrapper);
        Map<Integer, VenueType> typeMap = types.stream()
                .collect(Collectors.toMap(VenueType::getId, type -> type));

        // 构建VO列表
        return favorites.stream().map(favorite -> {
            UserFavoriteVenueVO vo = new UserFavoriteVenueVO();
            BeanUtils.copyProperties(favorite, vo);
            
            Venue venue = venueMap.get(favorite.getVenueId());
            if (venue != null) {
                vo.setVenueName(venue.getName());
                vo.setCoverImage(venue.getCoverImage());
                vo.setAddress(venue.getAddress());
                vo.setBasePrice(venue.getBasePrice().toString());
                
                VenueType type = typeMap.get(venue.getVenueTypeId());
                if (type != null) {
                    vo.setVenueTypeName(type.getName());
                }
            }
            
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public Page<UserFavoriteVenueVO> getUserFavoritesPage(Long userId, Integer pageNum, Integer pageSize) {
        // 构建分页对象
        Page<UserFavoriteVenue> page = new Page<>(pageNum, pageSize);
        
        // 查询条件
        LambdaQueryWrapper<UserFavoriteVenue> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFavoriteVenue::getUserId, userId)
                .orderByDesc(UserFavoriteVenue::getCreatedAt);
        
        // 执行分页查询
        Page<UserFavoriteVenue> favoritePage = userFavoriteVenueMapper.selectPage(page, wrapper);
        
        // 构建结果分页对象
        Page<UserFavoriteVenueVO> resultPage = new Page<>();
        resultPage.setCurrent(favoritePage.getCurrent());
        resultPage.setSize(favoritePage.getSize());
        resultPage.setTotal(favoritePage.getTotal());
        resultPage.setPages(favoritePage.getPages());
        
        // 如果没有数据，直接返回空分页结果
        if (favoritePage.getRecords().isEmpty()) {
            resultPage.setRecords(new ArrayList<>());
            return resultPage;
        }
        
        // 收集所有场馆ID
        List<Long> venueIds = favoritePage.getRecords().stream()
                .map(UserFavoriteVenue::getVenueId)
                .collect(Collectors.toList());
        
        // 批量查询场馆信息
        LambdaQueryWrapper<Venue> venueWrapper = new LambdaQueryWrapper<>();
        venueWrapper.in(Venue::getId, venueIds);
        List<Venue> venues = venueMapper.selectList(venueWrapper);
        Map<Long, Venue> venueMap = venues.stream()
                .collect(Collectors.toMap(Venue::getId, venue -> venue));
        
        // 收集所有场馆类型ID
        List<Integer> typeIds = venues.stream()
                .map(Venue::getVenueTypeId)
                .collect(Collectors.toList());
        
        Map<Integer, VenueType> typeMap = new java.util.HashMap<>();
        if (!typeIds.isEmpty()) {
            // 批量查询场馆类型信息
            LambdaQueryWrapper<VenueType> typeWrapper = new LambdaQueryWrapper<>();
            typeWrapper.in(VenueType::getId, typeIds);
            List<VenueType> types = venueTypeMapper.selectList(typeWrapper);
            typeMap = types.stream()
                    .collect(Collectors.toMap(VenueType::getId, type -> type));
        }
        
        // 最终的类型Map，用于在lambda表达式中使用（必须是final或有效final）
        final Map<Integer, VenueType> finalTypeMap = typeMap;
        
        // 构建VO列表
        List<UserFavoriteVenueVO> voList = favoritePage.getRecords().stream().map(favorite -> {
            UserFavoriteVenueVO vo = new UserFavoriteVenueVO();
            BeanUtils.copyProperties(favorite, vo);
            
            Venue venue = venueMap.get(favorite.getVenueId());
            if (venue != null) {
                vo.setVenueName(venue.getName());
                vo.setCoverImage(venue.getCoverImage());
                vo.setAddress(venue.getAddress());
                vo.setBasePrice(venue.getBasePrice().toString());
                
                VenueType type = finalTypeMap.get(venue.getVenueTypeId());
                if (type != null) {
                    vo.setVenueTypeName(type.getName());
                }
            }
            
            return vo;
        }).collect(Collectors.toList());
        
        resultPage.setRecords(voList);
        return resultPage;
    }

    @Override
    public UserFavoriteVenueVO getFavoriteDetail(Long favoriteId) {
        // 查询收藏记录
        UserFavoriteVenue favorite = userFavoriteVenueMapper.selectById(favoriteId);
        if (favorite == null) {
            return null;
        }
        
        // 查询场馆信息
        Venue venue = venueMapper.selectById(favorite.getVenueId());
        if (venue == null) {
            return null;
        }
        
        // 查询场馆类型
        VenueType venueType = venueTypeMapper.selectById(venue.getVenueTypeId());
        
        // 构建VO
        UserFavoriteVenueVO vo = new UserFavoriteVenueVO();
        BeanUtils.copyProperties(favorite, vo);
        
        vo.setVenueName(venue.getName());
        vo.setCoverImage(venue.getCoverImage());
        vo.setAddress(venue.getAddress());
        vo.setBasePrice(venue.getBasePrice().toString());
        
        if (venueType != null) {
            vo.setVenueTypeName(venueType.getName());
        }
        
        return vo;
    }
} 