package com.chua.starter.system.pojo.wallpaper;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 记录一次壁纸上传
 *
 * @author CH
 * @since 4.0.0
 */
@Data
@Schema(description = "壁纸上传记录")
public class WallpaperUploadRequest {

    @Schema(description = "图片地址（/v2/file/upload 返回的 url）")
    private String url;

    @Schema(description = "展示名")
    private String name;
}
