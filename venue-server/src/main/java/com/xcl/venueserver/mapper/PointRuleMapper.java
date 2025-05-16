package com.xcl.venueserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xcl.venueserver.entity.PointRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 积分规则Mapper接口
 */
@Mapper
public interface PointRuleMapper extends BaseMapper<PointRule> {

    /**
     * 根据规则类型查询积分规则
     * @param ruleType 规则类型
     * @return 积分规则
     */
    @Select("SELECT * FROM point_rules WHERE rule_type = #{ruleType} AND status = 1 LIMIT 1")
    PointRule selectByRuleType(@Param("ruleType") Integer ruleType);
} 