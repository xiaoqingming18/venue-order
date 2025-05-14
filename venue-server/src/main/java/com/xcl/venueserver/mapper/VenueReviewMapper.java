package com.xcl.venueserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xcl.venueserver.entity.VenueReview;
import com.xcl.venueserver.vo.VenueReviewStatsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 场馆评价Mapper接口
 */
@Mapper
public interface VenueReviewMapper extends BaseMapper<VenueReview> {

    /**
     * 统计场馆评价数据
     * @param venueId 场馆ID
     * @return 统计数据
     */
    @Select("SELECT " +
            "v.id as venueId, " +
            "v.name as venueName, " +
            "COUNT(r.id) as totalCount, " +
            "SUM(CASE WHEN r.status = 0 THEN 1 ELSE 0 END) as pendingCount, " +
            "SUM(CASE WHEN r.status = 1 THEN 1 ELSE 0 END) as approvedCount, " +
            "SUM(CASE WHEN r.status = 2 THEN 1 ELSE 0 END) as rejectedCount, " +
            "COALESCE(AVG(CASE WHEN r.status = 1 THEN r.environment_score ELSE NULL END), 0) as environmentAvgScore, " +
            "COALESCE(AVG(CASE WHEN r.status = 1 THEN r.facility_score ELSE NULL END), 0) as facilityAvgScore, " +
            "COALESCE(AVG(CASE WHEN r.status = 1 THEN r.service_score ELSE NULL END), 0) as serviceAvgScore, " +
            "COALESCE(AVG(CASE WHEN r.status = 1 THEN r.cost_performance_score ELSE NULL END), 0) as costPerformanceAvgScore, " +
            "COALESCE(AVG(CASE WHEN r.status = 1 THEN r.overall_score ELSE NULL END), 0) as overallAvgScore, " +
            "COALESCE(SUM(CASE WHEN r.status = 1 AND r.overall_score >= 4 THEN 1 ELSE 0 END) * 100.0 / NULLIF(SUM(CASE WHEN r.status = 1 THEN 1 ELSE 0 END), 0), 0) as goodReviewRate " +
            "FROM venue v " +
            "LEFT JOIN venue_review r ON v.id = r.venue_id " +
            "WHERE v.id = #{venueId} " +
            "GROUP BY v.id, v.name")
    VenueReviewStatsVO getVenueReviewStats(@Param("venueId") Long venueId);

    /**
     * 获取所有场馆的评价统计
     * @return 统计数据列表
     */
    @Select("SELECT " +
            "v.id as venueId, " +
            "v.name as venueName, " +
            "COUNT(r.id) as totalCount, " +
            "SUM(CASE WHEN r.status = 0 THEN 1 ELSE 0 END) as pendingCount, " +
            "SUM(CASE WHEN r.status = 1 THEN 1 ELSE 0 END) as approvedCount, " +
            "SUM(CASE WHEN r.status = 2 THEN 1 ELSE 0 END) as rejectedCount, " +
            "COALESCE(AVG(CASE WHEN r.status = 1 THEN r.environment_score ELSE NULL END), 0) as environmentAvgScore, " +
            "COALESCE(AVG(CASE WHEN r.status = 1 THEN r.facility_score ELSE NULL END), 0) as facilityAvgScore, " +
            "COALESCE(AVG(CASE WHEN r.status = 1 THEN r.service_score ELSE NULL END), 0) as serviceAvgScore, " +
            "COALESCE(AVG(CASE WHEN r.status = 1 THEN r.cost_performance_score ELSE NULL END), 0) as costPerformanceAvgScore, " +
            "COALESCE(AVG(CASE WHEN r.status = 1 THEN r.overall_score ELSE NULL END), 0) as overallAvgScore, " +
            "COALESCE(SUM(CASE WHEN r.status = 1 AND r.overall_score >= 4 THEN 1 ELSE 0 END) * 100.0 / NULLIF(SUM(CASE WHEN r.status = 1 THEN 1 ELSE 0 END), 0), 0) as goodReviewRate " +
            "FROM venue v " +
            "LEFT JOIN venue_review r ON v.id = r.venue_id " +
            "GROUP BY v.id, v.name")
    IPage<VenueReviewStatsVO> pageVenueReviewStats(Page<VenueReviewStatsVO> page);
} 