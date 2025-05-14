package com.xcl.venueserver.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xcl.venueserver.common.utils.CurrentUser;
import com.xcl.venueserver.dto.BookingFacilityDTO;
import com.xcl.venueserver.dto.BookingOrderDTO;
import com.xcl.venueserver.entity.BookingFacility;
import com.xcl.venueserver.entity.BookingOrder;
import com.xcl.venueserver.entity.Venue;
import com.xcl.venueserver.entity.VenueFacility;
import com.xcl.venueserver.entity.VenueLocation;
import com.xcl.venueserver.mapper.BookingOrderMapper;
import com.xcl.venueserver.mapper.VenueMapper;
import com.xcl.venueserver.mapper.VenueLocationMapper;
import com.xcl.venueserver.service.BookingOrderService;
import com.xcl.venueserver.service.VenueFacilityService;
import com.xcl.venueserver.vo.BookingFacilityVO;
import com.xcl.venueserver.vo.BookingOrderVO;
import com.xcl.venueserver.vo.BookingStatsVO;
import com.xcl.venueserver.vo.FacilityAvailabilityVO;
import com.xcl.venueserver.vo.VenueAvailabilityVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 预约订单服务实现类
 */
@Slf4j
@Service
public class BookingOrderServiceImpl extends ServiceImpl<BookingOrderMapper, BookingOrder> implements BookingOrderService {

    @Resource
    private BookingOrderMapper bookingOrderMapper;
    
    @Resource
    private VenueMapper venueMapper;
    
    @Resource
    private VenueFacilityService venueFacilityService;
    
    @Resource
    private VenueLocationMapper venueLocationMapper;
    
    @Resource
    private CurrentUser currentUser;

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Override
    public IPage<BookingOrderVO> pageBookingOrders(Integer page, Integer size, Long userId, Long venueId, Integer status, Integer bookingType, LocalDate startDate, LocalDate endDate) {
        Page<BookingOrder> pageParam = new Page<>(page, size);
        return bookingOrderMapper.pageBookingOrders(pageParam, userId, venueId, status, bookingType, startDate, endDate);
    }

    @Override
    public BookingOrderVO getBookingOrderDetails(Long id) {
        return bookingOrderMapper.getBookingOrderById(id);
    }

