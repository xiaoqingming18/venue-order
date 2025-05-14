package com.xcl.venueserver.controller;

import com.xcl.venueserver.common.utils.CurrentUser;
import com.xcl.venueserver.common.utils.Result;
import com.xcl.venueserver.dto.LoginDTO;
import com.xcl.venueserver.dto.RegisterDTO;
import com.xcl.venueserver.entity.User;
import com.xcl.venueserver.service.UserService;
import com.xcl.venueserver.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 */
@Slf4j
@RestController
@RequestMapping({"/api/auth", "/auth"})
public class AuthController {

    private final UserService userService;
    private final CurrentUser currentUser;

    public AuthController(UserService userService, CurrentUser currentUser) {
        this.userService = userService;
        this.currentUser = currentUser;
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Validated @RequestBody LoginDTO loginDTO) {
        try {
            log.info("用户登录请求: {}", loginDTO.getUsername());
            
            // 登录并获取token
            String token = userService.login(loginDTO);
            log.info("用户{}登录成功，生成token", loginDTO.getUsername());
            
            // 返回token和用户名
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("username", loginDTO.getUsername());
            
            return Result.success(data);
        } catch (Exception e) {
            log.error("登录失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Validated @RequestBody RegisterDTO registerDTO) {
        try {
            // 手动验证email和phone
            if (registerDTO.getEmail() != null && !registerDTO.getEmail().isEmpty() && !isValidEmail(registerDTO.getEmail())) {
                return Result.error("邮箱格式不正确");
            }
            
            if (registerDTO.getPhone() != null && !registerDTO.getPhone().isEmpty() && !isValidPhone(registerDTO.getPhone())) {
                return Result.error("手机号格式不正确");
            }
            
            // 注册用户
            User user = userService.register(registerDTO);
            
            // 生成token并返回
            String token = userService.login(new LoginDTO(){{
                setUsername(user.getUsername());
                setPassword(registerDTO.getPassword());
            }});
            
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("username", user.getUsername());
            
            return Result.success(data);
        } catch (Exception e) {
            log.error("注册失败", e);
            return Result.error(e.getMessage());
        }
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
                return Result.error("用户未登录或登录已过期");
            }
            
            // 获取用户信息
            UserVO userVO = userService.getUserInfoById(userId);
            if (userVO == null) {
                return Result.error("用户不存在");
            }
            
            return Result.success(userVO);
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 验证邮箱格式
     */
    private boolean isValidEmail(String email) {
        return email.matches("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$");
    }
    
    /**
     * 验证手机号格式
     */
    private boolean isValidPhone(String phone) {
        return phone.matches("^1[3-9]\\d{9}$");
    }
} 