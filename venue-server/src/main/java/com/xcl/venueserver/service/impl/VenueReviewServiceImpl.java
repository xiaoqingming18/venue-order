package com.xcl.venueserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xcl.venueserver.dto.ReviewAuditDTO;
import com.xcl.venueserver.dto.VenueReviewDTO;
import com.xcl.venueserver.dto.VenueReviewReplyDTO;
import com.xcl.venueserver.entity.BookingOrder;
import com.xcl.venueserver.entity.User;
import com.xcl.venueserver.entity.Venue;
import com.xcl.venueserver.entity.VenueReview;
import com.xcl.venueserver.entity.VenueReviewReply;
import com.xcl.venueserver.mapper.VenueReviewMapper;
import com.xcl.venueserver.mapper.VenueReviewReplyMapper;
import com.xcl.venueserver.service.BookingOrderService;
import com.xcl.venueserver.service.UserService;
import com.xcl.venueserver.service.VenueReviewService;
import com.xcl.venueserver.service.VenueService;
import com.xcl.venueserver.vo.UserVO;
import com.xcl.venueserver.vo.VenueReviewStatsVO;
import com.xcl.venueserver.vo.VenueReviewVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 场馆评价服务实现类
 */
@Service
public class VenueReviewServiceImpl extends ServiceImpl<VenueReviewMapper, VenueReview> implements VenueReviewService {

    @Resource
    private VenueReviewReplyMapper venueReviewReplyMapper;

    @Resource
    private BookingOrderService bookingOrderService;

    @Resource
    private UserService userService;

    @Resource
    private VenueService venueService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VenueReviewVO createReview(VenueReviewDTO dto, Long userId) {
        // 检查订单是否存在且属于当前用户
        BookingOrder order = bookingOrderService.getById(dto.getOrderId());
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权评价此订单");
        }
        if (order.getStatus() != 3) { // 3-已完成
            throw new IllegalArgumentException("只能评价已完成的订单");
        }

        // 检查是否已经评价过
        LambdaQueryWrapper<VenueReview> reviewWrapper = new LambdaQueryWrapper<>();
        reviewWrapper.eq(VenueReview::getOrderId, order.getId());
        long count = this.count(reviewWrapper);
        if (count > 0) {
            throw new IllegalArgumentException("此订单已评价，不能重复评价");
        }

