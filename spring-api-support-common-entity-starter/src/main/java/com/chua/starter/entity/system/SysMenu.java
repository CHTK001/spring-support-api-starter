package com.chua.starter.entity.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 系统菜单表
 *
 * @author CH
 * @since 2024/8/7
 */
@Schema(description = "系统菜单表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_menu")
public class SysMenu extends SysBase implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 菜单ID
     */
    @TableId(value = "sys_menu_id", type = IdType.AUTO)
    @Schema(description = "菜单ID")
    @NotNull(message = "菜单ID不能为空")
    private Integer sysMenuId;

    /**
     * 菜单名称
     */
    @TableField(value = "sys_menu_name")
    @Schema(description = "菜单名称")
    @Size(max = 255, message = "菜单名称最大长度需小于255")
    private String sysMenuName;

    /**
     * 是否首页; 0:否
     */
    @TableField(value = "sys_menu_home")
    @Schema(description = "是否首页; 0:否")
    private Integer sysMenuHome;

    /**
     * 是否engine布局主页; 0:否
     */
    @TableField(value = "sys_menu_engine")
    @Schema(description = "是否engine布局主页; 0:否")
    private Integer sysMenuEngine;

    /**
     * 菜单标题
     */
    @Schema(description = "菜单标题")
    @TableField(value = "sys_menu_title")
    private String sysMenuTitle;

    /**
     * 按钮权限标识
     */
    @Schema(description = "按钮权限标识")
    @TableField(value = "sys_menu_perm")
    private String sysMenuPerm;

    /**
     * 菜单激活路径
     */
    @Schema(description = "菜单激活路径")
    @TableField(value = "sys_menu_active_path")
    private String sysMenuActivePath;

    /**
     * iframe链接地址
     */
    @Schema(description = "iframe链接地址")
    @TableField(value = "sys_menu_frame_src")
    private String sysMenuFrameSrc;

    /**
     * 菜单路径
     */
    @TableField(value = "sys_menu_path")
    @Schema(description = "菜单路径")
    private String sysMenuPath;

    /**
     * 国际化key
     */
    @TableField(value = "sys_menu_i18n")
    @Schema(description = "国际化key")
    @Size(max = 255, message = "国际化key最大长度需小于255")
    private String sysMenuI18n;

    /**
     * 父菜单ID
     */
    @TableField(value = "sys_menu_pid")
    @Schema(description = "父菜单ID")
    private Integer sysMenuPid;

    /**
     * 菜单树路径(系统生成)
     */
    @TableField(value = "sys_menu_tree_id")
    @Schema(description = "菜单树路径(系统生成)")
    @Size(max = 255, message = "菜单树路径最大长度需小于255")
    private String sysMenuTreeId;

    /**
     * 菜单组件路径
     */
    @TableField(value = "sys_menu_component")
    @Schema(description = "菜单组件路径")
    @Size(max = 255, message = "菜单组件路径最大长度需小于255")
    private String sysMenuComponent;

    /**
     * 菜单图标
     */
    @TableField(value = "sys_menu_icon")
    @Schema(description = "菜单图标")
    @Size(max = 255, message = "菜单图标最大长度需小于255")
    private String sysMenuIcon;

    /**
     * 右侧额外图标
     */
    @TableField(value = "sys_menu_extra_icon")
    @Schema(description = "右侧额外图标")
    @Size(max = 255, message = "右侧额外图标最大长度需小于255")
    private String sysMenuExtraIcon;

    /**
     * 菜单类型: 0:菜单 1:iframe 2:外链 3:按钮
     */
    @TableField(value = "sys_menu_type")
    @Schema(description = "菜单类型: 0:菜单 1:iframe 2:外链 3:按钮")
    private Integer sysMenuType;

    /**
     * 排序优先级
     */
    @TableField(value = "sys_menu_sort")
    @Schema(description = "排序优先级")
    private Integer sysMenuSort;

    /**
     * 所属角色编码
     */
    @TableField(value = "sys_menu_role")
    @Schema(description = "所属角色编码")
    private String sysMenuRole;

    /**
     * 路由重定向地址
     */
    @TableField(value = "sys_menu_redirect")
    @Schema(description = "路由重定向地址")
    private String sysMenuRedirect;

    /**
     * 进场动画
     */
    @TableField(value = "sys_menu_enter_transition")
    @Schema(description = "进场动画")
    private String sysMenuEnterTransition;

    /**
     * 离场动画
     */
    @TableField(value = "sys_menu_leave_transition")
    @Schema(description = "离场动画")
    private String sysMenuLeaveTransition;

    /**
     * 是否缓存; 0:否
     */
    @TableField(value = "sys_menu_keep_alive")
    @Schema(description = "是否缓存; 0:否")
    private Integer sysMenuKeepAlive;

    /**
     * 是否在标签栏隐藏; 0:否
     */
    @TableField(value = "sys_menu_hidden_tag")
    @Schema(description = "是否在标签栏隐藏; 0:否")
    private Integer sysMenuHiddenTag;

    /**
     * 是否固定标签; 0:否
     */
    @TableField(value = "sys_menu_fixed_tag")
    @Schema(description = "是否固定标签; 0:否")
    private Integer sysMenuFixedTag;

    /**
     * 是否隐藏菜单; 0:显示
     */
    @TableField(value = "sys_menu_hidden")
    @Schema(description = "是否隐藏菜单; 0:显示")
    private Integer sysMenuHidden;

    /**
     * 当只有一个子级菜单时是否隐藏父级菜单; 0:显示
     */
    @TableField(value = "sys_menu_show_parent")
    @Schema(description = "当只有一个子级菜单时是否隐藏父级菜单; 0:显示")
    private Integer sysMenuShowParent;
}
