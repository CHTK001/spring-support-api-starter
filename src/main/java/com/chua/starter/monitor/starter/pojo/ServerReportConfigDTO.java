package com.chua.starter.monitor.starter.pojo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 服务器上报配置DTO
 *
 * @author CH
 * @since 2024/12/30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "ServerReportConfigDTO", description = "服务器上报配置DTO")
@Schema(description = "服务器上报配置DTO")
public class ServerReportConfigDTO implements Serializable {

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
     * 数据上报方式
     */
    @ApiModelProperty(value = "数据上报方式")
    @Schema(description = "数据上报方式")
    private String dataReportMethod;

    /**
     * 上报频率（秒）
     */
    @ApiModelProperty(value = "上报频率（秒）")
    @Schema(description = "上报频率（秒）")
    private Integer reportFrequency;

    /**
     * Prometheus推送网关地址
     */
    @ApiModelProperty(value = "Prometheus推送网关地址")
    @Schema(description = "Prometheus推送网关地址")
    private String prometheusPushGateway;

    /**
     * Prometheus作业名称
     */
    @ApiModelProperty(value = "Prometheus作业名称")
    @Schema(description = "Prometheus作业名称")
    private String prometheusJobName;

    /**
     * 是否启用上报
     */
    @ApiModelProperty(value = "是否启用上报")
    @Schema(description = "是否启用上报")
    private Integer reportEnabled;

    /**
     * 获取上报方式描述
     */
    public String getDataReportMethodDesc() {
        if (dataReportMethod == null) {
            return "未配置";
        }
        switch (dataReportMethod.toUpperCase()) {
            case "NONE":
                return "不上报";
            case "API":
                return "API上报";
            case "PROMETHEUS":
                return "Prometheus上报";
            case "LOCAL":
                return "本地收集";
            default:
                return dataReportMethod;
        }
    }

    /**
     * 是否启用上报
     */
    public boolean isReportEnabled() {
        return reportEnabled != null && reportEnabled == 1;
    }

    /**
     * 是否为API上报
     */
    public boolean isApiReport() {
        return "API".equalsIgnoreCase(dataReportMethod);
    }

    /**
     * 是否为Prometheus上报
     */
    public boolean isPrometheusReport() {
        return "PROMETHEUS".equalsIgnoreCase(dataReportMethod);
    }

    /**
     * 是否为本地收集
     */
    public boolean isLocalReport() {
        return "LOCAL".equalsIgnoreCase(dataReportMethod);
    }

    /**
     * 是否不上报
     */
    public boolean isNoReport() {
        return "NONE".equalsIgnoreCase(dataReportMethod);
    }
}