        // 创建评价实体
        VenueReview review = new VenueReview();
        BeanUtils.copyProperties(dto, review);
        review.setUserId(userId);
        review.setVenueId(order.getVenueId());
        review.setStatus(0); // 正常状态，无需审核
        review.setCreatedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());

        // 保存评价
        this.save(review);

        // 返回评价VO
        return convertToVO(review);
    }

    @Override
    public VenueReviewVO getReviewById(Long id) {
        VenueReview review = this.getById(id);
        if (review == null) {
            return null;
        }
        return convertToVO(review);
    }

    @Override
    public VenueReviewVO getReviewByOrderId(Long orderId) {
        // 根据订单ID查询评价
        LambdaQueryWrapper<VenueReview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VenueReview::getOrderId, orderId);
        wrapper.last("LIMIT 1"); // 只取一条记录
        
        VenueReview review = this.getOne(wrapper);
        if (review == null) {
            return null;
        }
        return convertToVO(review);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VenueReviewVO updateReview(Long id, VenueReviewDTO dto, Long userId) {
        // 检查评价是否存在且属于当前用户
        VenueReview review = this.getById(id);
        if (review == null) {
            throw new IllegalArgumentException("评价不存在");
        }
        if (!review.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权修改此评价");
        }

        // 检查是否已过评价修改期限（7天）
        if (review.getCreatedAt().plusDays(7).isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("评价已超过修改期限(7天)");
        }

        // 检查评价是否已被封禁
        if (review.getStatus() == 1) {
            throw new IllegalArgumentException("评价已被封禁，无法修改");
        }

        // 更新评价内容
        BeanUtils.copyProperties(dto, review);
        // 保持原状态，不改变 status
        review.setUpdatedAt(LocalDateTime.now());

        // 保存更新
        this.updateById(review);

        // 返回更新后的评价VO
        return convertToVO(review);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteReview(Long id, Long userId) {
        // 检查评价是否存在且属于当前用户
        VenueReview review = this.getById(id);
        if (review == null) {
            throw new IllegalArgumentException("评价不存在");
        }
        if (!review.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权删除此评价");
        }

        // 删除评价及其回复
        LambdaQueryWrapper<VenueReviewReply> replyWrapper = new LambdaQueryWrapper<>();
        replyWrapper.eq(VenueReviewReply::getReviewId, id);
        venueReviewReplyMapper.delete(replyWrapper);

        return this.removeById(id);
    }

    @Override
    public IPage<VenueReviewVO> pageReviews(int page, int size, Long userId, Long venueId, Integer status) {
        // 构建查询条件
        LambdaQueryWrapper<VenueReview> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(VenueReview::getUserId, userId);
        }
        if (venueId != null) {
            wrapper.eq(VenueReview::getVenueId, venueId);
        }
        if (status != null) {
            wrapper.eq(VenueReview::getStatus, status);
        }
        wrapper.orderByDesc(VenueReview::getCreatedAt);

        // 查询评价分页数据
        IPage<VenueReview> reviewPage = this.page(new Page<>(page, size), wrapper);

        // 转换为VO对象
        IPage<VenueReviewVO> voPage = reviewPage.convert(this::convertToVO);

        return voPage;
    }

    @Override
    public List<VenueReviewVO> getUserReviews(Long userId) {
        // 查询用户的所有评价
        LambdaQueryWrapper<VenueReview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VenueReview::getUserId, userId);
        wrapper.orderByDesc(VenueReview::getCreatedAt);

        List<VenueReview> reviews = this.list(wrapper);

        // 转换为VO对象
        return reviews.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public IPage<VenueReviewVO> getVenueReviews(Long venueId, int page, int size) {
        // 查询正常状态的场馆评价（状态为0的评价）
        LambdaQueryWrapper<VenueReview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VenueReview::getVenueId, venueId);
        wrapper.eq(VenueReview::getStatus, 0); // 只显示正常状态的评价
        wrapper.orderByDesc(VenueReview::getCreatedAt);

        IPage<VenueReview> reviewPage = this.page(new Page<>(page, size), wrapper);

        // 转换为VO对象
        IPage<VenueReviewVO> voPage = reviewPage.convert(this::convertToVO);

        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VenueReviewVO auditReview(Long id, ReviewAuditDTO dto, Long adminId) {
        // 检查评价是否存在
        VenueReview review = this.getById(id);
        if (review == null) {
            throw new IllegalArgumentException("评价不存在");
        }

        // 更新评价状态
        review.setStatus(dto.getStatus());
        review.setUpdatedAt(LocalDateTime.now());
        this.updateById(review);

        // 如果拒绝评价，添加管理员回复说明原因
        if (dto.getStatus() == 2 && dto.getRejectReason() != null && !dto.getRejectReason().isEmpty()) {
            VenueReviewReply reply = new VenueReviewReply();
            reply.setReviewId(id);
            reply.setUserId(adminId);
            reply.setContent("【评价被拒绝】" + dto.getRejectReason());
            reply.setIsAdmin(1);
            reply.setCreatedAt(LocalDateTime.now());
            reply.setUpdatedAt(LocalDateTime.now());
            venueReviewReplyMapper.insert(reply);
        }

        // 返回审核后的评价VO
        return convertToVO(review);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VenueReviewReply replyReview(Long reviewId, VenueReviewReplyDTO dto, Long userId, boolean isAdmin) {
        // 检查评价是否存在
        VenueReview review = this.getById(reviewId);
        if (review == null) {
            throw new IllegalArgumentException("评价不存在");
        }

        // 非管理员用户只能回复自己的评价
        if (!isAdmin && !review.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权回复此评价");
        }

        // 创建回复
        VenueReviewReply reply = new VenueReviewReply();
        reply.setReviewId(reviewId);
        reply.setUserId(userId);
        reply.setContent(dto.getContent());
        reply.setIsAdmin(isAdmin ? 1 : 0);
        reply.setCreatedAt(LocalDateTime.now());
        reply.setUpdatedAt(LocalDateTime.now());

        // 保存回复
        venueReviewReplyMapper.insert(reply);

        return reply;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteReply(Long replyId, Long userId, boolean isAdmin) {
        // 检查回复是否存在
        VenueReviewReply reply = venueReviewReplyMapper.selectById(replyId);
        if (reply == null) {
            throw new IllegalArgumentException("回复不存在");
        }

        // 只有回复者本人或管理员才能删除回复
        if (!isAdmin && !reply.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权删除此回复");
        }

        // 删除回复
        return venueReviewReplyMapper.deleteById(replyId) > 0;
    }

    @Override
    public VenueReviewStatsVO getVenueReviewStats(Long venueId) {
        return baseMapper.getVenueReviewStats(venueId);
    }

    @Override
    public IPage<VenueReviewStatsVO> pageVenueReviewStats(int page, int size) {
        return baseMapper.pageVenueReviewStats(new Page<>(page, size));
    }

    /**
     * 将评价实体转换为VO对象
     * @param review 评价实体
     * @return 评价VO
     */
    private VenueReviewVO convertToVO(VenueReview review) {
        VenueReviewVO vo = new VenueReviewVO();
        BeanUtils.copyProperties(review, vo);

        // 获取用户信息
        UserVO userVO = userService.getUserInfoById(review.getUserId());
        if (userVO != null) {
            vo.setUsername(userVO.getUsername());
            vo.setNickname(userVO.getNickname());
            vo.setAvatar(userVO.getAvatar());
        }

        // 获取场馆信息
        Venue venue = venueService.getById(review.getVenueId());
        if (venue != null) {
            vo.setVenueName(venue.getName());
        }

        // 获取回复列表
        LambdaQueryWrapper<VenueReviewReply> replyWrapper = new LambdaQueryWrapper<>();
        replyWrapper.eq(VenueReviewReply::getReviewId, review.getId());
        replyWrapper.orderByAsc(VenueReviewReply::getCreatedAt);
        List<VenueReviewReply> replies = venueReviewReplyMapper.selectList(replyWrapper);
        vo.setReplies(replies);

        return vo;
    }
} 