package com.xcl.venueserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xcl.venueserver.dto.FeedbackReplyDTO;
import com.xcl.venueserver.entity.FeedbackReply;
import com.xcl.venueserver.vo.FeedbackReplyVO;

import java.util.List;

/**
 * 反馈回复服务接口
 */
public interface FeedbackReplyService extends IService<FeedbackReply> {
    
    /**
     * 管理员回复反馈
     *
     * @param adminId 管理员ID
     * @param replyDTO 回复信息
     * @return 回复ID
     */
    Long replyFeedback(Long adminId, FeedbackReplyDTO replyDTO);
    
    /**
     * 获取反馈的所有回复
     *
     * @param feedbackId 反馈ID
     * @return 回复列表
     */
    List<FeedbackReplyVO> getFeedbackReplies(Long feedbackId);
} 