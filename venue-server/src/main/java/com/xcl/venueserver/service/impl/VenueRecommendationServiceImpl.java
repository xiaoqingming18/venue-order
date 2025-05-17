package com.xcl.venueserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xcl.venueserver.dto.BrowseHistoryDTO;
import com.xcl.venueserver.dto.RecommendationFeedbackDTO;
import com.xcl.venueserver.entity.RecommendationFeedback;
import com.xcl.venueserver.entity.UserBrowseHistory;
import com.xcl.venueserver.entity.UserVenuePreference;
import com.xcl.venueserver.entity.VenueRecommendation;
import com.xcl.venueserver.mapper.RecommendationFeedbackMapper;
import com.xcl.venueserver.mapper.UserBrowseHistoryMapper;
import com.xcl.venueserver.mapper.VenueRecommendationMapper;
import com.xcl.venueserver.service.VenueRecommendationService;
import com.xcl.venueserver.vo.VenueRecommendationVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 场馆推荐服务实现类
 */
@Slf4j
@Service
public class VenueRecommendationServiceImpl extends ServiceImpl<VenueRecommendationMapper, VenueRecommendation>
        implements VenueRecommendationService {

    private final UserBrowseHistoryMapper userBrowseHistoryMapper;
    private final RecommendationFeedbackMapper recommendationFeedbackMapper;

    public VenueRecommendationServiceImpl(UserBrowseHistoryMapper userBrowseHistoryMapper,
                                         RecommendationFeedbackMapper recommendationFeedbackMapper) {
        this.userBrowseHistoryMapper = userBrowseHistoryMapper;
        this.recommendationFeedbackMapper = recommendationFeedbackMapper;
    }

    @Override
    @Transactional
    public boolean recordBrowseHistory(BrowseHistoryDTO browseHistoryDTO) {
        if (browseHistoryDTO.getVenueId() == null) {
            return false;
        }

        try {
            // 记录浏览历史
            UserBrowseHistory history = new UserBrowseHistory();
            BeanUtils.copyProperties(browseHistoryDTO, history);
            history.setBrowseTime(LocalDateTime.now());
            userBrowseHistoryMapper.insert(history);
            
            // 更新用户场馆偏好
            Long userId = browseHistoryDTO.getUserId();
            Long venueId = browseHistoryDTO.getVenueId();
            
            if (userId != null) {
                log.info("更新用户{}对场馆{}的偏好数据", userId, venueId);
                updateUserVenuePreference(userId, venueId);
            }
            
            return true;
        } catch (Exception e) {
            log.error("记录浏览历史失败", e);
            return false;
        }
    }
    
    /**
     * 更新用户对场馆的偏好数据
     *
     * @param userId 用户ID
     * @param venueId 场馆ID
     */
    private void updateUserVenuePreference(Long userId, Long venueId) {
        try {
            // 查询当前用户对该场馆的偏好记录
            LambdaQueryWrapper<UserVenuePreference> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserVenuePreference::getUserId, userId)
                    .eq(UserVenuePreference::getVenueId, venueId);
            
            UserVenuePreference preference = getBaseMapper().selectUserVenuePreference(userId, venueId);
            
            LocalDateTime now = LocalDateTime.now();
            
            if (preference != null) {
                // 更新浏览次数和最后交互时间
                preference.setBrowseCount(preference.getBrowseCount() + 1);
                preference.setLastInteraction(now);
                
                // 计算新的偏好分数（简单实现，实际可能需要更复杂的算法）
                // 这里偏好分数随着浏览次数增加而增加，最高10分
                BigDecimal newScore = BigDecimal.valueOf(
                        Math.min(preference.getBrowseCount() * 0.5, 10)
                );
                preference.setPreferenceScore(newScore);
                
                // 更新偏好记录
                getBaseMapper().updateUserVenuePreference(preference);
                log.info("更新用户{}对场馆{}的偏好数据，浏览次数：{}，偏好分数：{}", 
                        userId, venueId, preference.getBrowseCount(), preference.getPreferenceScore());
            } else {
                // 创建新的偏好记录
                preference = new UserVenuePreference();
                preference.setUserId(userId);
                preference.setVenueId(venueId);
                preference.setBrowseCount(1);
                preference.setOrderCount(0);
                preference.setPreferenceScore(BigDecimal.valueOf(0.5)); // 初始偏好分数
                preference.setLastInteraction(now);
                preference.setUpdatedAt(now);
                
                // 插入偏好记录
                getBaseMapper().insertUserVenuePreference(preference);
                log.info("创建用户{}对场馆{}的偏好数据，初始偏好分数：{}", 
                        userId, venueId, preference.getPreferenceScore());
            }
        } catch (Exception e) {
            log.error("更新用户{}对场馆{}的偏好数据失败", userId, venueId, e);
        }
    }

    @Override
    public List<VenueRecommendationVO> getHomeRecommendations(Long userId, Integer limit) {
        if (userId == null) {
            // 未登录用户，返回热门推荐
            return getHotRecommendations(limit);
        }

        // 获取用户的个性化推荐
        Page<VenueRecommendation> page = new Page<>(1, limit);
        IPage<VenueRecommendation> recommendationsPage = baseMapper.getRecommendationsWithDetails(page, userId);
        List<VenueRecommendation> recommendations = recommendationsPage.getRecords();

        if (recommendations.isEmpty()) {
            // 如果没有个性化推荐，则生成推荐并再次查询
            int count = generateRecommendations(userId);
            if (count > 0) {
                recommendationsPage = baseMapper.getRecommendationsWithDetails(page, userId);
                recommendations = recommendationsPage.getRecords();
            }

            // 如果仍然没有推荐，则返回热门推荐
            if (recommendations.isEmpty()) {
                return getHotRecommendations(limit);
            }
        }

        // 标记推荐为已展示
        List<Long> recommendationIds = recommendations.stream()
                .map(VenueRecommendation::getId)
                .collect(Collectors.toList());
        baseMapper.markAsShown(recommendationIds);

        // 转换为VO对象
        return recommendations.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<VenueRecommendationVO> getSimilarVenueRecommendations(Long userId, Long venueId, Integer limit) {
        // 这里实现获取相似场馆的逻辑
        // 目前简单返回空列表，实际项目中需要实现基于相似度的推荐
        return Collections.emptyList();
    }

    @Override
    public IPage<VenueRecommendationVO> getPagedRecommendations(Long userId, Integer pageNum, Integer pageSize) {
        if (userId == null) {
            return new Page<>(pageNum, pageSize);
        }

        Page<VenueRecommendation> page = new Page<>(pageNum, pageSize);
        IPage<VenueRecommendation> recommendationsPage = baseMapper.getRecommendationsWithDetails(page, userId);
        
        // 转换为VO
        List<VenueRecommendationVO> voList = recommendationsPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        // 创建VO分页对象
        Page<VenueRecommendationVO> voPage = new Page<>(pageNum, pageSize, recommendationsPage.getTotal());
        voPage.setRecords(voList);
        
        return voPage;
    }

    @Override
    @Transactional
    public boolean submitFeedback(Long userId, RecommendationFeedbackDTO feedbackDTO) {
        if (userId == null || feedbackDTO.getVenueId() == null) {
            return false;
        }

        RecommendationFeedback feedback = new RecommendationFeedback();
        feedback.setUserId(userId);
        feedback.setVenueId(feedbackDTO.getVenueId());
        feedback.setRecommendationId(feedbackDTO.getRecommendationId());
        feedback.setFeedbackType(feedbackDTO.getFeedbackType());
        feedback.setReason(feedbackDTO.getReason());
        feedback.setCreatedAt(LocalDateTime.now());

        try {
            recommendationFeedbackMapper.insert(feedback);
            return true;
        } catch (Exception e) {
            log.error("提交反馈失败", e);
            return false;
        }
    }

    @Override
    public boolean markAsClicked(Long recommendationId) {
        if (recommendationId == null) {
            return false;
        }

        try {
            int rows = baseMapper.markAsClicked(recommendationId);
            return rows > 0;
        } catch (Exception e) {
            log.error("标记推荐为已点击失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public int generateRecommendations(Long userId) {
        // 这里简单实现，实际项目中可能需要更复杂的推荐算法
        // 例如基于协同过滤、内容相似度等

        // 先清除用户现有的未展示推荐
        LambdaQueryWrapper<VenueRecommendation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VenueRecommendation::getUserId, userId)
                .eq(VenueRecommendation::getIsShown, 0);
        baseMapper.delete(queryWrapper);

        // 生成一些推荐（此处仅示例，实际项目需替换为真实推荐算法）
        List<VenueRecommendation> recommendations = generateDummyRecommendations(userId);

        // 批量保存推荐
        try {
            recommendations.forEach(baseMapper::insert);
            return recommendations.size();
        } catch (Exception e) {
            log.error("生成推荐失败", e);
            return 0;
        }
    }

    /**
     * 获取热门推荐（示例）
     */
    private List<VenueRecommendationVO> getHotRecommendations(Integer limit) {
        // 这里应该实现获取热门场馆的逻辑
        // 目前简单返回空列表，实际项目中需要实现
        return Collections.emptyList();
    }

    /**
     * 生成示例推荐（实际项目中需要替换为真实推荐算法）
     */
    private List<VenueRecommendation> generateDummyRecommendations(Long userId) {
        return Collections.emptyList();
    }

    /**
     * 将推荐实体转换为VO
     */
    private VenueRecommendationVO convertToVO(VenueRecommendation recommendation) {
        VenueRecommendationVO vo = new VenueRecommendationVO();
        BeanUtils.copyProperties(recommendation, vo);
        
        // 设置推荐类型名称
        String typeName = "";
        switch (recommendation.getRecommendationType()) {
            case 0:
                typeName = "猜你喜欢";
                break;
            case 1:
                typeName = "相似用户喜欢";
                break;
            case 2:
                typeName = "热门推荐";
                break;
            case 3:
                typeName = "新上线";
                break;
            default:
                typeName = "推荐";
        }
        vo.setRecommendationTypeName(typeName);
        
        // 设置推荐原因
        String reason = "";
        switch (recommendation.getRecommendationType()) {
            case 0:
                reason = "根据您的浏览历史推荐";
                break;
            case 1:
                reason = "与您相似的用户也喜欢";
                break;
            case 2:
                reason = "热门场馆";
                break;
            case 3:
                reason = "新上线场馆";
                break;
            default:
                reason = "";
        }
        vo.setRecommendationReason(reason);
        
        return vo;
    }
} 