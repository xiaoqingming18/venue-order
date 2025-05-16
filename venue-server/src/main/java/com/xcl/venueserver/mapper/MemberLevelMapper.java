package com.xcl.venueserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xcl.venueserver.entity.MemberLevel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 会员等级Mapper接口
 */
@Mapper
public interface MemberLevelMapper extends BaseMapper<MemberLevel> {

    /**
     * 根据积分值查询对应的会员等级
     * @param points 积分值
     * @return 会员等级
     */
    @Select("SELECT * FROM member_levels WHERE status = 1 AND point_threshold <= #{points} " +
            "ORDER BY point_threshold DESC LIMIT 1")
    MemberLevel selectByPoints(@Param("points") Integer points);

    /**
     * 查询所有启用的会员等级，按积分阈值升序排列
     * @return 会员等级列表
     */
    @Select("SELECT * FROM member_levels WHERE status = 1 ORDER BY point_threshold ASC")
    List<MemberLevel> selectAllEnabled();

    /**
     * 查询下一级会员等级
     * @param currentLevelValue 当前等级值
     * @return 下一级会员等级
     */
    @Select("SELECT * FROM member_levels WHERE status = 1 AND level_value > #{currentLevelValue} " +
            "ORDER BY level_value ASC LIMIT 1")
    MemberLevel selectNextLevel(@Param("currentLevelValue") Integer currentLevelValue);
} 