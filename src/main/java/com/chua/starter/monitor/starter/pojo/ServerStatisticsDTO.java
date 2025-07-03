package com.chua.starter.monitor.starter.pojo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * 服务器统计信息DTO
 *
 * @author CH
 * @since 2024/12/30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "ServerStatisticsDTO", description = "服务器统计信息DTO")
@Schema(description = "服务器统计信息DTO")
public class ServerStatisticsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总服务器数
     */
    @ApiModelProperty(value = "总服务器数")
    @Schema(description = "总服务器数")
    private Long totalServers;

    /**
     * 在线服务器数
     */
    @ApiModelProperty(value = "在线服务器数")
    @Schema(description = "在线服务器数")
    private Long onlineServers;

    /**
     * 启用监控的服务器数
     */
    @ApiModelProperty(value = "启用监控的服务器数")
    @Schema(description = "启用监控的服务器数")
    private Long monitoringEnabled;

    /**
     * 按协议分组统计
     */
    @ApiModelProperty(value = "按协议分组统计")
    @Schema(description = "按协议分组统计")
    private Map<String, Long> protocolStats;

    /**
     * 按状态分组统计
     */
    @ApiModelProperty(value = "按状态分组统计")
    @Schema(description = "按状态分组统计")
    private Map<String, Long> statusStats;

    /**
     * 获取离线服务器数
     */
    public Long getOfflineServers() {
        if (totalServers == null || onlineServers == null) {
            return 0L;
        }
        return totalServers - onlineServers;
    }

    /**
     * 获取在线率
     */
    public Double getOnlineRate() {
        if (totalServers == null || totalServers == 0 || onlineServers == null) {
            return 0.0;
        }
        return (double) onlineServers / totalServers * 100;
    }

    /**
     * 获取监控启用率
     */
    public Double getMonitoringEnabledRate() {
        if (totalServers == null || totalServers == 0 || monitoringEnabled == null) {
            return 0.0;
        }
        return (double) monitoringEnabled / totalServers * 100;
    }

    /**
     * 获取启用状态的服务器数
     */
    public Long getEnabledServers() {
        if (statusStats == null) {
            return 0L;
        }
        return statusStats.getOrDefault("enabled", 0L);
    }

    /**
     * 获取停用状态的服务器数
     */
    public Long getDisabledServers() {
        if (statusStats == null) {
            return 0L;
        }
        return statusStats.getOrDefault("disabled", 0L);
    }
}
