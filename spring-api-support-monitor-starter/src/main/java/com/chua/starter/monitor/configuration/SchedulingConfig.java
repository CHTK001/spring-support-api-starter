package com.chua.starter.monitor.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 定时任务配置类
 * 启用Spring的定时任务功能
 *
 * @author CH
 * @since 2024/7/19
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
    // 配置类，启用@Scheduled注解
}