package com.xcl.venueserver.config;

import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.kernel.Config;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 支付宝配置类
 */
@Data
@Component
@ConfigurationProperties(prefix = "alipay")
public class AliPayConfig {
    // 应用ID
    private String appId;
    // 应用私钥
    private String appPrivateKey;
    // 支付宝公钥
    private String alipayPublicKey;
    // 异步通知回调地址
    private String notifyUrl;
    // 支付成功后的跳转地址
    private String returnUrl;
    
    // 支付宝网关
    private final String URL = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";
    // 签名方式
    private final String SIGN_TYPE = "RSA2";
    // 字符编码
    private final String CHARSET = "UTF-8";
    // 格式
    private final String FORMAT = "json";
    
    /**
     * 初始化支付宝SDK
     */
    @PostConstruct
    public void init() {
        // 设置参数（全局只需设置一次）
        Config options = getOptions();
        Factory.setOptions(options);
    }
    
    private Config getOptions() {
        Config config = new Config();
        config.protocol = "https";
        config.gatewayHost = "openapi.alipaydev.com";
        config.signType = SIGN_TYPE;
        config.appId = this.appId;
        config.merchantPrivateKey = this.appPrivateKey;
        config.alipayPublicKey = this.alipayPublicKey;
        config.notifyUrl = this.notifyUrl;
        return config;
    }
} 