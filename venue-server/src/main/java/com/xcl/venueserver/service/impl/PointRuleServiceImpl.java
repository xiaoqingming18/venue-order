package com.xcl.venueserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xcl.venueserver.dto.PointRuleDTO;
import com.xcl.venueserver.entity.PointRule;
import com.xcl.venueserver.mapper.PointRuleMapper;
import com.xcl.venueserver.service.PointRuleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 积分规则服务实现类
 */
@Slf4j
@Service
public class PointRuleServiceImpl implements PointRuleService {

    @Autowired
    private PointRuleMapper pointRuleMapper;

    @Override
    public List<PointRule> getAllEnabledRules() {
        return pointRuleMapper.selectList(
                new LambdaQueryWrapper<PointRule>()
                        .eq(PointRule::getStatus, 1)
                        .orderByAsc(PointRule::getRuleType));
    }

    @Override
    public IPage<PointRule> getRulesByPage(Integer page, Integer size) {
        Page<PointRule> pageParam = new Page<>(page, size);
        return pointRuleMapper.selectPage(pageParam, 
                new LambdaQueryWrapper<PointRule>().orderByAsc(PointRule::getRuleType));
    }

    @Override
    public PointRule getRuleById(Integer id) {
        return pointRuleMapper.selectById(id);
    }

    @Override
    public PointRule getRuleByType(Integer ruleType) {
        return pointRuleMapper.selectByRuleType(ruleType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveOrUpdateRule(PointRuleDTO pointRuleDTO) {
        PointRule pointRule = new PointRule();
        BeanUtils.copyProperties(pointRuleDTO, pointRule);
        
        // 检查规则类型是否已存在
        PointRule existingRule = null;
        if (pointRule.getId() == null) {
            existingRule = pointRuleMapper.selectOne(
                    new LambdaQueryWrapper<PointRule>()
                            .eq(PointRule::getRuleType, pointRule.getRuleType()));
        } else {
            existingRule = pointRuleMapper.selectOne(
                    new LambdaQueryWrapper<PointRule>()
                            .eq(PointRule::getRuleType, pointRule.getRuleType())
                            .ne(PointRule::getId, pointRule.getId()));
        }
        
        if (existingRule != null) {
            log.warn("积分规则类型已存在，ruleType: {}", pointRule.getRuleType());
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        if (pointRule.getId() == null) {
            // 新增
            pointRule.setCreateTime(now);
            pointRule.setUpdateTime(now);
            return pointRuleMapper.insert(pointRule) > 0;
        } else {
            // 更新
            pointRule.setUpdateTime(now);
            return pointRuleMapper.updateById(pointRule) > 0;
        }
    }

    @Override
    public boolean updateRuleStatus(Integer id, Integer status) {
        PointRule pointRule = pointRuleMapper.selectById(id);
        if (pointRule == null) {
            return false;
        }
        
        pointRule.setStatus(status);
        pointRule.setUpdateTime(LocalDateTime.now());
        
        return pointRuleMapper.updateById(pointRule) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRule(Integer id) {
        return pointRuleMapper.deleteById(id) > 0;
    }
} 