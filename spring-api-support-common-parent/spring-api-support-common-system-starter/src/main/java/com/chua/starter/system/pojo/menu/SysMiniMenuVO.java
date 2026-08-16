package com.chua.starter.system.pojo.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 小程序菜单卡片 VO
 *
 * @author CH
 * @since 2026/08/03
 */
@Data
@Schema(description = "小程序菜单卡片VO")
public class SysMiniMenuVO {

    /**
     * 小程序菜单ID
     */
    @Schema(description = "小程序菜单ID")
    private Integer sysMiniMenuId;

    /**
     * 卡片标题
     */
    @Schema(description = "卡片标题")
    private String sysMiniMenuTitle;

    /**
     * 卡片副标题
     */
    @Schema(description = "卡片副标题")
    private String sysMiniMenuSubtitle;

    /**
     * 卡片分组
     */
    @Schema(description = "卡片分组")
    private String sysMiniMenuCategory;

    /**
     * 页面路径
     */
    @Schema(description = "页面路径")
    private String sysMiniMenuPath;

    /**
     * 跳转模式
     */
    @Schema(description = "跳转模式")
    private Integer sysMiniMenuJumpMode;

    /**
     * 图标
     */
    @Schema(description = "图标")
    private String sysMiniMenuIcon;

    /**
     * 封面
     */
    @Schema(description = "封面")
    private String sysMiniMenuCover;

    /**
     * 角标
     */
    @Schema(description = "角标")
    private String sysMiniMenuBadge;

    /**
     * 角标样式
     */
    @Schema(description = "角标样式")
    private String sysMiniMenuBadgeType;

    /**
     * 权限标识
     */
    @Schema(description = "权限标识")
    private String sysMiniMenuPerm;

    /**
     * 角色编码
     */
    @Schema(description = "角色编码")
    private String sysMiniMenuRole;

    /**
     * 排序
     */
    @Schema(description = "排序")
    private Integer sysMiniMenuSort;

    /**
     * 是否缓存
     */
    @Schema(description = "是否缓存")
    private Integer sysMiniMenuKeepAlive;

    /**
     * 是否隐藏
     */
    @Schema(description = "是否隐藏")
    private Integer sysMiniMenuHidden;

    /**
     * 说明
     */
    @Schema(description = "说明")
    private String sysMiniMenuDescription;
}
