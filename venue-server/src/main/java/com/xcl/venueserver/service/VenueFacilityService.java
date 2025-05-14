package com.xcl.venueserver.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xcl.venueserver.entity.VenueFacility;
import com.xcl.venueserver.vo.VenueFacilityVO;

import java.util.List;

/**
 * 场馆设施服务接口
 */
public interface VenueFacilityService extends IService<VenueFacility> {
    
    /**
     * 分页查询场馆设施
     * @param page 页码
     * @param size 每页大小
     * @param venueId 场馆ID
     * @param facilityType 设施类型
     * @return 分页结果
     */
    IPage<VenueFacilityVO> pageFacilities(Integer page, Integer size, Long venueId, String facilityType);
    
    /**
     * 获取场馆设施详情
     * @param id 设施ID
     * @return 设施详情
     */
    VenueFacility getFacilityById(Long id);
    
    /**
     * 创建场馆设施
     * @param facility 设施信息
     * @return 创建的设施
     */
    VenueFacility createFacility(VenueFacility facility);
    
    /**
     * 更新场馆设施
     * @param id 设施ID
     * @param facility 设施信息
     * @return 更新后的设施
     */
    VenueFacility updateFacility(Long id, VenueFacility facility);
    
    /**
     * 删除场馆设施
     * @param id 设施ID
     * @return 是否成功
     */
    boolean deleteFacility(Long id);
    
    /**
     * 根据场馆ID查询设施列表
     * @param venueId 场馆ID
     * @return 设施列表
     */
    List<VenueFacility> getFacilitiesByVenueId(Long venueId);
    
    /**
     * 根据设施ID列表批量查询设施
     * @param facilityIds 设施ID列表
     * @return 设施列表
     */
    List<VenueFacility> getFacilitiesByIds(List<Long> facilityIds);
} 