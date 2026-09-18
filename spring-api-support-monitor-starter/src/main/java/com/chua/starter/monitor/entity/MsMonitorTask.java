package com.chua.starter.monitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 采集任务实体，定义针对监控资产的定时采集任务，包括健康检查、指标采集、告警等。
 *
 * @since 2026-08-11
 * @author CH
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ms_monitor_task")
@ApiModel(description = "采集任务")
@Schema(description = "采集任务")
public class MsMonitorTask extends SysBase {

    /**
     * 采集任务主键标识，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty("主键ID")
    @Schema(description = "主键ID")
    /** 标识 */
    private Long id;

    /**
    * 采集任务名称，用于前端展示
     */
    @TableField("task_name")
    @ApiModelProperty("任务名称")
    @Schema(description = "任务名称")
    /** 任务名称 */
    private String taskName;

    /**
    * 关联的监控资产标识
     */
    @TableField("monitor_id")
    @ApiModelProperty("监控资产ID")
    @Schema(description = "监控资产ID")
    /** 监控id */
    private Long monitorId;

    /**
    * 任务类型，取值 健康/COLLECT/ALERT/习俗
     */
    @TableField("task_type")
    @ApiModelProperty("任务类型（HEALTH/COLLECT/ALERT/CUSTOM）")
    @Schema(description = "任务类型")
    /** 任务类型 */
    private String taskType;

    /**
    * 任务Cron表达式，例如 0/30 * * * * ?
     */
    @TableField("task_cron")
    @ApiModelProperty("Cron表达式")
    @Schema(description = "Cron表达式")
    /** 任务cron */
    private String taskCron;

    /**
    * 任务启用状态，1=启用，0=禁用
     */
    @TableField("task_enabled")
    @ApiModelProperty("启用状态（0禁用/1启用）")
    @Schema(description = "启用状态（0禁用/1启用）")
    /** 任务是否启用 */
    private Integer taskEnabled;

    /**
    * 任务配置JSON，记录任务参数
     */
    @TableField("task_config")
    @ApiModelProperty("任务配置JSON")
    @Schema(description = "任务配置JSON")
    /** 任务配置 */
    private String taskConfig;

    /**
    * 任务运行状态，取值 RUNNING/STOPPED/错误
     */
    @TableField("task_status")
    @ApiModelProperty("运行状态（RUNNING/STOPPED/ERROR）")
    @Schema(description = "运行状态")
    /** 任务状态 */
    private String taskStatus;

    /**
    * 任务描述信息，可为空
     */
    @TableField("task_description")
    @ApiModelProperty("描述")
    @Schema(description = "描述")
    /** 任务描述 */
    private String taskDescription;

    /**
    * 逻辑删除标记，0=未删除，1=已删除
     */
    @TableField("deleted")
    @TableLogic
    @ApiModelProperty("逻辑删除")
    @Schema(description = "逻辑删除")
    /** 删除标记 */
    private Integer deleted;
}
