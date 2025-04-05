package com.chua.starter.monitor.pojo.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 维护主机响应对象
 *
 * @author CH
 */
@Data
@Schema(description = "维护主机响应")
public class MaintenanceHostResponse {

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
     * 是否启用
     */
    @Schema(description = "是否启用")
    private Boolean maintenanceHostEnabled;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime maintenanceHostCreateTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime maintenanceHostUpdateTime;
} 