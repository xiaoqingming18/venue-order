package com.xcl.venueserver.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xcl.venueserver.common.utils.CurrentUser;
import com.xcl.venueserver.common.utils.Result;
import com.xcl.venueserver.dto.ReviewAuditDTO;
import com.xcl.venueserver.dto.ReviewReportDTO;
import com.xcl.venueserver.dto.VenueReviewDTO;
import com.xcl.venueserver.dto.VenueReviewReplyDTO;
import com.xcl.venueserver.entity.VenueReviewReply;
import com.xcl.venueserver.service.MinioService;
import com.xcl.venueserver.service.VenueReviewReportService;
import com.xcl.venueserver.service.VenueReviewService;
import com.xcl.venueserver.vo.VenueReviewStatsVO;
import com.xcl.venueserver.vo.VenueReviewVO;
import com.xcl.venueserver.vo.VenueReviewReportVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 场馆评价控制器
 */
@RestController
@RequestMapping("/api/reviews")
@Validated
public class VenueReviewController {

    @Resource
    private VenueReviewService venueReviewService;

    @Resource
    private CurrentUser currentUser;

    @Resource
    private MinioService minioService;

    @Resource
    private VenueReviewReportService venueReviewReportService;

    /**
     * 创建评价
     */
    @PostMapping
    public Result<VenueReviewVO> createReview(@Validated @RequestBody VenueReviewDTO dto) {
        try {
            Long userId = currentUser.getUserId();
            if (userId == null) {
                return Result.error("请先登录");
            }
            VenueReviewVO review = venueReviewService.createReview(dto, userId);
            return Result.success(review);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取评价详情
     */
    @GetMapping("/{id}")
    public Result<VenueReviewVO> getReviewById(@PathVariable Long id) {
        VenueReviewVO review = venueReviewService.getReviewById(id);
        if (review == null) {
            return Result.error("评价不存在");
        }
        return Result.success(review);
    }

    /**
     * 根据订单ID获取评价
     */
    @GetMapping("/order/{orderId}")
    public Result<VenueReviewVO> getReviewByOrderId(@PathVariable Long orderId) {
        Long userId = currentUser.getUserId();
        if (userId == null) {
            return Result.error("请先登录");
        }
        
        VenueReviewVO review = venueReviewService.getReviewByOrderId(orderId);
        if (review == null) {
            // 如果评价不存在，直接返回空数据，不返回错误
            return Result.success(null);
        }
        return Result.success(review);
    }

    /**
     * 修改评价
     */
    @PutMapping("/{id}")
    public Result<VenueReviewVO> updateReview(
            @PathVariable Long id,
            @Validated @RequestBody VenueReviewDTO dto) {
        try {
            Long userId = currentUser.getUserId();
            if (userId == null) {
                return Result.error("请先登录");
            }
            VenueReviewVO review = venueReviewService.updateReview(id, dto, userId);
            return Result.success(review);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除评价
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteReview(@PathVariable Long id) {
        try {
            Long userId = currentUser.getUserId();
            if (userId == null) {
                return Result.error("请先登录");
            }
            boolean success = venueReviewService.deleteReview(id, userId);
            if (success) {
                return Result.success();
            } else {
                return Result.error("删除评价失败");
            }
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取我的评价列表
     */
    @GetMapping("/my")
    public Result<List<VenueReviewVO>> getMyReviews() {
        Long userId = currentUser.getUserId();
        if (userId == null) {
            return Result.error("请先登录");
        }
        List<VenueReviewVO> reviews = venueReviewService.getUserReviews(userId);
        return Result.success(reviews);
    }

    /**
     * 获取场馆评价列表
     */
    @GetMapping("/venue/{venueId}")
    public Result<IPage<VenueReviewVO>> getVenueReviews(
            @PathVariable Long venueId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        IPage<VenueReviewVO> reviews = venueReviewService.getVenueReviews(venueId, page, size);
        return Result.success(reviews);
    }

    /**
     * 回复评价
     */
    @PostMapping("/{id}/replies")
    public Result<VenueReviewReply> replyReview(
            @PathVariable Long id,
            @Validated @RequestBody VenueReviewReplyDTO dto) {
        try {
            Long userId = currentUser.getUserId();
            if (userId == null) {
                return Result.error("请先登录");
            }
            boolean isAdmin = currentUser.isAdmin();
            VenueReviewReply reply = venueReviewService.replyReview(id, dto, userId, isAdmin);
            return Result.success(reply);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除回复
     */
    @DeleteMapping("/replies/{replyId}")
    public Result<Void> deleteReply(@PathVariable Long replyId) {
        try {
            Long userId = currentUser.getUserId();
            if (userId == null) {
                return Result.error("请先登录");
            }
            boolean isAdmin = currentUser.isAdmin();
            boolean success = venueReviewService.deleteReply(replyId, userId, isAdmin);
            if (success) {
                return Result.success();
            } else {
                return Result.error("删除回复失败");
            }
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 上传评价图片
     */
    @PostMapping("/upload-image")
    public Result<Map<String, String>> uploadReviewImage(@RequestParam("file") MultipartFile file) {
        try {
            // 检查用户是否登录
            Long userId = currentUser.getUserId();
            if (userId == null) {
                return Result.error("请先登录");
            }

            // 上传图片到MinIO
            String fileUrl = minioService.uploadFile(file);

            // 构建返回结果
            Map<String, String> data = new HashMap<>(2);
            data.put("fileName", file.getOriginalFilename());
            data.put("fileUrl", fileUrl);

            return Result.success(data);
        } catch (Exception e) {
            return Result.error("上传图片失败: " + e.getMessage());
        }
    }

    /**
     * 获取场馆评价统计数据
     */
    @GetMapping("/stats/venue/{venueId}")
    public Result<VenueReviewStatsVO> getVenueReviewStats(@PathVariable Long venueId) {
        VenueReviewStatsVO stats = venueReviewService.getVenueReviewStats(venueId);
        return Result.success(stats);
    }

    /**
     * 举报评价
     */
    @PostMapping("/{id}/report")
    public Result<VenueReviewReportVO> reportReview(
            @PathVariable Long id,
            @Validated @RequestBody ReviewReportDTO dto) {
        try {
            Long userId = currentUser.getUserId();
            if (userId == null) {
                return Result.error("请先登录");
            }
            VenueReviewReportVO report = venueReviewReportService.createReport(id, dto, userId);
            return Result.success(report);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取我的举报列表
     */
    @GetMapping("/my-reports")
    public Result<IPage<VenueReviewReportVO>> getMyReports(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        try {
            Long userId = currentUser.getUserId();
            if (userId == null) {
                return Result.error("请先登录");
            }
            IPage<VenueReviewReportVO> reports = venueReviewReportService.getUserReports(userId, page, size);
            return Result.success(reports);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }
} 