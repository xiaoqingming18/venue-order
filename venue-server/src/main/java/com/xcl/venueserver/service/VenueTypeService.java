package com.xcl.venueserver.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xcl.venueserver.dto.VenueTypeDTO;
import com.xcl.venueserver.entity.VenueType;

/**
 * 场馆类型服务接口
 */
public interface VenueTypeService extends IService<VenueType> {
    
    /**
     * 分页查询场馆类型
     * @param page 页码
     * @param size 每页数量
     * @param name 类型名称（可选）
     * @return 分页数据
     */
    IPage<VenueType> pageVenueTypes(Integer page, Integer size, String name);
    
    /**
     * 根据ID获取场馆类型
     * @param id 类型ID
     * @return 场馆类型
     */
    VenueType getVenueTypeById(Integer id);
    
    /**
     * 保存场馆类型
     * @param dto 场馆类型DTO
     * @return 保存后的场馆类型
     */
    VenueType saveVenueType(VenueTypeDTO dto);
    
    /**
     * 更新场馆类型
     * @param id 类型ID
     * @param dto 场馆类型DTO
     * @return 更新后的场馆类型
     */
    VenueType updateVenueType(Integer id, VenueTypeDTO dto);
    
    /**
     * 删除场馆类型
     * @param id 类型ID
     * @return 是否删除成功
     */
    boolean deleteVenueType(Integer id);
} 