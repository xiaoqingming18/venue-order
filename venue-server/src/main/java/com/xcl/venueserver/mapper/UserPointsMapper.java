package com.xcl.venueserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xcl.venueserver.entity.UserPoints;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 用户积分账户Mapper接口
 */
@Mapper
public interface UserPointsMapper extends BaseMapper<UserPoints> {

    /**
     * 根据用户ID查询用户积分账户
     * @param userId 用户ID
     * @return 用户积分账户
     */
    @Select("SELECT * FROM user_points WHERE user_id = #{userId}")
    UserPoints selectByUserId(@Param("userId") Long userId);

    /**
     * 增加用户可用积分
     * @param userId 用户ID
     * @param points 增加的积分
     * @return 影响行数
     */
    @Update("UPDATE user_points SET total_points = total_points + #{points}, " +
            "available_points = available_points + #{points}, update_time = NOW() " +
            "WHERE user_id = #{userId}")
    int incrementPoints(@Param("userId") Long userId, @Param("points") Integer points);

    /**
     * 减少用户可用积分
     * @param userId 用户ID
     * @param points 减少的积分
     * @return 影响行数
     */
    @Update("UPDATE user_points SET available_points = available_points - #{points}, " +
            "update_time = NOW() WHERE user_id = #{userId} AND available_points >= #{points}")
    int decrementPoints(@Param("userId") Long userId, @Param("points") Integer points);

    /**
     * 冻结用户积分
     * @param userId 用户ID
     * @param points 冻结的积分
     * @return 影响行数
     */
    @Update("UPDATE user_points SET available_points = available_points - #{points}, " +
            "frozen_points = frozen_points + #{points}, update_time = NOW() " +
            "WHERE user_id = #{userId} AND available_points >= #{points}")
    int freezePoints(@Param("userId") Long userId, @Param("points") Integer points);

    /**
     * 解冻用户积分
     * @param userId 用户ID
     * @param points 解冻的积分
     * @return 影响行数
     */
    @Update("UPDATE user_points SET available_points = available_points + #{points}, " +
            "frozen_points = frozen_points - #{points}, update_time = NOW() " +
            "WHERE user_id = #{userId} AND frozen_points >= #{points}")
    int unfreezePoints(@Param("userId") Long userId, @Param("points") Integer points);

    /**
     * 使用冻结的积分
     * @param userId 用户ID
     * @param points 使用的积分
     * @return 影响行数
     */
    @Update("UPDATE user_points SET total_points = total_points - #{points}, " +
            "frozen_points = frozen_points - #{points}, update_time = NOW() " +
            "WHERE user_id = #{userId} AND frozen_points >= #{points}")
    int useFrozenPoints(@Param("userId") Long userId, @Param("points") Integer points);

    /**
     * 记录已过期积分
     * @param userId 用户ID
     * @param points 过期的积分
     * @return 影响行数
     */
    @Update("UPDATE user_points SET available_points = available_points - #{points}, " +
            "expired_points = expired_points + #{points}, update_time = NOW() " +
            "WHERE user_id = #{userId} AND available_points >= #{points}")
    int expirePoints(@Param("userId") Long userId, @Param("points") Integer points);
} 