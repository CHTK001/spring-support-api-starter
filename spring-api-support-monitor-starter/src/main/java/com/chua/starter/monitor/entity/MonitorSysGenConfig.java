package com.chua.starter.monitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 配置管理实体
 *
 * @author CH
 * @since 2024/12/16
 */
@Schema(description = "配置管理实体")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("monitor_sys_gen_config")
public class MonitorSysGenConfig extends SysBase {

    /**
     * 配置ID
     */
    @Schema(description = "配置ID", example = "1")
    @TableId(value = "monitor_sys_gen_config_id", type = IdType.AUTO)
    private Integer monitorSysGenConfigId;

    /**
     * 配置键
     */
    @Schema(description = "配置键", example = "app.name")
    @TableField("monitor_sys_gen_config_key")
    private String monitorSysGenConfigKey;

    /**
     * 配置值
     */
    @Schema(description = "配置值", example = "Monitor Application")
    @TableField("monitor_sys_gen_config_value")
    private String monitorSysGenConfigValue;

    /**
     * 配置描述
     */
    @Schema(description = "配置描述", example = "应用名称配置")
    @TableField("monitor_sys_gen_config_description")
    private String monitorSysGenConfigDescription;

    /**
     * 环境
     */
    @Schema(description = "环境", example = "dev")
    @TableField("monitor_sys_gen_config_env")
    private String monitorSysGenConfigEnv;

    /**
     * 配置状态 0-禁用 1-启用
     */
    @Schema(description = "配置状态", example = "1")
    @TableField("monitor_sys_gen_config_status")
    private Integer monitorSysGenConfigStatus;

    /**
     * 所属应用
     */
    @Schema(description = "所属应用", example = "monitor-app")
    @TableField("monitor_sys_gen_config_app")
    private String monitorSysGenConfigApp;

    /**
     * 检查配置是否启用
     */
    public boolean isEnabled() {
        return monitorSysGenConfigStatus != null && monitorSysGenConfigStatus == 1;
    }
}
