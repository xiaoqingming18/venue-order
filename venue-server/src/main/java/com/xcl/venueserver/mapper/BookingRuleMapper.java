package com.xcl.venueserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xcl.venueserver.entity.BookingRule;
import org.apache.ibatis.annotations.Mapper;

/**
 * 预约规则Mapper接口
 */
@Mapper
public interface BookingRuleMapper extends BaseMapper<BookingRule> {
} 