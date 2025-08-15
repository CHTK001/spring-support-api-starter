package com.chua.starter.monitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.monitor.enums.AlertNotificationMethod;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 消息推送模板（SPI 推送所需的账号/密钥/Endpoint 等统一配置）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("monitor_sys_gen_message_push_template")
@ApiModel("消息推送模板")
@Schema(name = "MonitorSysGenMessagePushTemplate")
public class MonitorSysGenMessagePushTemplate extends SysBase {

    @TableId(value = "monitor_sys_gen_message_push_template_id", type = IdType.AUTO)
    @ApiModelProperty("主键ID")
    @Schema(description = "主键ID")
    @NotNull(message = "主键不能为空", groups = { })
    private Integer monitorSysGenMessagePushTemplateId;

    /** 模板名称 */
    @TableField("monitor_sys_gen_message_push_template_name")
    @ApiModelProperty("模板名称")
    @Size(max = 255)
    private String monitorSysGenMessagePushTemplateName;

    /** 通知通道（EMAIL/SMS/WEBHOOK/DINGTALK/WECHAT 等） */
    @TableField("monitor_sys_gen_message_push_template_channel")
    @ApiModelProperty("通知通道")
    private AlertNotificationMethod monitorSysGenMessagePushTemplateChannel;

    /** 是否启用 */
    @TableField("monitor_sys_gen_message_push_template_enabled")
    @ApiModelProperty("是否启用")
    private Boolean monitorSysGenMessagePushTemplateEnabled;

    /** 目标地址（Webhook URL、邮箱、手机号等） */
    @TableField("monitor_sys_gen_message_push_template_endpoint")
    @ApiModelProperty("目标地址（Webhook URL、邮箱、手机号等）")
    @Size(max = 1024)
    private String monitorSysGenMessagePushTemplateEndpoint;

    /** 主账号 */
    @TableField("monitor_sys_gen_message_push_template_main_account")
    @ApiModelProperty("主账号")
    @Size(max = 255)
    private String monitorSysGenMessagePushTemplateMainAccount;

    /** 用户名 */
    @TableField("monitor_sys_gen_message_push_template_username")
    @ApiModelProperty("用户名")
    @Size(max = 255)
    private String monitorSysGenMessagePushTemplateUsername;

    /** 密码 */
    @TableField("monitor_sys_gen_message_push_template_password")
    @ApiModelProperty("密码")
    @Size(max = 512)
    private String monitorSysGenMessagePushTemplatePassword;

    /** 令牌/密钥 */
    @TableField("monitor_sys_gen_message_push_template_token")
    @ApiModelProperty("令牌/密钥")
    @Size(max = 1024)
    private String monitorSysGenMessagePushTemplateToken;

    /** 扩展参数（JSON） */
    @TableField("monitor_sys_gen_message_push_template_extra")
    @ApiModelProperty("扩展参数（JSON）")
    private String monitorSysGenMessagePushTemplateExtra;

    /** 备注 */
    @TableField("monitor_sys_gen_message_push_template_remark")
    @ApiModelProperty("备注")
    @Size(max = 1024)
    private String monitorSysGenMessagePushTemplateRemark;
}

