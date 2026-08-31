package com.chua.starter.geo.service;

import com.chua.starter.geo.model.Bounds;
import com.chua.starter.geo.model.DemSource;

import java.util.Map;

/**
 * DEM 高程模型下载服务，支持 Terrarium 编码瓦片下载与 GeoTIFF 导出。
 *
 * @author CH
 * @since 1.0.0
 */
public interface DemDownloadService {

    /**
     * 估算指定区域和缩放级别的瓦片数量。
     *
     * @param bounds 地理边界
     * @param zoom   缩放级别
     * @return 估算的瓦片数量
     */
    long estimateTiles(Bounds bounds, int zoom);

    /**
     * 下载并拼接 DEM 瓦片，导出为 GeoTIFF 高程文件。
     *
     * @param source           DEM 数据源
     * @param bounds           地理边界
     * @param zoom             缩放级别
     * @param proxy            代理地址（可为 null）
     * @param outputPath       输出路径（不含扩展名）
     * @param generateSidecars 是否生成 .prj 侧面文件
     * @return 输出文件路径
     */
    String downloadAndExport(DemSource source, Bounds bounds, int zoom,
                              String proxy, String outputPath, boolean generateSidecars) throws Exception;
}
