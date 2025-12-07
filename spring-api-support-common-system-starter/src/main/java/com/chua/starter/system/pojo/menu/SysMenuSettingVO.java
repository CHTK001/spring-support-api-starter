package com.chua.starter.system.pojo.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 鑿滃崟璁剧疆VO
 *
 * @author CH
 * @since 2024/8/20
 */
@Data
@Schema(description = "鑿滃崟璁剧疆VO")
public class SysMenuSettingVO {

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
     * 鑿滃崟鍥炬爣
     */
    @Schema(description = "鑿滃崟鍥炬爣")
    private String sysMenuIcon;

    /**
     * 鑿滃崟绫诲瀷
     */
    @Schema(description = "鑿滃崟绫诲瀷")
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