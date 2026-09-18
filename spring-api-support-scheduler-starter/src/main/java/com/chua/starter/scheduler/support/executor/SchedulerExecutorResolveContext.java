package com.chua.starter.scheduler.support.executor;

import com.chua.starter.scheduler.properties.SchedulerJobPlatformProperties;
import org.springframework.beans.factory.ListableBeanFactory;

/**
 * 调度执行器解析上下文。
 *
 * @param namespace           命名空间
 * @param namespaceProperties 命名空间配置
 * @param beanFactory         Bean 工厂
 * @since 4.0.0
 * @author CH
 */
public record SchedulerExecutorResolveContext(
        String namespace,
        SchedulerJobPlatformProperties.Namespace namespaceProperties,
        ListableBeanFactory beanFactory) {

}
