package com.xcl.venueserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xcl.venueserver.dto.SpecialDateRuleDTO;
import com.xcl.venueserver.entity.SpecialDateRule;
import com.xcl.venueserver.mapper.SpecialDateRuleMapper;
import com.xcl.venueserver.service.SpecialDateRuleService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 特殊日期规则服务实现类
 */
@Service
public class SpecialDateRuleServiceImpl extends ServiceImpl<SpecialDateRuleMapper, SpecialDateRule> 
    implements SpecialDateRuleService {

    @Override
    public IPage<SpecialDateRule> pageSpecialDateRules(Integer page, Integer size, Integer status,
                                                     LocalDate startDate, LocalDate endDate) {
        Page<SpecialDateRule> pageParam = new Page<>(page, size);
        
        LambdaQueryWrapper<SpecialDateRule> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加筛选条件
        if (status != null) {
            queryWrapper.eq(SpecialDateRule::getStatus, status);
        }
        
        if (startDate != null) {
            queryWrapper.ge(SpecialDateRule::getSpecialDate, startDate);
        }
        
        if (endDate != null) {
            queryWrapper.le(SpecialDateRule::getSpecialDate, endDate);
        }
        
        // 按日期排序
        queryWrapper.orderByAsc(SpecialDateRule::getSpecialDate);
        
        return page(pageParam, queryWrapper);
    }

    @Override
    public SpecialDateRule createSpecialDateRule(SpecialDateRuleDTO dto) {
        // 检查特殊日期是否已存在
        long count = count(new LambdaQueryWrapper<SpecialDateRule>()
                .eq(SpecialDateRule::getSpecialDate, dto.getSpecialDate()));
        
        if (count > 0) {
            throw new IllegalArgumentException("该日期的规则已存在");
        }
        
        SpecialDateRule rule = new SpecialDateRule();
        rule.setSpecialDate(dto.getSpecialDate());
        rule.setDescription(dto.getDescription());
        rule.setPriceRate(dto.getPriceRate());
        rule.setStatus(dto.getStatus());
        rule.setCreatedAt(LocalDateTime.now());
        rule.setUpdatedAt(LocalDateTime.now());
        
        save(rule);
        return rule;
    }

    @Override
    public SpecialDateRule updateSpecialDateRule(Long id, SpecialDateRuleDTO dto) {
        SpecialDateRule rule = getById(id);
        if (rule == null) {
            throw new IllegalArgumentException("规则不存在");
        }
        
        // 如果日期变更，需要检查是否与其他规则日期冲突
        if (!rule.getSpecialDate().equals(dto.getSpecialDate())) {
            long count = count(new LambdaQueryWrapper<SpecialDateRule>()
                    .eq(SpecialDateRule::getSpecialDate, dto.getSpecialDate()));
            
            if (count > 0) {
                throw new IllegalArgumentException("该日期的规则已存在");
            }
        }
        
        rule.setSpecialDate(dto.getSpecialDate());
        rule.setDescription(dto.getDescription());
        rule.setPriceRate(dto.getPriceRate());
        rule.setStatus(dto.getStatus());
        rule.setUpdatedAt(LocalDateTime.now());
        
        updateById(rule);
        return rule;
    }

    @Override
    public boolean deleteSpecialDateRule(Long id) {
        SpecialDateRule rule = getById(id);
        if (rule == null) {
            throw new IllegalArgumentException("规则不存在");
        }
        
        return removeById(id);
    }

    @Override
    public List<SpecialDateRule> getSpecialDateRulesByDateRange(LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<SpecialDateRule> queryWrapper = new LambdaQueryWrapper<>();
        
        if (startDate != null) {
            queryWrapper.ge(SpecialDateRule::getSpecialDate, startDate);
        }
        
        if (endDate != null) {
            queryWrapper.le(SpecialDateRule::getSpecialDate, endDate);
        }
        
        queryWrapper.orderByAsc(SpecialDateRule::getSpecialDate);
        
        return list(queryWrapper);
    }

    @Override
    public SpecialDateRule getSpecialDateRuleById(Long id) {
        return getById(id);
    }
} 