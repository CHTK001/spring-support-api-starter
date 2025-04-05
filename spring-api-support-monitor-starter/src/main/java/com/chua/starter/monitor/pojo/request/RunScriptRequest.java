package com.chua.starter.monitor.pojo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 运行脚本请求对象
 *
 * @author CH
 */
@Data
@Schema(description = "运行脚本请求")
public class RunScriptRequest {

    /**
     * 维护组ID
     */
    @Schema(description = "维护组ID")
    private Long maintenanceGroupId;

    /**
     * 脚本ID
     */
    @Schema(description = "脚本ID")
    private Long maintenanceScriptId;

    /**
     * 维护主机ID，为空表示运行所有主机
     */
    @Schema(description = "维护主机ID，为空表示运行所有主机")
    private Long maintenanceHostId;
} 