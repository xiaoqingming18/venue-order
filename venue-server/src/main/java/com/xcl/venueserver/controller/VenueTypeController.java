package com.xcl.venueserver.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xcl.venueserver.common.utils.Result;
import com.xcl.venueserver.dto.VenueTypeDTO;
import com.xcl.venueserver.entity.VenueType;
import com.xcl.venueserver.service.VenueTypeService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 场馆类型控制器
 */
@RestController
@RequestMapping("/api/venue/types")
public class VenueTypeController {
    
    @Resource
    private VenueTypeService venueTypeService;
    
    /**
     * 分页查询场馆类型
     */
    @GetMapping
    public Result<IPage<VenueType>> pageVenueTypes(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name) {
        
        IPage<VenueType> pageData = venueTypeService.pageVenueTypes(page, size, name);
        return Result.success(pageData);
    }
    
    /**
     * 获取所有场馆类型
     */
    @GetMapping("/all")
    public Result<List<VenueType>> getAllVenueTypes() {
        List<VenueType> venueTypes = venueTypeService.list();
        return Result.success(venueTypes);
    }
    
    /**
     * 根据ID获取场馆类型
     */
    @GetMapping("/{id}")
    public Result<VenueType> getVenueTypeById(@PathVariable Integer id) {
        VenueType venueType = venueTypeService.getVenueTypeById(id);
        if (venueType == null) {
            return Result.error("场馆类型不存在");
        }
        return Result.success(venueType);
    }
    
    /**
     * 创建场馆类型
     */
    @PostMapping
    public Result<VenueType> createVenueType(@Validated @RequestBody VenueTypeDTO dto) {
        VenueType venueType = venueTypeService.saveVenueType(dto);
        return Result.success(venueType);
    }
    
    /**
     * 更新场馆类型
     */
    @PutMapping("/{id}")
    public Result<VenueType> updateVenueType(
            @PathVariable Integer id,
            @Validated @RequestBody VenueTypeDTO dto) {
        
        try {
            VenueType venueType = venueTypeService.updateVenueType(id, dto);
            return Result.success(venueType);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除场馆类型
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteVenueType(@PathVariable Integer id) {
        try {
            boolean success = venueTypeService.deleteVenueType(id);
            if (success) {
                return Result.success();
            } else {
                return Result.error("删除场馆类型失败");
            }
        } catch (IllegalStateException e) {
            return Result.error(e.getMessage());
        }
    }
} 