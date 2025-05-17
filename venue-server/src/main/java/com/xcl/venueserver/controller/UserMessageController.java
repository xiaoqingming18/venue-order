package com.xcl.venueserver.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xcl.venueserver.common.utils.CurrentUser;
import com.xcl.venueserver.common.utils.Result;
import com.xcl.venueserver.entity.UserMessage;
import com.xcl.venueserver.service.UserMessageService;
import com.xcl.venueserver.vo.UserMessageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 用户消息控制器
 */
@Slf4j
@RestController
@RequestMapping({"/api/user/message", "/user/message"})
public class UserMessageController {

    private final UserMessageService userMessageService;
    private final CurrentUser currentUser;

    public UserMessageController(UserMessageService userMessageService, CurrentUser currentUser) {
        this.userMessageService = userMessageService;
        this.currentUser = currentUser;
    }

    /**
     * 获取用户未读消息数量
     */
    @GetMapping("/unread-count")
    public Result<Integer> getUnreadCount() {
        try {
            Long userId = currentUser.getUserId();
            if (userId == null) {
                return Result.error(401, "用户未登录或登录已过期");
            }
            
            Integer count = userMessageService.getUnreadCount(userId);
            return Result.success(count);
        } catch (Exception e) {
            log.error("获取未读消息数量失败", e);
            return Result.error("获取未读消息数量失败：" + e.getMessage());
        }
    }

    /**
     * 标记消息为已读
     */
    @PostMapping("/mark-read/{messageId}")
    public Result<Boolean> markAsRead(@PathVariable Long messageId) {
        try {
            Long userId = currentUser.getUserId();
            if (userId == null) {
                return Result.error(401, "用户未登录或登录已过期");
            }
            
            boolean success = userMessageService.markAsRead(messageId, userId);
            if (success) {
                return Result.success(true, "标记成功");
            } else {
                return Result.error("标记失败，消息不存在或无权操作");
            }
        } catch (Exception e) {
            log.error("标记消息已读失败", e);
            return Result.error("标记消息已读失败：" + e.getMessage());
        }
    }

    /**
     * 标记所有消息为已读
     */
    @PostMapping("/mark-all-read")
    public Result<Boolean> markAllAsRead() {
        try {
            Long userId = currentUser.getUserId();
            if (userId == null) {
                return Result.error(401, "用户未登录或登录已过期");
            }
            
            boolean success = userMessageService.markAllAsRead(userId);
            return Result.success(success, "标记全部已读成功");
        } catch (Exception e) {
            log.error("标记全部消息已读失败", e);
            return Result.error("标记全部消息已读失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户消息列表
     */
    @GetMapping("/list")
    public Result<Page<UserMessageVO>> getUserMessages(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            Long userId = currentUser.getUserId();
            if (userId == null) {
                return Result.error(401, "用户未登录或登录已过期");
            }
            
            Page<UserMessage> page = new Page<>(pageNum, pageSize);
            Page<UserMessageVO> messagePage = userMessageService.getUserMessages(userId, page);
            
            return Result.success(messagePage);
        } catch (Exception e) {
            log.error("获取用户消息列表失败", e);
            return Result.error("获取用户消息列表失败：" + e.getMessage());
        }
    }

    /**
     * 删除用户消息
     */
    @DeleteMapping("/delete/{messageId}")
    public Result<Boolean> deleteMessage(@PathVariable Long messageId) {
        try {
            Long userId = currentUser.getUserId();
            if (userId == null) {
                return Result.error(401, "用户未登录或登录已过期");
            }
            
            boolean success = userMessageService.deleteMessage(messageId, userId);
            if (success) {
                return Result.success(true, "删除成功");
            } else {
                return Result.error("删除失败，消息不存在或无权操作");
            }
        } catch (Exception e) {
            log.error("删除用户消息失败", e);
            return Result.error("删除用户消息失败：" + e.getMessage());
        }
    }
} 