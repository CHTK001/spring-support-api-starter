package com.chua.starter.system.pojo.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 菜单VO
 *
 * @author CH
 * @since 2024/8/20
 */
@Data
@Schema(description = "菜单VO")
public class SysMenuVO {

    /**
     * 菜单ID
     */
    @Schema(description = "菜单ID")
    private Integer sysMenuId;

    /**
     * 菜单名称
     */
    @Schema(description = "菜单名称")
    private String sysMenuName;

    /**
     * 菜单标题
     */
    @Schema(description = "菜单标题")
    private String sysMenuTitle;

    /**
     * 父菜单ID
     */
    @Schema(description = "父菜单ID")
    private Integer sysMenuPid;

    /**
     * 菜单路径
     */
    @Schema(description = "菜单路径")
    private String sysMenuPath;

    /**
     * 菜单组件
     */
    @Schema(description = "菜单组件")
    private String sysMenuComponent;

    /**
     * 菜单图标
     */
    @Schema(description = "菜单图标")
    private String sysMenuIcon;

    /**
     * 右侧图标
     */
    @Schema(description = "右侧图标")
    private String sysMenuExtraIcon;

    /**
     * 菜单类型; 0:菜单; 1:iframe; 2:外链; 3:按钮
     */
    @Schema(description = "菜单类型; 0:菜单; 1:iframe; 2:外链; 3:按钮")
    private Integer sysMenuType;

    /**
     * 优先级别
     */
    @Schema(description = "优先级别")
    private Integer sysMenuSort;

    /**
     * 菜单链路
     */
    @Schema(description = "菜单链路")
    private String sysMenuTreeId;

    /**
     * 权限标识
     */
    @Schema(description = "权限标识")
    private String sysMenuPerm;

    /**
     * 是否隐藏; 0:显示
     */
    @Schema(description = "是否隐藏; 0:显示")
    private Integer sysMenuHidden;

    /**
     * 是否缓存; 0:否
     */
    @Schema(description = "是否缓存; 0:否")
    private Integer sysMenuKeepAlive;

    /**
     * 重定向地址
     */
    @Schema(description = "重定向地址")
    private String sysMenuRedirect;

    /**
     * 子菜单
     */
    @Schema(description = "子菜单")
    private List<SysMenuVO> children;
}