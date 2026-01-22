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
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 服务器布局配置实体类
 *
 * @author CH
 * @since 2025/01/03
 */
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "服务器布局配置")
@Schema(description = "服务器布局配置")
@Data
@TableName(value = "monitor_sys_gen_server_layout")
public class MonitorSysGenServerLayout extends SysBase implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "monitor_sys_gen_server_layout_id", type = IdType.AUTO)
    @ApiModelProperty(value = "布局配置ID")
    @Schema(description = "布局配置ID")
    @NotNull(message = "布局ID不能为空", groups = {UpdateGroup.class})
    private Integer monitorSysGenServerLayoutId;

    /**
     * 关联服务器ID
     */
    @TableField(value = "monitor_sys_gen_server_id")
    @ApiModelProperty(value = "关联服务器ID")
    @Schema(description = "关联服务器ID")
    @NotNull(message = "服务器ID不能为空")
    private Integer monitorSysGenServerId;

    /**
     * 布局名称
     */
    @TableField(value = "monitor_sys_gen_server_layout_name")
    @ApiModelProperty(value = "布局名称")
    @Schema(description = "布局名称")
    @NotNull(message = "布局名称不能为空")
    @Size(max = 100, message = "布局名称最大长度要小于 100")
    private String monitorSysGenServerLayoutName;

    /**
     * 布局配置 (JSON格式)
     */
    @TableField(value = "monitor_sys_gen_server_layout_config")
    @ApiModelProperty(value = "布局配置")
    @Schema(description = "布局配置")
    @Size(max = 10000, message = "布局配置最大长度要小于 10000")
    private String monitorSysGenServerLayoutConfig;

    /**
     * 布局类型 (default: 默认布局, custom: 自定义布局)
     */
    @TableField(value = "monitor_sys_gen_server_layout_type")
    @ApiModelProperty(value = "布局类型")
    @Schema(description = "布局类型")
    @Size(max = 20, message = "布局类型最大长度要小于 20")
    private String monitorSysGenServerLayoutType;

    /**
     * 是否为默认布局 0:否 1:是
     */
    @TableField(value = "monitor_sys_gen_server_layout_default")
    @ApiModelProperty(value = "是否为默认布局")
    @Schema(description = "是否为默认布局")
    private Integer monitorSysGenServerLayoutDefault;

    /**
     * 布局状态 0:停用 1:启用
     */
    @TableField(value = "monitor_sys_gen_server_layout_status")
    @ApiModelProperty(value = "布局状态")
    @Schema(description = "布局状态")
    private Integer monitorSysGenServerLayoutStatus;

    /**
     * 布局描述
     */
    @TableField(value = "monitor_sys_gen_server_layout_description")
    @ApiModelProperty(value = "布局描述")
    @Schema(description = "布局描述")
    @Size(max = 500, message = "布局描述最大长度要小于 500")
    private String monitorSysGenServerLayoutDescription;

    /**
     * 布局版本
     */
    @TableField(value = "monitor_sys_gen_server_layout_version")
    @ApiModelProperty(value = "布局版本")
    @Schema(description = "布局版本")
    private Integer monitorSysGenServerLayoutVersion;

    /**
     * 布局排序
     */
    @TableField(value = "monitor_sys_gen_server_layout_sort")
    @ApiModelProperty(value = "布局排序")
    @Schema(description = "布局排序")
    private Integer monitorSysGenServerLayoutSort;
}
