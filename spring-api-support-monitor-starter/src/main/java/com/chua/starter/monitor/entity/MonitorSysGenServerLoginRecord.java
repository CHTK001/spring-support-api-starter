package com.chua.starter.monitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 服务器登录记录实体类
 *
 * @author CH
 */
@ApiModel(description = "服务器登录记录")
@Schema(description = "服务器登录记录")
@Data
@TableName("monitor_sys_gen_server_login_record")
public class MonitorSysGenServerLoginRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @ApiModelProperty(value = "主键ID")
    @Schema(description = "主键ID")
    @TableId(type = IdType.AUTO)
    private Integer id;

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
    private String host;

    /**
     * 登录用户名
     */
    @ApiModelProperty(value = "登录用户名")
    @Schema(description = "登录用户名")
    private String username;

    /**
     * 登录方式（SSH/TELNET/RDP/VNC等）
     */
    @ApiModelProperty(value = "登录方式")
    @Schema(description = "登录方式")
    private String loginType;

    /**
     * 登录IP地址
     */
    @ApiModelProperty(value = "登录IP地址")
    @Schema(description = "登录IP地址")
    private String loginIp;

    /**
     * 登录状态（SUCCESS/FAILED）
     */
    @ApiModelProperty(value = "登录状态")
    @Schema(description = "登录状态")
    private String status;

    /**
     * 登录时间
     */
    @ApiModelProperty(value = "登录时间")
    @Schema(description = "登录时间")
    private LocalDateTime loginTime;

    /**
     * 登出时间
     */
    @ApiModelProperty(value = "登出时间")
    @Schema(description = "登出时间")
    private LocalDateTime logoutTime;

    /**
     * 登录耗时（毫秒）
     */
    @ApiModelProperty(value = "登录耗时（毫秒）")
    @Schema(description = "登录耗时")
    private Long duration;

    /**
     * 登录结果描述
     */
    @ApiModelProperty(value = "登录结果描述")
    @Schema(description = "登录结果描述")
    private String message;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}