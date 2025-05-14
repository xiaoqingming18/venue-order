package com.xcl.venueserver.controller;

import com.xcl.venueserver.common.utils.CurrentUser;
import com.xcl.venueserver.common.utils.Result;
import com.xcl.venueserver.dto.AliPayDTO;
import com.xcl.venueserver.entity.BookingOrder;
import com.xcl.venueserver.service.BookingOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;

/**
 * 支付相关控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Resource
    private BookingOrderService bookingOrderService;
    
    @Resource
    private AliPayController aliPayController;
    
    @Resource
    private CurrentUser currentUser;
    
    /**
     * 订单支付接口 - 生成支付表单
     */
    @GetMapping(value = "/pay/{orderId}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> pay(@PathVariable Long orderId) {
        // 1. 验证用户权限
        Long userId = currentUser.getUserId();
        if (userId == null) {
            log.error("支付失败：用户未登录，orderId={}", orderId);
            return ResponseEntity.badRequest().body("<div style='color:red'>支付失败：请先登录</div>");
        }
        
        // 2. 查询订单
        BookingOrder order = bookingOrderService.getById(orderId);
        if (order == null) {
            log.error("支付失败：订单不存在，orderId={}", orderId);
            return ResponseEntity.badRequest().body("<div style='color:red'>支付失败：订单不存在</div>");
        }
        
        // 3. 验证是否是当前用户的订单
        if (!order.getUserId().equals(userId) && !currentUser.isAdmin()) {
            log.error("支付失败：无权支付此订单，orderId={}，用户ID={}", orderId, userId);
            return ResponseEntity.badRequest().body("<div style='color:red'>支付失败：无权支付此订单</div>");
        }
        
        // 4. 验证订单状态是否为待支付
        if (order.getStatus() != 1) {
            log.error("支付失败：订单状态不是待支付，orderId={}，状态={}", orderId, order.getStatus());
            return ResponseEntity.badRequest().body("<div style='color:red'>支付失败：订单状态不是待支付，无法支付</div>");
        }
        
        try {
            // 5. 构建支付参数
            AliPayDTO payDTO = new AliPayDTO();
            payDTO.setOrderNo(order.getOrderNo());
            payDTO.setSubject("场馆预约-" + order.getOrderNo());
            payDTO.setTotalAmount(order.getTotalAmount().toString());
            payDTO.setBody("场馆预约支付");
            
            log.info("准备调用支付宝支付接口，订单号={}，金额={}", payDTO.getOrderNo(), payDTO.getTotalAmount());
            
            // 6. 调用支付宝接口
            Result<String> payResult = aliPayController.pay(payDTO);
            
            if (payResult.getCode() != 200) {
                log.error("调用支付宝接口失败：{}", payResult.getMessage());
                return ResponseEntity.badRequest().body("<div style='color:red'>支付失败：" + payResult.getMessage() + "</div>");
            }
            
            // 直接返回HTML表单字符串
            return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(payResult.getData());
        } catch (Exception e) {
            log.error("支付过程中发生异常", e);
            return ResponseEntity.badRequest().body("<div style='color:red'>支付失败：" + e.getMessage() + "</div>");
        }
    }

    /**
     * 测试订单状态更新功能 - 仅用于排查问题
     * @param orderNo 订单号
     * @param status 目标状态
     * @return 处理结果
     */
    @GetMapping("/test-update-status/{orderNo}/{status}")
    public ResponseEntity<String> testUpdateOrderStatus(
            @PathVariable String orderNo,
            @PathVariable Integer status) {
        
        log.info("测试更新订单状态，订单号: {}, 目标状态: {}", orderNo, status);
        
        try {
            // 1. 查询当前订单状态
            BookingOrder order = bookingOrderService.getBookingOrderByOrderNo(orderNo);
            if (order == null) {
                log.error("测试更新订单状态：订单不存在, orderNo={}", orderNo);
                return ResponseEntity.badRequest().body("订单不存在：" + orderNo);
            }
            
            log.info("测试更新订单状态：当前订单状态为 {}, orderNo={}", order.getStatus(), orderNo);
            
            // 2. 直接调用服务更新订单状态
            boolean result = bookingOrderService.updateOrderStatusByOrderNo(orderNo, status);
            
            if (result) {
                log.info("测试更新订单状态：更新成功, orderNo={}, 新状态={}", orderNo, status);
                
                // 3. 再次查询订单状态，确认是否更新成功
                BookingOrder updatedOrder = bookingOrderService.getBookingOrderByOrderNo(orderNo);
                if (updatedOrder != null) {
                    log.info("测试更新订单状态：更新后订单状态为 {}, orderNo={}", updatedOrder.getStatus(), orderNo);
                    
                    if (updatedOrder.getStatus().equals(status)) {
                        return ResponseEntity.ok("订单状态更新成功！当前状态：" + status);
                    } else {
                        return ResponseEntity.ok("订单状态显示更新成功，但实际状态未变更：" + updatedOrder.getStatus());
                    }
                } else {
                    return ResponseEntity.ok("订单状态更新后无法查询到订单");
                }
            } else {
                log.error("测试更新订单状态：更新失败, orderNo={}, 目标状态={}", orderNo, status);
                return ResponseEntity.badRequest().body("订单状态更新失败");
            }
        } catch (Exception e) {
            log.error("测试更新订单状态异常", e);
            return ResponseEntity.badRequest().body("更新订单状态异常：" + e.getMessage());
        }
    }

    /**
     * 直接通过SQL更新订单状态 - 用于解决事务问题
     * @param orderNo 订单号
     * @return 处理结果
     */
    @GetMapping("/direct-update/{orderNo}")
    public ResponseEntity<String> directUpdateOrderStatus(@PathVariable String orderNo) {
        log.info("开始直接通过SQL更新订单状态，订单号: {}", orderNo);
        
        try {
            // 1. 查询当前订单状态
            BookingOrder order = bookingOrderService.getBookingOrderByOrderNo(orderNo);
            if (order == null) {
                log.error("直接更新订单状态：订单不存在, orderNo={}", orderNo);
                return ResponseEntity.badRequest().body("订单不存在：" + orderNo);
            }
            
            log.info("直接更新订单状态：当前订单状态为 {}, orderNo={}, id={}", 
                    order.getStatus(), orderNo, order.getId());
            
            // 如果订单已是已支付状态，则无需更新
            if (order.getStatus() == 2) {
                log.info("订单已是已支付状态，无需更新: {}", orderNo);
                return ResponseEntity.ok("订单已是已支付状态(2)，无需更新");
            }
            
            // 2. 直接使用JDBC执行SQL更新
            int affectedRows = bookingOrderService.directUpdateStatusByOrderNo(orderNo, 2);
            
            if (affectedRows > 0) {
                log.info("直接SQL更新订单状态成功, orderNo={}, 影响行数={}", orderNo, affectedRows);
                
                // 3. 再次查询订单状态，确认是否更新成功
                BookingOrder updatedOrder = bookingOrderService.getBookingOrderByOrderNo(orderNo);
                if (updatedOrder != null) {
                    log.info("直接更新后订单状态为 {}, orderNo={}", updatedOrder.getStatus(), orderNo);
                    
                    if (updatedOrder.getStatus() == 2) {
                        return ResponseEntity.ok("订单状态更新成功！当前状态：已支付(2)");
                    } else {
                        log.warn("SQL更新执行成功但状态未变更，尝试第二次更新: {}", orderNo);
                        
                        // 尝试第二次直接更新
                        try {
                            // 短暂延迟，避免数据库死锁
                            Thread.sleep(100);
                            
                            int secondAttempt = bookingOrderService.directUpdateStatusByOrderNo(orderNo, 2);
                            log.info("第二次直接SQL更新结果: 影响行数={}, orderNo={}", secondAttempt, orderNo);
                            
                            // 再次查询确认
                            BookingOrder secondCheck = bookingOrderService.getBookingOrderByOrderNo(orderNo);
                            if (secondCheck != null && secondCheck.getStatus() == 2) {
                                return ResponseEntity.ok("订单状态第二次更新成功！当前状态：已支付(2)");
                            } else if (secondCheck != null) {
                                return ResponseEntity.ok("订单状态更新失败，当前状态仍为：" + secondCheck.getStatus() + 
                                    "，请稍后再试或联系客服");
                            }
                        } catch (Exception e) {
                            log.error("第二次更新尝试失败", e);
                        }
                        
                        return ResponseEntity.ok("订单状态更新异常，当前状态仍为：" + updatedOrder.getStatus());
                    }
                } else {
                    return ResponseEntity.ok("订单状态更新后无法查询到订单");
                }
            } else {
                log.error("直接SQL更新订单状态失败, orderNo={}", orderNo);
                return ResponseEntity.badRequest().body("直接SQL更新订单状态失败");
            }
        } catch (Exception e) {
            log.error("直接更新订单状态异常", e);
            return ResponseEntity.badRequest().body("更新订单状态异常：" + e.getMessage());
        }
    }
} 