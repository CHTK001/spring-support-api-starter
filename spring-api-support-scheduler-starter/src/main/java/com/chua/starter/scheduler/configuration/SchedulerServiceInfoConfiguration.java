package com.chua.starter.scheduler.configuration;

import com.chua.starter.common.support.serviceinfo.ServiceInfoContributor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 定时任务服务信息配置
 *
 * <p>负责注册调度模块的服务信息。</p>
 *
   * @版本 1.0.0
 * @since 2025/5/8
 * @author CH
 */
@Configuration
public class SchedulerServiceInfoConfiguration {

    /**
     * 注册调度模块服务信息。
     *
     * @return 服务信息贡献器
     */
    @Bean
    public ServiceInfoContributor schedulerServiceInfoContributor() {
        return registry -> {
            registry.registerEndpoint("任务调度", "调度控制台", "/scheduler/**", "任务调度控制台");
            registry.registerFlag("scheduler", "任务调度", true, "任务调度模块");
        };
    }
}
