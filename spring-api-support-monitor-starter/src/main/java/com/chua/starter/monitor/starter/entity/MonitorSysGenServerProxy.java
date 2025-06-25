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
 * 服务器代理关联实体类
 *
 * @author CH
 * @since 2024/12/25
 */
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "服务器代理关联")
@Schema(description = "服务器代理关联")
@Data
@TableName(value = "monitor_sys_gen_server_proxy")
public class MonitorSysGenServerProxy extends SysBase implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "monitor_sys_gen_server_proxy_id", type = IdType.AUTO)
    @ApiModelProperty(value = "服务器代理关联ID")
    @Schema(description = "服务器代理关联ID")
    @NotNull(message = "不能为null")
    private Integer monitorSysGenServerProxyId;

    /**
     * 服务器ID
     */
    @TableField(value = "monitor_sys_gen_server_id")
    @ApiModelProperty(value = "服务器ID")
    @Schema(description = "服务器ID")
    @NotNull(message = "服务器ID不能为空")
    private Integer monitorSysGenServerId;



    /**
     * 代理类型 (GUACAMOLE, VNC, RDP等)
     */
    @TableField(value = "monitor_sys_gen_server_proxy_type")
    @ApiModelProperty(value = "代理类型")
    @Schema(description = "代理类型")
    @Size(max = 50, message = "代理类型最大长度要小于 50")
    private String monitorSysGenServerProxyType;

    /**
     * 代理配置 (JSON格式)
     */
    @TableField(value = "monitor_sys_gen_server_proxy_config")
    @ApiModelProperty(value = "代理配置")
    @Schema(description = "代理配置")
    @Size(max = 2000, message = "代理配置最大长度要小于 2000")
    private String monitorSysGenServerProxyConfig;

    /**
     * 是否启用 0:否 1:是
     */
    @TableField(value = "monitor_sys_gen_server_proxy_enabled")
    @ApiModelProperty(value = "是否启用")
    @Schema(description = "是否启用")
    private Integer monitorSysGenServerProxyEnabled;

    /**
     * 代理状态 0:离线 1:在线 2:连接中 3:连接失败
     */
    @TableField(value = "monitor_sys_gen_server_proxy_status")
    @ApiModelProperty(value = "代理状态")
    @Schema(description = "代理状态")
    private Integer monitorSysGenServerProxyStatus;

    /**
     * 最后连接时间
     */
    @TableField(value = "monitor_sys_gen_server_proxy_last_connect_time")
    @ApiModelProperty(value = "最后连接时间")
    @Schema(description = "最后连接时间")
    private LocalDateTime monitorSysGenServerProxyLastConnectTime;

    /**
     * 连接失败原因
     */
    @TableField(value = "monitor_sys_gen_server_proxy_connection_error")
    @ApiModelProperty(value = "连接失败原因")
    @Schema(description = "连接失败原因")
    @Size(max = 1000, message = "连接失败原因最大长度要小于 1000")
    private String monitorSysGenServerProxyConnectionError;

    /**
     * 代理URL
     */
    @TableField(value = "monitor_sys_gen_server_proxy_url")
    @ApiModelProperty(value = "代理URL")
    @Schema(description = "代理URL")
    @Size(max = 500, message = "代理URL最大长度要小于 500")
    private String monitorSysGenServerProxyUrl;

    /**
     * 创建时间
     */
    @TableField(value = "monitor_sys_gen_server_proxy_create_time")
    @ApiModelProperty(value = "创建时间")
    @Schema(description = "创建时间")
    private LocalDateTime monitorSysGenServerProxyCreateTime;

    /**
     * 更新时间
     */
    @TableField(value = "monitor_sys_gen_server_proxy_update_time")
    @ApiModelProperty(value = "更新时间")
    @Schema(description = "更新时间")
    private LocalDateTime monitorSysGenServerProxyUpdateTime;

    /**
     * 备注
     */
    @TableField(value = "monitor_sys_gen_server_proxy_remark")
    @ApiModelProperty(value = "备注")
    @Schema(description = "备注")
    @Size(max = 500, message = "备注最大长度要小于 500")
    private String monitorSysGenServerProxyRemark;
}
