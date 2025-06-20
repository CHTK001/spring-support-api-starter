package com.chua.starter.monitor.starter.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 服务器连接状态实体类
 *
 * @author CH
 * @since 2024/12/20
 */
@ApiModel(description = "服务器连接状态")
@Schema(description = "服务器连接状态")
@Data
public class MonitorSysGenServerConnectionStatus implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 服务器ID
     */
    @ApiModelProperty(value = "服务器ID")
    @Schema(description = "服务器ID")
    private Integer monitorSysGenServerId;

    /**
     * 服务器名称
     */
    @ApiModelProperty(value = "服务器名称")
    @Schema(description = "服务器名称")
    private String monitorSysGenServerName;

    /**
     * 服务器主机地址
     */
    @ApiModelProperty(value = "服务器主机地址")
    @Schema(description = "服务器主机地址")
    private String monitorSysGenServerHost;

    /**
     * 服务器端口
     */
    @ApiModelProperty(value = "服务器端口")
    @Schema(description = "服务器端口")
    private Integer monitorSysGenServerPort;

    /**
     * 连接协议
     */
    @ApiModelProperty(value = "连接协议")
    @Schema(description = "连接协议")
    private String monitorSysGenServerProtocol;

    /**
     * 连接状态 0:离线 1:在线 2:连接中 3:连接失败
     */
    @ApiModelProperty(value = "连接状态")
    @Schema(description = "连接状态 0:离线 1:在线 2:连接中 3:连接失败")
    private Integer monitorSysGenServerConnectionStatus;

    /**
     * 最后连接时间
     */
    @ApiModelProperty(value = "最后连接时间")
    @Schema(description = "最后连接时间")
    private LocalDateTime monitorSysGenServerLastConnectTime;

    /**
     * 连接失败原因
     */
    @ApiModelProperty(value = "连接失败原因")
    @Schema(description = "连接失败原因")
    private String monitorSysGenServerConnectionError;

    /**
     * 连接响应时间(毫秒)
     */
    @ApiModelProperty(value = "连接响应时间(毫秒)")
    @Schema(description = "连接响应时间(毫秒)")
    private Long monitorSysGenServerResponseTime;

    /**
     * 连接测试时间
     */
    @ApiModelProperty(value = "连接测试时间")
    @Schema(description = "连接测试时间")
    private LocalDateTime monitorSysGenServerTestTime;

    /**
     * 是否启用监控
     */
    @ApiModelProperty(value = "是否启用监控")
    @Schema(description = "是否启用监控")
    private Integer monitorSysGenServerMonitorEnabled;

    /**
     * 服务器状态
     */
    @ApiModelProperty(value = "服务器状态")
    @Schema(description = "服务器状态")
    private Integer monitorSysGenServerStatus;

    /**
     * 连接状态枚举
     */
    public enum ConnectionStatus {
        OFFLINE(0, "离线"),
        ONLINE(1, "在线"),
        CONNECTING(2, "连接中"),
        FAILED(3, "连接失败");

        private final int code;
        private final String desc;

        ConnectionStatus(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public int getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }

        public static ConnectionStatus fromCode(int code) {
            for (ConnectionStatus status : values()) {
                if (status.code == code) {
                    return status;
                }
            }
            return OFFLINE;
        }
    }

    /**
     * 获取连接状态描述
     */
    public String getConnectionStatusDesc() {
        return ConnectionStatus.fromCode(this.monitorSysGenServerConnectionStatus).getDesc();
    }

    /**
     * 是否在线
     */
    public boolean isOnline() {
        return ConnectionStatus.ONLINE.getCode() == this.monitorSysGenServerConnectionStatus;
    }

    /**
     * 是否离线
     */
    public boolean isOffline() {
        return ConnectionStatus.OFFLINE.getCode() == this.monitorSysGenServerConnectionStatus;
    }

    /**
     * 是否连接中
     */
    public boolean isConnecting() {
        return ConnectionStatus.CONNECTING.getCode() == this.monitorSysGenServerConnectionStatus;
    }

    /**
     * 是否连接失败
     */
    public boolean isFailed() {
        return ConnectionStatus.FAILED.getCode() == this.monitorSysGenServerConnectionStatus;
    }
}
