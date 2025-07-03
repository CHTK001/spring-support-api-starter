package com.chua.starter.monitor.starter.pojo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 服务器监控状态DTO
 *
 * @author CH
 * @since 2024/12/30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "ServerMonitoringStatusDTO", description = "服务器监控状态DTO")
@Schema(description = "服务器监控状态DTO")
public class ServerMonitoringStatusDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 服务器ID
     */
    @ApiModelProperty(value = "服务器ID")
    @Schema(description = "服务器ID")
    private Integer serverId;

    /**
     * 服务器名称
     */
    @ApiModelProperty(value = "服务器名称")
    @Schema(description = "服务器名称")
    private String serverName;

    /**
     * 监控是否启用
     */
    @ApiModelProperty(value = "监控是否启用")
    @Schema(description = "监控是否启用")
    private Integer monitorEnabled;

    /**
     * 数据保留天数
     */
    @ApiModelProperty(value = "数据保留天数")
    @Schema(description = "数据保留天数")
    private Integer retentionDays;

    /**
     * 最后更新时间
     */
    @ApiModelProperty(value = "最后更新时间")
    @Schema(description = "最后更新时间")
    private LocalDateTime lastUpdateTime;

    /**
     * 监控是否启用
     */
    public boolean isMonitoringEnabled() {
        return monitorEnabled != null && monitorEnabled == 1;
    }

    /**
     * 获取监控状态描述
     */
    public String getMonitoringStatusDesc() {
        return isMonitoringEnabled() ? "已启用" : "已停用";
    }
}
