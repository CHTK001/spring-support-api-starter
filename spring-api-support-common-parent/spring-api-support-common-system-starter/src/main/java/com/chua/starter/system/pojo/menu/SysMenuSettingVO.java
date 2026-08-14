package com.chua.starter.system.pojo.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 菜单设置 VO
 *
 * <p>菜单管理页的列表和编辑回填共用该对象，必须覆盖表单会用到的字段。</p>
 *
 * @author CH
 * @since 2024/8/20
 */
@Data
@Schema(description = "菜单设置VO")
public class SysMenuSettingVO {

    @Schema(description = "菜单ID")
    private Integer sysMenuId;

    @Schema(description = "菜单名称")
    private String sysMenuName;

    @Schema(description = "菜单标题")
    private String sysMenuTitle;

    @Schema(description = "菜单路径")
    private String sysMenuPath;

    @Schema(description = "国际化Key")
    private String sysMenuI18n;

    @Schema(description = "菜单组件路径")
    private String sysMenuComponent;

    @Schema(description = "权限标识")
    private String sysMenuPerm;

    @Schema(description = "菜单图标")
    private String sysMenuIcon;

    @Schema(description = "右侧额外图标")
    private String sysMenuExtraIcon;

    @Schema(description = "菜单类型")
    private Integer sysMenuType;

    @Schema(description = "排序")
    private Integer sysMenuSort;

    @Schema(description = "父菜单ID")
    private Integer sysMenuPid;

    @Schema(description = "菜单树路径")
    private String sysMenuTreeId;

    @Schema(description = "是否隐藏")
    private Integer sysMenuHidden;

    @Schema(description = "是否显示父级菜单")
    private Integer sysMenuShowParent;

    @Schema(description = "是否隐藏标签页")
    private Integer sysMenuHiddenTag;

    @Schema(description = "是否固定标签页")
    private Integer sysMenuFixedTag;

    @Schema(description = "是否开启菜单徽标; 0:否 1:是")
    private Integer sysMenuIsTag;

    @Schema(description = "徽标类型; danger/success/info/warning/primary")
    private String sysMenuTagType;

    @Schema(description = "徽标名称(留空则显示圆点徽标)")
    private String sysMenuTagName;

    @Schema(description = "路由重定向")
    private String sysMenuRedirect;

    @Schema(description = "进场动画")
    private String sysMenuEnterTransition;

    @Schema(description = "离场动画")
    private String sysMenuLeaveTransition;

    @Schema(description = "激活菜单路径")
    private String sysMenuActivePath;

    @Schema(description = "iframe/外链地址")
    private String sysMenuFrameSrc;

    @Schema(description = "是否缓存")
    private Integer sysMenuKeepAlive;

    @Schema(description = "所属角色")
    private String sysMenuRole;

    @Schema(description = "是否首页")
    private Integer sysMenuHome;

    @Schema(description = "菜单端类型; 0:PC 1:小程序")
    private Integer sysMenuEngine;

    @Schema(description = "子菜单")
    private List<SysMenuSettingVO> children;
}
