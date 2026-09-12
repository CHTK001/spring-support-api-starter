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
 * 服务器组件布局实体，保存单个监控组件（如 CPU、内存、磁盘卡片）在大屏栅格中的
 * 位置（x/y）、尺寸（w/h）、层级与交互能力（是否可移动/可缩放）以及图表配置 JSON。
 *
 * @since 2026-07-28
 * @author CH
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("monitor_sys_gen_server_component_layout")
@ApiModel(description = "服务器组件布局配置")
@Schema(description = "服务器组件布局配置")
public class MonitorSysGenServerComponentLayout extends SysBase {

    /**
      * 组件布局主键标识，自增
     */
    @TableId(value = "monitor_sys_gen_server_component_layout_id", type = IdType.AUTO)
    @ApiModelProperty("组件布局ID")
    @Schema(description = "组件布局ID")
    /** 监控sysgen服务器componentlayoutid */
    private Integer monitorSysGenServerComponentLayoutId;

    /**
      * 关联的服务器标识，标识该组件布局属于哪台服务器
     */
    @TableField("monitor_sys_gen_server_id")
    @ApiModelProperty("关联服务器ID")
    @Schema(description = "关联服务器ID")
    /** 监控sysgen服务器标识 */
    private Integer monitorSysGenServerId;

    /**
      * 关联的组件标识，用于区分 CPU、内存、磁盘等不同监控组件
     */
    @TableField("monitor_sys_gen_server_component_id")
    @ApiModelProperty("关联组件ID")
    @Schema(description = "关联组件ID")
    /** 监控sysgen服务器组件id */
    private Integer monitorSysGenServerComponentId;

    /**
     * 组件在栅格布局中的 X 坐标（列位置），从 0 开始
     */
    @TableField("monitor_sys_gen_server_component_layout_x")
    @ApiModelProperty("X 坐标")
    @Schema(description = "X 坐标")
    /** 监控sysgen服务器componentlayoutx坐标 */
    private Integer monitorSysGenServerComponentLayoutX;

    /**
     * 组件在栅格布局中的 Y 坐标（行位置），从 0 开始
     */
    @TableField("monitor_sys_gen_server_component_layout_y")
    @ApiModelProperty("Y 坐标")
    @Schema(description = "Y 坐标")
    /** 监控sysgen服务器componentlayouty坐标 */
    private Integer monitorSysGenServerComponentLayoutY;

    /**
     * 组件宽度（占用栅格列数）
     */
    @TableField("monitor_sys_gen_server_component_layout_w")
    @ApiModelProperty("宽度")
    @Schema(description = "宽度")
    /** 监控sysgen服务器componentlayoutw */
    private Integer monitorSysGenServerComponentLayoutW;

    /**
     * 组件高度（占用栅格行数）
     */
    @TableField("monitor_sys_gen_server_component_layout_h")
    @ApiModelProperty("高度")
    @Schema(description = "高度")
    /** 监控sysgen服务器componentlayouth */
    private Integer monitorSysGenServerComponentLayoutH;

    /**
     * 组件层级，数值越大越靠上，用于组件重叠时的显示顺序
     */
    @TableField("monitor_sys_gen_server_component_layout_z_index")
    @ApiModelProperty("层级")
    @Schema(description = "层级")
    /** 监控sysgen服务器componentlayoutzindex */
    private Integer monitorSysGenServerComponentLayoutZIndex;

    /**
     * 是否可移动，1-可移动，0-固定位置
     */
    @TableField("monitor_sys_gen_server_component_layout_movable")
    @ApiModelProperty("是否可移动（0/1）")
    @Schema(description = "是否可移动（0/1）")
    /** 监控sysgen服务器componentlayoutmovable */
    private Integer monitorSysGenServerComponentLayoutMovable;

    /**
     * 是否可调整大小，1-可调整，0-固定尺寸
     */
    @TableField("monitor_sys_gen_server_component_layout_resizable")
    @ApiModelProperty("是否可调整大小（0/1）")
    @Schema(description = "是否可调整大小（0/1）")
    /** 监控sysgen服务器componentlayoutresizable */
    private Integer monitorSysGenServerComponentLayoutResizable;

    /**
     * 布局状态，1-启用（在大屏中展示），0-禁用（隐藏）
     */
    @TableField("monitor_sys_gen_server_component_layout_status")
    @ApiModelProperty("布局状态（0禁用/1启用）")
    @Schema(description = "布局状态（0禁用/1启用）")
    /** 监控sysgen服务器componentlayout状态 */
    private Integer monitorSysGenServerComponentLayoutStatus;

    /**
     * 排序顺序，数值越小越靠前，用于组件列表展示排序
     */
    @TableField("monitor_sys_gen_server_component_layout_sort_order")
    @ApiModelProperty("排序顺序")
    @Schema(description = "排序顺序")
    /** 监控sysgen服务器componentlayoutsort排序 */
    private Integer monitorSysGenServerComponentLayoutSortOrder;

    /**
     * 组件图表配置 JSON，保存图表类型、颜色、阈值等个性化配置，可为空
     */
    @TableField("monitor_sys_gen_server_component_layout_chart_config")
    @ApiModelProperty("组件图表配置 JSON")
    @Schema(description = "组件图表配置 JSON")
    /** 监控sysgen服务器componentlayoutchart配置 */
    private String monitorSysGenServerComponentLayoutChartConfig;
}
