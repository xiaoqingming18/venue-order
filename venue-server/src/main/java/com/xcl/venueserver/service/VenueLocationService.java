package com.xcl.venueserver.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xcl.venueserver.dto.VenueLocationDTO;
import com.xcl.venueserver.entity.VenueLocation;

import java.util.List;

/**
 * 场馆位置服务接口
 */
public interface VenueLocationService extends IService<VenueLocation> {
    
    /**
     * 分页查询场馆位置
     * @param page 页码
     * @param size 每页数量
     * @param venueId 场馆ID
     * @param type 位置类型（可选）
     * @param status 状态（可选）
     * @return 分页数据
     */
    IPage<VenueLocation> pageLocations(Integer page, Integer size, Long venueId, String type, Integer status);
    
    /**
     * 根据场馆ID获取位置列表
     * @param venueId 场馆ID
     * @return 位置列表
     */
    List<VenueLocation> getLocationsByVenueId(Long venueId);
    
    /**
     * 根据ID获取场馆位置
     * @param id 位置ID
     * @return 场馆位置
     */
    VenueLocation getLocationById(Long id);
    
    /**
     * 保存场馆位置
     * @param dto 场馆位置DTO
     * @return 保存后的场馆位置
     */
    VenueLocation saveLocation(VenueLocationDTO dto);
    
    /**
     * 更新场馆位置
     * @param id 位置ID
     * @param dto 场馆位置DTO
     * @return 更新后的场馆位置
     */
    VenueLocation updateLocation(Long id, VenueLocationDTO dto);
    
    /**
     * 更新场馆位置状态
     * @param id 位置ID
     * @param status 状态：0-不可用，1-可用
     * @return 更新后的场馆位置
     */
    VenueLocation updateLocationStatus(Long id, Integer status);
    
    /**
     * 删除场馆位置
     * @param id 位置ID
     * @return 是否删除成功
     */
    boolean deleteLocation(Long id);
} 