package com.chua.starter.geo.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

/**
 * 瓦片下载请求体，描述一次下载任务的完整参数。
 *
 * @author CH
 * @since 1.0.0
 */
@Data
public class DownloadRequest {
    /** 地理边界，不能为空 */
    @NotNull(message = "bounds不能为空")
    private Bounds bounds;

    /** 缩放级别，不能为空且 >= 1 */
    @NotNull(message = "zoom不能为空")
    @Min(value = 1, message = "zoom最小为1")
    private Integer zoom;

    /** 最大缩放级别（多级别下载时使用） */
    private Integer zoomMax;

    /** 指定多个缩放级别（非空时覆盖 zoom..=zoomMax） */
    private List<Integer> zoomLevels;

    /** 图源 ID，不能为空 */
    @NotNull(message = "source不能为空")
    private String source;

    /** 输出格式，不能为空（geotiff/png/jpeg/tiles/pbf） */
    @NotNull(message = "format不能为空")
    private String format;

    /** 保存路径 */
    private String savePath;

    /** 并发数（null 时使用默认值） */
    private Integer concurrency;

    /** 代理地址（可为 null） */
    private String proxy;

    /** 裁剪多边形（多环，null 表示不裁剪） */
    private List<List<PolygonPoint>> polygon;

    /** 是否按多边形裁剪 */
    private Boolean cropToShape;

    /** 图源显示名称（用于任务命名） */
    private String sourceName;

    /** 天地图 Token（可为 null，使用内置默认 Token） */
    private String tiandituToken;

    /** TIFF 压缩方式：none/lzw/deflate */
    private String compression;

    /** 是否构建影像金字塔（仅 GeoTIFF 有效） */
    private Boolean buildPyramid;

    /** 是否生成 .prj 侧面文件 */
    private Boolean generateSidecars;

    /** 叠加图源 ID 列表 */
    private List<String> overlaySources;
}
