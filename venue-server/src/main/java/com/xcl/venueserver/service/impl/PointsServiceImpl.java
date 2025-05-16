package com.xcl.venueserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xcl.venueserver.dto.PointRecordQueryDTO;
import com.xcl.venueserver.entity.PointRecord;
import com.xcl.venueserver.entity.UserPoints;
import com.xcl.venueserver.mapper.PointRecordMapper;
import com.xcl.venueserver.mapper.UserPointsMapper;
import com.xcl.venueserver.service.PointsService;
import com.xcl.venueserver.vo.PointRecordVO;
import com.xcl.venueserver.vo.UserPointsSummaryVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 积分服务实现类
 */
@Service
@Slf4j
public class PointsServiceImpl implements PointsService {

    @Autowired
    private UserPointsMapper userPointsMapper;
    
    @Autowired
    private PointRecordMapper pointRecordMapper;

    @Override
    public UserPointsSummaryVO getUserPointsSummary(Long userId) {
        UserPoints userPoints = userPointsMapper.selectOne(
                new LambdaQueryWrapper<UserPoints>().eq(UserPoints::getUserId, userId)
        );
        
        UserPointsSummaryVO summaryVO = new UserPointsSummaryVO();
        if (userPoints != null) {
            summaryVO.setAvailablePoints(userPoints.getAvailablePoints());
            summaryVO.setFrozenPoints(userPoints.getFrozenPoints());
            summaryVO.setTotalPoints(userPoints.getTotalPoints());
            summaryVO.setExpiredPoints(userPoints.getExpiredPoints()); // 已过期积分
        } else {
            // 用户还没有积分账户，返回默认值
            summaryVO.setAvailablePoints(0);
            summaryVO.setFrozenPoints(0);
            summaryVO.setTotalPoints(0);
            summaryVO.setExpiredPoints(0);
        }
        return summaryVO;
    }

    @Override
    public IPage<PointRecordVO> getUserPointRecords(Long userId, PointRecordQueryDTO queryDTO) {
        Page<PointRecord> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        
        LambdaQueryWrapper<PointRecord> wrapper = new LambdaQueryWrapper<PointRecord>()
                .eq(PointRecord::getUserId, userId)
                .orderByDesc(PointRecord::getCreateTime);
        
        IPage<PointRecord> recordPage = pointRecordMapper.selectPage(page, wrapper);
        
        // 转换为VO
        IPage<PointRecordVO> voPage = recordPage.convert(record -> {
            PointRecordVO vo = new PointRecordVO();
            // 设置VO属性
            vo.setId(record.getId());
            vo.setPointType(record.getPointType());
            vo.setPoints(record.getPoints());
            vo.setBalance(record.getBalance());
            vo.setSourceType(record.getSourceType());
            vo.setSourceId(record.getSourceId());
            vo.setDescription(record.getDescription());
            vo.setCreateTime(record.getCreateTime());
            
            // 设置类型名称
            switch (record.getPointType()) {
                case 1: vo.setPointTypeName("获取积分"); break;
                case 2: vo.setPointTypeName("使用积分"); break;
                case 3: vo.setPointTypeName("冻结积分"); break;
                case 4: vo.setPointTypeName("解冻积分"); break;
                case 5: vo.setPointTypeName("确认使用"); break;
                default: vo.setPointTypeName("其他"); break;
            }
            
            // 设置来源类型名称
            switch (record.getSourceType()) {
                case 1: vo.setSourceTypeName("预约"); break;
                case 2: vo.setSourceTypeName("评价"); break;
                case 3: vo.setSourceTypeName("签到"); break;
                case 4: vo.setSourceTypeName("邀请"); break;
                case 5: vo.setSourceTypeName("兑换"); break;
                case 6: vo.setSourceTypeName("抵扣"); break;
                case 7: vo.setSourceTypeName("过期"); break;
                default: vo.setSourceTypeName("其他"); break;
            }
            
            return vo;
        });
        
        return voPage;
    }

    @Override
    @Transactional
    public Integer addPoints(Long userId, Integer sourceType, Object sourceId, String description, Double amount) {
        // TODO: 根据积分规则计算应增加的积分
        Integer points = amount != null ? amount.intValue() : 10; // 默认规则
        
        // 更新用户积分账户
        UserPoints userPoints = userPointsMapper.selectOne(
                new LambdaQueryWrapper<UserPoints>().eq(UserPoints::getUserId, userId)
        );
        
        if (userPoints == null) {
            // 创建新的积分账户
            userPoints = new UserPoints();
            userPoints.setUserId(userId);
            userPoints.setAvailablePoints(points);
            userPoints.setFrozenPoints(0);
            userPoints.setExpiredPoints(0);
            userPoints.setTotalPoints(points);
            userPoints.setCreateTime(LocalDateTime.now());
            userPoints.setUpdateTime(LocalDateTime.now());
            userPointsMapper.insert(userPoints);
        } else {
            // 更新已有账户
            userPoints.setAvailablePoints(userPoints.getAvailablePoints() + points);
            userPoints.setTotalPoints(userPoints.getTotalPoints() + points);
            userPoints.setUpdateTime(LocalDateTime.now());
            userPointsMapper.updateById(userPoints);
        }
        
        // 记录积分变动
        PointRecord record = new PointRecord();
        record.setUserId(userId);
        record.setPoints(points);
        record.setPointType(1); // 1-增加积分
        record.setSourceType(sourceType);
        record.setSourceId(sourceId.toString());
        record.setDescription(description);
        record.setCreateTime(LocalDateTime.now());
        pointRecordMapper.insert(record);
        
        return points;
    }

