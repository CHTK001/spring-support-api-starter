package com.chua.starter.collect.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 采集任务出参。
 *
 * @author CH
 * @since 4.0.0.42
 */
@Data
@Schema(description = "数据采集-任务出参")
public class CollectTaskVO {

    /**
     * 主键ID。
     */
    @Schema(description = "主键ID")
    private Long collectTaskId;

    /**
     * 任务号。
     */
    @Schema(description = "任务号")
    private String collectTaskNo;

    /**
     * 所属流程ID。
     */
    @Schema(description = "所属流程ID")
    private Integer collectTaskFlowId;

    /**
     * 流程名称快照。
     */
    @Schema(description = "流程名称快照")
    private String collectTaskFlowName;

    /**
     * 状态 RUNNING/SUCCESS/FAILURE/STOPPED。
     */
    @Schema(description = "状态")
    private String collectTaskStatus;

    /**
     * 触发方式 CRON/MANUAL/TEST。
     */
    @Schema(description = "触发方式")
    private String collectTaskTriggerType;

    /**
     * 本次执行入参覆盖（JSON）。
     */
    @Schema(description = "执行入参JSON")
    private String collectTaskParams;

    /**
     * 开始时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "开始时间")
    private LocalDateTime collectTaskBeginTime;

    /**
     * 结束时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "结束时间")
    private LocalDateTime collectTaskEndTime;

    /**
     * 耗时毫秒。
     */
    @Schema(description = "耗时毫秒")
    private Long collectTaskCost;

    /**
     * 执行节点次数。
     */
    @Schema(description = "执行节点次数")
    private Integer collectTaskNodeCount;

    /**
     * 发起抓取次数。
     */
    @Schema(description = "发起抓取次数")
    private Integer collectTaskRequestCount;

    /**
     * 产出记录数。
     */
    @Schema(description = "产出记录数")
    private Integer collectTaskRecordCount;

    /**
     * 失败原因。
     */
    @Schema(description = "失败原因")
    private String collectTaskError;

    /**
     * 执行节点标识。
     */
    @Schema(description = "执行节点标识")
    private String collectTaskExecutorId;

    /**
     * 是否仍在本机执行。
     */
    @Schema(description = "是否运行中")
    private Boolean running;

    /**
     * 创建人姓名。
     */
    @Schema(description = "创建人姓名")
    private String createName;
}
