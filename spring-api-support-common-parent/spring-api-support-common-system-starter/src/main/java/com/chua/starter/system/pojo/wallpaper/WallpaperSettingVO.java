package com.chua.starter.system.pojo.wallpaper;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 用户壁纸设置返回体
 *
 * <p>字段名与前端 {@code WallpaperSetting} 一一对应，前端不需要再做字段映射；
 * {@code css / cssDark} 由服务端按 {@code presetId} 从壁纸目录解析后填入，
 * 前端拿到就能直接写 CSS 变量。</p>
 *
 * @author CH
 * @since 4.0.0
 */
@Data
@Schema(description = "用户壁纸设置")
public class WallpaperSettingVO {

    @Schema(description = "记录ID")
    private Integer id;

    @Schema(description = "来源: default/builtin/upload")
    private String source;

    @Schema(description = "内置壁纸ID")
    private Integer presetId;

    @Schema(description = "内置壁纸编码（跨设备稳定的选中标识）")
    private String presetCode;

    @Schema(description = "壁纸名称")
    private String name;

    @Schema(description = "位图地址")
    private String url;

    @Schema(description = "CSS 背景值")
    private String css;

    @Schema(description = "暗色 CSS 背景值")
    private String cssDark;

    @Schema(description = "平铺方式")
    private String tile;

    @Schema(description = "不透明度 0~1")
    private BigDecimal opacity;

    @Schema(description = "模糊半径 0~24")
    private Integer blur;

    @Schema(description = "压暗 0~0.8")
    private BigDecimal dim;
}
