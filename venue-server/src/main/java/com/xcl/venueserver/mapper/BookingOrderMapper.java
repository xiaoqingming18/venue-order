package com.xcl.venueserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xcl.venueserver.entity.BookingOrder;
import com.xcl.venueserver.vo.BookingOrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 预约订单Mapper接口
 */
@Mapper
public interface BookingOrderMapper extends BaseMapper<BookingOrder> {
    
    /**
     * 分页查询预约订单列表，带详细信息
     * @param page 分页对象
     * @param userId 用户ID，可为null
     * @param venueId 场馆ID，可为null
     * @param status 订单状态，可为null
     * @param bookingType 预约类型，可为null
     * @param startDate 开始日期，可为null
     * @param endDate 结束日期，可为null
     * @return 分页结果
     */
    IPage<BookingOrderVO> pageBookingOrders(
            Page<BookingOrder> page, 
            @Param("userId") Long userId,
            @Param("venueId") Long venueId,
            @Param("status") Integer status,
            @Param("bookingType") Integer bookingType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
    
    /**
     * 查询指定时间段内场馆的预约记录数
     * @param venueId 场馆ID
     * @param bookingDate 预约日期
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 预约记录数
     */
    int countVenueBookings(
            @Param("venueId") Long venueId,
            @Param("bookingDate") LocalDate bookingDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );
    
    /**
     * 查询指定日期和时间段内的设施预约数量
     * @param facilityId 设施ID
     * @param bookingDate 预约日期
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 预约数量
     */
    int countFacilityBookings(
            @Param("facilityId") Long facilityId,
            @Param("bookingDate") LocalDate bookingDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );
    
    /**
     * 根据用户ID查询预约订单
     * @param userId 用户ID
     * @return 预约订单列表
     */
    List<BookingOrderVO> getBookingOrdersByUserId(@Param("userId") Long userId);
    
    /**
     * 根据ID查询预约订单详情
     * @param id 预约订单ID
     * @return 预约订单详情
     */
    BookingOrderVO getBookingOrderById(@Param("id") Long id);
    
    /**
     * 根据订单号查询预约订单
     * @param orderNo 订单号
     * @return 预约订单
     */
    BookingOrder getBookingOrderByOrderNo(@Param("orderNo") String orderNo);
} 