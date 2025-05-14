package com.xcl.venueserver.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xcl.venueserver.dto.ReviewBanDTO;
import com.xcl.venueserver.entity.VenueReviewBan;
import com.xcl.venueserver.vo.VenueReviewVO;

/**
 * 评价封禁服务接口
 */
public interface VenueReviewBanService extends IService<VenueReviewBan> {

    /**
     * 封禁评价
     * @param reviewId 评价ID
     * @param dto 封禁DTO
     * @param adminId 管理员ID
     * @param reportId 举报ID，可为null
     * @return 封禁后的评价VO
     */
    VenueReviewVO banReview(Long reviewId, ReviewBanDTO dto, Long adminId, Long reportId);

    /**
     * 解除评价封禁
     * @param reviewId 评价ID
     * @param adminId 管理员ID
     * @return 解除封禁后的评价VO
     */
    VenueReviewVO unbanReview(Long reviewId, Long adminId);

    /**
     * 获取评价封禁记录
     * @param reviewId 评价ID
     * @return 封禁记录，如不存在则返回null
     */
    VenueReviewBan getBanByReviewId(Long reviewId);

    /**
     * 分页获取所有封禁记录
     * @param page 页码
     * @param size 每页大小
     * @return 分页数据
     */
    IPage<VenueReviewBan> pageBanRecords(int page, int size);
} 