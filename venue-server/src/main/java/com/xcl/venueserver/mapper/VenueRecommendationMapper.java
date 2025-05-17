package com.xcl.venueserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xcl.venueserver.entity.UserVenuePreference;
import com.xcl.venueserver.entity.VenueRecommendation;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 场馆推荐Mapper接口
 */
@Mapper
public interface VenueRecommendationMapper extends BaseMapper<VenueRecommendation> {
    
    /**
     * 获取用户的推荐场馆列表（包含场馆详情）
     *
     * @param page 分页对象
     * @param userId 用户ID
     * @return 场馆推荐列表
     */
    @Select("SELECT r.*, v.name AS venueName, v.cover_image AS coverImage, " +
            "v.address, v.base_price AS basePrice, v.description, vt.name AS venueTypeName " +
            "FROM venue_recommendation r " +
            "LEFT JOIN venue v ON r.venue_id = v.id " +
            "LEFT JOIN venue_type vt ON v.venue_type_id = vt.id " +
            "WHERE r.user_id = #{userId} " +
            "ORDER BY r.recommendation_score DESC")
    IPage<VenueRecommendation> getRecommendationsWithDetails(IPage<?> page, @Param("userId") Long userId);
    
    /**
     * 更新推荐记录为已展示
     *
     * @param ids 推荐记录ID列表
     * @return 更新数量
     */
    @Update("<script>" +
            "UPDATE venue_recommendation SET is_shown = 1 " +
            "WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int markAsShown(@Param("ids") List<Long> ids);
    
    /**
     * 更新推荐记录为已点击
     *
     * @param id 推荐记录ID
     * @return 更新数量
     */
    @Update("UPDATE venue_recommendation SET is_clicked = 1 WHERE id = #{id}")
    int markAsClicked(@Param("id") Long id);
    
    /**
     * 获取用户对场馆的偏好记录
     *
     * @param userId 用户ID
     * @param venueId 场馆ID
     * @return 用户场馆偏好记录
     */
    @Select("SELECT * FROM user_venue_preference WHERE user_id = #{userId} AND venue_id = #{venueId}")
    UserVenuePreference selectUserVenuePreference(@Param("userId") Long userId, @Param("venueId") Long venueId);
    
    /**
     * 更新用户对场馆的偏好记录
     *
     * @param preference 用户场馆偏好记录
     * @return 更新数量
     */
    @Update("UPDATE user_venue_preference SET " +
            "preference_score = #{preferenceScore}, " +
            "browse_count = #{browseCount}, " +
            "order_count = #{orderCount}, " +
            "last_interaction = #{lastInteraction}, " +
            "updated_at = NOW() " +
            "WHERE user_id = #{userId} AND venue_id = #{venueId}")
    int updateUserVenuePreference(UserVenuePreference preference);
    
    /**
     * 插入用户对场馆的偏好记录
     *
     * @param preference 用户场馆偏好记录
     * @return 插入数量
     */
    @Insert("INSERT INTO user_venue_preference " +
            "(user_id, venue_id, preference_score, browse_count, order_count, last_interaction, updated_at) " +
            "VALUES " +
            "(#{userId}, #{venueId}, #{preferenceScore}, #{browseCount}, #{orderCount}, #{lastInteraction}, #{updatedAt})")
    int insertUserVenuePreference(UserVenuePreference preference);
} 