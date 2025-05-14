package com.xcl.venueserver.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.xcl.venueserver.common.utils.Result;
import com.xcl.venueserver.config.AliPayConfig;
import com.xcl.venueserver.dto.AliPayDTO;
import com.xcl.venueserver.entity.BookingOrder;
import com.xcl.venueserver.service.BookingOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付宝支付控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/alipay")
public class AliPayController {

    @Resource
    private AliPayConfig aliPayConfig;
    
    @Resource
    private BookingOrderService bookingOrderService;

    /**
     * 支付接口
     */
    @PostMapping(value = "/pay", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> payHtml(@RequestBody AliPayDTO aliPayDTO) {
        try {
            // 输出配置信息以进行调试
            log.info("支付宝配置信息 - AppID: {}, NotifyUrl: {}, ReturnUrl: {}", 
                    aliPayConfig.getAppId(), 
                    aliPayConfig.getNotifyUrl(), 
                    aliPayConfig.getReturnUrl());
                    
            // 验证AppID是否已配置
            if (aliPayConfig.getAppId() == null || aliPayConfig.getAppId().isEmpty()) {
                log.error("支付宝AppID未配置");
                return ResponseEntity.badRequest()
                    .body("<div style='color:red'>支付宝AppID未配置，请联系管理员</div>");
            }
            
            // 1. 创建支付宝客户端
            AlipayClient alipayClient = new DefaultAlipayClient(
                    aliPayConfig.getURL(),
                    aliPayConfig.getAppId(),
                    aliPayConfig.getAppPrivateKey(),
                    aliPayConfig.getFORMAT(),
                    aliPayConfig.getCHARSET(),
                    aliPayConfig.getAlipayPublicKey(),
                    aliPayConfig.getSIGN_TYPE());

            // 2. 创建API对应的request
            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
            
            // 设置回调地址
            request.setNotifyUrl(aliPayConfig.getNotifyUrl()); // 异步回调地址
            request.setReturnUrl(aliPayConfig.getReturnUrl()); // 同步返回地址
            
            // 对订单参数进行中文转码处理，确保中文内容正确
            String subject = aliPayDTO.getSubject();
            if (subject != null) {
                // 确保中文内容正确编码
                subject = new String(subject.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
            }
            
            // 3. 组装订单参数
            HashMap<String, Object> bizContent = new HashMap<>();
            bizContent.put("out_trade_no", aliPayDTO.getOrderNo());        // 商户订单号
            bizContent.put("total_amount", aliPayDTO.getTotalAmount());    // 订单总金额
            bizContent.put("subject", subject);                            // 订单标题
            bizContent.put("body", aliPayDTO.getBody());                   // 商品描述
            bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");      // 销售产品码，电脑网站支付使用固定值
            bizContent.put("timeout_express", "15m");                      // 设置订单超时时间
            
            // 4. 将订单参数转为JSON字符串并设置 - 使用标准JSON库，避免转义问题
            String bizContentStr = com.alibaba.fastjson.JSON.toJSONString(bizContent);
            request.setBizContent(bizContentStr);

            // 记录请求的完整信息
            log.info("支付宝请求参数: {}", bizContentStr);
            log.info("支付宝请求URL: {}", aliPayConfig.getURL());
            log.info("支付宝请求AppID: {}", aliPayConfig.getAppId());
            
            // 5. 发起API调用并获取支付表单HTML内容
            String form = alipayClient.pageExecute(request).getBody();
            
            // 记录表单信息
            log.info("支付宝返回表单HTML长度: {}, 内容前100字符: {}", 
                    form.length(), 
                    form.length() > 100 ? form.substring(0, 100) + "..." : form);
            
            return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(form);
        } catch (AlipayApiException e) {
            log.error("支付宝下单失败", e);
            return ResponseEntity.badRequest()
                .body("<div style='color:red'>支付宝下单失败: " + e.getMessage() + "</div>");
        }
    }

    /**
     * 支付接口 - 使用Result包装返回，用于兼容旧代码
     */
    @PostMapping(value = "/pay", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result<String> pay(@RequestBody AliPayDTO aliPayDTO) {
        ResponseEntity<String> response = payHtml(aliPayDTO);
        
        if (response.getStatusCode().is2xxSuccessful()) {
            return Result.success(response.getBody());
        } else {
            return Result.error(response.getBody().replace("<div style='color:red'>", "").replace("</div>", ""));
        }
    }

    /**
     * 支付宝异步回调接口
     * 此接口无需权限验证，已在过滤器和拦截器中排除
     */
    @PostMapping("/notify")
    public String payNotify(HttpServletRequest request) {
        log.info("进入支付宝异步回调");
        
        // 1. 获取所有请求参数
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        
        // 2. 将请求参数转换为Map
        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }
        
        // 输出所有回调参数用于调试
        log.info("支付宝异步回调完整参数: {}", params);
        
        try {
            // 3. 验证签名
            boolean signVerified = AlipaySignature.rsaCheckV1(
                    params, 
                    aliPayConfig.getAlipayPublicKey(),
                    aliPayConfig.getCHARSET(),
                    aliPayConfig.getSIGN_TYPE()
            );
            
            if (signVerified) {
                // 4. 验签成功，处理业务逻辑
                log.info("支付宝异步回调签名验证成功");
                
                // 获取交易状态
                String tradeStatus = params.get("trade_status");
                log.info("支付宝异步回调交易状态: {}", tradeStatus);
                
                // 获取订单号
                String outTradeNo = params.get("out_trade_no");
                log.info("支付宝异步回调订单号: {}", outTradeNo);
                
                // 获取交易号
                String tradeNo = params.get("trade_no");
                log.info("支付宝异步回调交易号: {}", tradeNo);
                
                // 获取总金额
                String totalAmount = params.get("total_amount");
                log.info("支付宝异步回调金额: {}", totalAmount);
                
                // 查询当前订单状态
                try {
                    BookingOrder order = bookingOrderService.getBookingOrderByOrderNo(outTradeNo);
                    if (order != null) {
                        log.info("支付宝异步回调: 订单 {} 当前状态为 {}", outTradeNo, order.getStatus());
                    } else {
                        log.error("支付宝异步回调: 未找到订单 {}", outTradeNo);
                    }
                } catch (Exception e) {
                    log.error("支付宝异步回调: 查询订单时异常", e);
                }
                
                // 判断交易状态
                if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                    try {
                        // 支付成功，更新订单状态为已支付(2)
                        boolean updateResult = bookingOrderService.updateOrderStatusByOrderNo(outTradeNo, 2);
                        
                        if (updateResult) {
                            log.info("订单 {} 支付成功，已更新状态为已支付", outTradeNo);
                        } else {
                            log.error("订单 {} 支付成功，但更新状态失败，尝试使用直接SQL更新", outTradeNo);
                            
                            // 尝试使用直接SQL更新方法
                            int directUpdateResult = bookingOrderService.directUpdateStatusByOrderNo(outTradeNo, 2);
                            if (directUpdateResult > 0) {
                                log.info("订单 {} 支付成功，已通过直接SQL更新状态为已支付", outTradeNo);
                                updateResult = true; // 标记为更新成功
                            } else {
                                log.error("订单 {} 支付成功，但直接SQL更新状态也失败", outTradeNo);
                            }
                        }
                        
                        // 再次查询订单状态，确认是否更新成功
                        try {
                            BookingOrder order = bookingOrderService.getBookingOrderByOrderNo(outTradeNo);
                            if (order != null) {
                                log.info("支付宝异步回调: 更新后订单 {} 状态为 {}", outTradeNo, order.getStatus());
                                
                                // 如果状态仍然是待支付，再次尝试直接更新
                                if (order.getStatus() == 1) {
                                    log.warn("订单状态仍为待支付，再次尝试直接更新: {}", outTradeNo);
                                    int retryResult = bookingOrderService.directUpdateStatusByOrderNo(outTradeNo, 2);
                                    log.info("再次直接更新结果: {}, 订单号: {}", retryResult > 0 ? "成功" : "失败", outTradeNo);
                                    
                                    // 更新后再次查询状态
                                    BookingOrder retryOrder = bookingOrderService.getBookingOrderByOrderNo(outTradeNo);
                                    if (retryOrder != null) {
                                        log.info("最终订单状态: {}, 订单号: {}", retryOrder.getStatus(), outTradeNo);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            log.error("支付宝异步回调: 更新后查询订单时异常", e);
                        }
                    } catch (Exception e) {
                        log.error("处理支付成功回调时异常", e);
                        
                        // 异常情况下，尝试直接SQL更新
                        try {
                            log.info("异常处理中，尝试直接SQL更新订单状态: {}", outTradeNo);
                            int directResult = bookingOrderService.directUpdateStatusByOrderNo(outTradeNo, 2);
                            log.info("异常处理中直接更新结果: {}, 订单号: {}", directResult > 0 ? "成功" : "失败", outTradeNo);
                        } catch (Exception ex) {
                            log.error("异常处理中尝试直接更新也失败", ex);
                        }
                    }
                } else {
                    log.warn("支付宝异步回调: 交易状态 {} 不是支付成功状态，不更新订单状态", tradeStatus);
                }
                
                // 返回成功，支付宝将不再重发通知
                return "success";
            } else {
                // 验签失败
                log.error("支付宝异步回调签名验证失败");
                return "fail";
            }
        } catch (AlipayApiException e) {
            log.error("支付宝异步回调异常", e);
            return "fail";
        }
    }

    /**
     * 支付宝同步回调接口 - 前端可直接跳转到配置的returnUrl
     * 用于用户支付完成后页面跳转，不用作业务处理，业务处理请使用异步回调
     */
    @GetMapping("/return")
    public void payReturn(HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.info("进入支付宝同步回调");
        
        // 重定向到配置的前端页面
        response.sendRedirect(aliPayConfig.getReturnUrl());
    }

    /**
     * 支付测试接口 - 用于测试支付配置是否正确
     * 此接口仅返回一个包含AppID的测试表单，不会真实发起支付
     */
    @GetMapping(value = "/test", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> test() {
        log.info("进入支付宝测试接口");
        
        try {
            // 验证配置是否正确
            if (aliPayConfig.getAppId() == null || aliPayConfig.getAppId().isEmpty()) {
                log.error("支付宝AppID未配置");
                return ResponseEntity.badRequest()
                    .body("<div style='color:red'>支付宝AppID未配置，请检查application.yml配置</div>");
            }
            
            // 记录配置信息
            log.info("支付宝配置 - AppID: {}, NotifyUrl: {}, ReturnUrl: {}", 
                    aliPayConfig.getAppId(), 
                    aliPayConfig.getNotifyUrl(), 
                    aliPayConfig.getReturnUrl());
            
            String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
            String outTradeNo = "test_order_" + System.currentTimeMillis();
            
            // 创建一个测试表单，包含所有必要的参数
            String testForm = String.format(
                "<form name='punchout_form' method='post' action='%s' target='_blank'>" +
                "  <input type='hidden' name='app_id' value='%s'>" +
                "  <input type='hidden' name='method' value='alipay.trade.page.pay'>" +
                "  <input type='hidden' name='charset' value='UTF-8'>" +
                "  <input type='hidden' name='sign_type' value='RSA2'>" +
                "  <input type='hidden' name='timestamp' value='%s'>" +
                "  <input type='hidden' name='version' value='1.0'>" +
                "  <input type='hidden' name='notify_url' value='%s'>" +
                "  <input type='hidden' name='return_url' value='%s'>" +
                "  <input type='hidden' name='biz_content' value='{\"out_trade_no\":\"%s\",\"total_amount\":\"0.01\",\"subject\":\"测试订单\",\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}'>" +
                "  <input type='submit' value='立即支付'>" +
                "</form>",
                aliPayConfig.getURL(),
                aliPayConfig.getAppId(),
                timestamp,
                aliPayConfig.getNotifyUrl(),
                aliPayConfig.getReturnUrl(),
                outTradeNo
            );
            
            log.info("生成测试表单成功 - 长度: {}", testForm.length());
            
            // 提示用户测试表单缺少签名
            log.warn("提示：测试表单中缺少签名(sign)参数，这只是一个结构演示，不能正常使用，请使用正式API进行支付测试");
            
            return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(testForm);
        } catch (Exception e) {
            log.error("生成测试表单失败", e);
            return ResponseEntity.badRequest()
                .body("<div style='color:red'>生成测试表单失败: " + e.getMessage() + "</div>");
        }
    }

    /**
     * 测试回调 - 模拟支付宝异步通知用于测试
     * 此接口用于手动模拟支付宝回调，便于测试
     */
    @GetMapping("/mock-notify")
    public String mockNotify(@RequestParam String orderNo) {
        log.info("进入模拟支付宝回调接口，订单号: {}", orderNo);
        
        try {
            // 查询订单
            BookingOrder order = bookingOrderService.getBookingOrderByOrderNo(orderNo);
            
            if (order == null) {
                log.error("模拟回调：订单 {} 不存在", orderNo);
                return "订单不存在，回调测试失败";
            }
            
            log.info("模拟回调：订单 {} 当前状态为 {}", orderNo, order.getStatus());
            
            // 更新订单状态为已支付(2)
            boolean updateResult = bookingOrderService.updateOrderStatusByOrderNo(orderNo, 2);
            
            if (updateResult) {
                log.info("模拟回调：订单 {} 支付成功，已更新状态为已支付", orderNo);
                return "模拟回调成功：订单状态已更新为已支付";
            } else {
                log.error("模拟回调：订单 {} 状态更新失败，尝试使用直接SQL更新", orderNo);
                
                // 尝试使用直接SQL更新
                int directResult = bookingOrderService.directUpdateStatusByOrderNo(orderNo, 2);
                
                if (directResult > 0) {
                    log.info("模拟回调：订单 {} 通过直接SQL更新状态成功", orderNo);
                    
                    // 再次查询确认状态
                    BookingOrder updatedOrder = bookingOrderService.getBookingOrderByOrderNo(orderNo);
                    if (updatedOrder != null && updatedOrder.getStatus() == 2) {
                        log.info("模拟回调：确认订单 {} 状态已更新为 {}", orderNo, updatedOrder.getStatus());
                        return "模拟回调成功：订单状态已通过直接SQL更新为已支付";
                    } else if (updatedOrder != null) {
                        log.warn("模拟回调：订单 {} 状态仍为 {}，可能存在并发或缓存问题", orderNo, updatedOrder.getStatus());
                        return "模拟回调警告：SQL更新执行成功，但状态可能未立即生效，当前状态：" + updatedOrder.getStatus();
                    }
                    
                    return "模拟回调成功：SQL更新执行成功，但未能确认最终状态";
                } else {
                    log.error("模拟回调：订单 {} 直接SQL更新也失败", orderNo);
                    return "模拟回调失败：订单状态更新失败，直接SQL更新也失败";
                }
            }
        } catch (Exception e) {
            log.error("模拟回调处理异常", e);
            return "模拟回调异常：" + e.getMessage();
        }
    }
} 