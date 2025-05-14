package com.xcl.venueserver.controller;

import com.xcl.venueserver.common.utils.CurrentUser;
import com.xcl.venueserver.common.utils.Result;
import com.xcl.venueserver.service.UserService;
import com.xcl.venueserver.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户控制器
 */
@Slf4j
@RestController
@RequestMapping({"/api/user", "/user"})
public class UserController {

    private final UserService userService;
    private final CurrentUser currentUser;

    public UserController(UserService userService, CurrentUser currentUser) {
        this.userService = userService;
        this.currentUser = currentUser;
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public Result<UserVO> getUserInfo() {
        try {
            // 从token中获取用户ID
            Long userId = currentUser.getUserId();
            if (userId == null) {
                return Result.error(401, "用户未登录或登录已过期");
            }
            
            // 获取用户信息
            UserVO userVO = userService.getUserInfoById(userId);
            if (userVO == null) {
                return Result.error("用户不存在");
            }
            
            return Result.success(userVO);
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return Result.error("获取用户信息失败：" + e.getMessage());
        }
    }
} 