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
    /** SYSmenuID */
    private Integer sysMenuId;

    @Schema(description = "菜单名称")
    /** SYSmenu名称 */
    private String sysMenuName;

    @Schema(description = "菜单标题")
    /** SYSmenu标题 */
    private String sysMenuTitle;

    @Schema(description = "菜单路径")
    /** SYSmenu路径 */
    private String sysMenuPath;

    @Schema(description = "国际化Key")
    /** SYSmenui18n */
    private String sysMenuI18n;

    @Schema(description = "菜单组件路径")
    /** SYSmenucomponent */
    private String sysMenuComponent;

    @Schema(description = "权限标识")
    /** SYSmenuperm */
    private String sysMenuPerm;

    @Schema(description = "菜单图标")
    /** SYSmenuicon */
    private String sysMenuIcon;

    @Schema(description = "右侧额外图标")
    /** SYSmenuextraicon */
    private String sysMenuExtraIcon;

    @Schema(description = "菜单类型")
    /** SYSmenu类型 */
    private Integer sysMenuType;

    @Schema(description = "排序")
    /** SYSmenusort */
    private Integer sysMenuSort;

    @Schema(description = "父菜单ID")
    /** SYSmenuPID */
    private Integer sysMenuPid;

    @Schema(description = "菜单树路径")
    /** SYSmenutreeID */
    private String sysMenuTreeId;

    @Schema(description = "是否隐藏")
    /** SYSmenuhidden */
    private Integer sysMenuHidden;

    @Schema(description = "是否显示父级菜单")
    /** SYSmenushow父级 */
    private Integer sysMenuShowParent;

    @Schema(description = "是否隐藏标签页")
    /** SYSmenuhidden标签 */
    private Integer sysMenuHiddenTag;

    @Schema(description = "是否固定标签页")
    /** SYSmenufixed标签 */
    private Integer sysMenuFixedTag;

    @Schema(description = "是否开启菜单徽标; 0:否 1:是")
    /** SYSmenuIS标签 */
    private Integer sysMenuIsTag;

    @Schema(description = "徽标类型; danger/success/info/warning/primary")
    /** SYSmenu标签类型 */
    private String sysMenuTagType;

    @Schema(description = "徽标名称(留空则显示圆点徽标)")
    /** SYSmenu标签名称 */
    private String sysMenuTagName;

    @Schema(description = "路由重定向")
    /** SYSmenuredirect */
    private String sysMenuRedirect;

    @Schema(description = "进场动画")
    /** SYSmenuentertransition */
    private String sysMenuEnterTransition;

    @Schema(description = "离场动画")
    /** SYSmenuleavetransition */
    private String sysMenuLeaveTransition;

    @Schema(description = "激活菜单路径")
    /** SYSmenu是否激活路径 */
    private String sysMenuActivePath;

    @Schema(description = "iframe/外链地址")
    /** SYSmenuframeSRC */
    private String sysMenuFrameSrc;

    @Schema(description = "是否缓存")
    /** SYSmenukeepalive */
    private Integer sysMenuKeepAlive;

    @Schema(description = "所属角色")
    /** SYSmenurole */
    private String sysMenuRole;

    @Schema(description = "是否首页")
    /** SYSmenuhome */
    private Integer sysMenuHome;

    @Schema(description = "菜单端类型; 0:PC 1:小程序")
    /** SYSmenu引擎 */
    private Integer sysMenuEngine;

    @Schema(description = "子菜单")
    /** Children */
    private List<SysMenuSettingVO> children;
}
