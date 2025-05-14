package com.xcl.venueserver.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xcl.venueserver.dto.ReviewAuditDTO;
import com.xcl.venueserver.dto.VenueReviewDTO;
import com.xcl.venueserver.dto.VenueReviewReplyDTO;
import com.xcl.venueserver.entity.VenueReview;
import com.xcl.venueserver.entity.VenueReviewReply;
import com.xcl.venueserver.vo.VenueReviewStatsVO;
import com.xcl.venueserver.vo.VenueReviewVO;

import java.util.List;

/**
 * 场馆评价服务接口
 */
public interface VenueReviewService extends IService<VenueReview> {

    /**
     * 创建评价
     * @param dto 评价DTO
     * @param userId 用户ID
     * @return 评价VO
     */
    VenueReviewVO createReview(VenueReviewDTO dto, Long userId);

    /**
     * 获取评价详情
     * @param id 评价ID
     * @return 评价VO
     */
    VenueReviewVO getReviewById(Long id);

    /**
     * 根据订单ID获取评价
     * @param orderId 订单ID
     * @return 评价VO，如不存在则返回null
     */
    VenueReviewVO getReviewByOrderId(Long orderId);

    /**
     * 更新评价
     * @param id 评价ID
     * @param dto 评价DTO
     * @param userId 用户ID
     * @return 更新后的评价VO
     */
    VenueReviewVO updateReview(Long id, VenueReviewDTO dto, Long userId);

    /**
     * 删除评价
     * @param id 评价ID
     * @param userId 用户ID
     * @return 是否删除成功
     */
    boolean deleteReview(Long id, Long userId);

    /**
     * 分页查询评价
     * @param page 页码
     * @param size 每页大小
     * @param userId 用户ID，可为null
     * @param venueId 场馆ID，可为null
     * @param status 状态，可为null
     * @return 分页数据
     */
    IPage<VenueReviewVO> pageReviews(int page, int size, Long userId, Long venueId, Integer status);

    /**
     * 获取用户评价列表
     * @param userId 用户ID
     * @return 评价列表
     */
    List<VenueReviewVO> getUserReviews(Long userId);

    /**
     * 获取场馆评价列表
     * @param venueId 场馆ID
     * @param page 页码
     * @param size 每页大小
     * @return 评价分页数据
     */
    IPage<VenueReviewVO> getVenueReviews(Long venueId, int page, int size);

    /**
     * 审核评价
     * @param id 评价ID
     * @param dto 审核DTO
     * @param adminId 管理员ID
     * @return 审核后的评价VO
     */
    VenueReviewVO auditReview(Long id, ReviewAuditDTO dto, Long adminId);

    /**
     * 回复评价
     * @param reviewId 评价ID
     * @param dto 回复DTO
     * @param userId 用户ID
     * @param isAdmin 是否管理员
     * @return 回复实体
     */
    VenueReviewReply replyReview(Long reviewId, VenueReviewReplyDTO dto, Long userId, boolean isAdmin);

    /**
     * 删除回复
     * @param replyId 回复ID
     * @param userId 用户ID
     * @param isAdmin 是否管理员
     * @return 是否删除成功
     */
    boolean deleteReply(Long replyId, Long userId, boolean isAdmin);

    /**
     * 获取场馆评价统计数据
     * @param venueId 场馆ID
     * @return 统计数据
     */
    VenueReviewStatsVO getVenueReviewStats(Long venueId);

    /**
     * 分页获取场馆评价统计数据
     * @param page 页码
     * @param size 每页大小
     * @return 分页统计数据
     */
    IPage<VenueReviewStatsVO> pageVenueReviewStats(int page, int size);
} 