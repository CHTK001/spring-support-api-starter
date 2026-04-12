package com.chua.starter.scheduler.support.executor;

import com.chua.common.support.core.annotation.Spi;
import com.chua.starter.scheduler.properties.SchedulerJobPlatformProperties;

import java.util.List;

/**
 * 静态执行器解析器。
 */
@Spi({"STATIC", "static"})
public class StaticSchedulerExecutorResolver implements SchedulerExecutorResolver {

    @Override
    public List<SchedulerJobPlatformProperties.Executor> resolve(SchedulerExecutorResolveContext context) {
        if (context == null || context.namespaceProperties() == null) {
            return List.of();
        }
        return context.namespaceProperties().getExecutors()
                .stream()
                .filter(this::hasUsableBaseUrl)
                .toList();
    }

    private boolean hasUsableBaseUrl(SchedulerJobPlatformProperties.Executor executor) {
        return executor != null
                && executor.getBaseUrl() != null
                && !executor.getBaseUrl().trim().isEmpty();
    }
}
