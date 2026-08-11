package com.chua.starter.monitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 指标解析规则实体，定义采集任务返回数据的匹配与提取规则。
 *
 * @author CH
 * @since 2026-08-11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ms_monitor_metric_config")
@ApiModel(description = "指标解析规则")
@Schema(description = "指标解析规则")
public class MsMonitorMetricConfig extends SysBase {

    /**
     * 解析规则主键ID，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty("主键ID")
    @Schema(description = "主键ID")
    private Long id;

    /**
     * 指标名称，例如 cpu_usage
     */
    @TableField("metric_name")
    @ApiModelProperty("指标名称")
    @Schema(description = "指标名称")
    private String metricName;

    /**
     * 关联的采集任务ID
     */
    @TableField("task_id")
    @ApiModelProperty("任务ID")
    @Schema(description = "任务ID")
    private Long taskId;

    /**
     * 指标类型，例如 CPU/MEMORY/DISK
     */
    @TableField("metric_type")
    @ApiModelProperty("指标类型")
    @Schema(description = "指标类型")
    private String metricType;

    /**
     * 匹配模式，用于定位指标数据
     */
    @TableField("match_pattern")
    @ApiModelProperty("匹配模式")
    @Schema(description = "匹配模式")
    private String matchPattern;

    /**
     * 提取规则，定义如何从原始数据中提取指标值
     */
    @TableField("extract_rule")
    @ApiModelProperty("提取规则")
    @Schema(description = "提取规则")
    private String extractRule;

    /**
     * 指标单位，例如 %/MB/s
     */
    @TableField("unit")
    @ApiModelProperty("单位")
    @Schema(description = "单位")
    private String unit;

    /**
     * 逻辑删除标记，0=未删除，1=已删除
     */
    @TableField("deleted")
    @TableLogic
    @ApiModelProperty("逻辑删除")
    @Schema(description = "逻辑删除")
    private Integer deleted;
}
