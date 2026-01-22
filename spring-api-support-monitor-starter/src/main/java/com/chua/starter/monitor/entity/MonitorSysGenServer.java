package com.chua.starter.monitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.common.support.base.validator.group.AddGroup;
import com.chua.common.support.base.validator.group.UpdateGroup;
import com.chua.starter.monitor.controller.server.MonitorSysGenServerController;
import com.chua.starter.monitor.mapper.MonitorSysGenServerComponentMapper;
import com.chua.starter.monitor.mapper.MonitorSysGenServerLogConfigMapper;
import com.chua.starter.monitor.mapper.MonitorSysGenServerMapper;
import com.chua.starter.monitor.mapper.MonitorSysGenServerSettingMapper;
import com.chua.starter.monitor.service.server.MonitorSysGenServerService;
import com.chua.starter.monitor.service.impl.MonitorSysGenServerServiceImpl;
import com.chua.starter.monitor.utils.ServerInfoDetector;
import com.chua.starter.monitor.utils.ServerLogUtils;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

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
public class MonitorSysGenServer extends SysBase {

    /**
     * 服务器配置ID
     *
     * @see MonitorSysGenServerController#getByIdAndConfig
     * @see MonitorSysGenServerController#testConnection
     * @see MonitorSysGenServerController#getConnectionStatus
     * @see MonitorSysGenServerService#getByIdWithCache
     * @see MonitorSysGenServerService#getByIdAndConfig
     * @see MonitorSysGenServerService#testConnection
     * @see MonitorSysGenServerMapper#selectByGenId
     * @see MonitorSysGenServerSettingMapper#selectByServerId
     * @see MonitorSysGenServerComponentMapper#selectByServerId
     * @see MonitorSysGenServerLogConfigMapper#selectByServerId
     */
    @TableId(value = "monitor_sys_gen_server_id", type = IdType.AUTO)
    @ApiModelProperty(value = "服务器配置ID")
    @Schema(description = "服务器配置ID")
    @NotNull(message = "服务器ID不能为空", groups = {UpdateGroup.class})
    private Integer monitorSysGenServerId;

    /**
     * 服务器名称
     *
     * @see MonitorSysGenServerController#page
     * @see MonitorSysGenServerController#cloneServer
     * @see MonitorSysGenServerService#pageFor
     * @see MonitorSysGenServerService#cloneServer
     * @see MonitorSysGenServerServiceImpl#disconnect
     */
    @TableField(value = "monitor_sys_gen_server_name")
    @ApiModelProperty(value = "服务器名称")
    @Schema(description = "服务器名称")
    @NotBlank(message = "服务器名称不能为空", groups = {AddGroup.class, UpdateGroup.class})
    @Size(max = 255, message = "服务器名称最大长度要小于 255")
    private String monitorSysGenServerName;

    /**
     * 服务器主机地址
     *
     * @see MonitorSysGenServerController#testConnection
     * @see MonitorSysGenServerService#testConnection
     * @see MonitorSysGenServerService#executeCommand
     * @see MonitorSysGenServerService#collectMetrics
     * @see MonitorSysGenServerServiceImpl#autoDetectServerInfo
     * @see ServerInfoDetector#detectServerInfo
     */
    @TableField(value = "monitor_sys_gen_server_host")
    @ApiModelProperty(value = "服务器主机地址")
    @Schema(description = "服务器主机地址")
    @NotBlank(message = "服务器地址不能为空", groups = {AddGroup.class, UpdateGroup.class})
    @Size(max = 255, message = "服务器主机地址最大长度要小于 255")
    private String monitorSysGenServerHost;

    /**
     * 服务器端口
     *
     * @see MonitorSysGenServerController#testConnection
     * @see MonitorSysGenServerService#testConnection
     * @see MonitorSysGenServerService#executeCommand
     * @see MonitorSysGenServerService#collectMetrics
     * @see ServerLogUtils#logServerConnect
     */
    @TableField(value = "monitor_sys_gen_server_port")
    @ApiModelProperty(value = "服务器端口")
    @Schema(description = "服务器端口")
    @NotNull(message = "端口号不能为空", groups = {AddGroup.class, UpdateGroup.class})
    private Integer monitorSysGenServerPort;

    /**
     * 连接协议 (SSH, RDP, TELNET, VNC等)
     *
     * @see MonitorSysGenServerService#collectMetrics
     * @see MonitorSysGenServerServiceImpl#getConnectionService
     * @see MonitorSysGenServerMapper#countByProtocol
     * @see MonitorSysGenServerMapper#selectByProtocol
     */
    @TableField(value = "monitor_sys_gen_server_protocol")
    @ApiModelProperty(value = "连接协议")
    @Schema(description = "连接协议")
    @NotBlank(message = "连接协议不能为空", groups = {AddGroup.class, UpdateGroup.class})
    @Size(max = 50, message = "连接协议最大长度要小于 50")
    private String monitorSysGenServerProtocol;

    /**
     * 用户名
     *
     * @see MonitorSysGenServerService#testConnection
     * @see MonitorSysGenServerService#executeCommand
     * @see MonitorSysGenServerService#collectMetrics
     * @see MonitorSysGenServerServiceImpl#isConnectionFieldsChanged
     */
    @TableField(value = "monitor_sys_gen_server_username")
    @ApiModelProperty(value = "用户名")
    @Schema(description = "用户名")
    @NotBlank(message = "用户名不能为空", groups = {AddGroup.class, UpdateGroup.class})
    @Size(max = 255, message = "用户名最大长度要小于 255")
    private String monitorSysGenServerUsername;

