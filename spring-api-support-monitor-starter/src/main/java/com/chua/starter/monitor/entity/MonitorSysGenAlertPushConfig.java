package com.chua.starter.monitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.monitor.enums.AlertNotificationMethod;
import com.chua.starter.monitor.enums.AlertType;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 告警推送配置
 *
 * 每个不同告警类型可配置多个通道（除 WEB 外），支持账号、密码、主账号、Webhook 等。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("monitor_sys_gen_alert_push_config")
@ApiModel("告警推送配置")
@Schema(name = "MonitorSysGenAlertPushConfig")
public class MonitorSysGenAlertPushConfig extends SysBase {

    @TableId(value = "monitor_sys_gen_alert_push_config_id", type = IdType.AUTO)
    @ApiModelProperty("主键ID")
    @Schema(description = "主键ID")
    @NotNull(message = "主键不能为空", groups = { })
    private Integer monitorSysGenAlertPushConfigId;

    /** 服务器ID，空或0表示全局 */
    @TableField("monitor_sys_gen_server_id")
    @ApiModelProperty("服务器ID，空或0表示全局")
    private Integer monitorSysGenServerId;

    /** 告警类型 */
    @TableField("monitor_sys_gen_alert_push_config_type")
    @ApiModelProperty("告警类型")
    private AlertType monitorSysGenAlertPushConfigType;

    /** 通知通道（不含 WEB） */
    @TableField("monitor_sys_gen_alert_push_config_channel")
    @ApiModelProperty("通知通道（EMAIL/SMS/WEBHOOK/DINGTALK/WECHAT 等）")
    private AlertNotificationMethod monitorSysGenAlertPushConfigChannel;

    /** 模板ID（优先于本条配置项） */
    @TableField("monitor_sys_gen_alert_push_config_template_id")
    @ApiModelProperty("模板ID（优先于本条配置项）")
    private Integer monitorSysGenAlertPushConfigTemplateId;

    /** 是否启用 */
    @TableField("monitor_sys_gen_alert_push_config_enabled")
    @ApiModelProperty("是否启用")
    private Boolean monitorSysGenAlertPushConfigEnabled;

    /** 目标地址（Webhook URL、邮箱、手机号等） */
    @TableField("monitor_sys_gen_alert_push_config_endpoint")
    @ApiModelProperty("目标地址（Webhook URL、邮箱、手机号等）")
    @Size(max = 1024)
    private String monitorSysGenAlertPushConfigEndpoint;

    /** 主账号 */
    @TableField("monitor_sys_gen_alert_push_config_main_account")
    @ApiModelProperty("主账号")
    @Size(max = 255)
    private String monitorSysGenAlertPushConfigMainAccount;

    /** 用户名 */
    @TableField("monitor_sys_gen_alert_push_config_username")
    @ApiModelProperty("用户名")
    @Size(max = 255)
    private String monitorSysGenAlertPushConfigUsername;

    /** 密码 */
    @TableField("monitor_sys_gen_alert_push_config_password")
    @ApiModelProperty("密码")
    @Size(max = 512)
    private String monitorSysGenAlertPushConfigPassword;

    /** 令牌/密钥 */
    @TableField("monitor_sys_gen_alert_push_config_token")
    @ApiModelProperty("令牌/密钥")
    @Size(max = 1024)
    private String monitorSysGenAlertPushConfigToken;

    /** 扩展参数（JSON） */
    @TableField("monitor_sys_gen_alert_push_config_extra")
    @ApiModelProperty("扩展参数（JSON）")
    private String monitorSysGenAlertPushConfigExtra;

    /** 备注 */
    @TableField("monitor_sys_gen_alert_push_config_remark")
    @ApiModelProperty("备注")
    @Size(max = 1024)
    private String monitorSysGenAlertPushConfigRemark;
}

