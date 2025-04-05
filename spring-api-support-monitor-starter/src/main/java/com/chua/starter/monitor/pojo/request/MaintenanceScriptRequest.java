package com.chua.starter.monitor.pojo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 维护脚本请求对象
 *
 * @author CH
 */
@Data
@Schema(description = "维护脚本请求")
public class MaintenanceScriptRequest {

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
     * 脚本描述
     */
    @Schema(description = "脚本描述")
    private String maintenanceScriptDesc;

    /**
     * 状态;0:停用;1:启用
     */
    @Schema(description = "状态;0:停用;1:启用")
    private Integer maintenanceScriptStatus;
}