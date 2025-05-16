package com.xcl.venueserver.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xcl.venueserver.dto.PointRecordQueryDTO;
import com.xcl.venueserver.vo.PointRecordVO;
import com.xcl.venueserver.vo.UserPointsSummaryVO;

/**
 * 积分服务接口
 */
public interface PointsService {

    /**
     * 获取用户积分概况
     * @param userId 用户ID
     * @return 用户积分概况
     */
    UserPointsSummaryVO getUserPointsSummary(Long userId);

    /**
     * 获取用户积分记录
     * @param userId 用户ID
     * @param queryDTO 查询参数
     * @return 分页积分记录
     */
    IPage<PointRecordVO> getUserPointRecords(Long userId, PointRecordQueryDTO queryDTO);

    /**
     * 增加用户积分
     * @param userId 用户ID
     * @param sourceType 来源类型
     * @param sourceId 来源ID
     * @param description 描述
     * @param amount 相关金额，可根据金额比例计算积分，为null则使用默认规则
     * @return 增加的积分数量
     */
    Integer addPoints(Long userId, Integer sourceType, Object sourceId, String description, Double amount);

    /**
     * 使用用户积分
     * @param userId 用户ID
     * @param points 使用的积分
     * @param sourceType 来源类型
     * @param sourceId 来源ID
     * @param description 描述
     * @return 是否成功
     */
    boolean usePoints(Long userId, Integer points, Integer sourceType, Object sourceId, String description);

    /**
     * 冻结用户积分
     * @param userId 用户ID
     * @param points 冻结的积分
     * @param sourceType 来源类型
     * @param sourceId 来源ID
     * @param description 描述
     * @return 是否成功
     */
    boolean freezePoints(Long userId, Integer points, Integer sourceType, Object sourceId, String description);

    /**
     * 解冻用户积分
     * @param userId 用户ID
     * @param points 解冻的积分
     * @param sourceType 来源类型
     * @param sourceId 来源ID
     * @param description 描述
     * @return 是否成功
     */
    boolean unfreezePoints(Long userId, Integer points, Integer sourceType, Object sourceId, String description);

    /**
     * 确认使用冻结的积分
     * @param userId 用户ID
     * @param points 使用的积分
     * @param sourceType 来源类型
     * @param sourceId 来源ID
     * @param description 描述
     * @return 是否成功
     */
    boolean confirmFrozenPoints(Long userId, Integer points, Integer sourceType, Object sourceId, String description);

    /**
     * 检查并处理过期积分
     * @param userId 用户ID
     * @return 处理的过期积分数量
     */
    Integer handleExpiredPoints(Long userId);
} 