package com.chua.starter.scheduler.support.executor;

import com.chua.starter.scheduler.properties.SchedulerJobPlatformProperties;
import org.springframework.beans.factory.ListableBeanFactory;

/**
 * 执行器解析上下文。
 */
public record SchedulerExecutorResolveContext(
        String namespace,
        SchedulerJobPlatformProperties.Namespace namespaceProperties,
        ListableBeanFactory beanFactory
) {
}
