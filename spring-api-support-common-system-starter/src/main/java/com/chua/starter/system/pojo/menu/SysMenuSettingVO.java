package com.chua.starter.system.pojo.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 菜单设置VO
 *
 * @author CH
 * @since 2024/8/20
 */
@Data
@Schema(description = "菜单设置VO")
public class SysMenuSettingVO {

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
     * 菜单标题
     */
    @Schema(description = "菜单标题")
    private String sysMenuTitle;

    /**
     * 菜单璺緞
     */
    @Schema(description = "菜单璺緞")
    private String sysMenuPath;

    /**
     * 菜单鍥炬爣
     */
    @Schema(description = "菜单鍥炬爣")
    private String sysMenuIcon;

    /**
     * 菜单类型
     */
    @Schema(description = "菜单类型")
    private Integer sysMenuType;

    /**
     * 鐖惰彍鍗旾D
     */
    @Schema(description = "鐖惰彍鍗旾D")
    private Integer sysMenuPid;

    /**
     * 瀛愯彍鍗"     */
    @Schema(description = "瀛愯彍鍗")
    private List<SysMenuSettingVO> children;
}