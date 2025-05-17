package com.xcl.venueserver.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xcl.venueserver.dto.FeedbackDTO;
import com.xcl.venueserver.entity.User;
import com.xcl.venueserver.entity.UserFeedback;
import com.xcl.venueserver.mapper.UserFeedbackMapper;
import com.xcl.venueserver.mapper.UserMapper;
import com.xcl.venueserver.service.FeedbackReplyService;
import com.xcl.venueserver.service.UserFeedbackService;
import com.xcl.venueserver.vo.FeedbackReplyVO;
import com.xcl.venueserver.vo.UserFeedbackVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用户反馈服务实现类
 */
@Slf4j
@Service
public class UserFeedbackServiceImpl extends ServiceImpl<UserFeedbackMapper, UserFeedback> implements UserFeedbackService {

    private final UserMapper userMapper;
    private final FeedbackReplyService feedbackReplyService;

    public UserFeedbackServiceImpl(UserMapper userMapper, FeedbackReplyService feedbackReplyService) {
        this.userMapper = userMapper;
        this.feedbackReplyService = feedbackReplyService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitFeedback(Long userId, FeedbackDTO feedbackDTO) {
        // 创建反馈实体
        UserFeedback feedback = new UserFeedback();
        feedback.setUserId(userId);
        feedback.setType(feedbackDTO.getType());
        feedback.setTitle(feedbackDTO.getTitle());
        feedback.setContent(feedbackDTO.getContent());
        feedback.setContact(feedbackDTO.getContact());
        feedback.setStatus(0); // 默认为待处理状态
        
        // 处理图片列表
        if (feedbackDTO.getImages() != null && !feedbackDTO.getImages().isEmpty()) {
            feedback.setImages(JSON.toJSONString(feedbackDTO.getImages()));
        }
        
        // 保存反馈
        save(feedback);
        return feedback.getId();
    }

    @Override
    public UserFeedbackVO getFeedbackDetail(Long id) {
        // 获取反馈信息
        UserFeedback feedback = getById(id);
        if (feedback == null) {
            return null;
        }
        
        // 转换为VO
        UserFeedbackVO feedbackVO = convertToVO(feedback);
        
        // 获取反馈回复列表
        List<FeedbackReplyVO> replies = feedbackReplyService.getFeedbackReplies(id);
        feedbackVO.setReplies(replies);
        
        return feedbackVO;
    }

    @Override
    public Page<UserFeedbackVO> getUserFeedbackList(Long userId, Page<UserFeedback> page) {
        // 优化查询条件，确保使用索引，限制返回数据量
        if (page.getSize() > 10) {
            // 限制每页最大数据量，避免过多数据排序导致内存不足
            page.setSize(10);
        }
        
        try {
            log.info("获取用户反馈列表，用户ID: {}, 当前页: {}, 每页大小: {}", 
                userId, page.getCurrent(), page.getSize());
            
            // 1. 先进行总数统计
            LambdaQueryWrapper<UserFeedback> countWrapper = Wrappers.<UserFeedback>lambdaQuery()
                .eq(UserFeedback::getUserId, userId);
            
            long total = count(countWrapper);
            log.info("用户反馈总数: {}", total);
            
            // 如果没有数据，直接返回空页
            if (total == 0) {
                Page<UserFeedbackVO> emptyPage = new Page<>(page.getCurrent(), page.getSize());
                emptyPage.setTotal(0);
                emptyPage.setRecords(new ArrayList<>());
                return emptyPage;
            }
            
            // 2. 只查询必要的字段并使用分页
            LambdaQueryWrapper<UserFeedback> queryWrapper = Wrappers.<UserFeedback>lambdaQuery()
                .eq(UserFeedback::getUserId, userId)
                .select(UserFeedback::getId, UserFeedback::getUserId, UserFeedback::getType, 
                        UserFeedback::getTitle, UserFeedback::getStatus, UserFeedback::getCreatedAt)
                .orderByDesc(UserFeedback::getCreatedAt);
            
            // 使用原生分页方式
            long current = page.getCurrent();
            long size = page.getSize();
            long offset = (current - 1) * size;
            
            // 直接使用mp的分页插件
            Page<UserFeedback> feedbackPage = page(page, queryWrapper);
            
            // 记录查询结果
            log.info("查询结果: 总数={}, 当前页记录数={}", 
                feedbackPage.getTotal(), feedbackPage.getRecords().size());
            
            // 3. 转换为VO
            Page<UserFeedbackVO> voPage = new Page<>();
            BeanUtils.copyProperties(feedbackPage, voPage, "records");
            
            if (feedbackPage.getRecords().isEmpty()) {
                voPage.setRecords(new ArrayList<>());
                return voPage;
            }
            
            List<UserFeedbackVO> voList = feedbackPage.getRecords().stream()
                .map(this::convertSimpleVO)
                .collect(Collectors.toList());
            
            voPage.setRecords(voList);
            log.info("返回VO对象: 总数={}, 记录数={}", voPage.getTotal(), voList.size());
            return voPage;
        } catch (Exception e) {
            log.error("获取用户反馈列表失败", e);
            // 出错时返回空页面
            Page<UserFeedbackVO> emptyPage = new Page<>(page.getCurrent(), page.getSize());
            emptyPage.setRecords(new ArrayList<>());
            return emptyPage;
        }
    }

    @Override
    public Page<UserFeedbackVO> getAllFeedbackList(Integer status, Integer type, String keyword, Page<UserFeedback> page) {
        // 构建查询条件
        LambdaQueryWrapper<UserFeedback> queryWrapper = Wrappers.<UserFeedback>lambdaQuery()
                .eq(status != null, UserFeedback::getStatus, status)
                .eq(type != null, UserFeedback::getType, type)
                .and(StringUtils.isNotBlank(keyword), wrapper ->
                        wrapper.like(UserFeedback::getTitle, keyword)
                                .or()
                                .like(UserFeedback::getContent, keyword))
                .orderByDesc(UserFeedback::getCreatedAt);
        
        Page<UserFeedback> feedbackPage = page(page, queryWrapper);
        
        // 转换为VO
        Page<UserFeedbackVO> voPage = new Page<>();
        BeanUtils.copyProperties(feedbackPage, voPage, "records");
        
        if (feedbackPage.getRecords().isEmpty()) {
            voPage.setRecords(new ArrayList<>());
            return voPage;
        }
        
        // 获取所有用户ID
        List<Long> userIds = feedbackPage.getRecords().stream()
                .map(UserFeedback::getUserId)
                .distinct()
                .collect(Collectors.toList());
        
        // 批量查询用户信息
        List<User> users = userMapper.selectBatchIds(userIds);
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        
        // 转换为VO并设置用户信息
        List<UserFeedbackVO> voList = feedbackPage.getRecords().stream()
                .map(feedback -> {
                    UserFeedbackVO vo = this.convertToVO(feedback);
                    
                    // 设置用户信息
                    User user = userMap.get(feedback.getUserId());
                    if (user != null) {
                        vo.setUsername(user.getUsername());
                        vo.setNickname(user.getNickname());
                    }
                    
                    return vo;
                })
                .collect(Collectors.toList());
        
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateFeedbackStatus(Long id, Integer status) {
        UserFeedback feedback = getById(id);
        if (feedback == null) {
            return false;
        }
        
        feedback.setStatus(status);
        feedback.setUpdatedAt(LocalDateTime.now());
        return updateById(feedback);
    }
    
    @Override
    public Map<String, Object> getFeedbackStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // 总数统计
            long total = count();
            stats.put("total", total);
            
            // 各状态统计
            LambdaQueryWrapper<UserFeedback> pendingWrapper = Wrappers.<UserFeedback>lambdaQuery()
                    .eq(UserFeedback::getStatus, 0);
            long pending = count(pendingWrapper);
            stats.put("pending", pending);
            
            LambdaQueryWrapper<UserFeedback> processingWrapper = Wrappers.<UserFeedback>lambdaQuery()
                    .eq(UserFeedback::getStatus, 1);
            long processing = count(processingWrapper);
            stats.put("processing", processing);
            
            LambdaQueryWrapper<UserFeedback> repliedWrapper = Wrappers.<UserFeedback>lambdaQuery()
                    .eq(UserFeedback::getStatus, 2);
            long replied = count(repliedWrapper);
            stats.put("replied", replied);
            
            LambdaQueryWrapper<UserFeedback> closedWrapper = Wrappers.<UserFeedback>lambdaQuery()
                    .eq(UserFeedback::getStatus, 3);
            long closed = count(closedWrapper);
            stats.put("closed", closed);
            
            // 今日数量统计
            LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
            LambdaQueryWrapper<UserFeedback> todayWrapper = Wrappers.<UserFeedback>lambdaQuery()
                    .ge(UserFeedback::getCreatedAt, today);
            long todayCount = count(todayWrapper);
            stats.put("todayCount", todayCount);
            
            // 本周数量统计
            LocalDateTime weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
            LambdaQueryWrapper<UserFeedback> weekWrapper = Wrappers.<UserFeedback>lambdaQuery()
                    .ge(UserFeedback::getCreatedAt, weekStart);
            long weekCount = count(weekWrapper);
            stats.put("weekCount", weekCount);
            
            // 本月数量统计
            LocalDateTime monthStart = today.withDayOfMonth(1);
            LambdaQueryWrapper<UserFeedback> monthWrapper = Wrappers.<UserFeedback>lambdaQuery()
                    .ge(UserFeedback::getCreatedAt, monthStart);
            long monthCount = count(monthWrapper);
            stats.put("monthCount", monthCount);
            
            // 按类型统计分布
            List<Map<String, Object>> typeDistribution = new ArrayList<>();
            for (int type = 1; type <= 4; type++) {
                final int typeVal = type;
                LambdaQueryWrapper<UserFeedback> typeWrapper = Wrappers.<UserFeedback>lambdaQuery()
                        .eq(UserFeedback::getType, typeVal);
                long typeCount = count(typeWrapper);
                
                Map<String, Object> typeData = new HashMap<>();
                typeData.put("type", typeVal);
                typeData.put("count", typeCount);
                
                // 计算百分比
                double percentage = total > 0 ? (double) typeCount / total * 100 : 0;
                typeData.put("percentage", Math.round(percentage * 100) / 100.0); // 保留两位小数
                
                // 设置类型名称
                switch (typeVal) {
                    case 1:
                        typeData.put("typeName", "问题反馈");
                        break;
                    case 2:
                        typeData.put("typeName", "功能建议");
                        break;
                    case 3:
                        typeData.put("typeName", "投诉");
                        break;
                    case 4:
                        typeData.put("typeName", "其他");
                        break;
                    default:
                        typeData.put("typeName", "未知");
                }
                
                typeDistribution.add(typeData);
            }
            stats.put("typeDistribution", typeDistribution);
            
        } catch (Exception e) {
            log.error("获取反馈统计数据失败", e);
            // 出错时返回空统计
            stats.put("total", 0);
            stats.put("pending", 0);
            stats.put("processing", 0);
            stats.put("replied", 0);
            stats.put("closed", 0);
            stats.put("todayCount", 0);
            stats.put("weekCount", 0);
            stats.put("monthCount", 0);
            stats.put("typeDistribution", new ArrayList<>());
        }
        
        return stats;
    }
    
