package com.xcl.venueserver.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xcl.venueserver.common.utils.Result;
import com.xcl.venueserver.dto.VenueFacilityDTO;
import com.xcl.venueserver.entity.VenueFacility;
import com.xcl.venueserver.service.VenueFacilityService;
import com.xcl.venueserver.vo.VenueFacilityVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 场馆设施控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/venue-facilities")
public class VenueFacilityController {
    
    @Resource
    private VenueFacilityService venueFacilityService;
    
    /**
     * 分页查询场馆设施
     */
    @GetMapping
    public Result<IPage<VenueFacilityVO>> pageFacilities(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam Long venueId,
            @RequestParam(required = false) String facilityType) {
        
        IPage<VenueFacilityVO> pageData = venueFacilityService.pageFacilities(page, size, venueId, facilityType);
        return Result.success(pageData);
    }
    
    /**
     * 根据场馆ID获取设施列表
     * 此接口允许未登录用户访问
     */
    @GetMapping("/venue/{venueId}")
    public Result<List<VenueFacility>> getFacilitiesByVenueId(@PathVariable Long venueId) {
        List<VenueFacility> facilities = venueFacilityService.getFacilitiesByVenueId(venueId);
        return Result.success(facilities);
    }
    
    /**
     * 根据ID获取场馆设施
     */
    @GetMapping("/{id}")
    public Result<VenueFacilityVO> getFacilityById(@PathVariable Long id) {
        VenueFacility facility = venueFacilityService.getFacilityById(id);
        if (facility == null) {
            return Result.error("场馆设施不存在");
        }
        // 转换为VO返回
        VenueFacilityVO facilityVO = new VenueFacilityVO();
        BeanUtils.copyProperties(facility, facilityVO);
        return Result.success(facilityVO);
    }
    
    /**
     * 创建场馆设施
     */
    @PostMapping
    public Result<VenueFacilityVO> createFacility(@Validated @RequestBody VenueFacilityDTO dto) {
        try {
            log.info("创建场馆设施，接收参数: {}", dto);
            
            // 从DTO转换为实体对象
            VenueFacility facility = new VenueFacility();
            BeanUtils.copyProperties(dto, facility);
            
            log.info("转换后的实体对象: {}, 价格: {}", facility, facility.getPrice());
            
            // 调用服务创建设施
            facility = venueFacilityService.createFacility(facility);
            
            log.info("创建后的设施对象: {}, 价格: {}", facility, facility.getPrice());
            
            // 转换为VO返回
            VenueFacilityVO facilityVO = new VenueFacilityVO();
            BeanUtils.copyProperties(facility, facilityVO);
            return Result.success(facilityVO);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新场馆设施
     */
    @PutMapping("/{id}")
    public Result<VenueFacilityVO> updateFacility(
            @PathVariable Long id,
            @Validated @RequestBody VenueFacilityDTO dto) {
        
        try {
            log.info("更新场馆设施，ID: {}, 接收参数: {}", id, dto);
            
            // 从DTO转换为实体对象
            VenueFacility facility = new VenueFacility();
            BeanUtils.copyProperties(dto, facility);
            
            log.info("转换后的实体对象: {}, 价格: {}", facility, facility.getPrice());
            
            // 调用服务更新设施
            facility = venueFacilityService.updateFacility(id, facility);
            
            log.info("更新后的设施对象: {}, 价格: {}", facility, facility.getPrice());
            
            // 转换为VO返回
            VenueFacilityVO facilityVO = new VenueFacilityVO();
            BeanUtils.copyProperties(facility, facilityVO);
            return Result.success(facilityVO);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除场馆设施
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteFacility(@PathVariable Long id) {
        try {
            boolean success = venueFacilityService.deleteFacility(id);
            if (success) {
                return Result.success();
            } else {
                return Result.error("删除场馆设施失败");
            }
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }
} 