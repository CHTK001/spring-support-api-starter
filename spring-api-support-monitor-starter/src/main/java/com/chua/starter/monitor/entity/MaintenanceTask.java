package com.chua.starter.monitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 维护任务
 *
 * @author CH
 */
@Data
@TableName("maintenance_task")
@Schema(description = "维护任务")
public class MaintenanceTask {

    /**
     * 任务ID
     */
    @TableId(value = "maintenance_task_id", type = IdType.AUTO)
    @Schema(description = "任务ID")
    private Integer maintenanceTaskId;

    /**
     * 维护组ID
     */
    @TableField("maintenance_group_id")
    @Schema(description = "维护组ID")
    private Integer maintenanceGroupId;

    /**
     * 维护主机ID
     */
    @TableField("maintenance_host_id")
    @Schema(description = "维护主机ID")
    private Integer maintenanceHostId;

    /**
     * 脚本ID
     */
    @TableField("maintenance_script_id")
    @Schema(description = "脚本ID")
    private Integer maintenanceScriptId;

    /**
     * 文件ID
     */
    @TableField("maintenance_file_id")
    @Schema(description = "文件ID")
    private Integer maintenanceFileId;

    /**
     * 任务类型;SCRIPT:脚本;FILE:文件
     */
    @TableField("maintenance_task_type")
    @Schema(description = "任务类型;SCRIPT:脚本;FILE:文件")
    private String maintenanceTaskType;

    /**
     * 任务状态;PENDING:待执行;RUNNING:执行中;SUCCESS:成功;FAILED:失败
     */
    @TableField("maintenance_task_status")
    @Schema(description = "任务状态;PENDING:待执行;RUNNING:执行中;SUCCESS:成功;FAILED:失败")
    private String maintenanceTaskStatus;

    /**
     * 任务结果
     */
    @TableField("maintenance_task_result")
    @Schema(description = "任务结果")
    private String maintenanceTaskResult;

    /**
     * 错误信息
     */
    @TableField("maintenance_task_error")
    @Schema(description = "错误信息")
    private String maintenanceTaskError;

    /**
     * 创建时间
     */
    @TableField("create_time")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 创建者
     */
    @TableField("create_by")
    @Schema(description = "创建者")
    private String createBy;

    /**
     * 更新者
     */
    @TableField("update_by")
    @Schema(description = "更新者")
    private String updateBy;
}