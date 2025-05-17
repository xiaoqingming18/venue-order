package com.xcl.venueserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xcl.venueserver.entity.UserFeedback;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户反馈Mapper接口
 */
@Mapper
public interface UserFeedbackMapper extends BaseMapper<UserFeedback> {
} 