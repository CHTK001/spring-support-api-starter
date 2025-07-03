package com.chua.starter.monitor.starter.pojo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 服务器信息检测结果DTO
 *
 * @author CH
 * @since 2024/12/30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "ServerInfoDetectionResultDTO", description = "服务器信息检测结果DTO")
@Schema(description = "服务器信息检测结果DTO")
public class ServerInfoDetectionResultDTO implements Serializable {

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
     * 主机地址
     */
    @ApiModelProperty(value = "主机地址")
    @Schema(description = "主机地址")
    private String host;

    /**
     * 是否本地服务器
     */
    @ApiModelProperty(value = "是否本地服务器")
    @Schema(description = "是否本地服务器")
    private Integer isLocal;

    /**
     * IP地址列表
     */
    @ApiModelProperty(value = "IP地址列表")
    @Schema(description = "IP地址列表")
    private List<String> ipAddresses;

    /**
     * 操作系统类型
     */
    @ApiModelProperty(value = "操作系统类型")
    @Schema(description = "操作系统类型")
    private String osType;

    /**
     * 是否支持Docker
     */
    @ApiModelProperty(value = "是否支持Docker")
    @Schema(description = "是否支持Docker")
    private Integer dockerSupported;

    /**
     * 检测时间戳
     */
    @ApiModelProperty(value = "检测时间戳")
    @Schema(description = "检测时间戳")
    private Long timestamp;

    /**
     * 是否检测成功
     */
    @ApiModelProperty(value = "是否检测成功")
    @Schema(description = "是否检测成功")
    private Boolean success;

    /**
     * 错误信息
     */
    @ApiModelProperty(value = "错误信息")
    @Schema(description = "错误信息")
    private String errorMessage;

    /**
     * 是否为本地服务器
     */
    public boolean isLocalServer() {
        return isLocal != null && isLocal == 1;
    }

    /**
     * 是否支持Docker
     */
    public boolean isDockerSupported() {
        return dockerSupported != null && dockerSupported == 1;
    }

    /**
     * 获取操作系统类型描述
     */
    public String getOsTypeDesc() {
        if (osType == null) {
            return "未知";
        }
        switch (osType.toLowerCase()) {
            case "windows":
                return "Windows";
            case "linux":
                return "Linux";
            case "macos":
                return "macOS";
            default:
                return osType;
        }
    }
}
