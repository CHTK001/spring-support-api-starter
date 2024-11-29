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
     * 是否首页
     */
    @TableField(value = "sys_menu_home")
    @ApiModelProperty(value = "是否首页; 0:否")
    @Schema(description = "是否首页; 0:否")
    private Integer sysMenuHome;
    /**
     * 菜单引擎
     */
    @TableField(value = "sys_menu_engine")
    @ApiModelProperty(value = "是否engine布局主页；0: 否")
    @Schema(description = "是否engine布局主页；0: 否")
    private Integer sysMenuEngine;

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
     * 按钮权限
     */
    @Schema(description = "菜单激活")
    @TableField(value = "sys_menu_active_path")
    private String sysMenuActivePath;
    /**
     * 按钮权限
     */
    @Schema(description = "链接地址")
    @TableField(value = "sys_menu_frame_src")
    private String sysMenuFrameSrc;

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
     * 右侧菜单图标
     */
    @TableField(value = "sys_menu_extra_icon")
    @ApiModelProperty(value = "右侧菜单图标")
    @Schema(description = "右侧菜单图标")
    @Size(max = 255, message = "右侧菜单图标最大长度要小于 255")
    private String sysMenuExtraIcon;

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
     * 所属权限
     */
    @TableField(value = "sys_menu_role")
    @ApiModelProperty(value = "所属权限")
    @Schema(description = "所属权限")
    private String sysMenuRole;
    /**
     * 路由重定向
     */
    @TableField(value = "sys_menu_redirect")
    @ApiModelProperty(value = "路由重定向")
    @Schema(description = "路由重定向")
    private String sysMenuRedirect;
    /**
     * 进场动画
     */
    @TableField(value = "sys_menu_enter_transition")
    @ApiModelProperty(value = "进场动画")
    @Schema(description = "进场动画")
    private String sysMenuEnterTransition;
    /**
     * 进场动画
     */
    @TableField(value = "sys_menu_leave_transition")
    @ApiModelProperty(value = "离场动画")
    @Schema(description = "离场动画")
    private String sysMenuLeaveTransition;

    /**
     * 是否缓存；0:否
     */
    @TableField(value = "sys_menu_keep_alive")
    @ApiModelProperty(value = "是否缓存；0:否")
    @Schema(description = "是否缓存；0:否")
    private Integer sysMenuKeepAlive;
    /**
     * 是否在标签栏隐藏；0:否
     */
    @TableField(value = "sys_menu_hidden_tag")
    @ApiModelProperty(value = "是否在标签栏隐藏；0:否")
    @Schema(description = "是否在标签栏隐藏；0:否")
    private Integer sysMenuHiddenTag;
    /**
     * 是否固定标签；0:否
     */
    @TableField(value = "sys_menu_fixed_tag")
    @ApiModelProperty(value = "是否固定标签；0:否")
    @Schema(description = "是否固定标签；0:否")
    private Integer sysMenuFixedTag;
    /**
     * 是否隐藏；0:显示
     */
    @TableField(value = "sys_menu_hidden")
    @ApiModelProperty(value = "是否隐藏；0:显示")
    @Schema(description = "是否隐藏；0:显示")
    private Integer sysMenuHidden;
    /**
     * 是否隐藏；0:显示
     */
    @TableField(value = "sys_menu_show_parent")
    @ApiModelProperty(value = "当一个子级菜单是否隐藏父级菜单；0:显示")
    @Schema(description = "当一个子级菜单是否隐藏父级菜单；0:显示")
    private Integer sysMenuShowParent;
}