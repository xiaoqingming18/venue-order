package com.xcl.venueserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xcl.venueserver.entity.UserFavoriteVenue;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户收藏场馆Mapper接口
 */
@Mapper
public interface UserFavoriteVenueMapper extends BaseMapper<UserFavoriteVenue> {
} 