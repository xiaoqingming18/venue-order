package com.xcl.venueserver.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xcl.venueserver.dto.ReviewReportAuditDTO;
import com.xcl.venueserver.dto.ReviewReportDTO;
import com.xcl.venueserver.entity.VenueReviewReport;
import com.xcl.venueserver.vo.VenueReviewReportVO;

/**
 * 评价举报服务接口
 */
public interface VenueReviewReportService extends IService<VenueReviewReport> {

    /**
     * 创建评价举报
     * @param reviewId 评价ID
     * @param dto 举报DTO
     * @param userId 用户ID
     * @return 举报VO
     */
    VenueReviewReportVO createReport(Long reviewId, ReviewReportDTO dto, Long userId);

    /**
     * 获取举报详情
     * @param id 举报ID
     * @return 举报VO
     */
    VenueReviewReportVO getReportById(Long id);

    /**
     * 分页查询举报
     * @param page 页码
     * @param size 每页大小
     * @param status 状态，可为null
     * @return 分页数据
     */
    IPage<VenueReviewReportVO> pageReports(int page, int size, Integer status);

    /**
     * 审核举报
     * @param id 举报ID
     * @param dto 审核DTO
     * @param adminId 管理员ID
     * @return 审核后的举报VO
     */
    VenueReviewReportVO auditReport(Long id, ReviewReportAuditDTO dto, Long adminId);

    /**
     * 获取用户提交的举报列表
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 分页数据
     */
    IPage<VenueReviewReportVO> getUserReports(Long userId, int page, int size);
} 