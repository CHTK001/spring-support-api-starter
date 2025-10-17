package com.chua.starter.monitor.configuration;

import com.chua.starter.monitor.properties.AcmeProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import com.chua.starter.monitor.service.impl.AcmeCertificateServiceImpl;

/**
 * ACME 自动装配与调度
 *
 * 作者: CH
 * 创建时间: 2025-10-17
 * 版本: 1.0.0
 */
@Configuration
@EnableScheduling
@EnableConfigurationProperties(AcmeProperties.class)
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "monitor.acme", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AcmeConfiguration {

    private final AcmeCertificateServiceImpl acmeCertificateService;

    /** 每天凌晨 3 点检查到期证书进行续签 */
    @Scheduled(cron = "0 0 3 * * ?")
    public void scheduleAutoRenew() {
        acmeCertificateService.checkAndRenewSoonExpired();
    }
}
