package com.chua.starter.monitor.pojo.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 维护组响应对象
 *
 * @author CH
 */
@Data
@Schema(description = "维护组响应")
public class MaintenanceGroupResponse {

    /**
     * 维护组ID
     */
    @Schema(description = "维护组ID")
    private Long maintenanceGroupId;

    /**
     * 维护组名称
     */
    @Schema(description = "维护组名称")
    private String maintenanceGroupName;

    /**
     * 维护组描述
     */
    @Schema(description = "维护组描述")
    private String maintenanceGroupDescription;

    /**
     * 是否启用
     */
    @Schema(description = "是否启用")
    private Boolean maintenanceGroupEnabled;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime maintenanceGroupCreateTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime maintenanceGroupUpdateTime;
} 