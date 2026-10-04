package com.chua.starter.entity.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.common.support.base.validator.group.UpdateGroup;
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
 * @since 2024/8/7
 * @author CH
 */
@Schema(description = "系统菜单表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_menu")
public class SysMenu extends SysBase implements Serializable {
    /**
     * 串行版本UID
    */
    private static final long serialVersionUID = 1L;

    /**
     * 菜单标识
     */
    @TableId(value = "sys_menu_id", type = IdType.AUTO)
    @Schema(description = "菜单ID")
    @NotNull(message = "菜单ID不能为空", groups = UpdateGroup.class)
    /**
     * sysmenuid
    */
    private Integer sysMenuId;

    /**
     * 菜单名称
     */
    @TableField(value = "sys_menu_name")
    @Schema(description = "菜单名称")
    @Size(max = 255, message = "菜单名称最大长度需小于255")
    /**
     * sysmenu名称
    */
    private String sysMenuName;

    /**
     * 是否首页; 0:否
     */
    @TableField(value = "sys_menu_home")
    @Schema(description = "是否首页; 0:否")
    /**
     * sysmenuhome
    */
    private Integer sysMenuHome;

    /**
     * 是否engine布局主页; 0:否
     */
    @TableField(value = "sys_menu_engine")
    @Schema(description = "是否engine布局主页; 0:否")
    /**
     * sysmenu引擎
    */
    private Integer sysMenuEngine;

    /**
     * 菜单标题
     */
    @Schema(description = "菜单标题")
    @TableField(value = "sys_menu_title")
    /**
     * sysmenu标题
    */
    private String sysMenuTitle;

    /**
     * 按钮权限标识
     */
    @Schema(description = "按钮权限标识")
    @TableField(value = "sys_menu_perm")
    /**
     * sysmenuperm
    */
    private String sysMenuPerm;

    /**
     * 菜单激活路径
     */
    @Schema(description = "菜单激活路径")
    @TableField(value = "sys_menu_active_path")
    /**
     * sysmenu是否激活路径
    */
    private String sysMenuActivePath;

    /**
     * iframe链接地址
     */
    @Schema(description = "iframe链接地址")
    @TableField(value = "sys_menu_frame_src")
    /**
     * sysmenuframesrc
    */
    private String sysMenuFrameSrc;

    /**
     * 菜单路径
     */
    @TableField(value = "sys_menu_path")
    @Schema(description = "菜单路径")
    /**
     * sysmenu路径
    */
    private String sysMenuPath;

    /**
     * 国际化键
     */
    @TableField(value = "sys_menu_i18n")
    @Schema(description = "国际化key")
    @Size(max = 255, message = "国际化key最大长度需小于255")
    /**
     * sysmenui18n
    */
    private String sysMenuI18n;

    /**
     * 父菜单标识
     */
    @TableField(value = "sys_menu_pid")
    @Schema(description = "父菜单ID")
    /**
     * sysmenupid
    */
    private Integer sysMenuPid;

    /**
     * 菜单树路径(系统生成)
     */
    @TableField(value = "sys_menu_tree_id")
    @Schema(description = "菜单树路径(系统生成)")
    @Size(max = 255, message = "菜单树路径最大长度需小于255")
    /**
     * sysmenutreeid
    */
    private String sysMenuTreeId;

    /**
     * 菜单组件路径
     */
    @TableField(value = "sys_menu_component")
    @Schema(description = "菜单组件路径")
    @Size(max = 255, message = "菜单组件路径最大长度需小于255")
    /**
     * sysmenucomponent
    */
    private String sysMenuComponent;

    /**
     * 菜单图标
     */
    @TableField(value = "sys_menu_icon")
    @Schema(description = "菜单图标")
    @Size(max = 255, message = "菜单图标最大长度需小于255")
    /**
     * sysmenuicon
    */
    private String sysMenuIcon;

    /**
     * 右侧额外图标
     */
    @TableField(value = "sys_menu_extra_icon")
    @Schema(description = "右侧额外图标")
    @Size(max = 255, message = "右侧额外图标最大长度需小于255")
    /**
     * sysmenuextraicon
    */
    private String sysMenuExtraIcon;

    /**
     * 菜单类型: 0:菜单 1:iframe 2:外链 3:按钮
     */
    @TableField(value = "sys_menu_type")
    @Schema(description = "菜单类型: 0:菜单 1:iframe 2:外链 3:按钮")
    /**
     * sysmenu类型
    */
    private Integer sysMenuType;

    /**
     * 排序优先级
     */
    @TableField(value = "sys_menu_sort")
    @Schema(description = "排序优先级")
    /**
     * sysmenusort
    */
    private Integer sysMenuSort;

    /**
     * 所属角色编码
     */
    @TableField(value = "sys_menu_role")
    @Schema(description = "所属角色编码")
    /**
     * sysmenurole
    */
    private String sysMenuRole;

