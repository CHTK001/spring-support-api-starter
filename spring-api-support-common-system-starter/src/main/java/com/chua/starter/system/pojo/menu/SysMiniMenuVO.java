package com.chua.starter.system.pojo.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "小程序菜单卡片VO")
public class SysMiniMenuVO {

    @Schema(description = "小程序菜单ID")
    private Integer sysMiniMenuId;

    @Schema(description = "卡片标题")
    private String sysMiniMenuTitle;

    @Schema(description = "卡片副标题")
    private String sysMiniMenuSubtitle;

    @Schema(description = "卡片分组")
    private String sysMiniMenuCategory;

    @Schema(description = "页面路径")
    private String sysMiniMenuPath;

    @Schema(description = "跳转模式")
    private Integer sysMiniMenuJumpMode;

    @Schema(description = "图标")
    private String sysMiniMenuIcon;

    @Schema(description = "封面")
    private String sysMiniMenuCover;

    @Schema(description = "角标")
    private String sysMiniMenuBadge;

    @Schema(description = "角标样式")
    private String sysMiniMenuBadgeType;

    @Schema(description = "权限标识")
    private String sysMiniMenuPerm;

    @Schema(description = "角色编码")
    private String sysMiniMenuRole;

    @Schema(description = "排序")
    private Integer sysMiniMenuSort;

    @Schema(description = "是否缓存")
    private Integer sysMiniMenuKeepAlive;

    @Schema(description = "是否隐藏")
    private Integer sysMiniMenuHidden;

    @Schema(description = "说明")
    private String sysMiniMenuDescription;
}
