package com.xcl.venueserver.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xcl.venueserver.dto.SpecialDateRuleDTO;
import com.xcl.venueserver.entity.SpecialDateRule;

import java.time.LocalDate;
import java.util.List;

/**
 * 特殊日期规则服务接口
 */
public interface SpecialDateRuleService extends IService<SpecialDateRule> {
    
    /**
     * 分页查询特殊日期规则
     * @param page 页码
     * @param size 每页大小
     * @param status 状态
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 分页数据
     */
    IPage<SpecialDateRule> pageSpecialDateRules(Integer page, Integer size, Integer status,
                                               LocalDate startDate, LocalDate endDate);
    
    /**
     * 创建特殊日期规则
     * @param dto 特殊日期规则DTO
     * @return 创建的特殊日期规则
     */
    SpecialDateRule createSpecialDateRule(SpecialDateRuleDTO dto);
    
    /**
     * 更新特殊日期规则
     * @param id 规则ID
     * @param dto 特殊日期规则DTO
     * @return 更新后的特殊日期规则
     */
    SpecialDateRule updateSpecialDateRule(Long id, SpecialDateRuleDTO dto);
    
    /**
     * 删除特殊日期规则
     * @param id 规则ID
     * @return 是否删除成功
     */
    boolean deleteSpecialDateRule(Long id);
    
    /**
     * 根据日期范围获取特殊日期规则
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 特殊日期规则列表
     */
    List<SpecialDateRule> getSpecialDateRulesByDateRange(LocalDate startDate, LocalDate endDate);
    
    /**
     * 获取特殊日期规则详情
     * @param id 规则ID
     * @return 特殊日期规则详情
     */
    SpecialDateRule getSpecialDateRuleById(Long id);
} 