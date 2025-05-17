package com.xcl.venueserver.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xcl.venueserver.dto.FeedbackDTO;
import com.xcl.venueserver.entity.UserFeedback;
import com.xcl.venueserver.vo.UserFeedbackVO;

import java.util.Map;

/**
 * 用户反馈服务接口
 */
public interface UserFeedbackService extends IService<UserFeedback> {
    
    /**
     * 提交用户反馈
     *
     * @param userId 用户ID
     * @param feedbackDTO 反馈信息
     * @return 反馈ID
     */
    Long submitFeedback(Long userId, FeedbackDTO feedbackDTO);
    
    /**
     * 获取用户反馈详情
     *
     * @param id 反馈ID
     * @return 反馈详情
     */
    UserFeedbackVO getFeedbackDetail(Long id);
    
    /**
     * 分页查询用户的反馈列表
     *
     * @param userId 用户ID
     * @param page 分页参数
     * @return 反馈列表
     */
    Page<UserFeedbackVO> getUserFeedbackList(Long userId, Page<UserFeedback> page);
    
    /**
     * 分页查询所有反馈（管理员使用）
     *
     * @param status 状态筛选，null表示全部
     * @param type 类型筛选，null表示全部
     * @param keyword 关键词搜索（标题、内容）
     * @param page 分页参数
     * @return 反馈列表
     */
    Page<UserFeedbackVO> getAllFeedbackList(Integer status, Integer type, String keyword, Page<UserFeedback> page);
    
    /**
     * 更新反馈状态
     *
     * @param id 反馈ID
     * @param status 新状态
     * @return 是否成功
     */
    boolean updateFeedbackStatus(Long id, Integer status);
    
    /**
     * 获取反馈统计数据
     * @return 统计数据
     */
    Map<String, Object> getFeedbackStats();
} 