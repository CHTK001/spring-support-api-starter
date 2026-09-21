package com.chua.starter.scheduler.dto;

/**
 * 调度任务更新请求。
 * <p>用于控制台或平台 API 部分更新任务的调度配置,
 * 仅需要传需要修改的字段。</p>
 *
 * @param cronExpression cron 表达式或调度间隔配置
 * @param enabled 是否启用任务
 *
 * @since 4.0.0.42
 * @author CH
 */
public record SchedulerTaskUpdateRequest(
        String cronExpression,
        Boolean enabled
) {
}
