package com.xcl.venueserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xcl.venueserver.entity.BookingFacility;
import com.xcl.venueserver.vo.BookingFacilityVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 预约设施Mapper接口
 */
@Mapper
public interface BookingFacilityMapper extends BaseMapper<BookingFacility> {
    
    /**
     * 根据订单ID查询预约设施列表
     * @param orderId 订单ID
     * @return 预约设施列表
     */
    List<BookingFacilityVO> getFacilitiesByOrderId(@Param("orderId") Long orderId);
    
    /**
     * 批量插入预约设施
     * @param facilities 预约设施列表
     * @return 插入数量
     */
    int batchInsert(@Param("facilities") List<BookingFacility> facilities);
} 