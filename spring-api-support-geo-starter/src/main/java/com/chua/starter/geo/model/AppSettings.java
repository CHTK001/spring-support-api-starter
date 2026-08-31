package com.chua.starter.geo.model;

import lombok.Data;
import java.util.List;

/**
 * 全局应用设置，对应 GeoProperties 的运行时快照。
 *
 * @author CH
 * @since 1.0.0
 */
@Data
public class AppSettings {
    /** 天地图 Token */
    private String tiandituToken;
    /** 代理是否启用 */
    private Boolean proxyEnabled;
    /** 代理地址 */
    private String proxyUrl;
    /** 默认并发数 */
    private int defaultConcurrency;
    /** 默认缩放级别 */
    private int defaultZoom;
    /** 默认输出格式 */
    private String defaultFormat;
    /** 默认图源 */
    private String defaultSource;
    /** 自定义图源列表 */
    private List<CustomTileSource> customSources;
    /** Cesium Ion Token */
    private String cesiumIonToken;
    /** 调试模式 */
    private Boolean debugMode;
    /** 内存预算（MB） */
    private int memoryBudgetMb;
    /** 是否允许无效 HTTPS 证书 */
    private Boolean allowInvalidCerts;
    /** 瓦片缓存是否启用 */
    private Boolean tileCacheEnabled;
    /** 瓦片缓存上限（MB） */
    private int tileCacheMaxSizeMb;
    /** 最小导出成功率阈值 */
    private Double minExportSuccessRatio;
    /** 导出缓冲区大小（MB） */
    private int exportBufferMb;
    /** 空瓦片探测行为 */
    private String emptyTileProbeAction;
}
