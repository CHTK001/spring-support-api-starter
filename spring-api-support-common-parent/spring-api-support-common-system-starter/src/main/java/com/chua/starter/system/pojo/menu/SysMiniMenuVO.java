package com.chua.starter.system.pojo.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 小程序菜单卡片 VO
 *
 * @since 2026/08/03
 * @author CH
 */
@Data
@Schema(description = "小程序菜单卡片VO")
public class SysMiniMenuVO {

    /**
     * 小程序菜单标识
     */
    @Schema(description = "小程序菜单ID")
    /** sysminimenuid */
    private Integer sysMiniMenuId;

    /**
    * 卡片标题
     */
    @Schema(description = "卡片标题")
    /** sysminimenu标题 */
    private String sysMiniMenuTitle;

    /**
    * 卡片副标题
     */
    @Schema(description = "卡片副标题")
    /** sysminimenusubtitle */
    private String sysMiniMenuSubtitle;

    /**
    * 卡片分组
     */
    @Schema(description = "卡片分组")
    /** sysminimenu分类 */
    private String sysMiniMenuCategory;

    /**
    * 页面路径
     */
    @Schema(description = "页面路径")
    /** sysminimenu路径 */
    private String sysMiniMenuPath;

    /**
    * 跳转模式
     */
    @Schema(description = "跳转模式")
    /** sysminimenujump模式 */
    private Integer sysMiniMenuJumpMode;

    /**
    * 图标
     */
    @Schema(description = "图标")
    /** sysminimenuicon */
    private String sysMiniMenuIcon;

    /**
    * 封面
     */
    @Schema(description = "封面")
    /** sysminimenucover */
    private String sysMiniMenuCover;

    /**
    * 角标
     */
    @Schema(description = "角标")
    /** sysminimenubadge */
    private String sysMiniMenuBadge;

    /**
    * 角标样式
     */
    @Schema(description = "角标样式")
    /** sysminimenubadge类型 */
    private String sysMiniMenuBadgeType;

    /**
    * 权限标识
     */
    @Schema(description = "权限标识")
    /** sysminimenuperm */
    private String sysMiniMenuPerm;

    /**
    * 角色编码
     */
    @Schema(description = "角色编码")
    /** sysminimenurole */
    private String sysMiniMenuRole;

    /**
    * 排序
     */
    @Schema(description = "排序")
    /** sysminimenusort */
    private Integer sysMiniMenuSort;

    /**
    * 是否缓存
     */
    @Schema(description = "是否缓存")
    /** sysminimenukeepalive */
    private Integer sysMiniMenuKeepAlive;

    /**
    * 是否隐藏
     */
    @Schema(description = "是否隐藏")
    /** sysminimenuhidden */
    private Integer sysMiniMenuHidden;

    /**
    * 说明
     */
    @Schema(description = "说明")
    /** sysminimenu描述 */
    private String sysMiniMenuDescription;
}
