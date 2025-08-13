package com.chua.starter.monitor.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 任务管理器配置类 启用定时任务功能和异步任务功能
 *
 * @author CH
 * @since 2024/12/28
 */
@Slf4j
@Configuration
@EnableScheduling
@EnableAsync
public class TaskManagerConfig {

    public TaskManagerConfig() {
        log.info("任务管理器配置初始化完成");
    }

    /**
     * 文件系统任务执行器 用于处理文件合并等异步任务
     *
     * @return 线程池任务执行器
     */
    @Bean("fileSystemTaskExecutor")
    public Executor fileSystemTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数
        executor.setCorePoolSize(2);

        // 最大线程数
        executor.setMaxPoolSize(10);

        // 队列容量
        executor.setQueueCapacity(100);

        // 线程名前缀
        executor.setThreadNamePrefix("FileSystem-");

        // 线程空闲时间（秒）
        executor.setKeepAliveSeconds(60);

        // 拒绝策略：调用者运行
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());

        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);

        // 等待时间
        executor.setAwaitTerminationSeconds(60);

        // 初始化
        executor.initialize();

        log.info("文件系统任务执行器配置完成: corePoolSize={}, maxPoolSize={}, queueCapacity={}", executor.getCorePoolSize(),
                executor.getMaxPoolSize(), executor.getQueueCapacity());

        return executor;
    }
}
