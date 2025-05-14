package com.xcl.venueserver.dto;

import lombok.Data;

/**
 * 支付宝支付请求参数
 */
@Data
public class AliPayDTO {
    /**
     * 订单号
     */
    private String orderNo;
    
    /**
     * 订单名称
     */
    private String subject;
    
    /**
     * 付款金额
     */
    private String totalAmount;
    
    /**
     * 商品描述
     */
    private String body;
} 