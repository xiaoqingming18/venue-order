package com.xcl.venueserver.service;

import com.xcl.venueserver.dto.LoginDTO;
import com.xcl.venueserver.dto.RegisterDTO;
import com.xcl.venueserver.entity.User;
import com.xcl.venueserver.vo.UserVO;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户登录
     * @param loginDTO 登录信息
     * @return token字符串
     */
    String login(LoginDTO loginDTO);

    /**
     * 用户注册
     * @param registerDTO 注册信息
     * @return 注册成功的用户
     */
    User register(RegisterDTO registerDTO);

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户实体
     */
    User getByUsername(String username);
    
    /**
     * 根据用户ID获取用户信息（非敏感）
     * @param userId 用户ID
     * @return 用户信息视图对象
     */
    UserVO getUserInfoById(Long userId);
} 