package com.xcl.venueserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xcl.venueserver.dto.FeedbackReplyDTO;
import com.xcl.venueserver.entity.FeedbackReply;
import com.xcl.venueserver.entity.User;
import com.xcl.venueserver.entity.UserFeedback;
import com.xcl.venueserver.entity.UserMessage;
import com.xcl.venueserver.mapper.FeedbackReplyMapper;
import com.xcl.venueserver.mapper.UserFeedbackMapper;
import com.xcl.venueserver.mapper.UserMapper;
import com.xcl.venueserver.mapper.UserMessageMapper;
import com.xcl.venueserver.service.FeedbackReplyService;
import com.xcl.venueserver.vo.FeedbackReplyVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 反馈回复服务实现类
 */
@Service
public class FeedbackReplyServiceImpl extends ServiceImpl<FeedbackReplyMapper, FeedbackReply> implements FeedbackReplyService {

    private final UserFeedbackMapper userFeedbackMapper;
    private final UserMapper userMapper;
    private final UserMessageMapper userMessageMapper;

    public FeedbackReplyServiceImpl(UserFeedbackMapper userFeedbackMapper, UserMapper userMapper, 
                                    UserMessageMapper userMessageMapper) {
        this.userFeedbackMapper = userFeedbackMapper;
        this.userMapper = userMapper;
        this.userMessageMapper = userMessageMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long replyFeedback(Long adminId, FeedbackReplyDTO replyDTO) {
        // 查询反馈信息
        UserFeedback feedback = userFeedbackMapper.selectById(replyDTO.getFeedbackId());
        if (feedback == null) {
            throw new IllegalArgumentException("反馈不存在");
        }
        
        // 创建回复
        FeedbackReply reply = new FeedbackReply();
        reply.setFeedbackId(replyDTO.getFeedbackId());
        reply.setAdminId(adminId);
        reply.setContent(replyDTO.getContent());
        reply.setCreatedAt(LocalDateTime.now());
        reply.setUpdatedAt(LocalDateTime.now());
        
        // 保存回复
        save(reply);
        
        // 更新反馈状态为已回复
        updateFeedbackStatus(feedback.getId(), 2);
        
        // 创建用户消息通知
        createUserMessage(feedback, reply);
        
        return reply.getId();
    }

    /**
     * 更新反馈状态
     */
    private void updateFeedbackStatus(Long feedbackId, Integer status) {
        UserFeedback feedback = userFeedbackMapper.selectById(feedbackId);
        if (feedback != null) {
            feedback.setStatus(status);
            feedback.setUpdatedAt(LocalDateTime.now());
            userFeedbackMapper.updateById(feedback);
        }
    }

    @Override
    public List<FeedbackReplyVO> getFeedbackReplies(Long feedbackId) {
        // 查询回复列表
        LambdaQueryWrapper<FeedbackReply> queryWrapper = Wrappers.<FeedbackReply>lambdaQuery()
                .eq(FeedbackReply::getFeedbackId, feedbackId)
                .orderByAsc(FeedbackReply::getCreatedAt);
        
        List<FeedbackReply> replies = list(queryWrapper);
        if (replies.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 获取所有管理员ID
        List<Long> adminIds = replies.stream()
                .map(FeedbackReply::getAdminId)
                .distinct()
                .collect(Collectors.toList());
        
        // 批量查询管理员信息
        List<User> admins = userMapper.selectBatchIds(adminIds);
        Map<Long, User> adminMap = admins.stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        
        // 转换为VO
        return replies.stream()
                .map(reply -> {
                    FeedbackReplyVO vo = new FeedbackReplyVO();
                    BeanUtils.copyProperties(reply, vo);
                    
                    // 设置管理员信息
                    User admin = adminMap.get(reply.getAdminId());
                    if (admin != null) {
                        vo.setAdminUsername(admin.getUsername());
                        vo.setAdminNickname(admin.getNickname());
                    }
                    
                    return vo;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 创建用户消息通知
     */
    private void createUserMessage(UserFeedback feedback, FeedbackReply reply) {
        UserMessage message = new UserMessage();
        message.setUserId(feedback.getUserId());
        message.setType(3); // 反馈回复类型
        message.setTitle("您的反馈已收到回复");
        message.setContent("您提交的反馈\"" + feedback.getTitle() + "\"已收到回复，请查看详情。");
        message.setRelatedId(feedback.getId());
        message.setIsRead(0);
        message.setCreatedAt(LocalDateTime.now());
        
        userMessageMapper.insert(message);
    }
} 