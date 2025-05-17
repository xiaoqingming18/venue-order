package com.xcl.venueserver.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xcl.venueserver.dto.LoginDTO;
import com.xcl.venueserver.dto.RegisterDTO;
import com.xcl.venueserver.dto.UserAddDTO;
import com.xcl.venueserver.dto.UserEditDTO;
import com.xcl.venueserver.dto.UserQueryDTO;
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
    
    /**
     * 分页查询用户列表
     * @param queryDTO 查询条件
     * @return 用户分页列表
     */
    Page<UserVO> getUserList(UserQueryDTO queryDTO);
    
    /**
     * 获取用户详情
     * @param id 用户ID
     * @return 用户详情
     */
    UserVO getUserDetail(Long id);
    
    /**
     * 添加用户
     * @param userAddDTO 用户添加信息
     * @return 添加后的用户信息
     */
    UserVO addUser(UserAddDTO userAddDTO);
    
    /**
     * 编辑用户
     * @param userEditDTO 用户编辑信息
     * @return 编辑后的用户信息
     */
    UserVO updateUser(UserEditDTO userEditDTO);
} 