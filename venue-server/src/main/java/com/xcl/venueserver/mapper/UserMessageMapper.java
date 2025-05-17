package com.xcl.venueserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xcl.venueserver.entity.UserMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户消息Mapper接口
 */
@Mapper
public interface UserMessageMapper extends BaseMapper<UserMessage> {
} 