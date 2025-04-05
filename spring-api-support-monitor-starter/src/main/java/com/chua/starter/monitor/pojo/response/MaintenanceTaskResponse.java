package com.chua.starter.monitor.pojo.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 维护任务响应对象
 *
 * @author CH
 */
@Data
@Schema(description = "维护任务响应")
public class MaintenanceTaskResponse {

    /**
     * 任务ID
     */
    @Schema(description = "任务ID")
    private Integer maintenanceTaskId;

    /**
     * 维护组ID
     */
    @Schema(description = "维护组ID")
    private Integer maintenanceGroupId;

    /**
     * 维护主机ID
     */
    @Schema(description = "维护主机ID")
    private Integer maintenanceHostId;

    /**
     * 脚本ID
     */
    @Schema(description = "脚本ID")
    private Integer maintenanceScriptId;

    /**
     * 文件ID
     */
    @Schema(description = "文件ID")
    private Integer maintenanceFileId;

    /**
     * 任务类型;SCRIPT:脚本;FILE:文件
     */
    @Schema(description = "任务类型;SCRIPT:脚本;FILE:文件")
    private String maintenanceTaskType;

    /**
     * 任务状态;PENDING:待执行;RUNNING:执行中;SUCCESS:成功;FAILED:失败
     */
    @Schema(description = "任务状态;PENDING:待执行;RUNNING:执行中;SUCCESS:成功;FAILED:失败")
    private String maintenanceTaskStatus;

    /**
     * 任务结果
     */
    @Schema(description = "任务结果")
    private String maintenanceTaskResult;

    /**
     * 错误信息
     */
    @Schema(description = "错误信息")
    private String maintenanceTaskError;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
    
    /**
     * 主机地址（非数据库字段）
     */
    @Schema(description = "主机地址")
    private String hostAddress;
    
    /**
     * 脚本名称（非数据库字段）
     */
    @Schema(description = "脚本名称")
    private String scriptName;
    
    /**
     * 文件名称（非数据库字段）
     */
    @Schema(description = "文件名称")
    private String fileName;
} 