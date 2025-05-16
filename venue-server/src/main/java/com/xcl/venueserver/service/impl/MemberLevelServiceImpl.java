package com.xcl.venueserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xcl.venueserver.dto.MemberLevelDTO;
import com.xcl.venueserver.entity.MemberLevel;
import com.xcl.venueserver.entity.UserPoints;
import com.xcl.venueserver.mapper.MemberLevelMapper;
import com.xcl.venueserver.mapper.UserPointsMapper;
import com.xcl.venueserver.service.MemberLevelService;
import com.xcl.venueserver.vo.MemberLevelVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 会员等级服务实现类
 */
@Slf4j
@Service
public class MemberLevelServiceImpl implements MemberLevelService {

    @Autowired
    private MemberLevelMapper memberLevelMapper;

    @Autowired
    private UserPointsMapper userPointsMapper;

    @Override
    public List<MemberLevel> getAllEnabledLevels() {
        return memberLevelMapper.selectAllEnabled();
    }

    @Override
    public IPage<MemberLevel> getLevelsByPage(Integer page, Integer size) {
        Page<MemberLevel> pageParam = new Page<>(page, size);
        return memberLevelMapper.selectPage(pageParam, 
                new LambdaQueryWrapper<MemberLevel>().orderByAsc(MemberLevel::getLevelValue));
    }

    @Override
    public MemberLevel getLevelById(Integer id) {
        return memberLevelMapper.selectById(id);
    }

    @Override
    public MemberLevel getLevelByValue(Integer levelValue) {
        return memberLevelMapper.selectOne(
                new LambdaQueryWrapper<MemberLevel>()
                        .eq(MemberLevel::getLevelValue, levelValue)
                        .eq(MemberLevel::getStatus, 1));
    }

    @Override
    public MemberLevel getLevelByPoints(Integer points) {
        return memberLevelMapper.selectByPoints(points);
    }

    @Override
    public MemberLevelVO getUserMemberLevel(Long userId) {
        // 获取用户积分
        UserPoints userPoints = userPointsMapper.selectByUserId(userId);
        if (userPoints == null) {
            return null;
        }

        Integer availablePoints = userPoints.getAvailablePoints();
        
        // 获取当前会员等级
        MemberLevel currentLevel = memberLevelMapper.selectByPoints(availablePoints);
        if (currentLevel == null) {
            return null;
        }
        
        // 获取下一级会员等级
        MemberLevel nextLevel = memberLevelMapper.selectNextLevel(currentLevel.getLevelValue());
        
        // 构建返回对象
        MemberLevelVO levelVO = new MemberLevelVO();
        levelVO.setLevelId(currentLevel.getId());
        levelVO.setLevelName(currentLevel.getLevelName());
        levelVO.setLevelValue(currentLevel.getLevelValue());
        levelVO.setIcon(currentLevel.getIconUrl());
        levelVO.setDiscount(currentLevel.getDiscountRate());
        
        if (nextLevel != null) {
            levelVO.setNextLevelName(nextLevel.getLevelName());
            levelVO.setNextLevelPoints(nextLevel.getPointThreshold() - availablePoints);
        }
        
        return levelVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveOrUpdateLevel(MemberLevelDTO memberLevelDTO) {
        MemberLevel memberLevel = new MemberLevel();
        BeanUtils.copyProperties(memberLevelDTO, memberLevel);
        
        // 检查等级值是否已存在
        MemberLevel existingLevel = null;
        if (memberLevel.getId() == null) {
            existingLevel = memberLevelMapper.selectOne(
                    new LambdaQueryWrapper<MemberLevel>()
                            .eq(MemberLevel::getLevelValue, memberLevel.getLevelValue()));
        } else {
            existingLevel = memberLevelMapper.selectOne(
                    new LambdaQueryWrapper<MemberLevel>()
                            .eq(MemberLevel::getLevelValue, memberLevel.getLevelValue())
                            .ne(MemberLevel::getId, memberLevel.getId()));
        }
        
        if (existingLevel != null) {
            log.warn("会员等级值已存在，levelValue: {}", memberLevel.getLevelValue());
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        if (memberLevel.getId() == null) {
            // 新增
            memberLevel.setCreateTime(now);
            memberLevel.setUpdateTime(now);
            return memberLevelMapper.insert(memberLevel) > 0;
        } else {
            // 更新
            memberLevel.setUpdateTime(now);
            return memberLevelMapper.updateById(memberLevel) > 0;
        }
    }

    @Override
    public boolean updateLevelStatus(Integer id, Integer status) {
        MemberLevel memberLevel = memberLevelMapper.selectById(id);
        if (memberLevel == null) {
            return false;
        }
        
        memberLevel.setStatus(status);
        memberLevel.setUpdateTime(LocalDateTime.now());
        
        return memberLevelMapper.updateById(memberLevel) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteLevel(Integer id) {
        MemberLevel memberLevel = memberLevelMapper.selectById(id);
        if (memberLevel == null) {
            return false;
        }
        
        // 检查是否是最低等级（普通会员）
        if (memberLevel.getLevelValue() == 1) {
            log.warn("无法删除最低等级(普通会员)");
            return false;
        }
        
        return memberLevelMapper.deleteById(id) > 0;
    }
} 