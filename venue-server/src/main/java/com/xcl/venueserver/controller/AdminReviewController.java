package com.xcl.venueserver.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xcl.venueserver.common.utils.CurrentUser;
import com.xcl.venueserver.common.utils.Result;
import com.xcl.venueserver.dto.ReviewAuditDTO;
import com.xcl.venueserver.dto.ReviewBanDTO;
import com.xcl.venueserver.dto.ReviewReportAuditDTO;
import com.xcl.venueserver.dto.VenueReviewReplyDTO;
import com.xcl.venueserver.entity.VenueReviewBan;
import com.xcl.venueserver.entity.VenueReviewReply;
import com.xcl.venueserver.service.VenueReviewBanService;
import com.xcl.venueserver.service.VenueReviewReportService;
import com.xcl.venueserver.service.VenueReviewService;
import com.xcl.venueserver.vo.VenueReviewReportVO;
import com.xcl.venueserver.vo.VenueReviewStatsVO;
import com.xcl.venueserver.vo.VenueReviewVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 管理员评价管理控制器
 */
@RestController
@RequestMapping("/api/admin/reviews")
@Validated
public class AdminReviewController {

    @Resource
    private VenueReviewService venueReviewService;

    @Resource
    private CurrentUser currentUser;
    
    @Resource
    private VenueReviewReportService venueReviewReportService;
    
    @Resource
    private VenueReviewBanService venueReviewBanService;

    /**
     * 分页获取所有评价
     */
    @GetMapping
    public Result<IPage<VenueReviewVO>> pageReviews(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long venueId,
            @RequestParam(required = false) Integer status) {

        // 检查是否是管理员
        if (!currentUser.isAdmin()) {
            return Result.error("无权访问");
        }

        IPage<VenueReviewVO> reviews = venueReviewService.pageReviews(page, size, userId, venueId, status);
        return Result.success(reviews);
    }

    /**
     * 封禁评价
     */
    @PostMapping("/{id}/ban")
    public Result<VenueReviewVO> banReview(
            @PathVariable Long id,
            @Validated @RequestBody ReviewBanDTO dto) {

        // 检查是否是管理员
        if (!currentUser.isAdmin()) {
            return Result.error("无权操作");
        }

        try {
            Long adminId = currentUser.getUserId();
            VenueReviewVO review = venueReviewBanService.banReview(id, dto, adminId, null);
            return Result.success(review);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 解除评价封禁
     */
    @PostMapping("/{id}/unban")
    public Result<VenueReviewVO> unbanReview(@PathVariable Long id) {

        // 检查是否是管理员
        if (!currentUser.isAdmin()) {
            return Result.error("无权操作");
        }

        try {
            Long adminId = currentUser.getUserId();
            VenueReviewVO review = venueReviewBanService.unbanReview(id, adminId);
            return Result.success(review);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取评价封禁记录
     */
    @GetMapping("/{id}/ban-record")
    public Result<VenueReviewBan> getBanRecord(@PathVariable Long id) {

        // 检查是否是管理员
        if (!currentUser.isAdmin()) {
            return Result.error("无权访问");
        }

        VenueReviewBan ban = venueReviewBanService.getBanByReviewId(id);
        return Result.success(ban);
    }

    /**
     * 分页获取所有封禁记录
     */
    @GetMapping("/ban-records")
    public Result<IPage<VenueReviewBan>> pageBanRecords(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        // 检查是否是管理员
        if (!currentUser.isAdmin()) {
            return Result.error("无权访问");
        }

        IPage<VenueReviewBan> records = venueReviewBanService.pageBanRecords(page, size);
        return Result.success(records);
    }

    /**
     * 分页获取所有举报记录
     */
    @GetMapping("/reports")
    public Result<IPage<VenueReviewReportVO>> pageReports(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {

        // 检查是否是管理员
        if (!currentUser.isAdmin()) {
            return Result.error("无权访问");
        }

        IPage<VenueReviewReportVO> reports = venueReviewReportService.pageReports(page, size, status);
        return Result.success(reports);
    }

    /**
     * 获取举报详情
     */
    @GetMapping("/reports/{id}")
    public Result<VenueReviewReportVO> getReportDetail(@PathVariable Long id) {

        // 检查是否是管理员
        if (!currentUser.isAdmin()) {
            return Result.error("无权访问");
        }

        VenueReviewReportVO report = venueReviewReportService.getReportById(id);
        if (report == null) {
            return Result.error("举报记录不存在");
        }
        return Result.success(report);
    }

    /**
     * 审核举报
     */
    @PutMapping("/reports/{id}/audit")
    public Result<VenueReviewReportVO> auditReport(
            @PathVariable Long id,
            @Validated @RequestBody ReviewReportAuditDTO dto) {

        // 检查是否是管理员
        if (!currentUser.isAdmin()) {
            return Result.error("无权操作");
        }

        try {
            Long adminId = currentUser.getUserId();
            VenueReviewReportVO report = venueReviewReportService.auditReport(id, dto, adminId);
            return Result.success(report);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 管理员回复评价
     */
    @PostMapping("/{id}/admin-reply")
    public Result<VenueReviewReply> adminReplyReview(
            @PathVariable Long id,
            @Validated @RequestBody VenueReviewReplyDTO dto) {

        // 检查是否是管理员
        if (!currentUser.isAdmin()) {
            return Result.error("无权操作");
        }

        try {
            Long adminId = currentUser.getUserId();
            VenueReviewReply reply = venueReviewService.replyReview(id, dto, adminId, true);
            return Result.success(reply);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 管理员删除评价
     */
    @DeleteMapping("/{id}")
    public Result<Void> adminDeleteReview(@PathVariable Long id) {
        // 检查是否是管理员
        if (!currentUser.isAdmin()) {
            return Result.error("无权操作");
        }

        try {
            // 管理员有权删除任何评价，所以这里直接使用管理员ID
            boolean success = venueReviewService.removeById(id);
            if (success) {
                return Result.success();
            } else {
                return Result.error("删除评价失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取所有场馆评价统计数据
     */
    @GetMapping("/stats")
    public Result<IPage<VenueReviewStatsVO>> getAllVenueReviewStats(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        // 检查是否是管理员
        if (!currentUser.isAdmin()) {
            return Result.error("无权访问");
        }

        IPage<VenueReviewStatsVO> stats = venueReviewService.pageVenueReviewStats(page, size);
        return Result.success(stats);
    }
} 