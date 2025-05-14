package com.xcl.venueserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xcl.venueserver.dto.BookingRuleDTO;
import com.xcl.venueserver.entity.BookingRule;

import java.util.List;
import java.util.Map;

/**
 * 预约规则服务接口
 */
public interface BookingRuleService extends IService<BookingRule> {
    
    /**
     * 获取所有预约规则
     * @return 预约规则列表
     */
    List<BookingRule> getAllRules();
    
    /**
     * 根据规则类型获取规则
     * @param ruleType 规则类型
     * @return 预约规则列表
     */
    List<BookingRule> getRulesByType(String ruleType);
    
    /**
     * 创建预约规则
     * @param dto 预约规则DTO
     * @return 创建的预约规则
     */
    BookingRule createRule(BookingRuleDTO dto);
    
    /**
     * 更新预约规则
     * @param id 规则ID
     * @param dto 预约规则DTO
     * @return 更新后的预约规则
     */
    BookingRule updateRule(Long id, BookingRuleDTO dto);
    
    /**
     * 删除预约规则
     * @param id 规则ID
     * @return 是否删除成功
     */
    boolean deleteRule(Long id);
    
    /**
     * 获取所有规则，按类型分组
     * @return 按类型分组的规则Map
     */
    Map<String, List<BookingRule>> getRulesGroupByType();
    
    /**
     * 获取规则详情
     * @param id 规则ID
     * @return 规则详情
     */
    BookingRule getRuleById(Long id);
} 