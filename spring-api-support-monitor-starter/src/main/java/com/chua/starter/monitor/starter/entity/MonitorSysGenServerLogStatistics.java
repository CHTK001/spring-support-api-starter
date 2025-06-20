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
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 服务器日志统计实体类
 *
 * @author CH
 * @since 2024/12/20
 */
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "服务器日志统计")
@Schema(description = "服务器日志统计")
@Data
@TableName(value = "monitor_sys_gen_server_log_statistics")
public class MonitorSysGenServerLogStatistics extends SysBase implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "monitor_sys_gen_server_log_stat_id", type = IdType.AUTO)
    @ApiModelProperty(value = "统计ID")
    @Schema(description = "统计ID")
    @NotNull(message = "不能为null")
    private Long monitorSysGenServerLogStatId;

    /**
     * 关联服务器ID
     */
    @TableField(value = "monitor_sys_gen_server_id")
    @ApiModelProperty(value = "关联服务器ID")
    @Schema(description = "关联服务器ID")
    @NotNull(message = "服务器ID不能为空")
    private Integer monitorSysGenServerId;

    /**
     * 统计日期
     */
    @TableField(value = "monitor_sys_gen_server_log_stat_date")
    @ApiModelProperty(value = "统计日期")
    @Schema(description = "统计日期")
    @NotNull(message = "统计日期不能为空")
    private LocalDate monitorSysGenServerLogStatDate;

    /**
     * 统计小时(0-23)
     */
    @TableField(value = "monitor_sys_gen_server_log_stat_hour")
    @ApiModelProperty(value = "统计小时")
    @Schema(description = "统计小时(0-23)")
    private Integer monitorSysGenServerLogStatHour;

    /**
     * 总日志数量
     */
    @TableField(value = "monitor_sys_gen_server_log_total_count")
    @ApiModelProperty(value = "总日志数量")
    @Schema(description = "总日志数量")
    private Long monitorSysGenServerLogTotalCount;

    /**
     * DEBUG级别数量
     */
    @TableField(value = "monitor_sys_gen_server_log_debug_count")
    @ApiModelProperty(value = "DEBUG级别数量")
    @Schema(description = "DEBUG级别数量")
    private Long monitorSysGenServerLogDebugCount;

    /**
     * INFO级别数量
     */
    @TableField(value = "monitor_sys_gen_server_log_info_count")
    @ApiModelProperty(value = "INFO级别数量")
    @Schema(description = "INFO级别数量")
    private Long monitorSysGenServerLogInfoCount;

    /**
     * WARN级别数量
     */
    @TableField(value = "monitor_sys_gen_server_log_warn_count")
    @ApiModelProperty(value = "WARN级别数量")
    @Schema(description = "WARN级别数量")
    private Long monitorSysGenServerLogWarnCount;

    /**
     * ERROR级别数量
     */
    @TableField(value = "monitor_sys_gen_server_log_error_count")
    @ApiModelProperty(value = "ERROR级别数量")
    @Schema(description = "ERROR级别数量")
    private Long monitorSysGenServerLogErrorCount;

    /**
     * FATAL级别数量
     */
    @TableField(value = "monitor_sys_gen_server_log_fatal_count")
    @ApiModelProperty(value = "FATAL级别数量")
    @Schema(description = "FATAL级别数量")
    private Long monitorSysGenServerLogFatalCount;

    /**
     * 日志大小(字节)
     */
    @TableField(value = "monitor_sys_gen_server_log_size_bytes")
    @ApiModelProperty(value = "日志大小(字节)")
    @Schema(description = "日志大小(字节)")
    private Long monitorSysGenServerLogSizeBytes;

    /**
     * 创建时间
     */
    @TableField(value = "monitor_sys_gen_server_log_stat_create_time")
    @ApiModelProperty(value = "创建时间")
    @Schema(description = "创建时间")
    private LocalDateTime monitorSysGenServerLogStatCreateTime;

    /**
     * 更新时间
     */
    @TableField(value = "monitor_sys_gen_server_log_stat_update_time")
    @ApiModelProperty(value = "更新时间")
    @Schema(description = "更新时间")
    private LocalDateTime monitorSysGenServerLogStatUpdateTime;

    /**
     * 获取错误日志总数
     */
    public Long getErrorLogCount() {
        long errorCount = (monitorSysGenServerLogErrorCount != null ? monitorSysGenServerLogErrorCount : 0);
        long fatalCount = (monitorSysGenServerLogFatalCount != null ? monitorSysGenServerLogFatalCount : 0);
        return errorCount + fatalCount;
    }

    /**
     * 获取警告日志总数
     */
    public Long getWarnLogCount() {
        return monitorSysGenServerLogWarnCount != null ? monitorSysGenServerLogWarnCount : 0;
    }

    /**
     * 获取正常日志总数
     */
    public Long getNormalLogCount() {
        long debugCount = (monitorSysGenServerLogDebugCount != null ? monitorSysGenServerLogDebugCount : 0);
        long infoCount = (monitorSysGenServerLogInfoCount != null ? monitorSysGenServerLogInfoCount : 0);
        return debugCount + infoCount;
    }

    /**
     * 获取错误率
     */
    public Double getErrorRate() {
        if (monitorSysGenServerLogTotalCount == null || monitorSysGenServerLogTotalCount == 0) {
            return 0.0;
        }
        return (double) getErrorLogCount() / monitorSysGenServerLogTotalCount * 100;
    }

    /**
     * 获取警告率
     */
    public Double getWarnRate() {
        if (monitorSysGenServerLogTotalCount == null || monitorSysGenServerLogTotalCount == 0) {
            return 0.0;
        }
        return (double) getWarnLogCount() / monitorSysGenServerLogTotalCount * 100;
    }

    /**
     * 获取日志大小(MB)
     */
    public Double getLogSizeMB() {
        if (monitorSysGenServerLogSizeBytes == null) {
            return 0.0;
        }
        return monitorSysGenServerLogSizeBytes / (1024.0 * 1024.0);
    }

    /**
     * 获取日志大小(GB)
     */
    public Double getLogSizeGB() {
        if (monitorSysGenServerLogSizeBytes == null) {
            return 0.0;
        }
        return monitorSysGenServerLogSizeBytes / (1024.0 * 1024.0 * 1024.0);
    }

    /**
     * 是否为小时级统计
     */
    public boolean isHourlyStatistics() {
        return monitorSysGenServerLogStatHour != null;
    }

    /**
     * 是否为日级统计
     */
    public boolean isDailyStatistics() {
        return monitorSysGenServerLogStatHour == null;
    }

    /**
     * 增加日志计数
     */
    public void addLogCount(String level, long count, long sizeBytes) {
        if (monitorSysGenServerLogTotalCount == null) {
            monitorSysGenServerLogTotalCount = 0L;
        }
        if (monitorSysGenServerLogSizeBytes == null) {
            monitorSysGenServerLogSizeBytes = 0L;
        }

        monitorSysGenServerLogTotalCount += count;
        monitorSysGenServerLogSizeBytes += sizeBytes;

        switch (level.toUpperCase()) {
            case "DEBUG":
                if (monitorSysGenServerLogDebugCount == null) {
                    monitorSysGenServerLogDebugCount = 0L;
                }
                monitorSysGenServerLogDebugCount += count;
                break;
            case "INFO":
                if (monitorSysGenServerLogInfoCount == null) {
                    monitorSysGenServerLogInfoCount = 0L;
                }
                monitorSysGenServerLogInfoCount += count;
                break;
            case "WARN":
                if (monitorSysGenServerLogWarnCount == null) {
                    monitorSysGenServerLogWarnCount = 0L;
                }
                monitorSysGenServerLogWarnCount += count;
                break;
            case "ERROR":
                if (monitorSysGenServerLogErrorCount == null) {
                    monitorSysGenServerLogErrorCount = 0L;
                }
                monitorSysGenServerLogErrorCount += count;
                break;
            case "FATAL":
                if (monitorSysGenServerLogFatalCount == null) {
                    monitorSysGenServerLogFatalCount = 0L;
                }
                monitorSysGenServerLogFatalCount += count;
                break;
        }
    }

    /**
     * 初始化统计数据
     */
    public void initializeStatistics() {
        if (monitorSysGenServerLogTotalCount == null) {
            monitorSysGenServerLogTotalCount = 0L;
        }
        if (monitorSysGenServerLogDebugCount == null) {
            monitorSysGenServerLogDebugCount = 0L;
        }
        if (monitorSysGenServerLogInfoCount == null) {
            monitorSysGenServerLogInfoCount = 0L;
        }
        if (monitorSysGenServerLogWarnCount == null) {
            monitorSysGenServerLogWarnCount = 0L;
        }
        if (monitorSysGenServerLogErrorCount == null) {
            monitorSysGenServerLogErrorCount = 0L;
        }
        if (monitorSysGenServerLogFatalCount == null) {
            monitorSysGenServerLogFatalCount = 0L;
        }
        if (monitorSysGenServerLogSizeBytes == null) {
            monitorSysGenServerLogSizeBytes = 0L;
        }
    }
}
