package com.chua.starter.monitor.pojo.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 维护脚本响应对象
 *
 * @author CH
 */
@Data
@Schema(description = "维护脚本响应")
public class MaintenanceScriptResponse {

    /**
     * 脚本ID
     */
    @Schema(description = "脚本ID")
    private Long maintenanceScriptId;

    /**
     * 维护组ID
     */
    @Schema(description = "维护组ID")
    private Long maintenanceGroupId;

    /**
     * 脚本名称
     */
    @Schema(description = "脚本名称")
    private String maintenanceScriptName;

    /**
     * 脚本内容
     */
    @Schema(description = "脚本内容")
    private String maintenanceScriptContent;

    /**
     * 脚本路径
     */
    @Schema(description = "脚本路径")
    private String maintenanceScriptPath;

    /**
     * 脚本状态：0-停用，1-启用
     */
    @Schema(description = "脚本状态：0-停用，1-启用")
    private Integer maintenanceScriptStatus;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime maintenanceScriptCreateTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime maintenanceScriptUpdateTime;
} 