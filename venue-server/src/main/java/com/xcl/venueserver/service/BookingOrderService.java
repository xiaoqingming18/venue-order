package com.xcl.venueserver.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xcl.venueserver.dto.BookingOrderDTO;
import com.xcl.venueserver.entity.BookingOrder;
import com.xcl.venueserver.vo.BookingOrderVO;
import com.xcl.venueserver.vo.BookingStatsVO;
import com.xcl.venueserver.vo.VenueAvailabilityVO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 预约订单服务接口
 */
public interface BookingOrderService extends IService<BookingOrder> {
    
    /**
     * 分页查询预约订单
     * @param page 页码
     * @param size 每页大小
     * @param userId 用户ID(可选)
     * @param venueId 场馆ID(可选)
     * @param status 状态(可选)
     * @param bookingType 预约类型(可选)
     * @param startDate 开始日期(可选)
     * @param endDate 结束日期(可选)
     * @return 分页结果
     */
    IPage<BookingOrderVO> pageBookingOrders(
            Integer page, 
            Integer size, 
            Long userId, 
            Long venueId, 
            Integer status, 
            Integer bookingType, 
            LocalDate startDate, 
            LocalDate endDate
    );
    
    /**
     * 获取预约订单详情
     * @param id 订单ID
     * @return 订单详情
     */
    BookingOrderVO getBookingOrderDetails(Long id);
    
    /**
     * 获取用户的预约订单列表
     * @param userId 用户ID
     * @return 订单列表
     */
    List<BookingOrderVO> getUserBookingOrders(Long userId);
    
    /**
     * 创建预约订单
     * @param dto 订单信息
     * @param userId 用户ID
     * @return 订单详情
     */
    BookingOrderVO createBookingOrder(BookingOrderDTO dto, Long userId);
    
    /**
     * 取消预约订单
     * @param id 订单ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean cancelBookingOrder(Long id, Long userId);
    
    /**
     * 查询场馆在指定日期和时间段的可用性
     * @param venueId 场馆ID
     * @param date 日期
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 场馆可用性信息
     */
    VenueAvailabilityVO checkVenueAvailability(
            Long venueId, 
            LocalDate date, 
            LocalTime startTime, 
            LocalTime endTime
    );
    
    /**
     * 完成订单
     * @param id 订单ID
     * @return 是否成功
     */
    boolean completeBookingOrder(Long id);
    
    /**
     * 管理员更新订单状态
     * @param id 订单ID
     * @param status 新状态
     * @return 是否成功
     */
    boolean updateOrderStatus(Long id, Integer status);
    
    /**
     * 根据订单号查询订单
     * @param orderNo 订单号
     * @return 订单信息
     */
    BookingOrder getBookingOrderByOrderNo(String orderNo);
    
    /**
     * 根据订单号更新订单状态
     * @param orderNo 订单号
     * @param status 新状态
     * @return 是否成功
     */
    boolean updateOrderStatusByOrderNo(String orderNo, Integer status);
    
    /**
     * 获取预约统计数据
     * @return 统计数据
     */
    BookingStatsVO getBookingStats();
    
    /**
     * 通过SQL直接更新订单状态，绕过MyBatis和事务机制
     * @param orderNo 订单号
     * @param status 目标状态
     * @return 受影响的行数
     */
    int directUpdateStatusByOrderNo(String orderNo, Integer status);
} 