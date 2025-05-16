package com.xcl.venueserver.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xcl.venueserver.common.enums.PointSourceTypeEnum;
import com.xcl.venueserver.dto.PointRecordQueryDTO;
import com.xcl.venueserver.service.PointsService;
import com.xcl.venueserver.vo.PointRecordVO;
import com.xcl.venueserver.vo.ResultVO;
import com.xcl.venueserver.vo.UserPointsSummaryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 用户积分控制器
 */
@RestController
@RequestMapping("/api/user/points")
public class UserPointsController {

    @Autowired
    private PointsService pointsService;

    /**
     * 获取用户积分概况
     */
    @GetMapping("/summary")
    public ResultVO<UserPointsSummaryVO> getPointsSummary(HttpServletRequest request) {
        // 从当前登录用户中获取用户ID
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResultVO.error("用户未登录");
        }
        
        UserPointsSummaryVO summary = pointsService.getUserPointsSummary(userId);
        return ResultVO.success(summary);
    }

    /**
     * 获取用户积分记录
     */
    @GetMapping("/records")
    public ResultVO<IPage<PointRecordVO>> getPointRecords(
            PointRecordQueryDTO queryDTO,
            HttpServletRequest request) {
        // 从当前登录用户中获取用户ID
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResultVO.error("用户未登录");
        }
        
        IPage<PointRecordVO> records = pointsService.getUserPointRecords(userId, queryDTO);
        return ResultVO.success(records);
    }

    /**
     * 签到获取积分
     */
    @PostMapping("/sign-in")
    public ResultVO<Integer> signIn(HttpServletRequest request) {
        // 从当前登录用户中获取用户ID
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResultVO.error("用户未登录");
        }
        
        // 签到获取积分
        Integer points = pointsService.addPoints(
                userId, 
                PointSourceTypeEnum.SIGN_IN.code, 
                null, 
                "每日签到", 
                null);
        
        if (points > 0) {
            return ResultVO.success(points, "签到成功，获得" + points + "积分");
        } else {
            return ResultVO.error("签到失败，可能今日已签到");
        }
    }

    /**
     * 查询积分过期情况并处理过期积分
     */
    @PostMapping("/handle-expired")
    public ResultVO<Integer> handleExpiredPoints(HttpServletRequest request) {
        // 从当前登录用户中获取用户ID
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResultVO.error("用户未登录");
        }
        
        // 处理过期积分
        Integer expiredPoints = pointsService.handleExpiredPoints(userId);
        
        if (expiredPoints > 0) {
            return ResultVO.success(expiredPoints, "已处理" + expiredPoints + "过期积分");
        } else {
            return ResultVO.success(0, "暂无过期积分");
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