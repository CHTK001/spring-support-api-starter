package com.chua.payment.support.service;

import com.chua.payment.support.entity.PaymentGlobalConfig;

/**
 * 支付全局配置服务
 */
public interface PaymentGlobalConfigService {

    /**
     * 获取全局配置（默认 key = DEFAULT）
     */
    PaymentGlobalConfig getConfig();

    /**
     * 保存或更新全局配置
     */
    PaymentGlobalConfig saveOrUpdate(PaymentGlobalConfig config);

    /**
     * 获取支付回调基础地址
     */
    String getNotifyBaseUrl();

    /**
     * 获取默认回跳地址
     */
    String getReturnUrl();

    /**
     * 根据订单号和商户 ID 生成支付回调完整地址
     */
    String buildPayNotifyUrl(String orderNo, Long merchantId);
}
