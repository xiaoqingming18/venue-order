package com.xcl.venueserver.controller;

import com.xcl.venueserver.common.utils.CurrentUser;
import com.xcl.venueserver.common.utils.Result;
import com.xcl.venueserver.dto.BookingRuleDTO;
import com.xcl.venueserver.entity.BookingRule;
import com.xcl.venueserver.service.BookingRuleService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 预约规则控制器
 */
@RestController
@RequestMapping("/api/booking-rules")
public class BookingRuleController {
    
    @Resource
    private BookingRuleService bookingRuleService;
    
    @Resource
    private CurrentUser currentUser;
    
    /**
     * 获取所有预约规则
     */
    @GetMapping
    public Result<List<BookingRule>> getAllRules() {
        List<BookingRule> rules = bookingRuleService.getAllRules();
        return Result.success(rules);
    }
    
    /**
     * 获取规则，按类型分组
     */
    @GetMapping("/group-by-type")
    public Result<Map<String, List<BookingRule>>> getRulesGroupByType() {
        Map<String, List<BookingRule>> rules = bookingRuleService.getRulesGroupByType();
        return Result.success(rules);
    }
    
    /**
     * 根据规则类型获取规则
     */
    @GetMapping("/type/{ruleType}")
    public Result<List<BookingRule>> getRulesByType(@PathVariable String ruleType) {
        List<BookingRule> rules = bookingRuleService.getRulesByType(ruleType);
        return Result.success(rules);
    }
    
    /**
     * 获取规则详情
     */
    @GetMapping("/{id}")
    public Result<BookingRule> getRuleById(@PathVariable Long id) {
        BookingRule rule = bookingRuleService.getRuleById(id);
        if (rule == null) {
            return Result.error("规则不存在");
        }
        return Result.success(rule);
    }
    
    /**
     * 创建预约规则
     */
    @PostMapping
    public Result<BookingRule> createRule(@Validated @RequestBody BookingRuleDTO dto) {
        // 检查权限（只有管理员可以创建规则）
        if (!currentUser.isAdmin()) {
            return Result.error("无权操作");
        }
        
        try {
            BookingRule rule = bookingRuleService.createRule(dto);
            return Result.success(rule);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新预约规则
     */
    @PutMapping("/{id}")
    public Result<BookingRule> updateRule(
            @PathVariable Long id,
            @Validated @RequestBody BookingRuleDTO dto) {
        
        // 检查权限（只有管理员可以更新规则）
        if (!currentUser.isAdmin()) {
            return Result.error("无权操作");
        }
        
        try {
            BookingRule rule = bookingRuleService.updateRule(id, dto);
            return Result.success(rule);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除预约规则
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteRule(@PathVariable Long id) {
        // 检查权限（只有管理员可以删除规则）
        if (!currentUser.isAdmin()) {
            return Result.error("无权操作");
        }
        
        try {
            boolean success = bookingRuleService.deleteRule(id);
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