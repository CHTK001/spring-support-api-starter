package com.chua.starter.monitor.entity;

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

/**
 * 被监控服务器主机实体，记录服务器的基础信息（名称、IP、主机名、操作系统、分组等），
 * 服务器布局、组件布局等均以 serverId 作为关联维度。
 *
 * @author CH
 * @since 2026-07-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("monitor_server")
@ApiModel(description = "被监控服务器")
@Schema(description = "被监控服务器")
public class MonitorServer extends SysBase {

    /**
     * 服务器主键ID，自增
     */
    @TableId(value = "server_id", type = IdType.AUTO)
    @ApiModelProperty("服务器ID")
    @Schema(description = "服务器ID")
    private Integer serverId;

    /**
     * 服务器名称，用于前端展示，例如 生产Web-01
     */
    @TableField("server_name")
    @ApiModelProperty("服务器名称")
    @Schema(description = "服务器名称")
    private String serverName;

    /**
     * 服务器IP地址，例如 192.168.1.10
     */
    @TableField("server_ip")
    @ApiModelProperty("服务器IP")
    @Schema(description = "服务器IP")
    private String serverIp;

    /**
     * 服务器主机名，例如 web-prod-01
     */
    @TableField("server_host")
    @ApiModelProperty("主机名")
    @Schema(description = "主机名")
    private String serverHost;

    /**
     * 服务器端口，可为空
     */
    @TableField("server_port")
    @ApiModelProperty("端口")
    @Schema(description = "端口")
    private Integer serverPort;

    /**
     * 操作系统，例如 Linux / Windows / macOS
     */
    @TableField("server_os")
    @ApiModelProperty("操作系统")
    @Schema(description = "操作系统")
    private String serverOs;

    /**
     * 服务器分组，用于归类，例如 生产环境 / 测试环境
     */
    @TableField("server_group")
    @ApiModelProperty("分组")
    @Schema(description = "分组")
    private String serverGroup;

    /**
     * 登录账号，可为空
     */
    @TableField("server_account")
    @ApiModelProperty("登录账号")
    @Schema(description = "登录账号")
    private String serverAccount;

    /**
     * 服务器状态，1-启用，0-禁用
     */
    @TableField("server_status")
    @ApiModelProperty("状态（0禁用/1启用）")
    @Schema(description = "状态（0禁用/1启用）")
    private Integer serverStatus;

    /**
     * 备注信息，可为空
     */
    @TableField("server_remark")
    @ApiModelProperty("备注")
    @Schema(description = "备注")
    private String serverRemark;
}
