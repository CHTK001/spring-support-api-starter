package com.chua.starter.monitor.message;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 服务器WebSocket消息实体
 *
 * @author CH
 * @since 2024/12/23
 */
@ApiModel(description = "服务器WebSocket消息")
@Schema(description = "服务器WebSocket消息")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServerWebSocketMessage implements Serializable {

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
     * 服务器主机地址
     */
    @ApiModelProperty(value = "服务器主机地址")
    @Schema(description = "服务器主机地址")
    private String serverHost;

    /**
     * 服务器端口
     */
    @ApiModelProperty(value = "服务器端口")
    @Schema(description = "服务器端口")
    private Integer serverPort;

    /**
     * 连接协议
     */
    @ApiModelProperty(value = "连接协议")
    @Schema(description = "连接协议")
    private String serverProtocol;

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
     * 错误消息
     */
    @ApiModelProperty(value = "错误消息")
    @Schema(description = "错误消息")
    private String errorMessage;

    /**
     * 响应时间(毫秒)
     */
    @ApiModelProperty(value = "响应时间(毫秒)")
    @Schema(description = "响应时间(毫秒)")
    private Long responseTime;

    /**
     * 连接时间
     */
    @ApiModelProperty(value = "连接时间")
    @Schema(description = "连接时间")
    private LocalDateTime connectTime;

    /**
     * 消息时间戳
     */
    @ApiModelProperty(value = "消息时间戳")
    @Schema(description = "消息时间戳")
    private Long timestamp;

    /**
     * 数据ID
     * 用于前端过滤消息，统一标识关联数据的ID
     * 可以是 serverId、containerId、softId 等
     */
    @ApiModelProperty(value = "数据ID，用于前端过滤")
    @Schema(description = "数据ID，用于前端过滤消息")
    private String dataId;

    /**
     * 扩展数据
     */
    @ApiModelProperty(value = "扩展数据")
    @Schema(description = "扩展数据")
    private Object data;

    /**
     * 创建连接状态变化消息
     */
    public static ServerWebSocketMessage createConnectionStatusMessage(Integer serverId, String serverName,
                                                                       String serverHost, Integer serverPort, String serverProtocol, Integer connectionStatus,
                                                                       String statusDesc, String errorMessage, Long responseTime) {
        return ServerWebSocketMessage.builder().messageType(MessageType.CONNECTION_STATUS_CHANGE)
                .serverId(serverId).serverName(serverName).serverHost(serverHost).serverPort(serverPort)
                .serverProtocol(serverProtocol).connectionStatus(connectionStatus)
                .statusDesc(statusDesc).errorMessage(errorMessage).responseTime(responseTime)
                .dataId(serverId != null ? String.valueOf(serverId) : null)
                .connectTime(LocalDateTime.now()).timestamp(System.currentTimeMillis()).build();
    }

    /**
     * 创建SSH数据消息
     */
    public static ServerWebSocketMessage createSSHDataMessage(Integer serverId, String serverName, String data) {
        return ServerWebSocketMessage.builder().messageType(MessageType.SSH_DATA).serverId(serverId)
                .serverName(serverName).data(data)
                .dataId(serverId != null ? String.valueOf(serverId) : null)
                .timestamp(System.currentTimeMillis()).build();
    }

    /**
     * 创建SSH连接消息
     */
    public static ServerWebSocketMessage createSSHConnectMessage(Integer serverId, String serverName,
                                                                 String serverHost, Integer serverPort) {
        return ServerWebSocketMessage.builder().messageType(MessageType.SSH_CONNECT).serverId(serverId)
                .serverName(serverName).serverHost(serverHost).serverPort(serverPort)
                .serverProtocol("SSH").connectTime(LocalDateTime.now())
                .dataId(serverId != null ? String.valueOf(serverId) : null)
                .timestamp(System.currentTimeMillis()).build();
    }

    /**
     * 创建SSH断开连接消息
     */
    public static ServerWebSocketMessage createSSHDisconnectMessage(Integer serverId, String serverName,
                                                                    String reason) {
        return ServerWebSocketMessage.builder().messageType(MessageType.SSH_DISCONNECT).serverId(serverId)
                .serverName(serverName).errorMessage(reason)
                .dataId(serverId != null ? String.valueOf(serverId) : null)
                .timestamp(System.currentTimeMillis()).build();
    }

    /**
     * 创建SSH错误消息
     */
    public static ServerWebSocketMessage createSSHErrorMessage(Integer serverId, String serverName,
                                                               String errorMessage) {
        return ServerWebSocketMessage.builder().messageType(MessageType.SSH_ERROR).serverId(serverId)
                .serverName(serverName).errorMessage(errorMessage)
                .dataId(serverId != null ? String.valueOf(serverId) : null)
                .timestamp(System.currentTimeMillis()).build();
    }

    /**
     * 创建Shell输出消息
     */
    public static ServerWebSocketMessage createShellOutputMessage(Integer serverId, String serverName,
                                                                  String output) {
        Map<String, Object> shellData = new HashMap<>();
        shellData.put("output", output);
        shellData.put("type", "output");

        return ServerWebSocketMessage.builder().messageType(MessageType.SHELL_OUTPUT).serverId(serverId)
                .serverName(serverName).data(shellData)
                .dataId(serverId != null ? String.valueOf(serverId) : null)
                .timestamp(System.currentTimeMillis()).build();
    }

    /**
     * 创建服务器指标消息
     */
    public static ServerWebSocketMessage createServerMetricsMessage(Integer serverId, String serverName,
                                                                    Object metricsData) {
        return ServerWebSocketMessage.builder().messageType(MessageType.SERVER_METRICS).serverId(serverId)
                .serverName(serverName).data(metricsData)
                .dataId(serverId != null ? String.valueOf(serverId) : null)
                .timestamp(System.currentTimeMillis()).build();
    }

    /**
     * 创建VNC数据消息
     */
    public static ServerWebSocketMessage createVncDataMessage(Integer serverId, String serverName, Object data) {
        return ServerWebSocketMessage.builder().messageType(MessageType.VNC_DATA).serverId(serverId)
                .serverName(serverName).data(data)
                .dataId(serverId != null ? String.valueOf(serverId) : null)
                .timestamp(System.currentTimeMillis()).build();
    }

    /**
     * 创建VNC输入消息
     */
    public static ServerWebSocketMessage createVncInputMessage(Integer serverId, String serverName,
                                                               Object inputData) {
        return ServerWebSocketMessage.builder().messageType(MessageType.VNC_INPUT).serverId(serverId)
                .serverName(serverName).data(inputData)
                .dataId(serverId != null ? String.valueOf(serverId) : null)
                .timestamp(System.currentTimeMillis()).build();
    }

    /**
     * 创建RDP数据消息
     */
    public static ServerWebSocketMessage createRdpDataMessage(Integer serverId, String serverName, Object data) {
        return ServerWebSocketMessage.builder().messageType(MessageType.RDP_DATA).serverId(serverId)
                .serverName(serverName).data(data)
                .dataId(serverId != null ? String.valueOf(serverId) : null)
                .timestamp(System.currentTimeMillis()).build();
    }

    /**
     * 创建RDP输入消息
     */
    public static ServerWebSocketMessage createRdpInputMessage(Integer serverId, String serverName,
                                                               Object inputData) {
        return ServerWebSocketMessage.builder().messageType(MessageType.RDP_INPUT).serverId(serverId)
                .serverName(serverName).data(inputData)
                .dataId(serverId != null ? String.valueOf(serverId) : null)
                .timestamp(System.currentTimeMillis()).build();
    }

    /**
     * 创建RDP连接消息
     */
    public static ServerWebSocketMessage createRdpConnectionMessage(Integer serverId, String serverName,
                                                                    String status) {
        return ServerWebSocketMessage.builder().messageType(MessageType.RDP_CONNECT).serverId(serverId)
                .serverName(serverName).data(Map.of("status", status))
                .dataId(serverId != null ? String.valueOf(serverId) : null)
                .timestamp(System.currentTimeMillis()).build();
    }

    /**
     * 创建RDP断开连接消息
     */
    public static ServerWebSocketMessage createRdpDisconnectMessage(Integer serverId, String serverName,
                                                                    String reason) {
        return ServerWebSocketMessage.builder().messageType(MessageType.RDP_DISCONNECT).serverId(serverId)
                .serverName(serverName).errorMessage(reason)
                .dataId(serverId != null ? String.valueOf(serverId) : null)
                .timestamp(System.currentTimeMillis()).build();
    }

    /**
     * 创建VNC连接消息
     */
    public static ServerWebSocketMessage createVncConnectionMessage(Integer serverId, String serverName,
                                                                    String status) {
        return ServerWebSocketMessage.builder().messageType(MessageType.VNC_CONNECT).serverId(serverId)
                .serverName(serverName).data(Map.of("status", status))
                .dataId(serverId != null ? String.valueOf(serverId) : null)
                .timestamp(System.currentTimeMillis()).build();
    }

    /**
     * 创建VNC断开连接消息
     */
    public static ServerWebSocketMessage createVncDisconnectMessage(Integer serverId, String serverName,
                                                                    String reason) {
        return ServerWebSocketMessage.builder().messageType(MessageType.VNC_DISCONNECT).serverId(serverId)
                .serverName(serverName).errorMessage(reason)
                .dataId(serverId != null ? String.valueOf(serverId) : null)
                .timestamp(System.currentTimeMillis()).build();
    }

    /**
     * 创建自定义消息
     */
    public static ServerWebSocketMessage createCustomMessage(Integer serverId, String serverName,
                                                             Map<String, Object> customData) {
        String messageType = (String) customData.getOrDefault("type", "custom");

        return ServerWebSocketMessage.builder().messageType(messageType).serverId(serverId)
                .serverName(serverName).data(customData).timestamp(System.currentTimeMillis()).build();
    }

    /**
     * 创建节点状态更新消息
     */
    public static ServerWebSocketMessage createNodeStatusUpdateMessage(String nodeId, Object nodeData) {
        return ServerWebSocketMessage.builder().messageType(MessageType.NODE_STATUS_UPDATE).data(nodeData)
                .timestamp(System.currentTimeMillis()).build();
    }

    /**
     * 创建节点列表更新消息
     */
    public static ServerWebSocketMessage createNodeListUpdateMessage(Object nodeListData) {
        return ServerWebSocketMessage.builder().messageType(MessageType.NODE_LIST_UPDATE).data(nodeListData)
                .timestamp(System.currentTimeMillis()).build();
    }

    /**
     * 创建节点统计信息更新消息
     */
    public static ServerWebSocketMessage createNodeStatisticsUpdateMessage(Object statisticsData) {
        return ServerWebSocketMessage.builder().messageType(MessageType.NODE_STATISTICS_UPDATE)
                .data(statisticsData).timestamp(System.currentTimeMillis()).build();
    }

    /**
     * 创建节点连接消息
     */
    public static ServerWebSocketMessage createNodeConnectMessage(String nodeId, String nodeName, String ipAddress,
                                                                  Integer port) {
        Map<String, Object> nodeData = new HashMap<>();
        nodeData.put("nodeId", nodeId);
        nodeData.put("nodeName", nodeName);
        nodeData.put("ipAddress", ipAddress);
        nodeData.put("port", port);
        nodeData.put("connectTime", LocalDateTime.now());

        return ServerWebSocketMessage.builder().messageType(MessageType.NODE_CONNECT).data(nodeData)
                .timestamp(System.currentTimeMillis()).build();
    }

    /**
     * 创建节点断开连接消息
     */
    public static ServerWebSocketMessage createNodeDisconnectMessage(String nodeId, String nodeName,
                                                                     String reason) {
        Map<String, Object> nodeData = new HashMap<>();
        nodeData.put("nodeId", nodeId);
        nodeData.put("nodeName", nodeName);
        nodeData.put("reason", reason);
        nodeData.put("disconnectTime", LocalDateTime.now());

        return ServerWebSocketMessage.builder().messageType(MessageType.NODE_DISCONNECT).data(nodeData)
                .timestamp(System.currentTimeMillis()).build();
    }

    /**
     * 消息类型常量
     */
    public static class MessageType {
        public static final String CONNECTION_STATUS_CHANGE = "connection_status_change";
        public static final String SERVER_ONLINE = "server_online";
        public static final String SERVER_OFFLINE = "server_offline";
        public static final String SERVER_UPDATE = "server_update";
        public static final String SERVER_DELETE = "server_delete";
        public static final String SERVER_ADD = "server_add";
        public static final String SERVER_CREATED = "server_created";
        public static final String SERVER_UPDATED = "server_updated";
        public static final String SERVER_HEALTH = "server_health";
        public static final String SERVER_LATENCY = "server_latency";
        public static final String BATCH_SERVER_LATENCY = "batch_server_latency";
        public static final String SERVER_METRICS = "server_metrics";
        public static final String CONNECTION_TEST_RESULT = "connection_test_result";
        public static final String SSH_DATA = "ssh_data";
        public static final String SSH_INPUT = "ssh_input";
        public static final String SSH_CONNECT = "ssh_connect";
        public static final String SSH_DISCONNECT = "ssh_disconnect";
        public static final String SSH_RESIZE = "ssh_resize";
        public static final String SSH_ERROR = "ssh_error";
        public static final String SHELL_OUTPUT = "shell_output";
        public static final String VNC_DATA = "vnc_data";
        public static final String VNC_INPUT = "vnc_input";
        public static final String VNC_CONNECT = "vnc_connect";
        public static final String VNC_DISCONNECT = "vnc_disconnect";
        public static final String VNC_ERROR = "vnc_error";
        public static final String RDP_DATA = "rdp_data";
        public static final String RDP_INPUT = "rdp_input";
        public static final String RDP_CONNECT = "rdp_connect";
        public static final String RDP_DISCONNECT = "rdp_disconnect";
        public static final String RDP_ERROR = "rdp_error";

        // 节点管理相关消息类型
        public static final String NODE_STATUS_UPDATE = "node_status_update";
        public static final String NODE_LIST_UPDATE = "node_list_update";
        public static final String NODE_STATISTICS_UPDATE = "node_statistics_update";
        public static final String NODE_CONNECT = "node_connect";
        public static final String NODE_DISCONNECT = "node_disconnect";
    }
}
