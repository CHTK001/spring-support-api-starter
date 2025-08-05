package com.chua.starter.monitor.pojo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 维护组请求对象
 *
 * @author CH
 */
@Data
@Schema(description = "维护组请求参数")
public class MaintenanceGroupRequest {

    /**
     * 维护组ID
     */
    @Schema(description = "维护组ID", example = "1")
    private Long maintenanceGroupId;

    /**
     * 维护组名称
     */
    @NotBlank(message = "维护组名称不能为空")
    @Size(max = 50, message = "维护组名称长度不能超过50个字符")
    @Schema(description = "维护组名称", example = "生产环境服务器组")
    private String maintenanceGroupName;

    /**
     * 维护组描述
     */
    @Size(max = 200, message = "维护组描述长度不能超过200个字符")
    @Schema(description = "维护组描述", example = "包含所有生产环境的应用服务器")
    private String maintenanceGroupDescription;

    /**
     * 是否启用
     */
    @Schema(description = "是否启用", example = "true", defaultValue = "true")
    private Boolean maintenanceGroupEnabled;
}