    /**
     * 密码
     *
     * @see MonitorSysGenServerService#testConnection
     * @see MonitorSysGenServerService#executeCommand
     * @see MonitorSysGenServerService#collectMetrics
     * @see MonitorSysGenServerServiceImpl#isConnectionFieldsChanged
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
     * 服务器标签 (用于分组)
     */
    @TableField(value = "monitor_sys_gen_server_tags")
    @ApiModelProperty(value = "服务器标签")
    @Schema(description = "服务器标签")
    @Size(max = 500, message = "服务器标签最大长度要小于 500")
    private String monitorSysGenServerTags;

    /**
     * 连接状态 0:离线 1:在线 2:连接中 3:连接失败
     */
    @TableField(value = "monitor_sys_gen_server_connection_status")
    @ApiModelProperty(value = "连接状态")
    @Schema(description = "连接状态")
    private Integer monitorSysGenServerConnectionStatus;

    /**
     * 最后连接时间
     */
    @TableField(value = "monitor_sys_gen_server_last_connect_time")
    @ApiModelProperty(value = "最后连接时间")
    @Schema(description = "最后连接时间")
    private LocalDateTime monitorSysGenServerLastConnectTime;

    /**
     * 连接失败原因
     */
    @TableField(value = "monitor_sys_gen_server_connection_error")
    @ApiModelProperty(value = "连接失败原因")
    @Schema(description = "连接失败原因")
    @Size(max = 1000, message = "连接失败原因最大长度要小于 1000")
    private String monitorSysGenServerConnectionError;

    /**
     * 代理ID (关联MonitorProxy表)
     */
    @TableField(value = "monitor_sys_gen_server_proxy_id")
    @ApiModelProperty(value = "代理ID")
    @Schema(description = "代理ID")
    private Integer monitorSysGenServerProxyId;

    /**
     * 是否本地服务器 0:否 1:是 (自动检测，不允许修改)
     */
    @TableField(value = "monitor_sys_gen_server_is_local")
    @ApiModelProperty(value = "是否本地服务器")
    @Schema(description = "是否本地服务器")
    private Integer monitorSysGenServerIsLocal;

    /**
     * 服务器IP地址列表 (JSON格式，支持多个IP)
     */
    @TableField(value = "monitor_sys_gen_server_ip_addresses")
    @ApiModelProperty(value = "服务器IP地址列表")
    @Schema(description = "服务器IP地址列表")
    @Size(max = 1000, message = "服务器IP地址列表最大长度要小于 1000")
    private String monitorSysGenServerIpAddresses;

    // Docker相关配置已迁移到 MonitorSysGenServerSetting 表

    /**
     * 操作系统类型 (自动检测)
     */
    @TableField(value = "monitor_sys_gen_server_os_type")
    @ApiModelProperty(value = "操作系统类型")
    @Schema(description = "操作系统类型")
    @Size(max = 50, message = "操作系统类型最大长度要小于 50")
    private String monitorSysGenServerOsType;

    /**
     * 操作系统版本 (自动检测)
     */
    @TableField(value = "monitor_sys_gen_server_os_version")
    @ApiModelProperty(value = "操作系统版本")
    @Schema(description = "操作系统版本")
    @Size(max = 100, message = "操作系统版本最大长度要小于 100")
    private String monitorSysGenServerOsVersion;

    /**
     * 操作系统架构 (自动检测)
     */
    @TableField(value = "monitor_sys_gen_server_os_arch")
    @ApiModelProperty(value = "操作系统架构")
    @Schema(description = "操作系统架构")
    @Size(max = 50, message = "操作系统架构最大长度要小于 50")
    private String monitorSysGenServerOsArch;

    /**
     * 网络延迟(毫秒)
     */
    @TableField(value = "monitor_sys_gen_server_latency")
    @ApiModelProperty(value = "网络延迟(毫秒)")
    @Schema(description = "网络延迟(毫秒)")
    private Long monitorSysGenServerLatency;

    /**
     * 服务器分组ID
     */
    @TableField(value = "monitor_sys_gen_server_group_id")
    @ApiModelProperty(value = "服务器分组ID")
    @Schema(description = "服务器分组ID")
    private Integer monitorSysGenServerGroupId;

    /**
     * 是否启用连接监控 0:否 1:是
     */
    @TableField(value = "monitor_sys_gen_server_connection_monitor_enabled")
    @ApiModelProperty(value = "是否启用连接监控")
    @Schema(description = "是否启用连接监控")
    private Integer monitorSysGenServerConnectionMonitorEnabled;

    /**
     * 连接监控配置(JSON)
     */
    @TableField(value = "monitor_sys_gen_server_connection_monitor_config")
    @ApiModelProperty(value = "连接监控配置")
    @Schema(description = "连接监控配置")
    private String monitorSysGenServerConnectionMonitorConfig;

    /**
     * 判断延迟是否正常 延迟小于100ms认为正常
     */
    public boolean isLatencyNormal() {
        return this.monitorSysGenServerLatency != null && this.monitorSysGenServerLatency < 100;
    }

    /**
     * 判断延迟是否较高 延迟大于等于100ms且小于500ms认为较高
     */
    public boolean isLatencyHigh() {
        return this.monitorSysGenServerLatency != null && this.monitorSysGenServerLatency >= 100
                && this.monitorSysGenServerLatency < 500;
    }

    /**
     * 判断延迟是否异常 延迟大于等于500ms认为异常
     */
    public boolean isLatencyAbnormal() {
        return this.monitorSysGenServerLatency != null && this.monitorSysGenServerLatency >= 500;
    }

}
