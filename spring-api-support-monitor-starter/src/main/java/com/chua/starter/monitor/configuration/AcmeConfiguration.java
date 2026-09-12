package com.chua.starter.monitor.configuration;

import com.chua.starter.common.support.serviceinfo.ServiceInfoContributor;
import com.chua.starter.monitor.properties.AcmeProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
* ACME 自动装配配置，按属性 {@code monitor.acme.enabled} 控制是否启用，
* 注册监控模块服务信息。
*
* <p>证书自动续签的定时调度统一由
* {@link com.chua.starter.monitor.job.AcmeCertificateRenewJob} 承担，
* 执行时间可通过 {@code monitor.acme.renew-cron} 配置，避免调度重复。</p>
*
* @since 2025-10-17
* @author CH
*/
@Configuration
@EnableScheduling
@EnableConfigurationProperties(AcmeProperties.class)
@ConditionalOnProperty(prefix = "monitor.acme", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AcmeConfiguration {

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
