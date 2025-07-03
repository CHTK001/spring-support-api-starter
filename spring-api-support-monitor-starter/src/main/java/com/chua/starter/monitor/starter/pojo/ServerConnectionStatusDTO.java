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
 * 服务器连接状态DTO
 *
 * @author CH
 * @since 2024/12/30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "ServerConnectionStatusDTO", description = "服务器连接状态DTO")
@Schema(description = "服务器连接状态DTO")
public class ServerConnectionStatusDTO implements Serializable {

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
     * 端口
     */
    @ApiModelProperty(value = "端口")
    @Schema(description = "端口")
    private Integer port;

    /**
     * 协议
     */
    @ApiModelProperty(value = "协议")
    @Schema(description = "协议")
    private String protocol;

    /**
     * 是否已连接
     */
    @ApiModelProperty(value = "是否已连接")
    @Schema(description = "是否已连接")
    private Boolean connected;

    /**
     * 会话ID
     */
    @ApiModelProperty(value = "会话ID")
    @Schema(description = "会话ID")
    private String sessionId;

    /**
     * 连接状态 0:离线 1:在线 2:连接中 3:连接失败
     */
    @ApiModelProperty(value = "连接状态")
    @Schema(description = "连接状态 0:离线 1:在线 2:连接中 3:连接失败")
    private Integer connectionStatus;

    /**
     * 状态描述
     */
    @ApiModelProperty(value = "状态描述")
    @Schema(description = "状态描述")
    private String statusDesc;

    /**
     * 最后连接时间
     */
    @ApiModelProperty(value = "最后连接时间")
    @Schema(description = "最后连接时间")
    private LocalDateTime lastConnectTime;

    /**
     * 连接错误信息
     */
    @ApiModelProperty(value = "连接错误信息")
    @Schema(description = "连接错误信息")
    private String connectionError;

    /**
     * 监控是否启用
     */
    @ApiModelProperty(value = "监控是否启用")
    @Schema(description = "监控是否启用")
    private Integer monitorEnabled;

    /**
     * 标签
     */
    @ApiModelProperty(value = "标签")
    @Schema(description = "标签")
    private String tags;

    /**
     * 时间戳
     */
    @ApiModelProperty(value = "时间戳")
    @Schema(description = "时间戳")
    private Long timestamp;

    /**
     * 获取连接状态描述
     */
    public String getConnectionStatusDesc() {
        if (connectionStatus == null) {
            return "未知";
        }
        switch (connectionStatus) {
            case 0:
                return "离线";
            case 1:
                return "在线";
            case 2:
                return "连接中";
            case 3:
                return "连接失败";
            default:
                return "未知";
        }
    }

    /**
     * 是否在线
     */
    public boolean isOnline() {
        return connectionStatus != null && connectionStatus == 1;
    }

    /**
     * 是否离线
     */
    public boolean isOffline() {
        return connectionStatus == null || connectionStatus == 0;
    }

    /**
     * 是否连接中
     */
    public boolean isConnecting() {
        return connectionStatus != null && connectionStatus == 2;
    }

    /**
     * 是否连接失败
     */
    public boolean isConnectionFailed() {
        return connectionStatus != null && connectionStatus == 3;
    }

    /**
     * 监控是否启用
     */
    public boolean isMonitoringEnabled() {
        return monitorEnabled != null && monitorEnabled == 1;
    }
}
