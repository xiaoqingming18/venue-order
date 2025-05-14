package com.xcl.venueserver.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xcl.venueserver.dto.VenueDTO;
import com.xcl.venueserver.entity.Venue;

/**
 * 场馆服务接口
 */
public interface VenueService extends IService<Venue> {
    
    /**
     * 分页查询场馆
     * @param page 页码
     * @param size 每页数量
     * @param name 场馆名称（可选）
     * @param venueTypeId 场馆类型ID（可选）
     * @param status 状态（可选）
     * @return 分页数据
     */
    IPage<Venue> pageVenues(Integer page, Integer size, String name, Integer venueTypeId, Integer status);
    
    /**
     * 根据ID获取场馆
     * @param id 场馆ID
     * @return 场馆
     */
    Venue getVenueById(Long id);
    
    /**
     * 保存场馆
     * @param dto 场馆DTO
     * @return 保存后的场馆
     */
    Venue saveVenue(VenueDTO dto);
    
    /**
     * 更新场馆
     * @param id 场馆ID
     * @param dto 场馆DTO
     * @return 更新后的场馆
     */
    Venue updateVenue(Long id, VenueDTO dto);
    
    /**
     * 更新场馆状态
     * @param id 场馆ID
     * @param status 状态：0-关闭，1-开放
     * @return 更新后的场馆
     */
    Venue updateVenueStatus(Long id, Integer status);
    
    /**
     * 删除场馆
     * @param id 场馆ID
     * @return 是否删除成功
     */
    boolean deleteVenue(Long id);
} 