package com.chua.starter.monitor.starter.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 节点系统表实体类
 *
 * @author CH
 * @since 2024/12/30
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName(value = "monitor_sys_gen_node_system")
@ApiModel(description = "节点系统表")
@Schema(description = "节点系统表")
public class MonitorSysGenNodeSystem extends SysBase {

    /**
     * 节点系统ID
     */
    @TableId(value = "node_system_id", type = IdType.INPUT)
    @ApiModelProperty(value = "节点系统ID")
    @Schema(description = "节点系统ID")
    private String nodeSystemId;

    /**
     * 节点系统名称
     */
    @TableField(value = "node_system_name")
    @ApiModelProperty(value = "节点系统名称")
    @Schema(description = "节点系统名称")
    private String nodeSystemName;

    /**
     * 节点系统应用名称
     */
    @TableField(value = "node_system_application_name")
    @ApiModelProperty(value = "节点系统应用名称")
    @Schema(description = "节点系统应用名称")
    private String nodeSystemApplicationName;

    /**
     * 节点系统IP地址
     */
    @TableField(value = "node_system_ip_address")
    @ApiModelProperty(value = "节点系统IP地址")
    @Schema(description = "节点系统IP地址")
    private String nodeSystemIpAddress;

    /**
     * 节点系统端口
     */
    @TableField(value = "node_system_port")
    @ApiModelProperty(value = "节点系统端口")
    @Schema(description = "节点系统端口")
    private Integer nodeSystemPort;

    /**
     * 节点系统状态
     */
    @TableField(value = "node_system_status")
    @ApiModelProperty(value = "节点系统状态")
    @Schema(description = "节点系统状态")
    private String nodeSystemStatus;

    /**
     * 节点系统类型
     */
    @TableField(value = "node_system_type")
    @ApiModelProperty(value = "节点系统类型")
    @Schema(description = "节点系统类型")
    private String nodeSystemType;

    /**
     * 节点系统版本
     */
    @TableField(value = "node_system_version")
    @ApiModelProperty(value = "节点系统版本")
    @Schema(description = "节点系统版本")
    private String nodeSystemVersion;

    /**
     * 节点系统是否健康
     */
    @TableField(value = "node_system_healthy")
    @ApiModelProperty(value = "节点系统是否健康")
    @Schema(description = "节点系统是否健康")
    private Boolean nodeSystemHealthy;

    /**
     * 节点系统首次发现时间
     */
    @TableField(value = "node_system_first_discovered_time")
    @ApiModelProperty(value = "节点系统首次发现时间")
    @Schema(description = "节点系统首次发现时间")
    private LocalDateTime nodeSystemFirstDiscoveredTime;

    /**
     * 节点系统最后在线时间
     */
    @TableField(value = "node_system_last_online_time")
    @ApiModelProperty(value = "节点系统最后在线时间")
    @Schema(description = "节点系统最后在线时间")
    private LocalDateTime nodeSystemLastOnlineTime;

    /**
     * 节点系统最后离线时间
     */
    @TableField(value = "node_system_last_offline_time")
    @ApiModelProperty(value = "节点系统最后离线时间")
    @Schema(description = "节点系统最后离线时间")
    private LocalDateTime nodeSystemLastOfflineTime;

    /**
     * 节点系统最后心跳时间
     */
    @TableField(value = "node_system_last_heartbeat_time")
    @ApiModelProperty(value = "节点系统最后心跳时间")
    @Schema(description = "节点系统最后心跳时间")
    private LocalDateTime nodeSystemLastHeartbeatTime;

    /**
     * 节点系统连接次数
     */
    @TableField(value = "node_system_connection_count")
    @ApiModelProperty(value = "节点系统连接次数")
    @Schema(description = "节点系统连接次数")
    private Integer nodeSystemConnectionCount;

    /**
     * 节点系统错误次数
     */
    @TableField(value = "node_system_error_count")
    @ApiModelProperty(value = "节点系统错误次数")
    @Schema(description = "节点系统错误次数")
    private Integer nodeSystemErrorCount;

    /**
     * 节点系统最后错误时间
     */
    @TableField(value = "node_system_last_error_time")
    @ApiModelProperty(value = "节点系统最后错误时间")
    @Schema(description = "节点系统最后错误时间")
    private LocalDateTime nodeSystemLastErrorTime;

    /**
     * 节点系统最后错误信息
     */
    @TableField(value = "node_system_last_error_message")
    @ApiModelProperty(value = "节点系统最后错误信息")
    @Schema(description = "节点系统最后错误信息")
    private String nodeSystemLastErrorMessage;

    /**
     * 节点系统平均CPU使用率
     */
    @TableField(value = "node_system_avg_cpu_usage")
    @ApiModelProperty(value = "节点系统平均CPU使用率")
    @Schema(description = "节点系统平均CPU使用率")
    private Double nodeSystemAvgCpuUsage;

    /**
     * 节点系统平均内存使用率
     */
    @TableField(value = "node_system_avg_memory_usage")
    @ApiModelProperty(value = "节点系统平均内存使用率")
    @Schema(description = "节点系统平均内存使用率")
    private Double nodeSystemAvgMemoryUsage;

    /**
     * 节点系统平均磁盘使用率
     */
    @TableField(value = "node_system_avg_disk_usage")
    @ApiModelProperty(value = "节点系统平均磁盘使用率")
    @Schema(description = "节点系统平均磁盘使用率")
    private Double nodeSystemAvgDiskUsage;

    /**
     * 节点系统平均网络延迟
     */
    @TableField(value = "node_system_avg_network_latency")
    @ApiModelProperty(value = "节点系统平均网络延迟")
    @Schema(description = "节点系统平均网络延迟")
    private Double nodeSystemAvgNetworkLatency;

    /**
     * 节点系统平均响应时间
     */
    @TableField(value = "node_system_avg_response_time")
    @ApiModelProperty(value = "节点系统平均响应时间")
    @Schema(description = "节点系统平均响应时间")
    private Double nodeSystemAvgResponseTime;

    /**
     * 节点系统总在线时长
     */
    @TableField(value = "node_system_total_online_duration")
    @ApiModelProperty(value = "节点系统总在线时长")
    @Schema(description = "节点系统总在线时长")
    private Long nodeSystemTotalOnlineDuration;

    /**
     * 节点系统元数据
     */
    @TableField(value = "node_system_metadata")
    @ApiModelProperty(value = "节点系统元数据")
    @Schema(description = "节点系统元数据")
    private String nodeSystemMetadata;
}
