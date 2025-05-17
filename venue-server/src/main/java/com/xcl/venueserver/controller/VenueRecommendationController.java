package com.xcl.venueserver.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xcl.venueserver.common.utils.CurrentUser;
import com.xcl.venueserver.common.utils.Result;
import com.xcl.venueserver.dto.BrowseHistoryDTO;
import com.xcl.venueserver.dto.RecommendationFeedbackDTO;
import com.xcl.venueserver.service.VenueRecommendationService;
import com.xcl.venueserver.vo.VenueRecommendationVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 场馆推荐控制器
 */
@Slf4j
@RestController
@RequestMapping({"/api/venue/recommendation", "/venue/recommendation"})
public class VenueRecommendationController {

    private final VenueRecommendationService venueRecommendationService;
    private final CurrentUser currentUser;

    public VenueRecommendationController(VenueRecommendationService venueRecommendationService,
                                        CurrentUser currentUser) {
        this.venueRecommendationService = venueRecommendationService;
        this.currentUser = currentUser;
    }

    /**
     * 记录用户浏览历史
     */
    @PostMapping("/history")
    public Result<Boolean> recordBrowseHistory(@RequestBody @Valid BrowseHistoryDTO browseHistoryDTO) {
        try {
            // 设置用户ID
            if (browseHistoryDTO.getUserId() == null && currentUser.getCurrentUserId() != null) {
                browseHistoryDTO.setUserId(currentUser.getCurrentUserId());
            }
            
            boolean success = venueRecommendationService.recordBrowseHistory(browseHistoryDTO);
            return Result.ok(success);
        } catch (Exception e) {
            log.error("记录浏览历史失败", e);
            return Result.fail("记录浏览历史失败");
        }
    }

    /**
     * 获取首页推荐场馆
     */
    @GetMapping("/home")
    public Result<List<VenueRecommendationVO>> getHomeRecommendations(
            @RequestParam(value = "limit", defaultValue = "6") Integer limit) {
        try {
            Long userId = currentUser.getCurrentUserId();
            List<VenueRecommendationVO> recommendations = 
                    venueRecommendationService.getHomeRecommendations(userId, limit);
            return Result.ok(recommendations);
        } catch (Exception e) {
            log.error("获取首页推荐失败", e);
            return Result.fail("获取首页推荐失败");
        }
    }

    /**
     * 获取详情页相似场馆推荐
     */
    @GetMapping("/similar/{venueId}")
    public Result<List<VenueRecommendationVO>> getSimilarVenueRecommendations(
            @PathVariable Long venueId,
            @RequestParam(value = "limit", defaultValue = "4") Integer limit) {
        try {
            Long userId = currentUser.getCurrentUserId();
            List<VenueRecommendationVO> recommendations = 
                    venueRecommendationService.getSimilarVenueRecommendations(userId, venueId, limit);
            return Result.ok(recommendations);
        } catch (Exception e) {
            log.error("获取相似场馆推荐失败", e);
            return Result.fail("获取相似场馆推荐失败");
        }
    }

    /**
     * 分页获取用户的推荐场馆列表
     */
    @GetMapping("/list")
    public Result<IPage<VenueRecommendationVO>> getPagedRecommendations(
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        try {
            Long userId = currentUser.getCurrentUserId();
            if (userId == null) {
                return Result.fail("用户未登录");
            }
            
            IPage<VenueRecommendationVO> page = 
                    venueRecommendationService.getPagedRecommendations(userId, pageNum, pageSize);
            return Result.ok(page);
        } catch (Exception e) {
            log.error("获取推荐列表失败", e);
            return Result.fail("获取推荐列表失败");
        }
    }

    /**
     * 提交推荐反馈
     */
    @PostMapping("/feedback")
    public Result<Boolean> submitFeedback(@RequestBody @Valid RecommendationFeedbackDTO feedbackDTO) {
        try {
            Long userId = currentUser.getCurrentUserId();
            if (userId == null) {
                return Result.fail("用户未登录");
            }
            
            boolean success = venueRecommendationService.submitFeedback(userId, feedbackDTO);
            return Result.ok(success);
        } catch (Exception e) {
            log.error("提交反馈失败", e);
            return Result.fail("提交反馈失败");
        }
    }

    /**
     * 标记推荐为已点击
     */
    @PostMapping("/click/{recommendationId}")
    public Result<Boolean> markAsClicked(@PathVariable Long recommendationId) {
        try {
            boolean success = venueRecommendationService.markAsClicked(recommendationId);
            return Result.ok(success);
        } catch (Exception e) {
            log.error("标记点击失败", e);
            return Result.fail("标记点击失败");
        }
    }
} 