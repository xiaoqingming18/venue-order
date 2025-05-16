package com.xcl.venueserver.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xcl.venueserver.common.utils.CurrentUser;
import com.xcl.venueserver.common.utils.Result;
import com.xcl.venueserver.dto.UserAddDTO;
import com.xcl.venueserver.dto.UserEditDTO;
import com.xcl.venueserver.dto.UserQueryDTO;
import com.xcl.venueserver.service.UserService;
import com.xcl.venueserver.vo.PageResult;
import com.xcl.venueserver.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员用户管理控制器
 */
@Slf4j
@RestController
@RequestMapping({"/api/admin/user", "/admin/user"})
public class AdminUserController {

    private final UserService userService;
    private final CurrentUser currentUser;

    public AdminUserController(UserService userService, CurrentUser currentUser) {
        this.userService = userService;
        this.currentUser = currentUser;
    }

    /**
     * 校验是否为管理员
     * @return true-是管理员，false-不是管理员
     */
    private boolean checkAdmin() {
        Long userId = currentUser.getUserId();
        if (userId == null) {
            return false;
        }
        
        UserVO user = userService.getUserInfoById(userId);
        return user != null && user.getRole() == 2;
    }

    /**
     * 获取用户列表（分页）
     * @param queryDTO 查询条件
     * @return 用户列表
     */
    @PostMapping("/list")
    public Result<PageResult<UserVO>> getUserList(@RequestBody UserQueryDTO queryDTO) {
        try {
            // 权限检查
            if (!checkAdmin()) {
                return Result.error(403, "无权限访问");
            }
            
            // 查询用户列表
            Page<UserVO> page = userService.getUserList(queryDTO);
            
            // 构建返回结果
            PageResult<UserVO> pageResult = PageResult.fromPage(page);
            
            return Result.success(pageResult);
        } catch (Exception e) {
            log.error("获取用户列表失败", e);
            return Result.error("获取用户列表失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户详情
     * @param id 用户ID
     * @return 用户详情
     */
    @GetMapping("/detail/{id}")
    public Result<UserVO> getUserDetail(@PathVariable Long id) {
        try {
            // 权限检查
            if (!checkAdmin()) {
                return Result.error(403, "无权限访问");
            }
            
            // 获取用户详情
            UserVO userVO = userService.getUserDetail(id);
            
            if (userVO == null) {
                return Result.error("用户不存在");
            }
            
            return Result.success(userVO);
        } catch (Exception e) {
            log.error("获取用户详情失败", e);
            return Result.error("获取用户详情失败：" + e.getMessage());
        }
    }
    
    /**
     * 添加用户
     * @param userAddDTO 用户添加信息
     * @return 添加结果
     */
    @PostMapping("/add")
    public Result<UserVO> addUser(@Validated @RequestBody UserAddDTO userAddDTO) {
        try {
            // 权限检查
            if (!checkAdmin()) {
                return Result.error(403, "无权限访问");
            }
            
            // 添加用户
            UserVO userVO = userService.addUser(userAddDTO);
            
            Result<UserVO> result = Result.success(userVO);
            result.setMessage("添加用户成功");
            return result;
        } catch (Exception e) {
            log.error("添加用户失败", e);
            return Result.error("添加用户失败：" + e.getMessage());
        }
    }
    
    /**
     * 编辑用户
     * @param userEditDTO 用户编辑信息
     * @return 编辑结果
     */
    @PutMapping("/edit")
    public Result<UserVO> updateUser(@Validated @RequestBody UserEditDTO userEditDTO) {
        try {
            // 权限检查
            if (!checkAdmin()) {
                return Result.error(403, "无权限访问");
            }
            
            // 编辑用户
            UserVO userVO = userService.updateUser(userEditDTO);
            
            Result<UserVO> result = Result.success(userVO);
            result.setMessage("编辑用户成功");
            return result;
        } catch (Exception e) {
            log.error("编辑用户失败", e);
            return Result.error("编辑用户失败：" + e.getMessage());
        }
    }
} 