package com.chua.starter.monitor.starter.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 任务管理器配置类
 * 启用定时任务功能
 *
 * @author CH
 * @since 2024/12/28
 */
@Slf4j
@Configuration
@EnableScheduling
public class TaskManagerConfig {

    public TaskManagerConfig() {
        log.info("任务管理器配置初始化完成");
    }
}