    /**
     * 将实体转换为VO对象
     */
    private UserFeedbackVO convertToVO(UserFeedback feedback) {
        UserFeedbackVO vo = new UserFeedbackVO();
        BeanUtils.copyProperties(feedback, vo);
        
        // 处理图片列表
        if (StringUtils.isNotBlank(feedback.getImages())) {
            try {
                vo.setImages(JSON.parseArray(feedback.getImages(), String.class));
            } catch (Exception e) {
                vo.setImages(new ArrayList<>());
            }
        } else {
            vo.setImages(new ArrayList<>());
        }
        
        // 设置类型名称
        switch (feedback.getType()) {
            case 1:
                vo.setTypeName("问题反馈");
                break;
            case 2:
                vo.setTypeName("功能建议");
                break;
            case 3:
                vo.setTypeName("投诉");
                break;
            case 4:
                vo.setTypeName("其他");
                break;
            default:
                vo.setTypeName("未知");
        }
        
        // 设置状态名称
        switch (feedback.getStatus()) {
            case 0:
                vo.setStatusName("待处理");
                break;
            case 1:
                vo.setStatusName("处理中");
                break;
            case 2:
                vo.setStatusName("已回复");
                break;
            case 3:
                vo.setStatusName("已关闭");
                break;
            default:
                vo.setStatusName("未知");
        }
        
        return vo;
    }

