package com.chua.starter.system.pojo.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 菜单设置 VO
 *
 * <p>菜单管理页的列表和编辑回填共用该对象，必须覆盖表单会用到的字段。</p>
 *
 * @since 2024/8/20
 * @author CH
 */
@Data
@Schema(description = "菜单设置VO")
public class SysMenuSettingVO {

    @Schema(description = "菜单ID")
    /**
     * sysmenuid
    */
    private Integer sysMenuId;

    @Schema(description = "菜单名称")
    /**
     * sysmenu名称
    */
    private String sysMenuName;

    @Schema(description = "菜单标题")
    /**
     * sysmenu标题
    */
    private String sysMenuTitle;

    @Schema(description = "菜单路径")
    /**
     * sysmenu路径
    */
    private String sysMenuPath;

    @Schema(description = "国际化Key")
    /**
     * sysmenui18n
    */
    private String sysMenuI18n;

    @Schema(description = "菜单组件路径")
    /**
     * sysmenucomponent
    */
    private String sysMenuComponent;

    @Schema(description = "权限标识")
    /**
     * sysmenuperm
    */
    private String sysMenuPerm;

    @Schema(description = "菜单图标")
    /**
     * sysmenuicon
    */
    private String sysMenuIcon;

    @Schema(description = "右侧额外图标")
    /**
     * sysmenuextraicon
    */
    private String sysMenuExtraIcon;

    @Schema(description = "菜单类型")
    /**
     * sysmenu类型
    */
    private Integer sysMenuType;

    @Schema(description = "排序")
    /**
     * sysmenusort
    */
    private Integer sysMenuSort;

    @Schema(description = "父菜单ID")
    /**
     * sysmenupid
    */
    private Integer sysMenuPid;

    @Schema(description = "菜单树路径")
    /**
     * sysmenutreeid
    */
    private String sysMenuTreeId;

    @Schema(description = "是否隐藏")
    /**
     * sysmenuhidden
    */
    private Integer sysMenuHidden;

    @Schema(description = "是否显示父级菜单")
    /**
     * sysmenushow父级
    */
    private Integer sysMenuShowParent;

    @Schema(description = "是否隐藏标签页")
    /**
     * sysmenuhidden标签
    */
    private Integer sysMenuHiddenTag;

    @Schema(description = "是否固定标签页")
    /**
     * sysmenufixed标签
    */
    private Integer sysMenuFixedTag;

    @Schema(description = "是否开启菜单徽标; 0:否 1:是")
    /**
     * sysmenu是否标签
    */
    private Integer sysMenuIsTag;

    @Schema(description = "徽标类型; danger/success/info/warning/primary")
    /**
     * sysmenu标签类型
    */
    private String sysMenuTagType;

    @Schema(description = "徽标名称(留空则显示圆点徽标)")
    /**
     * sysmenu标签名称
    */
    private String sysMenuTagName;

    @Schema(description = "路由重定向")
    /**
     * sysmenuredirect
    */
    private String sysMenuRedirect;

    @Schema(description = "进场动画")
    /**
     * sysmenuentertransition
    */
    private String sysMenuEnterTransition;

    @Schema(description = "离场动画")
    /**
     * sysmenuleavetransition
    */
    private String sysMenuLeaveTransition;

    @Schema(description = "激活菜单路径")
    /**
     * sysmenu是否激活路径
    */
    private String sysMenuActivePath;

    @Schema(description = "iframe/外链地址")
    /**
     * sysmenuframesrc
    */
    private String sysMenuFrameSrc;

    @Schema(description = "是否缓存")
    /**
     * sysmenukeepalive
    */
    private Integer sysMenuKeepAlive;

    @Schema(description = "所属角色")
    /**
     * sysmenurole
    */
    private String sysMenuRole;

    @Schema(description = "是否首页")
    /**
     * sysmenuhome
    */
    private Integer sysMenuHome;

    @Schema(description = "菜单端类型; 0:PC 1:小程序")
    /**
     * sysmenu引擎
    */
    private Integer sysMenuEngine;

    @Schema(description = "子菜单")
    /**
     * Children
    */
    private List<SysMenuSettingVO> children;
}
