package com.chua.starter.system.pojo.menu;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 小程序菜单卡片 VO
 *
 * @param sysMiniMenuId          小程序菜单标识
 * @param sysMiniMenuTitle       卡片标题
 * @param sysMiniMenuSubtitle    卡片副标题
 * @param sysMiniMenuCategory    卡片分组
 * @param sysMiniMenuPath        页面路径
 * @param sysMiniMenuJumpMode    跳转模式
 * @param sysMiniMenuIcon        图标
 * @param sysMiniMenuCover       封面
 * @param sysMiniMenuBadge       角标
 * @param sysMiniMenuBadgeType   角标样式
 * @param sysMiniMenuPerm        权限标识
 * @param sysMiniMenuRole        角色编码
 * @param sysMiniMenuSort        排序
 * @param sysMiniMenuKeepAlive   是否缓存
 * @param sysMiniMenuHidden      是否隐藏
 * @param sysMiniMenuDescription 说明
 * @since 2026/08/03
 * @author CH
 */
@Schema(description = "小程序菜单卡片VO")
public record SysMiniMenuVO(
        @Schema(description = "小程序菜单ID") Integer sysMiniMenuId,
        @Schema(description = "卡片标题") String sysMiniMenuTitle,
        @Schema(description = "卡片副标题") String sysMiniMenuSubtitle,
        @Schema(description = "卡片分组") String sysMiniMenuCategory,
        @Schema(description = "页面路径") String sysMiniMenuPath,
        @Schema(description = "跳转模式") Integer sysMiniMenuJumpMode,
        @Schema(description = "图标") String sysMiniMenuIcon,
        @Schema(description = "封面") String sysMiniMenuCover,
        @Schema(description = "角标") String sysMiniMenuBadge,
        @Schema(description = "角标样式") String sysMiniMenuBadgeType,
        @Schema(description = "权限标识") String sysMiniMenuPerm,
        @Schema(description = "角色编码") String sysMiniMenuRole,
        @Schema(description = "排序") Integer sysMiniMenuSort,
        @Schema(description = "是否缓存") Integer sysMiniMenuKeepAlive,
        @Schema(description = "是否隐藏") Integer sysMiniMenuHidden,
        @Schema(description = "说明") String sysMiniMenuDescription) {
}
