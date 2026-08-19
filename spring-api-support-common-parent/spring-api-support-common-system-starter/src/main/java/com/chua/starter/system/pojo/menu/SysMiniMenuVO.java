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
     * 小程序菜单ID
     */
    @Schema(description = "小程序菜单ID")
    /** SYSminimenuID */
    private Integer sysMiniMenuId;

    /**
     * 卡片标题
     */
    @Schema(description = "卡片标题")
    /** SYSminimenu标题 */
    private String sysMiniMenuTitle;

    /**
     * 卡片副标题
     */
    @Schema(description = "卡片副标题")
    /** SYSminimenusubtitle */
    private String sysMiniMenuSubtitle;

    /**
     * 卡片分组
     */
    @Schema(description = "卡片分组")
    /** SYSminimenu分类 */
    private String sysMiniMenuCategory;

    /**
     * 页面路径
     */
    @Schema(description = "页面路径")
    /** SYSminimenu路径 */
    private String sysMiniMenuPath;

    /**
     * 跳转模式
     */
    @Schema(description = "跳转模式")
    /** SYSminimenujump模式 */
    private Integer sysMiniMenuJumpMode;

    /**
     * 图标
     */
    @Schema(description = "图标")
    /** SYSminimenuicon */
    private String sysMiniMenuIcon;

    /**
     * 封面
     */
    @Schema(description = "封面")
    /** SYSminimenucover */
    private String sysMiniMenuCover;

    /**
     * 角标
     */
    @Schema(description = "角标")
    /** SYSminimenubadge */
    private String sysMiniMenuBadge;

    /**
     * 角标样式
     */
    @Schema(description = "角标样式")
    /** SYSminimenubadge类型 */
    private String sysMiniMenuBadgeType;

    /**
     * 权限标识
     */
    @Schema(description = "权限标识")
    /** SYSminimenuperm */
    private String sysMiniMenuPerm;

    /**
     * 角色编码
     */
    @Schema(description = "角色编码")
    /** SYSminimenurole */
    private String sysMiniMenuRole;

    /**
     * 排序
     */
    @Schema(description = "排序")
    /** SYSminimenusort */
    private Integer sysMiniMenuSort;

    /**
     * 是否缓存
     */
    @Schema(description = "是否缓存")
    /** SYSminimenukeepalive */
    private Integer sysMiniMenuKeepAlive;

    /**
     * 是否隐藏
     */
    @Schema(description = "是否隐藏")
    /** SYSminimenuhidden */
    private Integer sysMiniMenuHidden;

    /**
     * 说明
     */
    @Schema(description = "说明")
    /** SYSminimenu描述 */
    private String sysMiniMenuDescription;
}
