package com.chua.payment.support.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chua.payment.support.entity.PaymentGlobalConfig;
import com.chua.payment.support.mapper.PaymentGlobalConfigMapper;
import com.chua.payment.support.service.PaymentGlobalConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        return config != null ? config : getDefaultConfig();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentGlobalConfig saveOrUpdate(PaymentGlobalConfig config) {
        if (config.getConfigKey() == null) {
            config.setConfigKey("DEFAULT");
        }
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

    private PaymentGlobalConfig getDefaultConfig() {
        PaymentGlobalConfig config = new PaymentGlobalConfig();
        config.setConfigKey("DEFAULT");
        config.setPaymentCallbackPathTemplate("/{orderNo}/{merchantId}");
        config.setPaymentAutoRefreshSeconds(60);
        return config;
    }
}