    /**
     * 将实体转换为简化版VO对象(用于列表展示)
     */
    private UserFeedbackVO convertSimpleVO(UserFeedback feedback) {
        UserFeedbackVO vo = new UserFeedbackVO();
        vo.setId(feedback.getId());
        vo.setUserId(feedback.getUserId());
        vo.setType(feedback.getType());
        vo.setTitle(feedback.getTitle());
        vo.setStatus(feedback.getStatus());
        vo.setCreatedAt(feedback.getCreatedAt());
        
        // 设置类型名称
        switch (feedback.getType()) {
            case 1:
                vo.setTypeName("问题反馈");
                break;
            case 2:
                vo.setTypeName("功能建议");
                break;
            case 3:
                vo.setTypeName("投诉");
                break;
            case 4:
                vo.setTypeName("其他");
                break;
            default:
                vo.setTypeName("未知");
        }
        
        // 设置状态名称
        switch (feedback.getStatus()) {
            case 0:
                vo.setStatusName("待处理");
                break;
            case 1:
                vo.setStatusName("处理中");
                break;
            case 2:
                vo.setStatusName("已回复");
                break;
            case 3:
                vo.setStatusName("已关闭");
                break;
            default:
                vo.setStatusName("未知");
        }
        
        return vo;
    }
} 