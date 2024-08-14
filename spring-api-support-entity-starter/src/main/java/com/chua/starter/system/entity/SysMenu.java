package com.chua.starter.system.entity;

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

/**
 *
 * @since 2024/8/7
 * @author CH    
 */

/**
 * 菜单
 */
@ApiModel(description = "菜单")
@Schema(description = "菜单")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_menu")
public class SysMenu extends SysBase implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(value = "sys_menu_id", type = IdType.AUTO)
    @ApiModelProperty(value = "")
    @Schema(description = "")
    @NotNull(message = "不能为null")
    private Integer sysMenuId;

    /**
     * 菜单名称
     */
    @TableField(value = "sys_menu_name")
    @ApiModelProperty(value = "菜单名称")
    @Schema(description = "菜单名称")
    @Size(max = 255, message = "菜单名称最大长度要小于 255")
    private String sysMenuName;


    /**
     * 菜单标题
     */
    @Schema(description = "标题")
    @TableField(value = "sys_menu_title")
    @ApiModelProperty(value = "标题")
    private String sysMenuTitle;

    /**
     * 按钮权限
     */
    @Schema(description = "权限")
    @TableField(value = "sys_menu_perm")
    private String sysMenuPerm;

    /**
     * 菜单路径
     */
    @TableField(value = "sys_menu_path")
    @ApiModelProperty(value = "菜单路径")
    @Schema(description = "菜单路径")
    private String sysMenuPath;

    /**
     * 国际化
     */
    @TableField(value = "sys_menu_i18n")
    @ApiModelProperty(value = "国际化")
    @Schema(description = "国际化")
    @Size(max = 255, message = "国际化最大长度要小于 255")
    private String sysMenuI18n;

    /**
     * 父菜单
     */
    @TableField(value = "sys_menu_pid")
    @ApiModelProperty(value = "父菜单")
    @Schema(description = "父菜单")
    private Integer sysMenuPid;

    /**
     * 菜单路径
     */
    @TableField(value = "sys_menu_tree_id")
    @ApiModelProperty(value = "菜单路径")
    @Schema(description = "菜单路径(系统生成)")
    @Size(max = 255, message = "菜单路径最大长度要小于 255")
    private String sysMenuTreeId;

    /**
     * 菜单组件
     */
    @TableField(value = "sys_menu_component")
    @ApiModelProperty(value = "菜单组件")
    @Schema(description = "菜单组件")
    @Size(max = 255, message = "菜单组件最大长度要小于 255")
    private String sysMenuComponent;

    /**
     * 菜单图标
     */
    @TableField(value = "sys_menu_icon")
    @ApiModelProperty(value = "菜单图标")
    @Schema(description = "菜单图标")
    @Size(max = 255, message = "菜单图标最大长度要小于 255")
    private String sysMenuIcon;

    /**
     * 菜单 0 按钮 1
     */
    @TableField(value = "sys_menu_type")
    @ApiModelProperty(value = "菜单 0 按钮 1")
    @Schema(description = "菜单 0 按钮 1")
    private Integer sysMenuType;

    /**
     * 优先级
     */
    @TableField(value = "sys_menu_sort")
    @ApiModelProperty(value = "优先级")
    @Schema(description = "优先级")
    private Integer sysMenuSort;

    /**
     * 是否隐藏；0:显示
     */
    @TableField(value = "sys_menu_hidden")
    @ApiModelProperty(value = "是否隐藏；0:显示")
    @Schema(description = "是否隐藏；0:显示")
    private Integer sysMenuHidden;
}