package com.xcl.venueserver.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xcl.venueserver.dto.PointRuleDTO;
import com.xcl.venueserver.entity.PointRule;

import java.util.List;

/**
 * 积分规则服务接口
 */
public interface PointRuleService {

    /**
     * 获取所有启用的积分规则
     * @return 积分规则列表
     */
    List<PointRule> getAllEnabledRules();

    /**
     * 分页查询积分规则
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    IPage<PointRule> getRulesByPage(Integer page, Integer size);

    /**
     * 根据ID获取积分规则
     * @param id 积分规则ID
     * @return 积分规则
     */
    PointRule getRuleById(Integer id);

    /**
     * 根据规则类型获取积分规则
     * @param ruleType 规则类型
     * @return 积分规则
     */
    PointRule getRuleByType(Integer ruleType);

    /**
     * 添加或更新积分规则
     * @param pointRuleDTO 积分规则DTO
     * @return 是否成功
     */
    boolean saveOrUpdateRule(PointRuleDTO pointRuleDTO);

    /**
     * 更新积分规则状态
     * @param id 积分规则ID
     * @param status 状态：0-禁用 1-启用
     * @return 是否成功
     */
    boolean updateRuleStatus(Integer id, Integer status);

    /**
     * 删除积分规则
     * @param id 积分规则ID
     * @return 是否成功
     */
    boolean deleteRule(Integer id);
} 