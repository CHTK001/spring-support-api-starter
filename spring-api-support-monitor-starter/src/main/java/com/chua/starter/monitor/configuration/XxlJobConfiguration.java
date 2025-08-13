package com.chua.starter.monitor.configuration;

import com.chua.starter.monitor.job.trigger.SchedulerTrigger;
import com.chua.starter.monitor.properties.ReportJobProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * xxl-job定时任务配置类
 * 启用Spring的定时任务功能
 *
 * @author CH
 * @since 2024/7/19
 */
@Configuration
@EnableConfigurationProperties(ReportJobProperties.class)
@ConditionalOnProperty(prefix = ReportJobProperties.PRE, name = "enable", havingValue = "true", matchIfMissing = true)
public class XxlJobConfiguration {

    /**
     * 定时任务触发器
     *
     * @return 定时任务触发器
     */
    @Bean
    @ConditionalOnMissingBean
    public SchedulerTrigger schedulerTrigger() {
        return new SchedulerTrigger();
    }
}