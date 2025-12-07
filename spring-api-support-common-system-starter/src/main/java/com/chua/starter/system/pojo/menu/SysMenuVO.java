package com.chua.starter.system.pojo.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 鑿滃崟VO
 *
 * @author CH
 * @since 2024/8/20
 */
@Data
@Schema(description = "鑿滃崟VO")
public class SysMenuVO {

    /**
     * 鑿滃崟ID
     */
    @Schema(description = "鑿滃崟ID")
    private Integer sysMenuId;

    /**
     * 鑿滃崟鍚嶇О
     */
    @Schema(description = "鑿滃崟鍚嶇О")
    private String sysMenuName;

    /**
     * 鑿滃崟鏍囬
     */
    @Schema(description = "鑿滃崟鏍囬")
    private String sysMenuTitle;

    /**
     * 鑿滃崟璺緞
     */
    @Schema(description = "鑿滃崟璺緞")
    private String sysMenuPath;

    /**
     * 鑿滃崟缁勪欢
     */
    @Schema(description = "鑿滃崟缁勪欢")
    private String sysMenuComponent;

    /**
     * 鑿滃崟鍥炬爣
     */
    @Schema(description = "鑿滃崟鍥炬爣")
    private String sysMenuIcon;

    /**
     * 鍙充晶鍥炬爣
     */
    @Schema(description = "鍙充晶鍥炬爣")
    private String sysMenuExtraIcon;

    /**
     * 鑿滃崟绫诲瀷; 0:鑿滃崟; 1:iframe; 2:澶栭摼; 3:鎸夐挳
     */
    @Schema(description = "鑿滃崟绫诲瀷; 0:鑿滃崟; 1:iframe; 2:澶栭摼; 3:鎸夐挳")
    private Integer sysMenuType;

    /**
     * 浼樺厛绾?     */
    @Schema(description = "浼樺厛绾?)
    private Integer sysMenuSort;

    /**
     * 鐖惰彍鍗旾D
     */
    @Schema(description = "鐖惰彍鍗旾D")
    private Integer sysMenuPid;

    /**
     * 鑿滃崟閾捐矾
     */
    @Schema(description = "鑿滃崟閾捐矾")
    private String sysMenuTreeId;

    /**
     * 鏉冮檺鏍囪瘑
     */
    @Schema(description = "鏉冮檺鏍囪瘑")
    private String sysMenuPerm;

    /**
     * 鏄惁闅愯棌; 0:鏄剧ず
     */
    @Schema(description = "鏄惁闅愯棌; 0:鏄剧ず")
    private Integer sysMenuHidden;

    /**
     * 鏄惁缂撳瓨; 0:鍚?     */
    @Schema(description = "鏄惁缂撳瓨; 0:鍚?)
    private Integer sysMenuKeepAlive;

    /**
     * 閲嶅畾鍚戝湴鍧€
     */
    @Schema(description = "閲嶅畾鍚戝湴鍧€")
    private String sysMenuRedirect;

    /**
     * 瀛愯彍鍗?     */
    @Schema(description = "瀛愯彍鍗?)
    private List<SysMenuVO> children;
}