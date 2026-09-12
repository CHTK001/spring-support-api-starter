package com.chua.starter.scheduler.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
* 调度平台任务视图。
*
* <p>面向平台管理页面与对外 API 返回的任务展示模型,
* 聚合任务基本信息与最近一次执行日志概览。</p>
*
* @since 4.0.0.42
* @author CH
 */
@Data
public class SchedulerTaskVO {

    /**
    * 任务 标识
     */
    private Long jobId;

    /**
    * 任务编号
     */
    private String jobNo;

    /**
    * 任务标识 (执行 Bean 或任务名称)
     */
    private String taskKey;

    /**
    * 任务名称
     */
    private String taskName;

    /**
    * 调度类型, 如 CRON / FIXED / 无
     */
    private String scheduleType;

    /**
    * cron 表达式或调度间隔配置
     */
    private String cronExpression;

    /**
    * 是否启用
     */
    private Boolean enabled;

    /**
    * 任务描述
     */
    private String description;

    /**
    * 是否已参与调度
     */
    private Boolean scheduled;

    /**
    * 分发模式
     */
    private String dispatchMode;

    /**
    * 远程执行器地址
     */
    private String remoteExecutorAddress;

    /**
    * 日志存储模式
     */
    private String storageMode;

    /**
    * 下次执行时间
     */
    private LocalDateTime nextExecutionTime;

    /**
    * 最近一次开始执行时间
     */
    private LocalDateTime lastStartedAt;

    /**
    * 最近一次结束执行时间
     */
    private LocalDateTime lastFinishedAt;

    /**
    * 最近一次运行状态
     */
    private String lastRunStatus;

    /**
    * 最近一次运行消息
     */
    private String lastRunMessage;

    /**
    * 是否已删除
     */
    private Boolean deleted;
}
