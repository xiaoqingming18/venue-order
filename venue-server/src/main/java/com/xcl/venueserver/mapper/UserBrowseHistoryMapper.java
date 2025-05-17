package com.xcl.venueserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xcl.venueserver.entity.UserBrowseHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户浏览历史Mapper接口
 */
@Mapper
public interface UserBrowseHistoryMapper extends BaseMapper<UserBrowseHistory> {
    
    /**
     * 获取用户最近浏览的场馆ID列表
     *
     * @param userId 用户ID
     * @param limit  限制数量
     * @return 场馆ID列表
     */
    @Select("SELECT venue_id FROM user_browse_history WHERE user_id = #{userId} " +
            "GROUP BY venue_id ORDER BY MAX(browse_time) DESC LIMIT #{limit}")
    List<Long> findRecentBrowsedVenueIds(@Param("userId") Long userId, @Param("limit") Integer limit);
    
    /**
     * 获取用户浏览次数最多的场馆ID列表
     *
     * @param userId 用户ID
     * @param limit  限制数量
     * @return 场馆ID列表
     */
    @Select("SELECT venue_id FROM user_browse_history WHERE user_id = #{userId} " +
            "GROUP BY venue_id ORDER BY COUNT(*) DESC LIMIT #{limit}")
    List<Long> findMostBrowsedVenueIds(@Param("userId") Long userId, @Param("limit") Integer limit);
} 