package com.chua.starter.monitor.pojo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 在线节点信息
 *
 * @author CH
 * @since 2024/12/30
 */
@ApiModel(description = "在线节点信息")
@Schema(description = "在线节点信息")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnlineNodeInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 节点ID（唯一标识）
     */
    @ApiModelProperty(value = "节点ID")
    @Schema(description = "节点ID")
    private String nodeId;

    /**
     * 节点名称
     */
    @ApiModelProperty(value = "节点名称")
    @Schema(description = "节点名称")
    private String nodeName;

    /**
     * 应用名称
     */
    @ApiModelProperty(value = "应用名称")
    @Schema(description = "应用名称")
    private String applicationName;

    /**
     * IP地址
     */
    @ApiModelProperty(value = "IP地址")
    @Schema(description = "IP地址")
    private String ipAddress;

    /**
     * 端口号
     */
    @ApiModelProperty(value = "端口号")
    @Schema(description = "端口号")
    private Integer port;

    /**
     * 节点状态
     */
    @ApiModelProperty(value = "节点状态")
    @Schema(description = "节点状态")
    private NodeStatus status;

    /**
     * 连接时间
     */
    @ApiModelProperty(value = "连接时间")
    @Schema(description = "连接时间")
    private LocalDateTime connectTime;

    /**
     * 最后心跳时间
     */
    @ApiModelProperty(value = "最后心跳时间")
    @Schema(description = "最后心跳时间")
    private LocalDateTime lastHeartbeatTime;

    /**
     * 连接数
     */
    @ApiModelProperty(value = "连接数")
    @Schema(description = "连接数")
    private Integer connectionCount;

    /**
     * 节点版本
     */
    @ApiModelProperty(value = "节点版本")
    @Schema(description = "节点版本")
    private String version;

    /**
     * 节点类型
     */
    @ApiModelProperty(value = "节点类型")
    @Schema(description = "节点类型")
    private String nodeType;

    /**
     * 健康状态
     */
    @ApiModelProperty(value = "健康状态")
    @Schema(description = "健康状态")
    private Boolean healthy;

    /**
     * 响应时间（毫秒）
     */
    @ApiModelProperty(value = "响应时间")
    @Schema(description = "响应时间（毫秒）")
    private Long responseTime;

    /**
     * CPU使用率
     */
    @ApiModelProperty(value = "CPU使用率")
    @Schema(description = "CPU使用率")
    private Double cpuUsage;

    /**
     * 内存使用率
     */
    @ApiModelProperty(value = "内存使用率")
    @Schema(description = "内存使用率")
    private Double memoryUsage;

    /**
     * 磁盘使用率
     */
    @ApiModelProperty(value = "磁盘使用率")
    @Schema(description = "磁盘使用率")
    private Double diskUsage;

    /**
     * 网络延迟
     */
    @ApiModelProperty(value = "网络延迟")
    @Schema(description = "网络延迟（毫秒）")
    private Long networkLatency;

    /**
     * 扩展元数据
     */
    @ApiModelProperty(value = "扩展元数据")
    @Schema(description = "扩展元数据")
    private Map<String, Object> metadata;

    /**
     * 获取节点显示名称
     */
    public String getDisplayName() {
        return nodeName != null ? nodeName : applicationName;
    }

    /**
     * 获取节点地址
     */
    public String getAddress() {
        return ipAddress + ":" + port;
    }

    /**
     * 判断节点是否在线
     */
    public boolean isOnline() {
        return NodeStatus.ONLINE.equals(status);
    }

    /**
     * 判断节点是否健康
     */
    public boolean isHealthy() {
        return Boolean.TRUE.equals(healthy) && isOnline();
    }

    /**
     * 获取状态颜色
     */
    public String getStatusColor() {
        if (status == null) {
            return "#909399"; // 灰色
        }
        switch (status) {
            case ONLINE:
                return "#67C23A"; // 绿色
            case OFFLINE:
                return "#F56C6C"; // 红色
            case CONNECTING:
                return "#E6A23C"; // 橙色
            case ERROR:
                return "#F56C6C"; // 红色
            case MAINTENANCE:
                return "#909399"; // 灰色
            default:
                return "#909399";
        }
    }

    /**
     * 获取健康状态颜色
     */
    public String getHealthColor() {
        if (isHealthy()) {
            return "#67C23A"; // 绿色
        } else {
            return "#F56C6C"; // 红色
        }
    }

    /**
     * 节点状态枚举
     */
    public enum NodeStatus {
        /**
         * 在线
         */
        ONLINE("在线"),
        /**
         * 离线
         */
        OFFLINE("离线"),
        /**
         * 连接中
         */
        CONNECTING("连接中"),
        /**
         * 异常
         */
        ERROR("异常"),
        /**
         * 维护中
         */
        MAINTENANCE("维护中");

        private final String description;

        NodeStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
