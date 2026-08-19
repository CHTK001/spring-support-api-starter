package com.chua.starter.monitor.configuration;

import com.chua.starter.common.support.serviceinfo.ServiceInfoContributor;
import com.chua.starter.monitor.properties.AcmeProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import com.chua.starter.monitor.service.impl.AcmeCertificateServiceImpl;

/**
 * ACME 自动装配与调度配置，按属性 {@code monitor.acme.enabled} 控制是否启用，
 * 注册监控模块服务信息，并调度证书自动续签任务。
 *
 * @since 2025-10-17
 * @author CH
 */
@Configuration
@EnableScheduling
@EnableConfigurationProperties(AcmeProperties.class)
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "monitor.acme", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AcmeConfiguration {

    /**
     * 自动续签调度 Cron 表达式：每天凌晨 3 点执行
     */
    private static final String AUTO_RENEW_CRON = "0 0 3 * * ?";

    /**
     * ACME 证书服务实现，用于驱动自动续签调度
     */
    private final AcmeCertificateServiceImpl acmeCertificateService;

    /**
     * 每天凌晨 3 点检查到期证书并自动续签。
     */
    @Scheduled(cron = AUTO_RENEW_CRON)
    public void scheduleAutoRenew() {
        acmeCertificateService.checkAndRenewSoonExpired();
    }

    /**
     * 注册监控模块服务信息。
     *
     * @return 服务信息贡献器
     */
    @Bean
    public ServiceInfoContributor monitorServiceInfoContributor() {
        return registry -> registry.registerFlag("monitor", "监控", true, "ACME 证书监控模块");
    }
}
