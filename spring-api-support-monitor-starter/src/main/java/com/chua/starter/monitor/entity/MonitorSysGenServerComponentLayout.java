package com.chua.starter.monitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.common.support.base.validator.group.UpdateGroup;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 服务器组件布局配置实体类
 *
 * @author CH
 * @since 2025/01/06
 */
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "服务器组件布局配置")
@Schema(description = "服务器组件布局配置")
@Data
@TableName(value = "monitor_sys_gen_server_component_layout")
public class MonitorSysGenServerComponentLayout extends SysBase implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "monitor_sys_gen_server_component_layout_id", type = IdType.AUTO)
    @ApiModelProperty(value = "组件布局配置ID")
    @Schema(description = "组件布局配置ID")
    @NotNull(message = "布局ID不能为空", groups = {UpdateGroup.class})
    private Long monitorSysGenServerComponentLayoutId;

    /**
     * 关联服务器ID
     */
    @TableField(value = "monitor_sys_gen_server_id")
    @ApiModelProperty(value = "关联服务器ID")
    @Schema(description = "关联服务器ID")
    @NotNull(message = "服务器ID不能为空")
    private Integer monitorSysGenServerId;

    /**
     * 关联组件ID
     */
    @TableField(value = "monitor_sys_gen_server_component_id")
    @ApiModelProperty(value = "关联组件ID")
    @Schema(description = "关联组件ID")
    @NotNull(message = "组件ID不能为空")
    private Long monitorSysGenServerComponentId;

    /**
     * 布局X坐标
     */
    @TableField(value = "monitor_sys_gen_server_component_layout_x")
    @ApiModelProperty(value = "布局X坐标")
    @Schema(description = "布局X坐标")
    private Integer monitorSysGenServerComponentLayoutX;

    /**
     * 布局Y坐标
     */
    @TableField(value = "monitor_sys_gen_server_component_layout_y")
    @ApiModelProperty(value = "布局Y坐标")
    @Schema(description = "布局Y坐标")
    private Integer monitorSysGenServerComponentLayoutY;

    /**
     * 布局宽度
     */
    @TableField(value = "monitor_sys_gen_server_component_layout_w")
    @ApiModelProperty(value = "布局宽度")
    @Schema(description = "布局宽度")
    private Integer monitorSysGenServerComponentLayoutW;

    /**
     * 布局高度
     */
    @TableField(value = "monitor_sys_gen_server_component_layout_h")
    @ApiModelProperty(value = "布局高度")
    @Schema(description = "布局高度")
    private Integer monitorSysGenServerComponentLayoutH;

    /**
     * 布局层级
     */
    @TableField(value = "monitor_sys_gen_server_component_layout_z_index")
    @ApiModelProperty(value = "布局层级")
    @Schema(description = "布局层级")
    private Integer monitorSysGenServerComponentLayoutZIndex;

    /**
     * 是否可移动
     */
    @TableField(value = "monitor_sys_gen_server_component_layout_movable")
    @ApiModelProperty(value = "是否可移动")
    @Schema(description = "是否可移动")
    private Boolean monitorSysGenServerComponentLayoutMovable;

    /**
     * 是否可调整大小
     */
    @TableField(value = "monitor_sys_gen_server_component_layout_resizable")
    @ApiModelProperty(value = "是否可调整大小")
    @Schema(description = "是否可调整大小")
    private Boolean monitorSysGenServerComponentLayoutResizable;

    /**
     * 布局状态 (1:启用 0:禁用)
     */
    @TableField(value = "monitor_sys_gen_server_component_layout_status")
    @ApiModelProperty(value = "布局状态")
    @Schema(description = "布局状态")
    private Integer monitorSysGenServerComponentLayoutStatus;

    /**
     * 排序顺序
     */
    @TableField(value = "monitor_sys_gen_server_component_layout_sort_order")
    @ApiModelProperty(value = "排序顺序")
    @Schema(description = "排序顺序")
    private Integer monitorSysGenServerComponentLayoutSortOrder;
}
