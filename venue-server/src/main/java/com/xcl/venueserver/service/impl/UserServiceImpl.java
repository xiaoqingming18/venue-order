package com.xcl.venueserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xcl.venueserver.common.utils.JwtUtils;
import com.xcl.venueserver.common.utils.PasswordUtils;
import com.xcl.venueserver.dto.LoginDTO;
import com.xcl.venueserver.dto.RegisterDTO;
import com.xcl.venueserver.dto.UserQueryDTO;
import com.xcl.venueserver.entity.User;
import com.xcl.venueserver.mapper.UserMapper;
import com.xcl.venueserver.service.UserService;
import com.xcl.venueserver.vo.UserVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserMapper userMapper;
    private final PasswordUtils passwordUtils;
    private final JwtUtils jwtUtils;
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    public UserServiceImpl(UserMapper userMapper, PasswordUtils passwordUtils, JwtUtils jwtUtils) {
        this.userMapper = userMapper;
        this.passwordUtils = passwordUtils;
        this.jwtUtils = jwtUtils;
    }

    @Override
    public String login(LoginDTO loginDTO) {
        // 根据用户名查询用户
        User user = getByUsername(loginDTO.getUsername());
        
        // 用户不存在
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 账号被禁用
        if (user.getStatus() != 1) {
            throw new RuntimeException("账号已被禁用");
        }
        
        // 验证密码
        if (!passwordUtils.matches(loginDTO.getPassword(), user.getSalt(), user.getPassword())) {
            // 记录登录失败次数
            user.setLoginFailCount(user.getLoginFailCount() + 1);
            userMapper.updateById(user);
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 更新登录信息
        user.setLoginFailCount(0);
        user.setLastLoginTime(LocalDateTime.now());
        // 实际项目中这里应该获取真实IP
        user.setLastLoginIp("127.0.0.1");
        userMapper.updateById(user);
        
        log.info("用户{}登录成功，角色: {}", user.getUsername(), user.getRole());
        
        // 生成token, 传入用户角色
        return jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User register(RegisterDTO registerDTO) {
        // 验证用户名是否已存在
        if (getByUsername(registerDTO.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 验证两次密码是否一致
        if (!Objects.equals(registerDTO.getPassword(), registerDTO.getConfirmPassword())) {
            throw new RuntimeException("两次密码输入不一致");
        }
        
        // 验证邮箱是否已存在
        if (registerDTO.getEmail() != null && !registerDTO.getEmail().isEmpty()) {
            User existUser = getOne(new LambdaQueryWrapper<User>().eq(User::getEmail, registerDTO.getEmail()));
            if (existUser != null) {
                throw new RuntimeException("邮箱已被注册");
            }
        }
        
        // 验证手机号是否已存在
        if (registerDTO.getPhone() != null && !registerDTO.getPhone().isEmpty()) {
            User existUser = getOne(new LambdaQueryWrapper<User>().eq(User::getPhone, registerDTO.getPhone()));
            if (existUser != null) {
                throw new RuntimeException("手机号已被注册");
            }
        }
        
        // 创建新用户
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setNickname(registerDTO.getNickname() != null ? registerDTO.getNickname() : registerDTO.getUsername());
        user.setEmail(registerDTO.getEmail());
        user.setPhone(registerDTO.getPhone());
        user.setStatus(1);
        
        // 根据isAdmin字段设置用户角色
        if (registerDTO.getIsAdmin() != null && registerDTO.getIsAdmin()) {
            user.setRole(2); // 设置为管理员
        } else {
            user.setRole(1); // 设置为普通用户
        }
        
        user.setLoginFailCount(0);
        user.setRegisterTime(LocalDateTime.now());
        // 实际项目中这里应该获取真实IP
        user.setRegisterIp("127.0.0.1");
        
        // 设置密码
        String salt = passwordUtils.generateSalt();
        user.setSalt(salt);
        user.setPassword(passwordUtils.encryptPassword(registerDTO.getPassword(), salt));
        
        // 保存用户
        userMapper.insert(user);
        
        return user;
    }

    @Override
    public User getByUsername(String username) {
        return getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    }
    
    @Override
    public UserVO getUserInfoById(Long userId) {
        // 获取用户实体
        User user = getById(userId);
        if (user == null) {
            return null;
        }
        
        // 转换为VO对象，排除敏感信息
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        
        return userVO;
    }
    
    @Override
    public Page<UserVO> getUserList(UserQueryDTO queryDTO) {
        // 构建查询条件
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        // 添加过滤条件
        if (StringUtils.hasText(queryDTO.getUsername())) {
            wrapper.like(User::getUsername, queryDTO.getUsername());
        }
        if (StringUtils.hasText(queryDTO.getNickname())) {
            wrapper.like(User::getNickname, queryDTO.getNickname());
        }
        if (StringUtils.hasText(queryDTO.getPhone())) {
            wrapper.like(User::getPhone, queryDTO.getPhone());
        }
        if (StringUtils.hasText(queryDTO.getEmail())) {
            wrapper.like(User::getEmail, queryDTO.getEmail());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(User::getStatus, queryDTO.getStatus());
        }
        if (queryDTO.getRole() != null) {
            wrapper.eq(User::getRole, queryDTO.getRole());
        }
        
        // 添加排序
        if (StringUtils.hasText(queryDTO.getOrderField())) {
            boolean isAsc = "asc".equalsIgnoreCase(queryDTO.getOrderType());
            
            // 根据字段名动态排序
            if ("username".equals(queryDTO.getOrderField())) {
                wrapper.orderBy(true, isAsc, User::getUsername);
            } else if ("registerTime".equals(queryDTO.getOrderField())) {
                wrapper.orderBy(true, isAsc, User::getRegisterTime);
            } else if ("lastLoginTime".equals(queryDTO.getOrderField())) {
                wrapper.orderBy(true, isAsc, User::getLastLoginTime);
            } else {
                // 默认按注册时间降序
                wrapper.orderByDesc(User::getRegisterTime);
            }
        } else {
            // 默认按注册时间降序
            wrapper.orderByDesc(User::getRegisterTime);
        }
        
        // 执行分页查询
        Page<User> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        Page<User> userPage = page(page, wrapper);
        
        // 转换为VO
        Page<UserVO> voPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        List<UserVO> voList = userPage.getRecords().stream().map(user -> {
            UserVO vo = new UserVO();
            BeanUtils.copyProperties(user, vo);
            return vo;
        }).collect(Collectors.toList());
        voPage.setRecords(voList);
        
        return voPage;
    }
    
    @Override
    public UserVO getUserDetail(Long id) {
        // 获取用户
        User user = getById(id);
        if (user == null) {
            return null;
        }
        
        // 转换为VO
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        
        return userVO;
    }
} 