package com.xcl.venueserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xcl.venueserver.entity.UserMessage;
import com.xcl.venueserver.mapper.UserMessageMapper;
import com.xcl.venueserver.service.UserMessageService;
import com.xcl.venueserver.vo.UserMessageVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户消息服务实现类
 */
@Service
public class UserMessageServiceImpl extends ServiceImpl<UserMessageMapper, UserMessage> implements UserMessageService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createMessage(Long userId, Integer type, String title, String content, Long relatedId) {
        UserMessage message = new UserMessage();
        message.setUserId(userId);
        message.setType(type);
        message.setTitle(title);
        message.setContent(content);
        message.setRelatedId(relatedId);
        message.setIsRead(0);
        message.setCreatedAt(LocalDateTime.now());
        
        save(message);
        return message.getId();
    }

    @Override
    public Integer getUnreadCount(Long userId) {
        LambdaQueryWrapper<UserMessage> queryWrapper = Wrappers.<UserMessage>lambdaQuery()
                .eq(UserMessage::getUserId, userId)
                .eq(UserMessage::getIsRead, 0);
        
        return Math.toIntExact(count(queryWrapper));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markAsRead(Long messageId, Long userId) {
        LambdaUpdateWrapper<UserMessage> updateWrapper = Wrappers.<UserMessage>lambdaUpdate()
                .eq(UserMessage::getId, messageId)
                .eq(UserMessage::getUserId, userId)
                .set(UserMessage::getIsRead, 1);
        
        return update(updateWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markAllAsRead(Long userId) {
        LambdaUpdateWrapper<UserMessage> updateWrapper = Wrappers.<UserMessage>lambdaUpdate()
                .eq(UserMessage::getUserId, userId)
                .eq(UserMessage::getIsRead, 0)
                .set(UserMessage::getIsRead, 1);
        
        return update(updateWrapper);
    }

    @Override
    public Page<UserMessageVO> getUserMessages(Long userId, Page<UserMessage> page) {
        LambdaQueryWrapper<UserMessage> queryWrapper = Wrappers.<UserMessage>lambdaQuery()
                .eq(UserMessage::getUserId, userId)
                .orderByDesc(UserMessage::getCreatedAt);
        
        Page<UserMessage> messagePage = page(page, queryWrapper);
        
        // 转换为VO
        Page<UserMessageVO> voPage = new Page<>();
        BeanUtils.copyProperties(messagePage, voPage, "records");
        
        List<UserMessageVO> voList = messagePage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteMessage(Long messageId, Long userId) {
        LambdaQueryWrapper<UserMessage> queryWrapper = Wrappers.<UserMessage>lambdaQuery()
                .eq(UserMessage::getId, messageId)
                .eq(UserMessage::getUserId, userId);
        
        return remove(queryWrapper);
    }
    
    /**
     * 将实体转换为VO
     */
    private UserMessageVO convertToVO(UserMessage message) {
        UserMessageVO vo = new UserMessageVO();
        BeanUtils.copyProperties(message, vo);
        vo.setTypeName(UserMessageVO.getTypeName(message.getType()));
        return vo;
    }
} 