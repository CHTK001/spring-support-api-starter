package com.chua.starter.monitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 全局配置设置实体类
 * <p>
 * 用于控制监控系统的全局任务开关，包括：
 * <ul>
 *   <li>全局调度器开关</li>
 *   <li>服务器状态汇总推送开关</li>
 *   <li>性能趋势推送开关</li>
 *   <li>连接统计推送开关</li>
 *   <li>健康状态推送开关</li>
 *   <li>全局告警处理开关</li>
 *   <li>无效任务清理开关</li>
 *   <li>配置刷新开关</li>
 *   <li>文件上传检查开关</li>
 *   <li>数据清理开关</li>
 *   <li>备份任务开关</li>
 * </ul>
 * </p>
 *
 * @author CH
 * @since 2024/12/10
 * @version 1.0.0
 */
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "全局配置设置")
@Schema(description = "全局配置设置")
@Data
@TableName(value = "monitor_sys_gen_global_setting")
public class MonitorSysGenGlobalSetting extends SysBase {

    /**
     * 全局配置ID
     */
    @TableId(value = "monitor_sys_gen_global_setting_id", type = IdType.AUTO)
    @ApiModelProperty(value = "全局配置ID")
    @Schema(description = "全局配置ID")
    @NotNull(message = "配置ID不能为空", groups = { UpdateGroup.class })
    private Integer monitorSysGenGlobalSettingId;

    /**
     * 配置名称
     */
    @TableField(value = "monitor_sys_gen_global_setting_name")
    @ApiModelProperty(value = "配置名称")
    @Schema(description = "配置名称")
    private String monitorSysGenGlobalSettingName;

    /**
     * 配置描述
     */
    @TableField(value = "monitor_sys_gen_global_setting_description")
    @ApiModelProperty(value = "配置描述")
    @Schema(description = "配置描述")
    private String monitorSysGenGlobalSettingDescription;

    /**
     * 全局调度器总开关 0:关闭 1:开启
     */
    @TableField(value = "monitor_sys_gen_global_setting_dispatcher_enabled")
    @ApiModelProperty(value = "全局调度器总开关")
    @Schema(description = "全局调度器总开关 0:关闭 1:开启")
    private Integer monitorSysGenGlobalSettingDispatcherEnabled;

    /**
     * 服务器状态汇总推送开关 0:关闭 1:开启
     */
    @TableField(value = "monitor_sys_gen_global_setting_status_summary_enabled")
    @ApiModelProperty(value = "服务器状态汇总推送开关")
    @Schema(description = "服务器状态汇总推送开关 0:关闭 1:开启")
    private Integer monitorSysGenGlobalSettingStatusSummaryEnabled;

    /**
     * 性能趋势推送开关 0:关闭 1:开启
     */
    @TableField(value = "monitor_sys_gen_global_setting_performance_trends_enabled")
    @ApiModelProperty(value = "性能趋势推送开关")
    @Schema(description = "性能趋势推送开关 0:关闭 1:开启")
    private Integer monitorSysGenGlobalSettingPerformanceTrendsEnabled;

    /**
     * 连接统计推送开关 0:关闭 1:开启
     */
    @TableField(value = "monitor_sys_gen_global_setting_connection_stats_enabled")
    @ApiModelProperty(value = "连接统计推送开关")
    @Schema(description = "连接统计推送开关 0:关闭 1:开启")
    private Integer monitorSysGenGlobalSettingConnectionStatsEnabled;

    /**
     * 健康状态推送开关 0:关闭 1:开启
     */
    @TableField(value = "monitor_sys_gen_global_setting_health_status_enabled")
    @ApiModelProperty(value = "健康状态推送开关")
    @Schema(description = "健康状态推送开关 0:关闭 1:开启")
    private Integer monitorSysGenGlobalSettingHealthStatusEnabled;

    /**
     * 全局告警处理开关 0:关闭 1:开启
     */
    @TableField(value = "monitor_sys_gen_global_setting_global_alert_enabled")
    @ApiModelProperty(value = "全局告警处理开关")
    @Schema(description = "全局告警处理开关 0:关闭 1:开启")
    private Integer monitorSysGenGlobalSettingGlobalAlertEnabled;

    /**
     * 无效任务清理开关 0:关闭 1:开启
     */
    @TableField(value = "monitor_sys_gen_global_setting_cleanup_enabled")
    @ApiModelProperty(value = "无效任务清理开关")
    @Schema(description = "无效任务清理开关 0:关闭 1:开启")
    private Integer monitorSysGenGlobalSettingCleanupEnabled;

    /**
     * 配置刷新开关 0:关闭 1:开启
     */
    @TableField(value = "monitor_sys_gen_global_setting_config_refresh_enabled")
    @ApiModelProperty(value = "配置刷新开关")
    @Schema(description = "配置刷新开关 0:关闭 1:开启")
    private Integer monitorSysGenGlobalSettingConfigRefreshEnabled;

    /**
     * 文件上传检查开关 0:关闭 1:开启
     */
    @TableField(value = "monitor_sys_gen_global_setting_file_upload_check_enabled")
    @ApiModelProperty(value = "文件上传检查开关")
    @Schema(description = "文件上传检查开关 0:关闭 1:开启")
    private Integer monitorSysGenGlobalSettingFileUploadCheckEnabled;

    /**
     * 数据清理开关 0:关闭 1:开启
     */
    @TableField(value = "monitor_sys_gen_global_setting_data_cleanup_enabled")
    @ApiModelProperty(value = "数据清理开关")
    @Schema(description = "数据清理开关 0:关闭 1:开启")
    private Integer monitorSysGenGlobalSettingDataCleanupEnabled;

    /**
     * 备份任务开关 0:关闭 1:开启
     */
    @TableField(value = "monitor_sys_gen_global_setting_backup_enabled")
    @ApiModelProperty(value = "备份任务开关")
    @Schema(description = "备份任务开关 0:关闭 1:开启")
    private Integer monitorSysGenGlobalSettingBackupEnabled;

    /**
     * 获取默认全局配置
     *
     * @return 默认全局配置（所有开关默认开启）
     */
    public static MonitorSysGenGlobalSetting getDefaultSetting() {
        MonitorSysGenGlobalSetting setting = new MonitorSysGenGlobalSetting();
        setting.setMonitorSysGenGlobalSettingName("默认全局配置");
        setting.setMonitorSysGenGlobalSettingDescription("系统默认全局配置");
        setting.setMonitorSysGenGlobalSettingDispatcherEnabled(1);
        setting.setMonitorSysGenGlobalSettingStatusSummaryEnabled(1);
        setting.setMonitorSysGenGlobalSettingPerformanceTrendsEnabled(1);
        setting.setMonitorSysGenGlobalSettingConnectionStatsEnabled(1);
        setting.setMonitorSysGenGlobalSettingHealthStatusEnabled(1);
        setting.setMonitorSysGenGlobalSettingGlobalAlertEnabled(1);
        setting.setMonitorSysGenGlobalSettingCleanupEnabled(1);
        setting.setMonitorSysGenGlobalSettingConfigRefreshEnabled(1);
        setting.setMonitorSysGenGlobalSettingFileUploadCheckEnabled(1);
        setting.setMonitorSysGenGlobalSettingDataCleanupEnabled(1);
        setting.setMonitorSysGenGlobalSettingBackupEnabled(1);
        return setting;
    }
}
