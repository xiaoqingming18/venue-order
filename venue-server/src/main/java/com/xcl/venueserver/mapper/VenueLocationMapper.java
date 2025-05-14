package com.xcl.venueserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xcl.venueserver.entity.VenueLocation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 场馆位置Mapper接口
 */
@Mapper
public interface VenueLocationMapper extends BaseMapper<VenueLocation> {
} 