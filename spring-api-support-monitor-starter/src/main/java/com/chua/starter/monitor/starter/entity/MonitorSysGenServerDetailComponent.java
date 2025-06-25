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
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 服务器详情页组件配置实体类
 *
 * @author CH
 * @since 2024/12/30
 */
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "服务器详情页组件配置")
@Schema(description = "服务器详情页组件配置")
@Data
@TableName(value = "monitor_sys_gen_server_detail_component")
public class MonitorSysGenServerDetailComponent extends SysBase implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "monitor_sys_gen_server_detail_component_id", type = IdType.AUTO)
    @ApiModelProperty(value = "组件配置ID")
    @Schema(description = "组件配置ID")
    @NotNull(message = "不能为null")
    private Long monitorSysGenServerDetailComponentId;

    /**
     * 关联服务器ID
     */
    @TableField(value = "monitor_sys_gen_server_id")
    @ApiModelProperty(value = "关联服务器ID")
    @Schema(description = "关联服务器ID")
    @NotNull(message = "服务器ID不能为null")
    private Integer monitorSysGenServerId;

    /**
     * 组件名称
     */
    @TableField(value = "monitor_sys_gen_server_detail_component_name")
    @ApiModelProperty(value = "组件名称")
    @Schema(description = "组件名称")
    @Size(max = 255, message = "组件名称最大长度要小于 255")
    private String monitorSysGenServerDetailComponentName;

    /**
     * 组件标题
     */
    @TableField(value = "monitor_sys_gen_server_detail_component_title")
    @ApiModelProperty(value = "组件标题")
    @Schema(description = "组件标题")
    @Size(max = 255, message = "组件标题最大长度要小于 255")
    private String monitorSysGenServerDetailComponentTitle;

    /**
     * 组件类型 (card:卡片, gauge:仪表盘, line:折线图, bar:柱状图, pie:饼图, table:表格)
     */
    @TableField(value = "monitor_sys_gen_server_detail_component_type")
    @ApiModelProperty(value = "组件类型")
    @Schema(description = "组件类型")
    @Size(max = 50, message = "组件类型最大长度要小于 50")
    private String monitorSysGenServerDetailComponentType;

    /**
     * 表达式类型 (PROMETHEUS:prometheus表达式, SQL:sql表达式)
     */
    @TableField(value = "monitor_sys_gen_server_detail_component_expression_type")
    @ApiModelProperty(value = "表达式类型")
    @Schema(description = "表达式类型")
    @Size(max = 20, message = "表达式类型最大长度要小于 20")
    private String monitorSysGenServerDetailComponentExpressionType;

    /**
     * 查询表达式 (Prometheus PromQL 或 SQL)
     */
    @TableField(value = "monitor_sys_gen_server_detail_component_expression")
    @ApiModelProperty(value = "查询表达式")
    @Schema(description = "查询表达式")
    @Size(max = 2000, message = "查询表达式最大长度要小于 2000")
    private String monitorSysGenServerDetailComponentExpression;

    /**
     * 组件位置信息 (JSON格式存储x,y,w,h等布局信息)
     */
    @TableField(value = "monitor_sys_gen_server_detail_component_position")
    @ApiModelProperty(value = "组件位置信息")
    @Schema(description = "组件位置信息")
    @Size(max = 1000, message = "组件位置信息最大长度要小于 1000")
    private String monitorSysGenServerDetailComponentPosition;

    /**
     * 图表配置 (JSON格式存储图表样式、颜色等配置)
     */
    @TableField(value = "monitor_sys_gen_server_detail_component_chart_config")
    @ApiModelProperty(value = "图表配置")
    @Schema(description = "图表配置")
    @Size(max = 2000, message = "图表配置最大长度要小于 2000")
    private String monitorSysGenServerDetailComponentChartConfig;

    /**
     * 刷新间隔(秒)
     */
    @TableField(value = "monitor_sys_gen_server_detail_component_refresh_interval")
    @ApiModelProperty(value = "刷新间隔(秒)")
    @Schema(description = "刷新间隔(秒)")
    private Integer monitorSysGenServerDetailComponentRefreshInterval;

    /**
     * 是否启用 0:否 1:是
     */
    @TableField(value = "monitor_sys_gen_server_detail_component_enabled")
    @ApiModelProperty(value = "是否启用")
    @Schema(description = "是否启用")
    private Integer monitorSysGenServerDetailComponentEnabled;

    /**
     * 排序序号
     */
    @TableField(value = "monitor_sys_gen_server_detail_component_sort_order")
    @ApiModelProperty(value = "排序序号")
    @Schema(description = "排序序号")
    private Integer monitorSysGenServerDetailComponentSortOrder;

    /**
     * 组件描述
     */
    @TableField(value = "monitor_sys_gen_server_detail_component_desc")
    @ApiModelProperty(value = "组件描述")
    @Schema(description = "组件描述")
    @Size(max = 500, message = "组件描述最大长度要小于 500")
    private String monitorSysGenServerDetailComponentDesc;

    /**
     * 创建时间
     */
    @TableField(value = "monitor_sys_gen_server_detail_component_create_time")
    @ApiModelProperty(value = "创建时间")
    @Schema(description = "创建时间")
    private LocalDateTime monitorSysGenServerDetailComponentCreateTime;

    /**
     * 更新时间
     */
    @TableField(value = "monitor_sys_gen_server_detail_component_update_time")
    @ApiModelProperty(value = "更新时间")
    @Schema(description = "更新时间")
    private LocalDateTime monitorSysGenServerDetailComponentUpdateTime;
}
