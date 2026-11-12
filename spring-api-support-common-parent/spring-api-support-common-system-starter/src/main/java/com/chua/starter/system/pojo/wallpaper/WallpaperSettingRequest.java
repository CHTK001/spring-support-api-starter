package com.chua.starter.system.pojo.wallpaper;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 保存壁纸设置的请求体
 *
 * <p>只接受「用户能选的东西」：来源、内置 id、上传地址与四项显示参数。
 * {@code css / name} 一律由服务端从目录解析，不接受前端传入，
 * 避免有人往库里塞任意 CSS。</p>
 *
 * @author CH
 * @since 4.0.0
 */
@Data
@Schema(description = "保存壁纸设置")
public class WallpaperSettingRequest {

    @Schema(description = "来源: default/builtin/upload")
    private String source;

    @Schema(description = "内置壁纸ID（source=builtin）")
    private Integer presetId;

    @Schema(description = "内置壁纸编码：presetId 缺失时按编码解析，便于前端静态清单直接保存")
    private String presetCode;

    @Schema(description = "图片地址（source=upload）")
    private String url;

    @Schema(description = "平铺方式")
    private String tile;

    @Schema(description = "不透明度 0~1")
    private BigDecimal opacity;

    @Schema(description = "模糊半径 0~24")
    private Integer blur;

    @Schema(description = "压暗 0~0.8")
    private BigDecimal dim;
}
