package com.chua.starter.monitor.starter.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 服务器配置实体类
 *
 * @author CH
 * @since 2024/12/18
 */
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "服务器配置")
@Schema(description = "服务器配置")
@Data
@TableName(value = "monitor_sys_gen_server")
public class MonitorSysGenServer extends SysBase implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "monitor_sys_gen_server_id", type = IdType.AUTO)
    @ApiModelProperty(value = "服务器配置ID")
    @Schema(description = "服务器配置ID")
    @NotNull(message = "不能为null")
    private Integer monitorSysGenServerId;

    /**
     * 服务器名称
     */
    @TableField(value = "monitor_sys_gen_server_name")
    @ApiModelProperty(value = "服务器名称")
    @Schema(description = "服务器名称")
    @Size(max = 255, message = "服务器名称最大长度要小于 255")
    private String monitorSysGenServerName;

    /**
     * 服务器主机地址
     */
    @TableField(value = "monitor_sys_gen_server_host")
    @ApiModelProperty(value = "服务器主机地址")
    @Schema(description = "服务器主机地址")
    @Size(max = 255, message = "服务器主机地址最大长度要小于 255")
    private String monitorSysGenServerHost;

    /**
     * 服务器端口
     */
    @TableField(value = "monitor_sys_gen_server_port")
    @ApiModelProperty(value = "服务器端口")
    @Schema(description = "服务器端口")
    private Integer monitorSysGenServerPort;

    /**
     * 连接协议 (SSH, RDP, TELNET, VNC等)
     */
    @TableField(value = "monitor_sys_gen_server_protocol")
    @ApiModelProperty(value = "连接协议")
    @Schema(description = "连接协议")
    @Size(max = 50, message = "连接协议最大长度要小于 50")
    private String monitorSysGenServerProtocol;

    /**
     * 用户名
     */
    @TableField(value = "monitor_sys_gen_server_username")
    @ApiModelProperty(value = "用户名")
    @Schema(description = "用户名")
    @Size(max = 255, message = "用户名最大长度要小于 255")
    private String monitorSysGenServerUsername;

    /**
     * 密码
     */
    @TableField(value = "monitor_sys_gen_server_password")
    @ApiModelProperty(value = "密码")
    @Schema(description = "密码")
    @Size(max = 255, message = "密码最大长度要小于 255")
    private String monitorSysGenServerPassword;

    /**
     * 私钥文件路径 (SSH协议使用)
     */
    @TableField(value = "monitor_sys_gen_server_private_key")
    @ApiModelProperty(value = "私钥文件路径")
    @Schema(description = "私钥文件路径")
    @Size(max = 500, message = "私钥文件路径最大长度要小于 500")
    private String monitorSysGenServerPrivateKey;

    /**
     * 指标存储时间(天)
     */
    @TableField(value = "monitor_sys_gen_server_metrics_retention_days")
    @ApiModelProperty(value = "指标存储时间(天)")
    @Schema(description = "指标存储时间(天)")
    private Integer monitorSysGenServerMetricsRetentionDays;

    /**
     * 监控间隔(秒)
     */
    @TableField(value = "monitor_sys_gen_server_monitor_interval")
    @ApiModelProperty(value = "监控间隔(秒)")
    @Schema(description = "监控间隔(秒)")
    private Integer monitorSysGenServerMonitorInterval;

    /**
     * 连接超时时间(毫秒)
     */
    @TableField(value = "monitor_sys_gen_server_timeout")
    @ApiModelProperty(value = "连接超时时间(毫秒)")
    @Schema(description = "连接超时时间(毫秒)")
    private Integer monitorSysGenServerTimeout;

    /**
     * 服务器描述
     */
    @TableField(value = "monitor_sys_gen_server_desc")
    @ApiModelProperty(value = "服务器描述")
    @Schema(description = "服务器描述")
    @Size(max = 500, message = "服务器描述最大长度要小于 500")
    private String monitorSysGenServerDesc;

    /**
     * 服务器状态 0:停用 1:启用
     */
    @TableField(value = "monitor_sys_gen_server_status")
    @ApiModelProperty(value = "服务器状态")
    @Schema(description = "服务器状态")
    private Integer monitorSysGenServerStatus;

    /**
     * 是否启用监控 0:否 1:是
     */
    @TableField(value = "monitor_sys_gen_server_monitor_enabled")
    @ApiModelProperty(value = "是否启用监控")
    @Schema(description = "是否启用监控")
    private Integer monitorSysGenServerMonitorEnabled;

    /**
     * 服务器标签 (用于分组)
     */
    @TableField(value = "monitor_sys_gen_server_tags")
    @ApiModelProperty(value = "服务器标签")
    @Schema(description = "服务器标签")
    @Size(max = 500, message = "服务器标签最大长度要小于 500")
    private String monitorSysGenServerTags;

    /**
     * 生成器id
     */
    @TableField(value = "gen_id")
    @ApiModelProperty(value = "生成器id")
    @Schema(description = "生成器id")
    private Integer genId;

    /**
     * 创建时间
     */
    @TableField(value = "monitor_sys_gen_server_create_time")
    @ApiModelProperty(value = "创建时间")
    @Schema(description = "创建时间")
    private LocalDateTime monitorSysGenServerCreateTime;

    /**
     * 更新时间
     */
    @TableField(value = "monitor_sys_gen_server_update_time")
    @ApiModelProperty(value = "更新时间")
    @Schema(description = "更新时间")
    private LocalDateTime monitorSysGenServerUpdateTime;
}
