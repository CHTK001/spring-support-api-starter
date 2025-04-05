package com.chua.starter.monitor.pojo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 维护组请求对象
 *
 * @author CH
 */
@Data
@Schema(description = "维护组请求")
public class MaintenanceGroupRequest {

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
}