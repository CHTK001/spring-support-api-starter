package com.chua.starter.monitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 配置推送历史实体
 *
 * @author CH
 * @since 2024/12/16
 */
@Schema(description = "配置推送历史实体")
@Data
@TableName("monitor_sys_gen_config_push_history")
public class MonitorSysGenConfigPushHistory {

    /**
     * 历史ID
     */
    @Schema(description = "历史ID", example = "1")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 配置ID
     */
    @Schema(description = "配置ID", example = "1")
    @TableField("monitor_sys_gen_config_id")
    private Integer monitorSysGenConfigId;

    /**
     * 配置键
     */
    @Schema(description = "配置键", example = "app.name")
    @TableField("monitor_sys_gen_config_key")
    private String monitorSysGenConfigKey;

    /**
     * 配置值（推送时的快照）
     */
    @Schema(description = "配置值", example = "Monitor Application")
    @TableField("monitor_sys_gen_config_value")
    private String monitorSysGenConfigValue;

    /**
     * 服务器ID
     */
    @Schema(description = "服务器ID", example = "1")
    @TableField("monitor_sys_gen_server_id")
    private Integer monitorSysGenServerId;

    /**
     * 服务器名称
     */
    @Schema(description = "服务器名称", example = "Server-01")
    @TableField("monitor_sys_gen_server_name")
    private String monitorSysGenServerName;

    /**
     * 是否成功 0-失败 1-成功
     */
    @Schema(description = "是否成功", example = "1")
    @TableField("push_success")
    private Integer pushSuccess;

    /**
     * 错误信息
     */
    @Schema(description = "错误信息")
    @TableField("error_message")
    private String errorMessage;

    /**
     * 推送时间
     */
    @Schema(description = "推送时间")
    @TableField("push_time")
    private LocalDateTime pushTime;

    /**
     * 操作人
     */
    @Schema(description = "操作人")
    @TableField("operator")
    private String operator;

    /**
     * 检查是否推送成功
     */
    public boolean isSuccess() {
        return pushSuccess != null && pushSuccess == 1;
    }
}
