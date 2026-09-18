package com.chua.starter.scheduler.support.executor;

import com.chua.starter.scheduler.properties.SchedulerJobPlatformProperties;

import java.util.List;

/**
 * 调度执行器解析器。
 * <p>
 * 按命名空间配置解析远程执行器列表。实现类可通过 SPI
 * 注册不同的解析策略（如静态配置、发现服务）。
 * </p>
 *
 * @since 4.0.0
 * @author CH
 */
public interface SchedulerExecutorResolver {

    /**
     * 解析命名空间下的远程执行器。
     *
     * @param context 解析上下文
     * @return 执行器列表
     */
    List<SchedulerJobPlatformProperties.Executor> resolve(SchedulerExecutorResolveContext context);
}
