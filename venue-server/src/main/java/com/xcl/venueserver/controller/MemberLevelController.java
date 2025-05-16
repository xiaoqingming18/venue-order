package com.xcl.venueserver.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xcl.venueserver.dto.MemberLevelDTO;
import com.xcl.venueserver.entity.MemberLevel;
import com.xcl.venueserver.service.MemberLevelService;
import com.xcl.venueserver.vo.MemberLevelVO;
import com.xcl.venueserver.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

/**
 * 会员等级控制器
 */
@RestController
@RequestMapping("/api")
public class MemberLevelController {

    @Autowired
    private MemberLevelService memberLevelService;

    /**
     * 获取用户会员等级信息
     */
    @GetMapping("/user/member/level")
    public ResultVO<MemberLevelVO> getUserMemberLevel(HttpServletRequest request) {
        // 从当前登录用户中获取用户ID
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResultVO.error("用户未登录");
        }
        
        MemberLevelVO levelVO = memberLevelService.getUserMemberLevel(userId);
        return ResultVO.success(levelVO);
    }

    /**
     * 获取所有会员等级(用户端)
     */
    @GetMapping("/user/member/levels")
    public ResultVO<List<MemberLevel>> getAllLevels() {
        List<MemberLevel> levels = memberLevelService.getAllEnabledLevels();
        return ResultVO.success(levels);
    }

    /**
     * 分页获取会员等级(管理端)
     */
    @GetMapping("/admin/member/levels")
    public ResultVO<IPage<MemberLevel>> getLevelsByPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        IPage<MemberLevel> pageResult = memberLevelService.getLevelsByPage(page, size);
        return ResultVO.success(pageResult);
    }

    /**
     * 根据ID获取会员等级
     */
    @GetMapping("/admin/member/levels/{id}")
    public ResultVO<MemberLevel> getLevelById(@PathVariable Integer id) {
        MemberLevel level = memberLevelService.getLevelById(id);
        if (level == null) {
            return ResultVO.error("会员等级不存在");
        }
        return ResultVO.success(level);
    }

    /**
     * 添加或更新会员等级
     */
    @PostMapping("/admin/member/levels")
    public ResultVO<Void> saveOrUpdateLevel(@Valid @RequestBody MemberLevelDTO memberLevelDTO) {
        boolean result = memberLevelService.saveOrUpdateLevel(memberLevelDTO);
        if (result) {
            return ResultVO.success("保存成功");
        } else {
            return ResultVO.error("保存失败，可能等级值已存在");
        }
    }

    /**
     * 更新会员等级状态
     */
    @PutMapping("/admin/member/levels/{id}/status/{status}")
    public ResultVO<Void> updateLevelStatus(
            @PathVariable Integer id,
            @PathVariable Integer status) {
        boolean result = memberLevelService.updateLevelStatus(id, status);
        if (result) {
            return ResultVO.success("更新成功");
        } else {
            return ResultVO.error("更新失败，会员等级不存在");
        }
    }

    /**
     * 删除会员等级
     */
    @DeleteMapping("/admin/member/levels/{id}")
    public ResultVO<Void> deleteLevel(@PathVariable Integer id) {
        boolean result = memberLevelService.deleteLevel(id);
        if (result) {
            return ResultVO.success("删除成功");
        } else {
            return ResultVO.error("删除失败，可能是最低等级或会员等级不存在");
        }
    }

    /**
     * 从请求中获取用户ID
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        // 这里应该从你的用户认证系统中获取当前登录用户的ID
        // 假设已通过拦截器校验了用户登录状态，并将用户ID存在了request的attribute中
        Object userId = request.getAttribute("userId");
        return userId != null ? Long.valueOf(userId.toString()) : null;
    }
} 