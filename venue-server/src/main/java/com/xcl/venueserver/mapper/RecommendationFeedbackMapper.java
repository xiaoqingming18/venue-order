package com.xcl.venueserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xcl.venueserver.entity.RecommendationFeedback;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 推荐反馈Mapper接口
 */
@Mapper
public interface RecommendationFeedbackMapper extends BaseMapper<RecommendationFeedback> {
    
    /**
     * 获取用户的反馈记录
     *
     * @param userId 用户ID
     * @param limit  限制数量
     * @return 反馈记录列表
     */
    @Select("SELECT * FROM recommendation_feedback WHERE user_id = #{userId} " +
            "ORDER BY created_at DESC LIMIT #{limit}")
    List<RecommendationFeedback> findUserFeedbacks(@Param("userId") Long userId, @Param("limit") Integer limit);
    
    /**
     * 获取场馆的反馈记录数量
     *
     * @param venueId 场馆ID
     * @param feedbackType 反馈类型
     * @return 反馈记录数量
     */
    @Select("SELECT COUNT(*) FROM recommendation_feedback WHERE venue_id = #{venueId} " +
            "AND feedback_type = #{feedbackType}")
    int countVenueFeedbacks(@Param("venueId") Long venueId, @Param("feedbackType") Integer feedbackType);
} 