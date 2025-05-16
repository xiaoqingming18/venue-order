package com.xcl.venueserver.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xcl.venueserver.dto.MemberLevelDTO;
import com.xcl.venueserver.entity.MemberLevel;
import com.xcl.venueserver.vo.MemberLevelVO;

import java.util.List;

/**
 * 会员等级服务接口
 */
public interface MemberLevelService {

    /**
     * 获取所有启用的会员等级
     * @return 会员等级列表
     */
    List<MemberLevel> getAllEnabledLevels();

    /**
     * 分页查询会员等级
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    IPage<MemberLevel> getLevelsByPage(Integer page, Integer size);

    /**
     * 根据ID获取会员等级
     * @param id 会员等级ID
     * @return 会员等级
     */
    MemberLevel getLevelById(Integer id);

    /**
     * 根据等级值获取会员等级
     * @param levelValue 等级值
     * @return 会员等级
     */
    MemberLevel getLevelByValue(Integer levelValue);

    /**
     * 根据积分获取会员等级
     * @param points 积分
     * @return 会员等级
     */
    MemberLevel getLevelByPoints(Integer points);

    /**
     * 获取用户当前的会员等级和下一级信息
     * @param userId 用户ID
     * @return 会员等级VO
     */
    MemberLevelVO getUserMemberLevel(Long userId);

    /**
     * 添加或更新会员等级
     * @param memberLevelDTO 会员等级DTO
     * @return 是否成功
     */
    boolean saveOrUpdateLevel(MemberLevelDTO memberLevelDTO);

    /**
     * 更新会员等级状态
     * @param id 会员等级ID
     * @param status 状态：0-禁用 1-启用
     * @return 是否成功
     */
    boolean updateLevelStatus(Integer id, Integer status);

    /**
     * 删除会员等级
     * @param id 会员等级ID
     * @return 是否成功
     */
    boolean deleteLevel(Integer id);
} 