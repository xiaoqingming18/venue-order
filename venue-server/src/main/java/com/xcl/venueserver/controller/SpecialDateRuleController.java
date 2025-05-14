package com.xcl.venueserver.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xcl.venueserver.common.utils.CurrentUser;
import com.xcl.venueserver.common.utils.Result;
import com.xcl.venueserver.dto.SpecialDateRuleDTO;
import com.xcl.venueserver.entity.SpecialDateRule;
import com.xcl.venueserver.service.SpecialDateRuleService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;

/**
 * 特殊日期规则控制器
 */
@RestController
@RequestMapping("/api/special-date-rules")
public class SpecialDateRuleController {
    
    @Resource
    private SpecialDateRuleService specialDateRuleService;
    
    @Resource
    private CurrentUser currentUser;
    
    /**
     * 分页查询特殊日期规则
     */
    @GetMapping
    public Result<IPage<SpecialDateRule>> pageSpecialDateRules(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        
        IPage<SpecialDateRule> pageData = specialDateRuleService.pageSpecialDateRules(
                page, size, status, startDate, endDate);
        return Result.success(pageData);
    }
    
    /**
     * 根据日期范围获取特殊日期规则
     */
    @GetMapping("/date-range")
    public Result<List<SpecialDateRule>> getSpecialDateRulesByDateRange(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        
        List<SpecialDateRule> rules = specialDateRuleService.getSpecialDateRulesByDateRange(startDate, endDate);
        return Result.success(rules);
    }
    
    /**
     * 获取特殊日期规则详情
     */
    @GetMapping("/{id}")
    public Result<SpecialDateRule> getSpecialDateRuleById(@PathVariable Long id) {
        SpecialDateRule rule = specialDateRuleService.getSpecialDateRuleById(id);
        if (rule == null) {
            return Result.error("规则不存在");
        }
        return Result.success(rule);
    }
    
    /**
     * 创建特殊日期规则
     */
    @PostMapping
    public Result<SpecialDateRule> createSpecialDateRule(@Validated @RequestBody SpecialDateRuleDTO dto) {
        // 检查权限（只有管理员可以创建规则）
        if (!currentUser.isAdmin()) {
            return Result.error("无权操作");
        }
        
        try {
            SpecialDateRule rule = specialDateRuleService.createSpecialDateRule(dto);
            return Result.success(rule);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新特殊日期规则
     */
    @PutMapping("/{id}")
    public Result<SpecialDateRule> updateSpecialDateRule(
            @PathVariable Long id,
            @Validated @RequestBody SpecialDateRuleDTO dto) {
        
        // 检查权限（只有管理员可以更新规则）
        if (!currentUser.isAdmin()) {
            return Result.error("无权操作");
        }
        
        try {
            SpecialDateRule rule = specialDateRuleService.updateSpecialDateRule(id, dto);
            return Result.success(rule);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除特殊日期规则
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteSpecialDateRule(@PathVariable Long id) {
        // 检查权限（只有管理员可以删除规则）
        if (!currentUser.isAdmin()) {
            return Result.error("无权操作");
        }
        
        try {
            boolean success = specialDateRuleService.deleteSpecialDateRule(id);
            if (success) {
                return Result.success();
            } else {
                return Result.error("删除规则失败");
            }
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }
} 