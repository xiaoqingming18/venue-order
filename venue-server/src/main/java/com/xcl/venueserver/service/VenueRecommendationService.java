package com.xcl.venueserver.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xcl.venueserver.dto.BrowseHistoryDTO;
import com.xcl.venueserver.dto.RecommendationFeedbackDTO;
import com.xcl.venueserver.entity.VenueRecommendation;
import com.xcl.venueserver.vo.VenueRecommendationVO;

import java.util.List;

/**
 * 场馆推荐服务接口
 */
public interface VenueRecommendationService extends IService<VenueRecommendation> {

    /**
     * 记录用户浏览历史
     *
     * @param browseHistoryDTO 浏览历史DTO
     * @return 是否成功
     */
    boolean recordBrowseHistory(BrowseHistoryDTO browseHistoryDTO);

    /**
     * 获取首页推荐场馆
     *
     * @param userId 用户ID
     * @param limit  限制数量
     * @return 推荐场馆列表
     */
    List<VenueRecommendationVO> getHomeRecommendations(Long userId, Integer limit);

    /**
     * 获取详情页相似场馆推荐
     *
     * @param userId  用户ID
     * @param venueId 当前场馆ID
     * @param limit   限制数量
     * @return 相似场馆列表
     */
    List<VenueRecommendationVO> getSimilarVenueRecommendations(Long userId, Long venueId, Integer limit);

    /**
     * 分页获取用户的推荐场馆列表
     *
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 分页推荐场馆列表
     */
    IPage<VenueRecommendationVO> getPagedRecommendations(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 提交推荐反馈
     *
     * @param userId       用户ID
     * @param feedbackDTO 反馈DTO
     * @return 是否成功
     */
    boolean submitFeedback(Long userId, RecommendationFeedbackDTO feedbackDTO);

    /**
     * 标记推荐为已点击
     *
     * @param recommendationId 推荐ID
     * @return 是否成功
     */
    boolean markAsClicked(Long recommendationId);

    /**
     * 生成用户推荐（由调度任务调用）
     *
     * @param userId 用户ID
     * @return 生成的推荐数量
     */
    int generateRecommendations(Long userId);
} 