    @Override
    @Transactional
    public boolean usePoints(Long userId, Integer points, Integer sourceType, Object sourceId, String description) {
        UserPoints userPoints = userPointsMapper.selectOne(
                new LambdaQueryWrapper<UserPoints>().eq(UserPoints::getUserId, userId)
        );
        
        if (userPoints == null || userPoints.getAvailablePoints() < points) {
            return false; // 积分不足
        }
        
        // 更新用户积分
        userPoints.setAvailablePoints(userPoints.getAvailablePoints() - points);
        userPoints.setExpiredPoints(userPoints.getExpiredPoints() + points); // 使用expired作为已使用
        userPoints.setUpdateTime(LocalDateTime.now());
        userPointsMapper.updateById(userPoints);
        
        // 记录积分变动
        PointRecord record = new PointRecord();
        record.setUserId(userId);
        record.setPoints(points);
        record.setPointType(2); // 2-使用积分
        record.setSourceType(sourceType);
        record.setSourceId(sourceId.toString());
        record.setDescription(description);
        record.setCreateTime(LocalDateTime.now());
        pointRecordMapper.insert(record);
        
        return true;
    }

    @Override
    @Transactional
    public boolean freezePoints(Long userId, Integer points, Integer sourceType, Object sourceId, String description) {
        UserPoints userPoints = userPointsMapper.selectOne(
                new LambdaQueryWrapper<UserPoints>().eq(UserPoints::getUserId, userId)
        );
        
        if (userPoints == null || userPoints.getAvailablePoints() < points) {
            return false; // 积分不足
        }
        
        // 更新用户积分
        userPoints.setAvailablePoints(userPoints.getAvailablePoints() - points);
        userPoints.setFrozenPoints(userPoints.getFrozenPoints() + points);
        userPoints.setUpdateTime(LocalDateTime.now());
        userPointsMapper.updateById(userPoints);
        
        // 记录积分变动
        PointRecord record = new PointRecord();
        record.setUserId(userId);
        record.setPoints(points);
        record.setPointType(3); // 3-冻结积分
        record.setSourceType(sourceType);
        record.setSourceId(sourceId.toString());
        record.setDescription(description);
        record.setCreateTime(LocalDateTime.now());
        pointRecordMapper.insert(record);
        
        return true;
    }

    @Override
    @Transactional
    public boolean unfreezePoints(Long userId, Integer points, Integer sourceType, Object sourceId, String description) {
        UserPoints userPoints = userPointsMapper.selectOne(
                new LambdaQueryWrapper<UserPoints>().eq(UserPoints::getUserId, userId)
        );
        
        if (userPoints == null || userPoints.getFrozenPoints() < points) {
            return false; // 冻结积分不足
        }
        
        // 更新用户积分
        userPoints.setAvailablePoints(userPoints.getAvailablePoints() + points);
        userPoints.setFrozenPoints(userPoints.getFrozenPoints() - points);
        userPoints.setUpdateTime(LocalDateTime.now());
        userPointsMapper.updateById(userPoints);
        
        // 记录积分变动
        PointRecord record = new PointRecord();
        record.setUserId(userId);
        record.setPoints(points);
        record.setPointType(4); // 4-解冻积分
        record.setSourceType(sourceType);
        record.setSourceId(sourceId.toString());
        record.setDescription(description);
        record.setCreateTime(LocalDateTime.now());
        pointRecordMapper.insert(record);
        
        return true;
    }

    @Override
    @Transactional
    public boolean confirmFrozenPoints(Long userId, Integer points, Integer sourceType, Object sourceId, String description) {
        UserPoints userPoints = userPointsMapper.selectOne(
                new LambdaQueryWrapper<UserPoints>().eq(UserPoints::getUserId, userId)
        );
        
        if (userPoints == null || userPoints.getFrozenPoints() < points) {
            return false; // 冻结积分不足
        }
        
        // 更新用户积分
        userPoints.setFrozenPoints(userPoints.getFrozenPoints() - points);
        userPoints.setExpiredPoints(userPoints.getExpiredPoints() + points); // 使用expired作为已使用
        userPoints.setUpdateTime(LocalDateTime.now());
        userPointsMapper.updateById(userPoints);
        
        // 记录积分变动
        PointRecord record = new PointRecord();
        record.setUserId(userId);
        record.setPoints(points);
        record.setPointType(5); // 5-确认使用冻结积分
        record.setSourceType(sourceType);
        record.setSourceId(sourceId.toString());
        record.setDescription(description);
        record.setCreateTime(LocalDateTime.now());
        pointRecordMapper.insert(record);
        
        return true;
    }

    @Override
    @Transactional
    public Integer handleExpiredPoints(Long userId) {
        // TODO: 处理过期积分的逻辑
        return 0; // 暂时未实现
    }
} 