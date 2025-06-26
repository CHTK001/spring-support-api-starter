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
     * 代理类型 (GUACAMOLE, VNC, RDP等)
     */
    @TableField(value = "monitor_sys_gen_server_proxy_type")
    @ApiModelProperty(value = "代理类型")
    @Schema(description = "代理类型")
    @Size(max = 50, message = "代理类型最大长度要小于 50")
    private String monitorSysGenServerProxyType;



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
     * 代理主机地址
     */
    @TableField(value = "monitor_sys_gen_server_proxy_host")
    @ApiModelProperty(value = "代理主机地址")
    @Schema(description = "代理主机地址")
    @Size(max = 255, message = "代理主机地址最大长度要小于 255")
    private String monitorSysGenServerProxyHost;

    /**
     * 代理端口
     */
    @TableField(value = "monitor_sys_gen_server_proxy_port")
    @ApiModelProperty(value = "代理端口")
    @Schema(description = "代理端口")
    private Integer monitorSysGenServerProxyPort;


    /**
     * 备注
     */
    @TableField(value = "monitor_sys_gen_server_proxy_remark")
    @ApiModelProperty(value = "备注")
    @Schema(description = "备注")
    @Size(max = 500, message = "备注最大长度要小于 500")
    private String monitorSysGenServerProxyRemark;
}
