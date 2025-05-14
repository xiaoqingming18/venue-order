package com.xcl.venueserver.controller;

import com.xcl.venueserver.common.utils.CurrentUser;
import com.xcl.venueserver.common.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/test")
public class TestController {

    private final CurrentUser currentUser;

    public TestController(CurrentUser currentUser) {
        this.currentUser = currentUser;
    }

    /**
     * 不需要认证的接口
     */
    @GetMapping("/public")
    public Result<String> publicApi() {
        return Result.success("这是一个公开接口，无需登录");
    }

    /**
     * 需要认证的接口
     */
    @GetMapping("/private")
    public Result<Map<String, Object>> privateApi() {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", currentUser.getUserId());
        data.put("username", currentUser.getUsername());
        data.put("msg", "这是一个需要登录的接口");
        return Result.success(data);
    }
} 