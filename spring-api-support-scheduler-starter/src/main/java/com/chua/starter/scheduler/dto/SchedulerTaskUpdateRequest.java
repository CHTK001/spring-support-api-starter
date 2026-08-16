package com.chua.starter.scheduler.dto;

import lombok.Data;

/**
 * 调度任务更新请求。
 *
 * <p>用于控制台或平台 API 部分更新任务的调度配置,
 * 仅需要传需要修改的字段。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Data
public class SchedulerTaskUpdateRequest {

    /**
     * cron 表达式或调度间隔配置
     */
    private String cronExpression;

    /**
     * 是否启用任务
     */
    private Boolean enabled;
}
