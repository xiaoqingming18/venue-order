package com.xcl.venueserver.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xcl.venueserver.common.utils.CurrentUser;
import com.xcl.venueserver.common.utils.Result;
import com.xcl.venueserver.dto.FeedbackDTO;
import com.xcl.venueserver.entity.UserFeedback;
import com.xcl.venueserver.service.UserFeedbackService;
import com.xcl.venueserver.vo.UserFeedbackVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 用户反馈控制器
 */
@Slf4j
@RestController
@RequestMapping({"/api/user/feedback", "/user/feedback"})
public class UserFeedbackController {

    private final UserFeedbackService userFeedbackService;
    private final CurrentUser currentUser;

    public UserFeedbackController(UserFeedbackService userFeedbackService, CurrentUser currentUser) {
        this.userFeedbackService = userFeedbackService;
        this.currentUser = currentUser;
    }

    /**
     * 提交反馈
     */
    @PostMapping("/submit")
    public Result<Long> submitFeedback(@RequestBody @Valid FeedbackDTO feedbackDTO) {
        try {
            Long userId = currentUser.getUserId();
            if (userId == null) {
                return Result.error(401, "用户未登录或登录已过期");
            }
            
            Long feedbackId = userFeedbackService.submitFeedback(userId, feedbackDTO);
            return Result.success(feedbackId, "反馈提交成功");
        } catch (Exception e) {
            log.error("提交反馈失败", e);
            return Result.error("提交反馈失败：" + e.getMessage());
        }
    }

    /**
     * 获取反馈详情
     */
    @GetMapping("/detail/{id}")
    public Result<UserFeedbackVO> getFeedbackDetail(@PathVariable Long id) {
        try {
            Long userId = currentUser.getUserId();
            if (userId == null) {
                return Result.error(401, "用户未登录或登录已过期");
            }
            
            UserFeedbackVO feedbackVO = userFeedbackService.getFeedbackDetail(id);
            if (feedbackVO == null) {
                return Result.error("反馈不存在");
            }
            
            // 验证是否是当前用户的反馈
            if (!feedbackVO.getUserId().equals(userId) && currentUser.getUserRole() != 2) {
                return Result.error(403, "无权查看此反馈");
            }
            
            return Result.success(feedbackVO);
        } catch (Exception e) {
            log.error("获取反馈详情失败", e);
            return Result.error("获取反馈详情失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户反馈列表
     */
    @GetMapping("/list")
    public Result<Page<UserFeedbackVO>> getUserFeedbackList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            Long userId = currentUser.getUserId();
            log.info("获取用户反馈列表，用户ID: {}", userId);
            
            if (userId == null) {
                log.warn("未能获取到用户ID，请求头: {}", currentUser.toString());
                return Result.error(401, "用户未登录或登录已过期");
            }
            
            try {
                Page<UserFeedback> page = new Page<>(pageNum, pageSize);
                log.info("开始查询用户反馈，用户ID: {}, 页码: {}, 每页大小: {}", userId, pageNum, pageSize);
                Page<UserFeedbackVO> feedbackPage = userFeedbackService.getUserFeedbackList(userId, page);
                log.info("查询用户反馈成功，总条数: {}", feedbackPage.getTotal());
                
                return Result.success(feedbackPage);
            } catch (Exception e) {
                log.error("查询用户反馈过程中出错: ", e);
                return Result.error("获取用户反馈列表失败：" + e.getMessage());
            }
        } catch (Exception e) {
            log.error("获取用户反馈列表失败", e);
            return Result.error("获取用户反馈列表失败：" + e.getMessage());
        }
    }

    /**
     * 获取所有反馈列表（管理员使用）
     */
    @GetMapping("/admin/list")
    public Result<Page<UserFeedbackVO>> getAllFeedbackList(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            Long userId = currentUser.getUserId();
            if (userId == null) {
                return Result.error(401, "用户未登录或登录已过期");
            }
            
            // 验证是否为管理员
            if (currentUser.getUserRole() != 2) {
                return Result.error(403, "无权访问此接口");
            }
            
            Page<UserFeedback> page = new Page<>(pageNum, pageSize);
            Page<UserFeedbackVO> feedbackPage = userFeedbackService.getAllFeedbackList(status, type, keyword, page);
            
            return Result.success(feedbackPage);
        } catch (Exception e) {
            log.error("获取所有反馈列表失败", e);
            return Result.error("获取所有反馈列表失败：" + e.getMessage());
        }
    }

    /**
     * 更新反馈状态（管理员使用）
     */
    @PostMapping("/admin/update-status")
    public Result<Boolean> updateFeedbackStatus(
            @RequestParam Long id,
            @RequestParam Integer status) {
        try {
            Long userId = currentUser.getUserId();
            if (userId == null) {
                return Result.error(401, "用户未登录或登录已过期");
            }
            
            // 验证是否为管理员
            if (currentUser.getUserRole() != 2) {
                return Result.error(403, "无权访问此接口");
            }
            
            boolean success = userFeedbackService.updateFeedbackStatus(id, status);
            if (success) {
                return Result.success(true, "更新状态成功");
            } else {
                return Result.error("更新状态失败");
            }
        } catch (Exception e) {
            log.error("更新反馈状态失败", e);
            return Result.error("更新反馈状态失败：" + e.getMessage());
        }
    }
} 