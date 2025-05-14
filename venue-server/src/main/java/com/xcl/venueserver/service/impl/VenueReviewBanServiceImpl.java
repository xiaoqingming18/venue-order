package com.xcl.venueserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xcl.venueserver.dto.ReviewBanDTO;
import com.xcl.venueserver.entity.VenueReview;
import com.xcl.venueserver.entity.VenueReviewBan;
import com.xcl.venueserver.mapper.VenueReviewBanMapper;
import com.xcl.venueserver.mapper.VenueReviewMapper;
import com.xcl.venueserver.service.VenueReviewBanService;
import com.xcl.venueserver.service.VenueReviewService;
import com.xcl.venueserver.vo.VenueReviewVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 评价封禁服务实现类
 */
@Service
public class VenueReviewBanServiceImpl extends ServiceImpl<VenueReviewBanMapper, VenueReviewBan> implements VenueReviewBanService {

    @Resource
    private VenueReviewMapper venueReviewMapper;

    @Resource
    private VenueReviewService venueReviewService;

    @Override
    @Transactional
    public VenueReviewVO banReview(Long reviewId, ReviewBanDTO dto, Long adminId, Long reportId) {
        // 检查评价是否存在
        VenueReview review = venueReviewMapper.selectById(reviewId);
        if (review == null) {
            throw new IllegalArgumentException("评价不存在");
        }

        // 检查评价是否已经被封禁
        if (review.getStatus() == 1) {
            throw new IllegalArgumentException("评价已被封禁");
        }

        // 封禁评价
        review.setStatus(1); // 设置为已封禁
        review.setUpdatedAt(LocalDateTime.now());
        venueReviewMapper.updateById(review);

        // 创建封禁记录
        VenueReviewBan ban = new VenueReviewBan();
        ban.setReviewId(reviewId);
        ban.setAdminId(adminId);
        ban.setReportId(reportId); // 可为null
        ban.setReason(dto.getReason());
        ban.setCreatedAt(LocalDateTime.now());
        this.save(ban);

        // 返回更新后的评价VO
        return venueReviewService.getReviewById(reviewId);
    }

    @Override
    @Transactional
    public VenueReviewVO unbanReview(Long reviewId, Long adminId) {
        // 检查评价是否存在
        VenueReview review = venueReviewMapper.selectById(reviewId);
        if (review == null) {
            throw new IllegalArgumentException("评价不存在");
        }

        // 检查评价是否已被封禁
        if (review.getStatus() != 1) {
            throw new IllegalArgumentException("评价未被封禁");
        }

        // 解除封禁
        review.setStatus(0); // 设置为正常
        review.setUpdatedAt(LocalDateTime.now());
        venueReviewMapper.updateById(review);

        // 删除封禁记录（或者可以保留历史记录，取决于业务需求）
        // 这里不删除记录，以便保留历史封禁记录

        // 返回更新后的评价VO
        return venueReviewService.getReviewById(reviewId);
    }

    @Override
    public VenueReviewBan getBanByReviewId(Long reviewId) {
        LambdaQueryWrapper<VenueReviewBan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VenueReviewBan::getReviewId, reviewId);
        wrapper.orderByDesc(VenueReviewBan::getCreatedAt);
        wrapper.last("LIMIT 1"); // 取最新一条记录
        return this.getOne(wrapper);
    }

    @Override
    public IPage<VenueReviewBan> pageBanRecords(int page, int size) {
        LambdaQueryWrapper<VenueReviewBan> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(VenueReviewBan::getCreatedAt);
        return this.page(new Page<>(page, size), wrapper);
    }
} 