package com.chua.starter.monitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author CH
 * @since 2025/6/6 16:56
 */

/**
 * Prometheus配置
 */
@ApiModel(description = "Prometheus配置")
@Schema(description = "Prometheus配置")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "monitor_sys_gen_prometheus_config")
public class MonitorSysGenPrometheusConfig extends SysBase {
    @TableId(value = "monitor_sys_gen_promethus_config_id", type = IdType.AUTO)
    @ApiModelProperty(value = "")
    @Schema(description = "")
    private Integer monitorSysGenPromethusConfigId;

    /**
     * 名称
     */
    @TableField(value = "monitor_sys_gen_promethus_config_name")
    @ApiModelProperty(value = "名称")
    @Schema(description = "名称")
    private String monitorSysGenPromethusConfigName;

    /**
     * 位置
     */
    @TableField(value = "monitor_sys_gen_promethus_config_postion")
    @ApiModelProperty(value = "位置")
    @Schema(description = "位置")
    private String monitorSysGenPromethusConfigPostion;

    /**
     * 表达式
     */
    @TableField(value = "monitor_sys_gen_promethus_config_ql")
    @ApiModelProperty(value = "表达式")
    @Schema(description = "表达式")
    private String monitorSysGenPromethusConfigQl;

    /**
     * 图表类型
     */
    @TableField(value = "monitor_sys_gen_promethus_config_chart_type")
    @ApiModelProperty(value = "图表类型")
    @Schema(description = "图表类型")
    private String monitorSysGenPromethusConfigChartType;

    /**
     * 是否开启
     */
    @TableField(value = "monitor_sys_gen_promethus_config_enable")
    @ApiModelProperty(value = "是否开启")
    @Schema(description = "是否开启")
    private Boolean monitorSysGenPromethusConfigEnable;

    /**
     * 所属genId
     */
    @TableField(value = "monitor_sys_gen_id")
    @ApiModelProperty(value = "所属genId")
    @Schema(description = "所属genId")
    private Integer monitorSysGenId;

    /**
     * 是否共享
     */
    @TableField(value = "monitor_sys_gen_promethus_config_share")
    @ApiModelProperty(value = "是否共享")
    @Schema(description = "是否共享")
    private Boolean monitorSysGenPromethusConfigShare;

    /**
     * 共享来源
     */
    @TableField(value = "monitor_sys_gen_promethus_config_origin")
    @ApiModelProperty(value = "共享来源")
    @Schema(description = "共享来源")
    private String monitorSysGenPromethusConfigOrigin;
}