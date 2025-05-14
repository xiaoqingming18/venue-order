package com.xcl.venueserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xcl.venueserver.dto.BookingRuleDTO;
import com.xcl.venueserver.entity.BookingRule;
import com.xcl.venueserver.mapper.BookingRuleMapper;
import com.xcl.venueserver.service.BookingRuleService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 预约规则服务实现类
 */
@Service
public class BookingRuleServiceImpl extends ServiceImpl<BookingRuleMapper, BookingRule> implements BookingRuleService {

    @Override
    public List<BookingRule> getAllRules() {
        return list(new LambdaQueryWrapper<BookingRule>()
                .orderByAsc(BookingRule::getRuleType)
                .orderByAsc(BookingRule::getRuleKey));
    }

    @Override
    public List<BookingRule> getRulesByType(String ruleType) {
        return list(new LambdaQueryWrapper<BookingRule>()
                .eq(BookingRule::getRuleType, ruleType)
                .orderByAsc(BookingRule::getRuleKey));
    }

    @Override
    public BookingRule createRule(BookingRuleDTO dto) {
        // 检查规则是否已存在
        long count = count(new LambdaQueryWrapper<BookingRule>()
                .eq(BookingRule::getRuleType, dto.getRuleType())
                .eq(BookingRule::getRuleKey, dto.getRuleKey()));
        
        if (count > 0) {
            throw new IllegalArgumentException("该规则已存在");
        }
        
        BookingRule rule = new BookingRule();
        rule.setRuleType(dto.getRuleType());
        rule.setRuleKey(dto.getRuleKey());
        rule.setRuleValue(dto.getRuleValue());
        rule.setDescription(dto.getDescription());
        rule.setStatus(dto.getStatus());
        rule.setCreatedAt(LocalDateTime.now());
        rule.setUpdatedAt(LocalDateTime.now());
        
        save(rule);
        return rule;
    }

    @Override
    public BookingRule updateRule(Long id, BookingRuleDTO dto) {
        BookingRule rule = getById(id);
        if (rule == null) {
            throw new IllegalArgumentException("规则不存在");
        }
        
        // 如果规则类型或键有变化，需要检查是否与已有规则冲突
        if (!rule.getRuleType().equals(dto.getRuleType()) || !rule.getRuleKey().equals(dto.getRuleKey())) {
            long count = count(new LambdaQueryWrapper<BookingRule>()
                    .eq(BookingRule::getRuleType, dto.getRuleType())
                    .eq(BookingRule::getRuleKey, dto.getRuleKey()));
            
            if (count > 0) {
                throw new IllegalArgumentException("规则类型和键已存在");
            }
        }
        
        rule.setRuleType(dto.getRuleType());
        rule.setRuleKey(dto.getRuleKey());
        rule.setRuleValue(dto.getRuleValue());
        rule.setDescription(dto.getDescription());
        rule.setStatus(dto.getStatus());
        rule.setUpdatedAt(LocalDateTime.now());
        
        updateById(rule);
        return rule;
    }

    @Override
    public boolean deleteRule(Long id) {
        BookingRule rule = getById(id);
        if (rule == null) {
            throw new IllegalArgumentException("规则不存在");
        }
        
        return removeById(id);
    }

    @Override
    public Map<String, List<BookingRule>> getRulesGroupByType() {
        List<BookingRule> rules = getAllRules();
        return rules.stream()
                .collect(Collectors.groupingBy(BookingRule::getRuleType));
    }

    @Override
    public BookingRule getRuleById(Long id) {
        return getById(id);
    }
} 