package com.chua.starter.monitor.pojo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 维护主机请求对象
 *
 * @author CH
 */
@Data
@Schema(description = "维护主机请求")
public class MaintenanceHostRequest {

    /**
     * 维护主机ID
     */
    @Schema(description = "维护主机ID")
    private Long maintenanceHostId;

    /**
     * 维护组ID
     */
    @Schema(description = "维护组ID")
    private Long maintenanceGroupId;

    /**
     * 主机地址
     */
    @Schema(description = "主机地址")
    private String maintenanceHostAddress;

    /**
     * 主机端口
     */
    @Schema(description = "主机端口")
    private Integer maintenanceHostPort;

    /**
     * 用户名
     */
    @Schema(description = "用户名")
    private String maintenanceHostUsername;

    /**
     * 密码
     */
    @Schema(description = "密码")
    private String maintenanceHostPassword;

    /**
     * 是否启用
     */
    @Schema(description = "是否启用")
    private Boolean maintenanceHostEnabled;
}