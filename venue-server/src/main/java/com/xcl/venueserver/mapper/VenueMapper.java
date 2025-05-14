package com.xcl.venueserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xcl.venueserver.entity.Venue;
import org.apache.ibatis.annotations.Mapper;

/**
 * 场馆Mapper接口
 */
@Mapper
public interface VenueMapper extends BaseMapper<Venue> {
} 