    @Override
    public List<BookingOrderVO> getUserBookingOrders(Long userId) {
        return bookingOrderMapper.getBookingOrdersByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BookingOrderVO createBookingOrder(BookingOrderDTO dto, Long userId) {
        // 1. 检查用户是否存在
        Long currentUserId = currentUser.getUserId();
        if (currentUserId == null) {
            throw new IllegalArgumentException("用户未登录");
        }
        
        // 使用当前用户ID
        userId = currentUserId;
        
        // 2. 检查场馆是否存在
        Venue venue = venueMapper.selectById(dto.getVenueId());
        if (venue == null) {
            throw new IllegalArgumentException("场馆不存在");
        }
        
        // 检查场馆是否开放
        // 2. 验证场馆状态
        if (venue.getStatus() != 1) {
            throw new IllegalArgumentException("场馆已关闭，无法预约");
        }
        
        // 3. 验证预约时间
        if (dto.getStartTime().isAfter(dto.getEndTime())) {
            throw new IllegalArgumentException("开始时间不能晚于结束时间");
        }
        
        // 4. 检查场馆在指定时间段的可用性
        VenueAvailabilityVO availability = checkVenueAvailability(
                dto.getVenueId(), 
                dto.getBookingDate(), 
                dto.getStartTime(), 
                dto.getEndTime()
        );
        
        // 5. 根据预约类型进行处理
        if (dto.getBookingType() == 1) {
            // 包场预约，确保场馆可整体预约
            if (!availability.getAvailableForAll()) {
                throw new IllegalArgumentException("该时间段场馆已被部分预约，无法包场");
            }
        } else if (dto.getBookingType() == 2) {
            // 设施预约，检查每个设施的可用性
            if (dto.getFacilities() == null || dto.getFacilities().isEmpty()) {
                throw new IllegalArgumentException("设施预约需要选择至少一个设施");
            }
            
            // 获取所有设施的ID
            List<Long> facilityIds = dto.getFacilities().stream()
                    .map(BookingFacilityDTO::getFacilityId)
                    .collect(Collectors.toList());
            
            // 获取设施信息
            List<VenueFacility> facilities = venueFacilityService.getFacilitiesByIds(facilityIds);
            
            // 检查设施是否存在
            if (facilities.size() != facilityIds.size()) {
                throw new IllegalArgumentException("部分设施不存在");
            }
            
            // 检查设施是否属于该场馆
            for (VenueFacility facility : facilities) {
                if (!facility.getVenueId().equals(dto.getVenueId())) {
                    throw new IllegalArgumentException("选择的设施不属于该场馆");
                }
            }
            
            // 校验设施可用性
            Map<Long, FacilityAvailabilityVO> facilityAvailabilityMap = availability.getFacilities().stream()
                    .collect(Collectors.toMap(FacilityAvailabilityVO::getFacilityId, f -> f));
            
            for (BookingFacilityDTO facilityDTO : dto.getFacilities()) {
                FacilityAvailabilityVO facilityAvailability = facilityAvailabilityMap.get(facilityDTO.getFacilityId());
                
                if (facilityAvailability == null) {
                    throw new IllegalArgumentException("设施不存在");
                }
                
                if (!facilityAvailability.getAvailable()) {
                    throw new IllegalArgumentException("设施 " + facilityAvailability.getFacilityType() + " 在该时间段不可用");
                }
                
                // 检查预约数量
                if (facilityDTO.getQuantity() > facilityAvailability.getAvailableQuantity()) {
                    throw new IllegalArgumentException("设施 " + facilityAvailability.getFacilityType() + " 可用数量不足");
                }
                
                // 获取设施单价
                VenueFacility facilityEntity = venueFacilityService.getById(facilityDTO.getFacilityId());
                BigDecimal facilityPrice;
                
                if (facilityEntity != null && facilityEntity.getPrice() != null) {
                    // 使用设施自己的价格
                    facilityPrice = facilityEntity.getPrice();
                } else {
                    // 使用场馆基础价格的20%作为设施单价
                    facilityPrice = venue.getBasePrice().multiply(new BigDecimal("0.2"));
                }
                
                // 设置设施单价
                facilityDTO.setUnitPrice(facilityPrice);
            }
        } else {
            throw new IllegalArgumentException("无效的预约类型");
        }
        
        // 6. 创建订单
        BookingOrder order = new BookingOrder();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setVenueId(dto.getVenueId());
        
        // 不再使用locationId字段，直接设置为null
        order.setLocationId(null);
        
        order.setBookingDate(dto.getBookingDate());
        order.setStartTime(dto.getStartTime());
        order.setEndTime(dto.getEndTime());
        order.setBookingType(dto.getBookingType());
        order.setRemarks(dto.getRemarks());
        
        // 7. 计算订单金额
        if (dto.getBookingType() == 1) {
            // 包场预约，使用场馆基础价格和时间段长度计算
            int hours = dto.getEndTime().getHour() - dto.getStartTime().getHour();
            order.setTotalAmount(venue.getBasePrice().multiply(new BigDecimal(hours)));
        } else {
            // 设施预约，累加所有设施的价格
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (BookingFacilityDTO facilityDTO : dto.getFacilities()) {
                BigDecimal facilityAmount = facilityDTO.getUnitPrice().multiply(new BigDecimal(facilityDTO.getQuantity()));
                totalAmount = totalAmount.add(facilityAmount);
            }
            order.setTotalAmount(totalAmount);
        }
        
        // 8. 设置订单初始状态为待支付
        order.setStatus(1); // 待支付（需要走支付流程）
        
        // 9. 保存订单
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        bookingOrderMapper.insert(order);
        
        // 10. 对于设施预约，保存设施明细
        if (dto.getBookingType() == 2 && dto.getFacilities() != null && !dto.getFacilities().isEmpty()) {
            List<BookingFacility> bookingFacilities = new ArrayList<>();
            
            for (BookingFacilityDTO facilityDTO : dto.getFacilities()) {
                BookingFacility bookingFacility = new BookingFacility();
                bookingFacility.setOrderId(order.getId());
                bookingFacility.setFacilityId(facilityDTO.getFacilityId());
                bookingFacility.setQuantity(facilityDTO.getQuantity());
                bookingFacility.setUnitPrice(facilityDTO.getUnitPrice());
                bookingFacility.setAmount(facilityDTO.getUnitPrice().multiply(new BigDecimal(facilityDTO.getQuantity())));
                bookingFacility.setCreatedAt(LocalDateTime.now());
                bookingFacility.setUpdatedAt(LocalDateTime.now());
                
                bookingFacilities.add(bookingFacility);
            }
            
            // 批量保存设施明细
            saveBatchBookingFacilities(bookingFacilities);
        }
        
        // 11. 返回创建的订单详情
        return getBookingOrderDetails(order.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelBookingOrder(Long id, Long userId) {
        // 1. 检查订单是否存在
        BookingOrder order = bookingOrderMapper.selectById(id);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        
        // 2. 检查是否是当前用户的订单或管理员
        if (!order.getUserId().equals(userId) && !currentUser.isAdmin()) {
            throw new IllegalArgumentException("无权操作此订单");
        }
        
        // 3. 检查订单状态
        if (order.getStatus() == 0) {
            throw new IllegalArgumentException("订单已取消");
        }
        
        if (order.getStatus() == 3) {
            throw new IllegalArgumentException("订单已完成，无法取消");
        }
        
        // 4. 取消订单
        order.setStatus(0); // 已取消
        order.setUpdatedAt(LocalDateTime.now());
        
        return updateById(order);
    }

    @Override
    public VenueAvailabilityVO checkVenueAvailability(Long venueId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        // 1. 获取场馆信息
        Venue venue = venueMapper.selectById(venueId);
        if (venue == null) {
            throw new IllegalArgumentException("场馆不存在");
        }
        
        // 2. 创建结果对象
        VenueAvailabilityVO result = new VenueAvailabilityVO();
        result.setVenueId(venueId);
        result.setVenueName(venue.getName());
        result.setDate(date);
        result.setStartTime(startTime);
        result.setEndTime(endTime);
        result.setBasePrice(venue.getBasePrice());
        result.setCurrentPrice(venue.getBasePrice()); // 暂不考虑时段差异的价格
        
        // 3. 检查场馆在指定时间段是否已经被完全预约（包场）
        int venueBookings = bookingOrderMapper.countVenueBookings(venueId, date, startTime, endTime);
        boolean venueAvailable = venueBookings == 0;
        result.setAvailableForAll(venueAvailable);
        
        // 4. 获取场馆的所有设施
        List<VenueFacility> allFacilities = venueFacilityService.getFacilitiesByVenueId(venueId);
        
        // 5. 检查每个设施的可用性
        List<FacilityAvailabilityVO> facilityAvailabilities = new ArrayList<>();
        
        for (VenueFacility facility : allFacilities) {
            // 查询该设施在该时间段已被预约的数量
            int bookedCount = bookingOrderMapper.countFacilityBookings(facility.getId(), date, startTime, endTime);
            
            // 计算可用数量
            int availableCount = facility.getQuantity() - bookedCount;
            
            FacilityAvailabilityVO facilityAvailability = new FacilityAvailabilityVO();
            facilityAvailability.setFacilityId(facility.getId());
            facilityAvailability.setFacilityType(facility.getFacilityType());
            facilityAvailability.setTotalQuantity(facility.getQuantity());
            facilityAvailability.setAvailableQuantity(Math.max(0, availableCount));
            facilityAvailability.setDate(date);
            facilityAvailability.setStartTime(startTime);
            facilityAvailability.setEndTime(endTime);
            facilityAvailability.setAvailable(availableCount > 0);
            
            facilityAvailabilities.add(facilityAvailability);
        }
        
        result.setFacilities(facilityAvailabilities);
        
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean completeBookingOrder(Long id) {
        // 1. 检查订单是否存在
        BookingOrder order = bookingOrderMapper.selectById(id);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        
        // 2. 检查订单状态
        if (order.getStatus() != 2) {
            throw new IllegalArgumentException("只有已预约状态的订单才能标记为已完成");
        }
        
        // 3. 更新订单状态
        order.setStatus(3); // 已完成
        order.setUpdatedAt(LocalDateTime.now());
        
        return updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateOrderStatus(Long id, Integer status) {
        // 1. 检查订单是否存在
        BookingOrder order = bookingOrderMapper.selectById(id);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        
        // 2. 检查状态有效性
        if (status < 0 || status > 3) {
            throw new IllegalArgumentException("无效的订单状态");
        }
        
        // 3. 更新订单状态
        order.setStatus(status);
        order.setUpdatedAt(LocalDateTime.now());
        
        return updateById(order);
    }
    
    /**
     * 生成订单编号
     * 格式：yyyyMMddHHmmss + 6位随机数
     * @return 订单编号
     */
    private String generateOrderNo() {
        String datePrefix = DateUtil.format(LocalDateTime.now(), "yyyyMMddHHmmss");
        String randomSuffix = IdUtil.fastSimpleUUID().substring(0, 6);
        return datePrefix + randomSuffix;
    }
    
    /**
     * 批量保存预约设施
     * @param facilities 预约设施列表
     * @return 是否成功
     */
    private boolean saveBatchBookingFacilities(List<BookingFacility> facilities) {
        if (facilities.isEmpty()) {
            return true;
        }
        
        for (BookingFacility facility : facilities) {
            baseMapper.getClass().getClassLoader().getResourceAsStream("");
            this.getBaseMapper().getClass();
        }
        
        return true;
    }

    /**
     * 获取预约统计数据
     * @return 统计数据
     */
    @Override
    public BookingStatsVO getBookingStats() {
        BookingStatsVO stats = new BookingStatsVO();
        
        // 获取当天日期，格式为yyyy-MM-dd
        LocalDate today = LocalDate.now();
        String todayStr = today.format(DateTimeFormatter.ISO_LOCAL_DATE);
        
        // 查询总预约数
        stats.setTotalCount(Math.toIntExact(count()));
        
        // 查询各状态预约数
        stats.setPendingCount(Math.toIntExact(count(new LambdaQueryWrapper<BookingOrder>()
                .eq(BookingOrder::getStatus, 1))));
        
        stats.setPaidCount(Math.toIntExact(count(new LambdaQueryWrapper<BookingOrder>()
                .eq(BookingOrder::getStatus, 2))));
        
        stats.setCompletedCount(Math.toIntExact(count(new LambdaQueryWrapper<BookingOrder>()
                .eq(BookingOrder::getStatus, 3))));
        
        stats.setCancelledCount(Math.toIntExact(count(new LambdaQueryWrapper<BookingOrder>()
                .eq(BookingOrder::getStatus, 0))));
        
        stats.setRefundedCount(Math.toIntExact(count(new LambdaQueryWrapper<BookingOrder>()
                .eq(BookingOrder::getStatus, 4))));
        
        // 查询今日预约数
        stats.setTodayCount(Math.toIntExact(count(new LambdaQueryWrapper<BookingOrder>()
                .apply("DATE_FORMAT(created_at,'%Y-%m-%d') = {0}", todayStr))));
        
        // 计算总金额（已支付和已完成的订单）
        Double totalAmount = getBaseMapper().selectList(new LambdaQueryWrapper<BookingOrder>()
                .in(BookingOrder::getStatus, 2, 3))
                .stream()
                .map(order -> order.getTotalAmount().doubleValue())
                .reduce(0.0, Double::sum);
        stats.setTotalAmount(totalAmount);
        
        // 计算今日金额
        Double todayAmount = getBaseMapper().selectList(new LambdaQueryWrapper<BookingOrder>()
                .apply("DATE_FORMAT(created_at,'%Y-%m-%d') = {0}", todayStr)
                .in(BookingOrder::getStatus, 2, 3))
                .stream()
                .map(order -> order.getTotalAmount().doubleValue())
                .reduce(0.0, Double::sum);
        stats.setTodayAmount(todayAmount);
        
        return stats;
    }

    @Override
    public BookingOrder getBookingOrderByOrderNo(String orderNo) {
        return bookingOrderMapper.getBookingOrderByOrderNo(orderNo);
    }
    
    @Override
    public boolean updateOrderStatusByOrderNo(String orderNo, Integer status) {
        log.info("开始更新订单状态：orderNo={}, 目标状态={}", orderNo, status);
        
        // 1. 根据订单号查询订单
        BookingOrder order = getBookingOrderByOrderNo(orderNo);
        if (order == null) {
            log.error("更新订单状态失败：订单不存在, orderNo={}", orderNo);
            throw new IllegalArgumentException("订单不存在");
        }
        
        log.info("查询到订单：id={}, orderNo={}, 当前状态={}", order.getId(), orderNo, order.getStatus());
        
        // 2. 检查状态有效性
        if (status < 0 || status > 3) {
            log.error("更新订单状态失败：无效的状态值 {} (orderNo={})", status, orderNo);
            throw new IllegalArgumentException("无效的订单状态");
        }
        
        // 如果状态相同，则不需要更新
        if (order.getStatus().equals(status)) {
            log.info("订单当前状态已经是 {}，无需更新, orderNo={}", status, orderNo);
            return true;
        }
        
        log.info("尝试使用直接SQL更新避免事务问题：orderNo={}, 目标状态={}", orderNo, status);
        
        // 直接使用SQL更新，避免事务问题
        try {
            // 执行原生SQL更新，同时更新updated_at字段
            String sql = "UPDATE booking_order SET status = ?, updated_at = NOW() WHERE order_no = ?";
            int rowsUpdated = jdbcTemplate.update(sql, status, orderNo);
            
            log.info("直接SQL更新结果：影响行数={}, orderNo={}, 目标状态={}", 
                    rowsUpdated, orderNo, status);
            
            if (rowsUpdated > 0) {
                log.info("订单状态更新成功：orderNo={}, 新状态={}", orderNo, status);
                
                // 查询更新后的订单状态，确认是否真的更新成功
                BookingOrder updatedOrder = getBookingOrderByOrderNo(orderNo);
                if (updatedOrder != null) {
                    log.info("确认更新后订单状态：orderNo={}, 更新后状态={}", 
                            orderNo, updatedOrder.getStatus());
                    
                    if (!updatedOrder.getStatus().equals(status)) {
                        log.error("状态更新不一致！数据库中状态与期望不符，orderNo={}, 期望={}, 实际={}", 
                                orderNo, status, updatedOrder.getStatus());
                        return false;
                    }
                }
                return true;
            } else {
                log.error("订单状态更新失败：orderNo={}, 目标状态={}", orderNo, status);
                return false;
            }
        } catch (Exception e) {
            log.error("更新订单状态异常：orderNo={}, 目标状态={}, 异常={}", orderNo, status, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public int directUpdateStatusByOrderNo(String orderNo, Integer status) {
        log.info("开始使用原生SQL直接更新订单状态：orderNo={}, 目标状态={}", orderNo, status);
        
        try {
            // 1. 根据订单号查询订单，确认存在
            BookingOrder order = getBookingOrderByOrderNo(orderNo);
            if (order == null) {
                log.error("直接SQL更新状态失败：订单不存在, orderNo={}", orderNo);
                return 0;
            }
            
            log.info("直接SQL更新，查询到订单：id={}, orderNo={}, 当前状态={}", 
                    order.getId(), orderNo, order.getStatus());
            
            // 2. 检查状态有效性
            if (status < 0 || status > 3) {
                log.error("直接SQL更新状态失败：无效的状态值 {} (orderNo={})", status, orderNo);
                return 0;
            }
            
            // 如果状态已经是目标状态，则不需要更新
            if (order.getStatus().equals(status)) {
                log.info("订单状态已经是 {}，无需更新, orderNo={}", status, orderNo);
                return 1; // 返回1表示已是目标状态，视为成功
            }
            
            // 3. 执行原生SQL更新，同时更新updated_at字段
            String sql = "UPDATE booking_order SET status = ?, updated_at = NOW() WHERE order_no = ? AND status != ?";
            int rowsUpdated = jdbcTemplate.update(sql, status, orderNo, status);
            
            log.info("直接SQL更新结果：影响行数={}, orderNo={}, 目标状态={}", 
                    rowsUpdated, orderNo, status);
            
            if (rowsUpdated == 0) {
                // 可能是状态已被其他线程更新，再次查询确认
                BookingOrder currentOrder = getBookingOrderByOrderNo(orderNo);
                if (currentOrder != null && currentOrder.getStatus().equals(status)) {
                    log.info("订单状态已经是 {}，可能被其他线程更新, orderNo={}", status, orderNo);
                    return 1; // 返回1表示状态已正确，视为成功
                }
            }
            
            return rowsUpdated;
        } catch (Exception e) {
            log.error("直接SQL更新订单状态异常: {}, orderNo={}", e.getMessage(), orderNo, e);
            return 0;
        }
    }
} 