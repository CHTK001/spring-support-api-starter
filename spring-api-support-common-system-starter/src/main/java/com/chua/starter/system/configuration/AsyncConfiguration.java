package com.chua.starter.system.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务配置
 * <p>
 * 提供通用的异步任务线程池，用于AppKey使用统计更新等异步操作
 * </p>
 *
 * @author CH
 * @since 2024/12/28
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfiguration {

    /**
     * 核心线程数
     */
    private static final int CORE_POOL_SIZE = 4;

    /**
     * 最大线程数
     */
    private static final int MAX_POOL_SIZE = 16;

    /**
     * 队列容量
     */
    private static final int QUEUE_CAPACITY = 500;

    /**
     * 线程存活时间（秒）
     */
    private static final int KEEP_ALIVE_SECONDS = 60;

    /**
     * 关闭等待时间（秒）
     */
    private static final int AWAIT_TERMINATION_SECONDS = 60;

    /**
     * 线程名前缀
     */
    private static final String THREAD_NAME_PREFIX = "async-task-";

    /**
     * 任务执行器线程池
     * <p>
     * 核心配置：
     * <ul>
     *     <li>核心线程数：{@value #CORE_POOL_SIZE}</li>
     *     <li>最大线程数：{@value #MAX_POOL_SIZE}</li>
     *     <li>队列容量：{@value #QUEUE_CAPACITY}</li>
     *     <li>拒绝策略：CallerRunsPolicy（调用者线程执行）</li>
     * </ul>
     * </p>
     *
     * @return 线程池执行器
     */
    @Bean("taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CORE_POOL_SIZE);
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        executor.setQueueCapacity(QUEUE_CAPACITY);
        executor.setThreadNamePrefix(THREAD_NAME_PREFIX);
        executor.setKeepAliveSeconds(KEEP_ALIVE_SECONDS);
        // 拒绝策略：由调用线程处理，防止任务丢失
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(AWAIT_TERMINATION_SECONDS);
        
        executor.initialize();
        log.info("异步任务线程池初始化完成: corePoolSize={}, maxPoolSize={}, queueCapacity={}",
                CORE_POOL_SIZE, MAX_POOL_SIZE, QUEUE_CAPACITY);
        return executor;
    }
}
