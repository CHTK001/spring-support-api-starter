package com.chua.starter.scheduler.dto;

import lombok.Data;

/**
 * 调度任务更新请求。
 */
@Data
public class SchedulerTaskUpdateRequest {

    private String cronExpression;

    private Boolean enabled;
}
