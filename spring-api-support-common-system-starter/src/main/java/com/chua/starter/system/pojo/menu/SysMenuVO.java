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
     * 菜单缁勪欢
     */
    @Schema(description = "菜单缁勪欢")
    private String sysMenuComponent;

    /**
     * 菜单鍥炬爣
     */
    @Schema(description = "菜单鍥炬爣")
    private String sysMenuIcon;

    /**
     * 鍙充晶鍥炬爣
     */
    @Schema(description = "鍙充晶鍥炬爣")
    private String sysMenuExtraIcon;

    /**
     * 菜单类型; 0:菜单; 1:iframe; 2:澶栭摼; 3:鎸夐挳
     */
    @Schema(description = "菜单类型; 0:菜单; 1:iframe; 2:澶栭摼; 3:鎸夐挳")
    private Integer sysMenuType;

    /**
     * 浼樺厛级别?     */
    @Schema(description = "浼樺厛级别")
    private Integer sysMenuSort;

    /**
     * 鐖惰彍鍗旾D
     */
    @Schema(description = "鐖惰彍鍗旾D")
    private Integer sysMenuPid;

    /**
     * 菜单閾捐矾
     */
    @Schema(description = "菜单閾捐矾")
    private String sysMenuTreeId;

    /**
     * 权限鏍囪瘑
     */
    @Schema(description = "权限鏍囪瘑")
    private String sysMenuPerm;

    /**
     * 鏄惁隐藏; 0:显示
     */
    @Schema(description = "鏄惁隐藏; 0:显示")
    private Integer sysMenuHidden;

    /**
     * 鏄惁缂撳瓨; 0:鍚     */
    @Schema(description = "鏄惁缂撳瓨; 0:鍚")
    private Integer sysMenuKeepAlive;

    /**
     * 重复畾鍚戝湴鍧€
     */
    @Schema(description = "重复畾鍚戝湴鍧€")
    private String sysMenuRedirect;

    /**
     * 瀛愯彍鍗"     */
    @Schema(description = "瀛愯彍鍗")
    private List<SysMenuVO> children;
}