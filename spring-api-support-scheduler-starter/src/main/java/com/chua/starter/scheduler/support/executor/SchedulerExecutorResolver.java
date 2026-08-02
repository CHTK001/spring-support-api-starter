package com.chua.starter.scheduler.support.executor;

import com.chua.common.support.spi.annotations.Spi;
import com.chua.common.support.spi.annotations.SpiDescribe;
import com.chua.starter.scheduler.properties.SchedulerJobPlatformProperties;

import java.util.List;

/**
 * 调度中心执行器来源解析 SPI。
 */
@Spi
@SpiDescribe("调度中心执行器来源解析 SPI")
public interface SchedulerExecutorResolver {

    /**
     * 按命名空间配置解析可用执行器。
     */
    List<SchedulerJobPlatformProperties.Executor> resolve(SchedulerExecutorResolveContext context);
}
