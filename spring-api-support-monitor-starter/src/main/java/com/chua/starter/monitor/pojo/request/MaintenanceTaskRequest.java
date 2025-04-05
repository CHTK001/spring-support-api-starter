package com.chua.starter.monitor.pojo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 维护任务请求
 *
 * @author CH
 */
@Data
@Schema(description = "维护任务请求")
public class MaintenanceTaskRequest {

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
     * 目标路径
     */
    @Schema(description = "目标路径")
    private String targetPath;
}