package com.xcl.venueserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xcl.venueserver.entity.PointRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 积分记录Mapper接口
 */
@Mapper
public interface PointRecordMapper extends BaseMapper<PointRecord> {

    /**
     * 分页查询用户积分记录
     * @param page 分页对象
     * @param userId 用户ID
     * @param pointType 积分类型
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 分页结果
     */
    @Select("<script>" +
            "SELECT * FROM point_records WHERE user_id = #{userId} " +
            "<if test='pointType != null'> AND point_type = #{pointType} </if>" +
            "<if test='startTime != null'> AND create_time &gt;= #{startTime} </if>" +
            "<if test='endTime != null'> AND create_time &lt;= #{endTime} </if>" +
            "ORDER BY create_time DESC" +
            "</script>")
    IPage<PointRecord> selectPageByUserId(Page<PointRecord> page,
                                         @Param("userId") Long userId,
                                         @Param("pointType") Integer pointType,
                                         @Param("startTime") LocalDateTime startTime,
                                         @Param("endTime") LocalDateTime endTime);

    /**
     * 查询用户待过期积分记录
     * @param userId 用户ID
     * @param expireTime 过期时间
     * @return 积分记录列表
     */
    @Select("SELECT * FROM point_records WHERE user_id = #{userId} " +
            "AND point_type = 1 AND expire_time IS NOT NULL " +
            "AND expire_time &lt;= #{expireTime} " +
            "AND NOT EXISTS (SELECT 1 FROM point_records pr WHERE pr.source_id = id AND pr.point_type = 3)")
    List<PointRecord> selectExpiring(@Param("userId") Long userId, @Param("expireTime") LocalDateTime expireTime);
} 