package com.chua.starter.scheduler.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 调度平台任务视图。
 */
@Data
public class SchedulerTaskVO {

    private Long jobId;

    private String jobNo;

    private String taskKey;

    private String taskName;

    private String scheduleType;

    private String cronExpression;

    private Boolean enabled;

    private String description;

    private Boolean scheduled;

    private String dispatchMode;

    private String remoteExecutorAddress;

    private String storageMode;

    private LocalDateTime nextExecutionTime;

    private LocalDateTime lastStartedAt;

    private LocalDateTime lastFinishedAt;

    private String lastRunStatus;

    private String lastRunMessage;

    private Boolean deleted;
}
