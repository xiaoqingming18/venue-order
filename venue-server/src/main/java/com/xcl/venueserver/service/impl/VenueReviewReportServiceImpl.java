package com.xcl.venueserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xcl.venueserver.dto.ReviewBanDTO;
import com.xcl.venueserver.dto.ReviewReportAuditDTO;
import com.xcl.venueserver.dto.ReviewReportDTO;
import com.xcl.venueserver.entity.User;
import com.xcl.venueserver.entity.VenueReview;
import com.xcl.venueserver.entity.VenueReviewBan;
import com.xcl.venueserver.entity.VenueReviewReport;
import com.xcl.venueserver.mapper.UserMapper;
import com.xcl.venueserver.mapper.VenueReviewBanMapper;
import com.xcl.venueserver.mapper.VenueReviewMapper;
import com.xcl.venueserver.mapper.VenueReviewReportMapper;
import com.xcl.venueserver.service.VenueReviewReportService;
import com.xcl.venueserver.service.VenueReviewService;
import com.xcl.venueserver.vo.VenueReviewReportVO;
import com.xcl.venueserver.vo.VenueReviewVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 评价举报服务实现类
 */
@Service
public class VenueReviewReportServiceImpl extends ServiceImpl<VenueReviewReportMapper, VenueReviewReport> implements VenueReviewReportService {

    @Resource
    private VenueReviewMapper venueReviewMapper;

    @Resource
    private UserMapper userMapper;
    
    @Resource
    private VenueReviewBanMapper venueReviewBanMapper;
    
    @Resource
    private VenueReviewService venueReviewService;

    @Override
    @Transactional
    public VenueReviewReportVO createReport(Long reviewId, ReviewReportDTO dto, Long userId) {
        // 检查评价是否存在
        VenueReview review = venueReviewMapper.selectById(reviewId);
        if (review == null) {
            throw new IllegalArgumentException("评价不存在");
        }

        // 检查是否已经举报过
        LambdaQueryWrapper<VenueReviewReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VenueReviewReport::getReviewId, reviewId)
                .eq(VenueReviewReport::getReporterId, userId);
        if (this.count(wrapper) > 0) {
            throw new IllegalArgumentException("您已举报过该评价");
        }

        // 创建举报记录
        VenueReviewReport report = new VenueReviewReport();
        report.setReviewId(reviewId);
        report.setReporterId(userId);
        report.setReason(dto.getReason());
        report.setReasonDetail(dto.getReasonDetail());
        report.setStatus(0); // 待处理状态
        report.setCreatedAt(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());

        // 保存举报记录
        this.save(report);

        // 转换为VO并返回
        return convertToVO(report);
    }

    @Override
    public VenueReviewReportVO getReportById(Long id) {
        VenueReviewReport report = this.getById(id);
        if (report == null) {
            return null;
        }
        return convertToVO(report);
    }

    @Override
    public IPage<VenueReviewReportVO> pageReports(int page, int size, Integer status) {
        // 构建查询条件
        LambdaQueryWrapper<VenueReviewReport> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(VenueReviewReport::getStatus, status);
        }
        
        // 按创建时间倒序排序
        wrapper.orderByDesc(VenueReviewReport::getCreatedAt);
        
        // 分页查询
        Page<VenueReviewReport> reportPage = new Page<>(page, size);
        Page<VenueReviewReport> resultPage = this.page(reportPage, wrapper);
        
        // 转换为VO
        List<VenueReviewReportVO> voList = resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
                
        // 构建VO分页结果
        Page<VenueReviewReportVO> voPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        voPage.setRecords(voList);
        
        return voPage;
    }

    @Override
    @Transactional
    public VenueReviewReportVO auditReport(Long id, ReviewReportAuditDTO dto, Long adminId) {
        // 获取举报记录
        VenueReviewReport report = this.getById(id);
        if (report == null) {
            throw new IllegalArgumentException("举报记录不存在");
        }
        
        // 检查状态
        if (report.getStatus() != 0) {
            throw new IllegalArgumentException("该举报已处理，不能重复审核");
        }
        
        // 更新举报状态
        report.setStatus(dto.getStatus());
        report.setAdminId(adminId);
        report.setAdminNotes(dto.getAdminNotes());
        report.setUpdatedAt(LocalDateTime.now());
        this.updateById(report);
        
        // 如果审核通过且需要封禁评价
        if (dto.getStatus() == 1 && dto.isBanReview()) {
            VenueReview review = venueReviewMapper.selectById(report.getReviewId());
            if (review != null) {
                // 封禁评价
                review.setStatus(1); // 设置为已封禁
                review.setUpdatedAt(LocalDateTime.now());
                venueReviewMapper.updateById(review);
                
                // 记录封禁信息
                VenueReviewBan ban = new VenueReviewBan();
                ban.setReviewId(review.getId());
                ban.setAdminId(adminId);
                ban.setReportId(report.getId());
                ban.setReason(dto.getBanReason() != null ? dto.getBanReason() : "违规内容，根据用户举报审核决定封禁");
                ban.setCreatedAt(LocalDateTime.now());
                venueReviewBanMapper.insert(ban);
            }
        }
        
        // 返回更新后的记录
        return convertToVO(report);
    }

    @Override
    public IPage<VenueReviewReportVO> getUserReports(Long userId, int page, int size) {
        // 构建查询条件
        LambdaQueryWrapper<VenueReviewReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VenueReviewReport::getReporterId, userId);
        wrapper.orderByDesc(VenueReviewReport::getCreatedAt);
        
        // 分页查询
        Page<VenueReviewReport> reportPage = new Page<>(page, size);
        Page<VenueReviewReport> resultPage = this.page(reportPage, wrapper);
        
        // 转换为VO
        List<VenueReviewReportVO> voList = resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
                
        // 构建VO分页结果
        Page<VenueReviewReportVO> voPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        voPage.setRecords(voList);
        
        return voPage;
    }
    
    /**
     * 将实体转换为VO
     */
    private VenueReviewReportVO convertToVO(VenueReviewReport report) {
        VenueReviewReportVO vo = new VenueReviewReportVO();
        BeanUtils.copyProperties(report, vo);
        
        // 设置举报人信息
        User reporter = userMapper.selectById(report.getReporterId());
        if (reporter != null) {
            vo.setReporterUsername(reporter.getUsername());
            vo.setReporterNickname(reporter.getNickname());
        }
        
        // 设置管理员信息
        if (report.getAdminId() != null) {
            User admin = userMapper.selectById(report.getAdminId());
            if (admin != null) {
                vo.setAdminUsername(admin.getUsername());
            }
        }
        
        // 设置关联的评价信息
        VenueReviewVO reviewVO = venueReviewService.getReviewById(report.getReviewId());
        vo.setReview(reviewVO);
        
        return vo;
    }
} 