package com.xcl.venueserver.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xcl.venueserver.common.utils.Result;
import com.xcl.venueserver.dto.VenueLocationDTO;
import com.xcl.venueserver.entity.VenueLocation;
import com.xcl.venueserver.service.VenueLocationService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.List;

/**
 * 场馆位置控制器
 */
@RestController
@RequestMapping("/api/venue-locations")
@Validated
public class VenueLocationController {
    
    @Resource
    private VenueLocationService venueLocationService;
    
    /**
     * 分页查询场馆位置
     */
    @GetMapping
    public Result<IPage<VenueLocation>> pageLocations(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam Long venueId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer status) {
        
        IPage<VenueLocation> pageData = venueLocationService.pageLocations(page, size, venueId, type, status);
        return Result.success(pageData);
    }
    
    /**
     * 根据场馆ID获取位置列表
     */
    @GetMapping("/venue/{venueId}")
    public Result<List<VenueLocation>> getLocationsByVenueId(@PathVariable Long venueId) {
        List<VenueLocation> locations = venueLocationService.getLocationsByVenueId(venueId);
        return Result.success(locations);
    }
    
    /**
     * 根据ID获取场馆位置
     */
    @GetMapping("/{id}")
    public Result<VenueLocation> getLocationById(@PathVariable Long id) {
        VenueLocation location = venueLocationService.getLocationById(id);
        if (location == null) {
            return Result.error("场馆位置不存在");
        }
        return Result.success(location);
    }
    
    /**
     * 创建场馆位置
     */
    @PostMapping
    public Result<VenueLocation> createLocation(@Validated @RequestBody VenueLocationDTO dto) {
        try {
            VenueLocation location = venueLocationService.saveLocation(dto);
            return Result.success(location);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新场馆位置
     */
    @PutMapping("/{id}")
    public Result<VenueLocation> updateLocation(
            @PathVariable Long id,
            @Validated @RequestBody VenueLocationDTO dto) {
        
        try {
            VenueLocation location = venueLocationService.updateLocation(id, dto);
            return Result.success(location);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新场馆位置状态
     */
    @PutMapping("/{id}/status")
    public Result<VenueLocation> updateLocationStatus(
            @PathVariable Long id,
            @RequestParam @Min(0) @Max(1) Integer status) {
        
        try {
            VenueLocation location = venueLocationService.updateLocationStatus(id, status);
            return Result.success(location);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除场馆位置
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteLocation(@PathVariable Long id) {
        try {
            boolean success = venueLocationService.deleteLocation(id);
            if (success) {
                return Result.success();
            } else {
                return Result.error("删除场馆位置失败");
            }
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }
} 