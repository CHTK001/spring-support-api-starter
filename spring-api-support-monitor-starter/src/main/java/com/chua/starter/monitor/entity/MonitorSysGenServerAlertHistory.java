package com.chua.starter.monitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.monitor.starter.enums.AlertLevel;
import com.chua.starter.monitor.starter.enums.AlertStatus;
import com.chua.starter.monitor.starter.enums.AlertType;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 服务器告警历史实体类
 *
 * @author CH
 * @since 2024/12/27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("monitor_sys_gen_server_alert_history")
@ApiModel(value = "MonitorSysGenServerAlertHistory对象", description = "服务器告警历史")
@Schema(description = "服务器告警历史")
public class MonitorSysGenServerAlertHistory extends SysBase {

    /**
     * 告警历史ID
     */
    @TableId(value = "monitor_sys_gen_server_alert_history_id", type = IdType.AUTO)
    @ApiModelProperty(value = "告警历史ID")
    @Schema(description = "告警历史ID")
    private Long monitorSysGenServerAlertHistoryId;

    /**
     * 服务器ID
     */
    @TableField(value = "monitor_sys_gen_server_alert_history_server_id")
    @ApiModelProperty(value = "服务器ID")
    @Schema(description = "服务器ID")
    @NotNull(message = "服务器ID不能为空")
    private Integer monitorSysGenServerAlertHistoryServerId;

    /**
     * 告警类型
     */
    @TableField(value = "monitor_sys_gen_server_alert_history_type")
    @ApiModelProperty(value = "告警类型")
    @Schema(description = "告警类型")
    @NotNull(message = "告警类型不能为空")
    private AlertType monitorSysGenServerAlertHistoryType;

    /**
     * 告警级别
     */
    @TableField(value = "monitor_sys_gen_server_alert_history_level")
    @ApiModelProperty(value = "告警级别")
    @Schema(description = "告警级别")
    @NotNull(message = "告警级别不能为空")
    private AlertLevel monitorSysGenServerAlertHistoryLevel;

    /**
     * 告警状态
     */
    @TableField(value = "monitor_sys_gen_server_alert_history_status")
    @ApiModelProperty(value = "告警状态")
    @Schema(description = "告警状态")
    @NotNull(message = "告警状态不能为空")
    private AlertStatus monitorSysGenServerAlertHistoryStatus;

    /**
     * 告警标题
     */
    @TableField(value = "monitor_sys_gen_server_alert_history_title")
    @ApiModelProperty(value = "告警标题")
    @Schema(description = "告警标题")
    @Size(max = 200, message = "告警标题最大长度要小于 200")
    private String monitorSysGenServerAlertHistoryTitle;

    /**
     * 告警内容
     */
    @TableField(value = "monitor_sys_gen_server_alert_history_content")
    @ApiModelProperty(value = "告警内容")
    @Schema(description = "告警内容")
    private String monitorSysGenServerAlertHistoryContent;

    /**
     * 当前值
     */
    @TableField(value = "monitor_sys_gen_server_alert_history_current_value")
    @ApiModelProperty(value = "当前值")
    @Schema(description = "当前值")
    private BigDecimal monitorSysGenServerAlertHistoryCurrentValue;

    /**
     * 阈值
     */
    @TableField(value = "monitor_sys_gen_server_alert_history_threshold_value")
    @ApiModelProperty(value = "阈值")
    @Schema(description = "阈值")
    private BigDecimal monitorSysGenServerAlertHistoryThresholdValue;

    /**
     * 单位
     */
    @TableField(value = "monitor_sys_gen_server_alert_history_unit")
    @ApiModelProperty(value = "单位")
    @Schema(description = "单位")
    @Size(max = 20, message = "单位最大长度要小于 20")
    private String monitorSysGenServerAlertHistoryUnit;

    /**
     * 触发时间
     */
    @TableField(value = "monitor_sys_gen_server_alert_history_trigger_time")
    @ApiModelProperty(value = "触发时间")
    @Schema(description = "触发时间")
    @NotNull(message = "触发时间不能为空")
    private LocalDateTime monitorSysGenServerAlertHistoryTriggerTime;

    /**
     * 恢复时间
     */
    @TableField(value = "monitor_sys_gen_server_alert_history_recovery_time")
    @ApiModelProperty(value = "恢复时间")
    @Schema(description = "恢复时间")
    private LocalDateTime monitorSysGenServerAlertHistoryRecoveryTime;

    /**
     * 静默截止时间
     */
    @TableField(value = "monitor_sys_gen_server_alert_history_silence_until")
    @ApiModelProperty(value = "静默截止时间")
    @Schema(description = "静默截止时间")
    private LocalDateTime monitorSysGenServerAlertHistorySilenceUntil;

    /**
     * 告警指纹（用于去重）
     */
    @TableField(value = "monitor_sys_gen_server_alert_history_fingerprint")
    @ApiModelProperty(value = "告警指纹")
    @Schema(description = "告警指纹（用于去重）")
    @Size(max = 64, message = "告警指纹最大长度要小于 64")
    private String monitorSysGenServerAlertHistoryFingerprint;

    /**
     * 是否已发送通知
     */
    @TableField(value = "monitor_sys_gen_server_alert_history_notification_sent")
    @ApiModelProperty(value = "是否已发送通知")
    @Schema(description = "是否已发送通知")
    private Boolean monitorSysGenServerAlertHistoryNotificationSent;

    /**
     * 通知方式
     */
    @TableField(value = "monitor_sys_gen_server_alert_history_notification_method")
    @ApiModelProperty(value = "通知方式")
    @Schema(description = "通知方式")
    @Size(max = 50, message = "通知方式最大长度要小于 50")
    private String monitorSysGenServerAlertHistoryNotificationMethod;

    /**
     * 通知地址
     */
    @TableField(value = "monitor_sys_gen_server_alert_history_notification_address")
    @ApiModelProperty(value = "通知地址")
    @Schema(description = "通知地址")
    @Size(max = 200, message = "通知地址最大长度要小于 200")
    private String monitorSysGenServerAlertHistoryNotificationAddress;

    /**
     * 通知发送时间
     */
    @TableField(value = "monitor_sys_gen_server_alert_history_notification_time")
    @ApiModelProperty(value = "通知发送时间")
    @Schema(description = "通知发送时间")
    private LocalDateTime monitorSysGenServerAlertHistoryNotificationTime;

    /**
     * 备注
     */
    @TableField(value = "monitor_sys_gen_server_alert_history_remark")
    @ApiModelProperty(value = "备注")
    @Schema(description = "备注")
    @Size(max = 500, message = "备注最大长度要小于 500")
    private String monitorSysGenServerAlertHistoryRemark;

    /**
     * 忽略原因
     */
    @TableField(value = "monitor_sys_gen_server_alert_history_ignore_reason")
    @ApiModelProperty(value = "忽略原因")
    @Schema(description = "忽略原因")
    @Size(max = 500, message = "忽略原因最大长度要小于 500")
    private String monitorSysGenServerAlertHistoryIgnoreReason;

    /**
     * 计算告警持续时间（毫秒）
     */
    public Long calculateDuration() {
        if (this.monitorSysGenServerAlertHistoryTriggerTime == null) {
            return null;
        }

        LocalDateTime endTime = this.monitorSysGenServerAlertHistoryRecoveryTime;
        if (endTime == null) {
            // 如果没有恢复时间，使用当前时间
            endTime = LocalDateTime.now();
        }

        Duration duration = Duration.between(this.monitorSysGenServerAlertHistoryTriggerTime, endTime);
        return duration.toMillis();
    }
}
