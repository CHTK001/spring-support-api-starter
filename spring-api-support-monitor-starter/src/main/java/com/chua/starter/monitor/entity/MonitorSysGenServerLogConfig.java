package com.chua.starter.monitor.entity;

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
 * 服务器日志配置实体类
 *
 * @author CH
 * @since 2024/12/20
 */
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "服务器日志配置")
@Schema(description = "服务器日志配置")
@Data
@TableName(value = "monitor_sys_gen_server_log_config")
public class MonitorSysGenServerLogConfig extends SysBase implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "monitor_sys_gen_server_log_config_id", type = IdType.AUTO)
    @ApiModelProperty(value = "配置ID")
    @Schema(description = "配置ID")
    @NotNull(message = "不能为null")
    private Long monitorSysGenServerLogConfigId;

    /**
     * 关联服务器ID
     */
    @TableField(value = "monitor_sys_gen_server_id")
    @ApiModelProperty(value = "关联服务器ID")
    @Schema(description = "关联服务器ID")
    @NotNull(message = "服务器ID不能为空")
    private Integer monitorSysGenServerId;

    /**
     * 日志保留天数
     */
    @TableField(value = "monitor_sys_gen_server_log_retention_days")
    @ApiModelProperty(value = "日志保留天数")
    @Schema(description = "日志保留天数")
    private Integer monitorSysGenServerLogRetentionDays;

    /**
     * 是否启用日志收集
     */
    @TableField(value = "monitor_sys_gen_server_log_enabled")
    @ApiModelProperty(value = "是否启用日志收集")
    @Schema(description = "是否启用日志收集(0:否 1:是)")
    private Integer monitorSysGenServerLogEnabled;

    /**
     * 日志级别过滤器
     */
    @TableField(value = "monitor_sys_gen_server_log_level_filter")
    @ApiModelProperty(value = "日志级别过滤器")
    @Schema(description = "日志级别过滤器")
    @Size(max = 100, message = "日志级别过滤器最大长度要小于 100")
    private String monitorSysGenServerLogLevelFilter;

    /**
     * 日志来源过滤器
     */
    @TableField(value = "monitor_sys_gen_server_log_source_filter")
    @ApiModelProperty(value = "日志来源过滤器")
    @Schema(description = "日志来源过滤器")
    @Size(max = 200, message = "日志来源过滤器最大长度要小于 200")
    private String monitorSysGenServerLogSourceFilter;

    /**
     * 单个日志文件最大大小(MB)
     */
    @TableField(value = "monitor_sys_gen_server_log_max_size_mb")
    @ApiModelProperty(value = "单个日志文件最大大小(MB)")
    @Schema(description = "单个日志文件最大大小(MB)")
    private Integer monitorSysGenServerLogMaxSizeMb;

    /**
     * 最大日志文件数量
     */
    @TableField(value = "monitor_sys_gen_server_log_max_files")
    @ApiModelProperty(value = "最大日志文件数量")
    @Schema(description = "最大日志文件数量")
    private Integer monitorSysGenServerLogMaxFiles;

    /**
     * 日志收集间隔(秒)
     */
    @TableField(value = "monitor_sys_gen_server_log_collection_interval")
    @ApiModelProperty(value = "日志收集间隔(秒)")
    @Schema(description = "日志收集间隔(秒)")
    private Integer monitorSysGenServerLogCollectionInterval;

    /**
     * 批量处理大小
     */
    @TableField(value = "monitor_sys_gen_server_log_batch_size")
    @ApiModelProperty(value = "批量处理大小")
    @Schema(description = "批量处理大小")
    private Integer monitorSysGenServerLogBatchSize;

    /**
     * 是否启用压缩
     */
    @TableField(value = "monitor_sys_gen_server_log_compression_enabled")
    @ApiModelProperty(value = "是否启用压缩")
    @Schema(description = "是否启用压缩(0:否 1:是)")
    private Integer monitorSysGenServerLogCompressionEnabled;

    /**
     * 是否启用实时日志
     */
    @TableField(value = "monitor_sys_gen_server_log_real_time_enabled")
    @ApiModelProperty(value = "是否启用实时日志")
    @Schema(description = "是否启用实时日志(0:否 1:是)")
    private Integer monitorSysGenServerLogRealTimeEnabled;

    /**
     * 日志文件匹配模式(JSON格式)
     */
    @TableField(value = "monitor_sys_gen_server_log_file_patterns")
    @ApiModelProperty(value = "日志文件匹配模式")
    @Schema(description = "日志文件匹配模式(JSON格式)")
    private String monitorSysGenServerLogFilePatterns;

    /**
     * 排除模式(JSON格式)
     */
    @TableField(value = "monitor_sys_gen_server_log_exclude_patterns")
    @ApiModelProperty(value = "排除模式")
    @Schema(description = "排除模式(JSON格式)")
    private String monitorSysGenServerLogExcludePatterns;

    /**
     * 自定义解析器配置(JSON格式)
     */
    @TableField(value = "monitor_sys_gen_server_log_custom_parsers")
    @ApiModelProperty(value = "自定义解析器配置")
    @Schema(description = "自定义解析器配置(JSON格式)")
    private String monitorSysGenServerLogCustomParsers;

    /**
     * 告警规则配置(JSON格式)
     */
    @TableField(value = "monitor_sys_gen_server_log_alert_rules")
    @ApiModelProperty(value = "告警规则配置")
    @Schema(description = "告警规则配置(JSON格式)")
    private String monitorSysGenServerLogAlertRules;

    /**
     * 创建时间
     */
    @TableField(value = "monitor_sys_gen_server_log_create_time")
    @ApiModelProperty(value = "创建时间")
    @Schema(description = "创建时间")
    private LocalDateTime monitorSysGenServerLogCreateTime;

    /**
     * 更新时间
     */
    @TableField(value = "monitor_sys_gen_server_log_update_time")
    @ApiModelProperty(value = "更新时间")
    @Schema(description = "更新时间")
    private LocalDateTime monitorSysGenServerLogUpdateTime;

    /**
     * 配置状态(0:禁用 1:启用)
     */
    @TableField(value = "monitor_sys_gen_server_log_config_status")
    @ApiModelProperty(value = "配置状态")
    @Schema(description = "配置状态(0:禁用 1:启用)")
    private Integer monitorSysGenServerLogConfigStatus;

    /**
     * 是否启用日志收集
     */
    public boolean isLogEnabled() {
        return this.monitorSysGenServerLogEnabled != null && this.monitorSysGenServerLogEnabled == 1;
    }

    /**
     * 是否启用压缩
     */
    public boolean isCompressionEnabled() {
        return this.monitorSysGenServerLogCompressionEnabled != null && this.monitorSysGenServerLogCompressionEnabled == 1;
    }

    /**
     * 是否启用实时日志
     */
    public boolean isRealTimeEnabled() {
        return this.monitorSysGenServerLogRealTimeEnabled != null && this.monitorSysGenServerLogRealTimeEnabled == 1;
    }

    /**
     * 配置是否启用
     */
    public boolean isConfigEnabled() {
        return this.monitorSysGenServerLogConfigStatus != null && this.monitorSysGenServerLogConfigStatus == 1;
    }

    /**
     * 获取日志级别过滤器数组
     */
    public String[] getLogLevelFilterArray() {
        if (this.monitorSysGenServerLogLevelFilter == null || this.monitorSysGenServerLogLevelFilter.trim().isEmpty()) {
            return new String[]{"INFO", "WARN", "ERROR", "FATAL"};
        }
        return this.monitorSysGenServerLogLevelFilter.split(",");
    }

    /**
     * 获取日志来源过滤器数组
     */
    public String[] getLogSourceFilterArray() {
        if (this.monitorSysGenServerLogSourceFilter == null || this.monitorSysGenServerLogSourceFilter.trim().isEmpty()) {
            return new String[0];
        }
        return this.monitorSysGenServerLogSourceFilter.split(",");
    }

    /**
     * 设置默认配置
     */
    public void setDefaultConfig() {
        if (this.monitorSysGenServerLogRetentionDays == null) {
            this.monitorSysGenServerLogRetentionDays = 30;
        }
        if (this.monitorSysGenServerLogEnabled == null) {
            this.monitorSysGenServerLogEnabled = 1;
        }
        if (this.monitorSysGenServerLogLevelFilter == null) {
            this.monitorSysGenServerLogLevelFilter = "INFO,WARN,ERROR,FATAL";
        }
        if (this.monitorSysGenServerLogMaxSizeMb == null) {
            this.monitorSysGenServerLogMaxSizeMb = 100;
        }
        if (this.monitorSysGenServerLogMaxFiles == null) {
            this.monitorSysGenServerLogMaxFiles = 10;
        }
        if (this.monitorSysGenServerLogCollectionInterval == null) {
            this.monitorSysGenServerLogCollectionInterval = 60;
        }
        if (this.monitorSysGenServerLogBatchSize == null) {
            this.monitorSysGenServerLogBatchSize = 1000;
        }
        if (this.monitorSysGenServerLogCompressionEnabled == null) {
            this.monitorSysGenServerLogCompressionEnabled = 1;
        }
        if (this.monitorSysGenServerLogRealTimeEnabled == null) {
            this.monitorSysGenServerLogRealTimeEnabled = 0;
        }
        if (this.monitorSysGenServerLogConfigStatus == null) {
            this.monitorSysGenServerLogConfigStatus = 1;
        }
    }

    /**
     * 验证配置有效性
     */
    public boolean isValidConfig() {
        return this.monitorSysGenServerId != null &&
               this.monitorSysGenServerLogRetentionDays != null && this.monitorSysGenServerLogRetentionDays > 0 &&
               this.monitorSysGenServerLogCollectionInterval != null && this.monitorSysGenServerLogCollectionInterval > 0 &&
               this.monitorSysGenServerLogBatchSize != null && this.monitorSysGenServerLogBatchSize > 0;
    }
}
