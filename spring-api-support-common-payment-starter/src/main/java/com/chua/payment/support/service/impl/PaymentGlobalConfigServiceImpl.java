package com.chua.payment.support.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chua.payment.support.entity.PaymentGlobalConfig;
import com.chua.payment.support.mapper.PaymentGlobalConfigMapper;
import com.chua.payment.support.service.PaymentGlobalConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 支付全局配置服务实现
 */
@Service
@RequiredArgsConstructor
public class PaymentGlobalConfigServiceImpl implements PaymentGlobalConfigService {

    private final PaymentGlobalConfigMapper globalConfigMapper;

    @Override
    public PaymentGlobalConfig getConfig() {
        PaymentGlobalConfig config = globalConfigMapper.selectOne(
                new LambdaQueryWrapper<PaymentGlobalConfig>()
                        .eq(PaymentGlobalConfig::getConfigKey, "DEFAULT")
                        .last("limit 1"));
        if (config != null) {
            fillSampleUrls(config);
            return config;
        }
        return getDefaultConfig();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentGlobalConfig saveOrUpdate(PaymentGlobalConfig config) {
        if (config.getConfigKey() == null) {
            config.setConfigKey("DEFAULT");
        }
        fillSampleUrls(config);
        PaymentGlobalConfig existing = globalConfigMapper.selectOne(
                new LambdaQueryWrapper<PaymentGlobalConfig>()
                        .eq(PaymentGlobalConfig::getConfigKey, config.getConfigKey())
                        .last("limit 1"));
        if (existing != null) {
            config.setId(existing.getId());
            globalConfigMapper.updateById(config);
        } else {
            globalConfigMapper.insert(config);
        }
        return getConfig();
    }

    @Override
    public String getNotifyBaseUrl() {
        PaymentGlobalConfig config = getConfig();
        return config.getPaymentNotifyBaseUrl();
    }

    @Override
    public String getReturnUrl() {
        PaymentGlobalConfig config = getConfig();
        return config.getPaymentReturnUrl();
    }

    @Override
    public String buildPayNotifyUrl(String orderNo, Long merchantId) {
        PaymentGlobalConfig config = getConfig();
        String baseUrl = config.getPaymentNotifyBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            return null;
        }
        String template = config.getPaymentCallbackPathTemplate();
        if (template == null || template.isBlank()) {
            template = "/{orderNo}/{merchantId}";
        }
        String path = template
                .replace("{orderNo}", orderNo)
                .replace("{merchantId}", String.valueOf(merchantId));
        return baseUrl + path;
    }

    /**
     * 根据基础地址补充示例回调 URL
     */
    private void fillSampleUrls(PaymentGlobalConfig config) {
        String baseUrl = config.getPaymentNotifyBaseUrl();
        if (!StringUtils.hasText(baseUrl)) {
            return;
        }
        baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;

        config.setPaymentSamplePayNotifyUrl(
                baseUrl + "/api/notify/wechat/pay/20250101120000/1001");
        config.setPaymentSampleRefundNotifyUrl(
                baseUrl + "/api/notify/wechat/refund/RF20250101120000/1001");
        config.setPaymentSampleWalletRechargeNotifyUrl(
                baseUrl + "/api/notify/wallet/alipay/RECHARGE/20250101120000");
        config.setPaymentSampleWalletTransferNotifyUrl(
                baseUrl + "/api/notify/wallet/alipay/TRANSFER/TS20250101120000");
        config.setPaymentSampleWalletWithdrawNotifyUrl(
                baseUrl + "/api/notify/wallet/alipay/WITHDRAW/WD20250101120000");
    }

    private PaymentGlobalConfig getDefaultConfig() {
        PaymentGlobalConfig config = new PaymentGlobalConfig();
        config.setConfigKey("DEFAULT");
        config.setPaymentCallbackPathTemplate("/{orderNo}/{merchantId}");
        config.setPaymentAutoRefreshSeconds(60);
        return config;
    }
}