    /**
     * 路由重定向地址
     */
    @TableField(value = "sys_menu_redirect")
    @Schema(description = "路由重定向地址")
    /**
     * sysmenuredirect
    */
    private String sysMenuRedirect;

    /**
     * 进场动画
     */
    @TableField(value = "sys_menu_enter_transition")
    @Schema(description = "进场动画")
    /**
     * sysmenuentertransition
    */
    private String sysMenuEnterTransition;

    /**
     * 离场动画
     */
    @TableField(value = "sys_menu_leave_transition")
    @Schema(description = "离场动画")
    /**
     * sysmenuleavetransition
    */
    private String sysMenuLeaveTransition;

    /**
     * 是否缓存; 0:否
     */
    @TableField(value = "sys_menu_keep_alive")
    @Schema(description = "是否缓存; 0:否")
    /**
     * sysmenukeepalive
    */
    private Integer sysMenuKeepAlive;

    /**
     * 是否在标签栏隐藏; 0:否
     */
    @TableField(value = "sys_menu_hidden_tag")
    @Schema(description = "是否在标签栏隐藏; 0:否")
    /**
     * sysmenuhidden标签
    */
    private Integer sysMenuHiddenTag;

    /**
     * 是否固定标签; 0:否
     */
    @TableField(value = "sys_menu_fixed_tag")
    @Schema(description = "是否固定标签; 0:否")
    /**
     * sysmenufixed标签
    */
    private Integer sysMenuFixedTag;

    /**
     * 是否开启菜单徽标; 0:否 1:是
     */
    @TableField(value = "sys_menu_is_tag")
    @Schema(description = "是否开启菜单徽标; 0:否 1:是")
    /**
     * sysmenu是否标签
    */
    private Integer sysMenuIsTag;

    /**
     * 徽标类型; danger/成功/信息/警告/primary
     */
    @TableField(value = "sys_menu_tag_type")
    @Schema(description = "徽标类型; danger/success/info/warning/primary")
    /**
     * sysmenu标签类型
    */
    private String sysMenuTagType;

    /**
     * 徽标名称(留空则显示圆点徽标)
     */
    @TableField(value = "sys_menu_tag_name")
    @Schema(description = "徽标名称(留空则显示圆点徽标)")
    /**
     * sysmenu标签名称
    */
    private String sysMenuTagName;

    /**
     * 是否隐藏菜单; 0:显示
     */
    @TableField(value = "sys_menu_hidden")
    @Schema(description = "是否隐藏菜单; 0:显示")
    /**
     * sysmenuhidden
    */
    private Integer sysMenuHidden;

    /**
     * 当只有一个子级菜单时是否隐藏父级菜单; 0:显示
     */
    @TableField(value = "sys_menu_show_parent")
    @Schema(description = "当只有一个子级菜单时是否隐藏父级菜单; 0:显示")
    /**
     * sysmenushow父级
    */
    private Integer sysMenuShowParent;

    /**
     * 是否脱离菜单控制; 0:作为菜单处理 1:只注册路由, 不被 router 当作菜单处理
     */
    @TableField(value = "sys_menu_remind")
    @Schema(description = "是否脱离菜单控制; 0:作为菜单处理 1:只注册路由不作菜单")
    /**
     * sysmenuremind
    */
    private Integer sysMenuRemind;

    /**
     * 菜单位置; standard:标准(侧边栏) top:顶部(顶栏按钮, 强制 remind=1 且不可修改)
     */
    @TableField(value = "sys_menu_position")
    @Schema(description = "菜单位置; standard:标准 top:顶部")
    @Size(max = 20, message = "菜单位置最大长度需小于20")
    /**
     * sysmenuposition
    */
    private String sysMenuPosition;

    /**
     * 动态显示条件类型: ALWAYS/CLASS/SPEL/SWITCH/PROPERTY
     */
    @TableField(value = "sys_menu_show_type")
    @Schema(description = "动态显示条件类型: ALWAYS/CLASS/SPEL/SWITCH/PROPERTY")
    @Size(max = 20, message = "条件类型最大长度需小于20")
    private String sysMenuShowType;

    /**
     * 动态显示条件值，含义随条件类型变化（类全名/SpEL/功能开关键/配置键）
     */
    @TableField(value = "sys_menu_show_value")
    @Schema(description = "动态显示条件值")
    @Size(max = 500, message = "条件值最大长度需小于500")
    private String sysMenuShowValue;

    /**
     * 条件为开关型时是否放行; 0:否 1:是
     */
    @TableField(value = "sys_menu_show_switch")
    @Schema(description = "条件为开关型时是否放行; 0:否 1:是")
    private Integer sysMenuShowSwitch;

    /**
     * 条件为开关型时联动的功能键（sys_firewall_config 中 type='FEATURE' 的 biz_key）
     */
    @TableField(value = "sys_menu_show_key")
    @Schema(description = "条件为开关型时联动的功能键")
    @Size(max = 255, message = "联动功能键最大长度需小于255")
    private String sysMenuShowKey;
}
