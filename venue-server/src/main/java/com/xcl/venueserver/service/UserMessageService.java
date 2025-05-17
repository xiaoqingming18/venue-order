package com.xcl.venueserver.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xcl.venueserver.entity.UserMessage;
import com.xcl.venueserver.vo.UserMessageVO;

import java.util.List;

/**
 * 用户消息服务接口
 */
public interface UserMessageService extends IService<UserMessage> {
    
    /**
     * 创建用户消息
     *
     * @param userId 用户ID
     * @param type 消息类型
     * @param title 消息标题
     * @param content 消息内容
     * @param relatedId 关联ID
     * @return 消息ID
     */
    Long createMessage(Long userId, Integer type, String title, String content, Long relatedId);
    
    /**
     * 获取用户未读消息数量
     *
     * @param userId 用户ID
     * @return 未读消息数量
     */
    Integer getUnreadCount(Long userId);
    
    /**
     * 标记消息为已读
     *
     * @param messageId 消息ID
     * @param userId 用户ID（用于权限验证）
     * @return 是否成功
     */
    boolean markAsRead(Long messageId, Long userId);
    
    /**
     * 标记用户所有消息为已读
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean markAllAsRead(Long userId);
    
    /**
     * 分页获取用户消息列表
     *
     * @param userId 用户ID
     * @param page 分页参数
     * @return 消息列表
     */
    Page<UserMessageVO> getUserMessages(Long userId, Page<UserMessage> page);
    
    /**
     * 删除用户消息
     *
     * @param messageId 消息ID
     * @param userId 用户ID（用于权限验证）
     * @return 是否成功
     */
    boolean deleteMessage(Long messageId, Long userId);
} 