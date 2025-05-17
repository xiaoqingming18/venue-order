package com.xcl.venueserver.controller;

import com.xcl.venueserver.common.utils.CurrentUser;
import com.xcl.venueserver.common.utils.Result;
import com.xcl.venueserver.dto.FeedbackReplyDTO;
import com.xcl.venueserver.service.FeedbackReplyService;
import com.xcl.venueserver.vo.FeedbackReplyVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 反馈回复控制器
 */
@Slf4j
@RestController
@RequestMapping({"/api/feedback/reply", "/feedback/reply"})
public class FeedbackReplyController {

    private final FeedbackReplyService feedbackReplyService;
    private final CurrentUser currentUser;

    public FeedbackReplyController(FeedbackReplyService feedbackReplyService, CurrentUser currentUser) {
        this.feedbackReplyService = feedbackReplyService;
        this.currentUser = currentUser;
    }

    /**
     * 管理员回复反馈
     */
    @PostMapping("/admin/reply")
    public Result<Long> replyFeedback(@RequestBody @Valid FeedbackReplyDTO replyDTO) {
        try {
            Long userId = currentUser.getUserId();
            if (userId == null) {
                return Result.error(401, "用户未登录或登录已过期");
            }
            
            // 验证是否为管理员
            if (currentUser.getUserRole() != 2) {
                return Result.error(403, "无权访问此接口");
            }
            
            Long replyId = feedbackReplyService.replyFeedback(userId, replyDTO);
            return Result.success(replyId, "回复成功");
        } catch (Exception e) {
            log.error("回复反馈失败", e);
            return Result.error("回复反馈失败：" + e.getMessage());
        }
    }

    /**
     * 获取反馈回复列表
     */
    @GetMapping("/list/{feedbackId}")
    public Result<List<FeedbackReplyVO>> getFeedbackReplies(@PathVariable Long feedbackId) {
        try {
            Long userId = currentUser.getUserId();
            if (userId == null) {
                return Result.error(401, "用户未登录或登录已过期");
            }
            
            List<FeedbackReplyVO> replies = feedbackReplyService.getFeedbackReplies(feedbackId);
            return Result.success(replies);
        } catch (Exception e) {
            log.error("获取反馈回复列表失败", e);
            return Result.error("获取反馈回复列表失败：" + e.getMessage());
        }
    }
} 