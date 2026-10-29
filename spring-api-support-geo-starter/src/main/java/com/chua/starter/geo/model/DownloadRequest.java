package com.chua.starter.geo.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 瓦片下载请求体，描述一次下载任务的完整参数。
 *
 * @param bounds          地理边界，不能为空
 * @param zoom            缩放级别，不能为空且 >= 1
 * @param zoomMax         最大缩放级别（多级别下载时使用）
 * @param zoomLevels      指定多个缩放级别（非空时覆盖 zoom..=zoomMax）
 * @param source          图源标识，不能为空
 * @param format          输出格式，不能为空（geotiff/png/jpeg/tile/pbf）
 * @param savePath        保存路径
 * @param concurrency     并发数（空时使用默认值）
 * @param proxy           代理地址（可为空）
 * @param polygon         裁剪多边形（多环，空表示不裁剪）
 * @param cropToShape     是否按多边形裁剪
 * @param sourceName      图源显示名称（用于任务命名）
 * @param tiandituToken   天地图令牌（可为空，使用内置默认令牌）
 * @param compression     TIFF 压缩方式：无/lzw/deflate
 * @param buildPyramid    是否构建影像金字塔（仅 geotiff 有效）
 * @param generateSidecars 是否生成 .prj 侧面文件
 * @param overlaySources  叠加图源标识列表
 * @author CH
 * @since 1.0.0
 */
public record DownloadRequest(
        @NotNull(message = "bounds不能为空")
        Bounds bounds,

        @NotNull(message = "zoom不能为空")
        @Min(value = 1, message = "zoom最小为1")
        Integer zoom,

        Integer zoomMax,

        List<Integer> zoomLevels,

        @NotNull(message = "source不能为空")
        String source,

        @NotNull(message = "format不能为空")
        String format,

        String savePath,

        Integer concurrency,

        String proxy,

        List<List<PolygonPoint>> polygon,

        Boolean cropToShape,

        String sourceName,

        String tiandituToken,

        String compression,

        Boolean buildPyramid,

        Boolean generateSidecars,

        List<String> overlaySources) {
}
