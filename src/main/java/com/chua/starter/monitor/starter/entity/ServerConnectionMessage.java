package com.chua.starter.monitor.starter.entity;

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
 * 服务器连接状态消息实体
 *
 * @author CH
 * @since 2024/12/20
 */
@ApiModel(description = "服务器连接状态消息")
@Schema(description = "服务器连接状态消息")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServerConnectionMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息类型
     */
    @ApiModelProperty(value = "消息类型")
    @Schema(description = "消息类型")
    private String messageType;

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
     * 连接状态
     */
    @ApiModelProperty(value = "连接状态")
    @Schema(description = "连接状态")
    private Integer connectionStatus;

    /**
     * 状态描述
     */
    @ApiModelProperty(value = "状态描述")
    @Schema(description = "状态描述")
    private String statusDesc;

    /**
     * 错误信息
     */
    @ApiModelProperty(value = "错误信息")
    @Schema(description = "错误信息")
    private String errorMessage;

    /**
     * 连接时间
     */
    @ApiModelProperty(value = "连接时间")
    @Schema(description = "连接时间")
    private LocalDateTime connectTime;

    /**
     * 时间戳
     */
    @ApiModelProperty(value = "时间戳")
    @Schema(description = "时间戳")
    private Long timestamp;

    /**
     * 消息类型常量
     */
    public static class MessageType {
        public static final String CONNECTION_STATUS_CHANGE = "connection_status_change";
        public static final String CONNECTION_TEST_RESULT = "connection_test_result";
        public static final String SERVER_ONLINE = "server_online";
        public static final String SERVER_OFFLINE = "server_offline";
    }
}
