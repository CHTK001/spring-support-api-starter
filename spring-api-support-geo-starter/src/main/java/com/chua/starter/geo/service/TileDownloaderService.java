package com.chua.starter.geo.service;

import com.chua.starter.geo.model.TileCoord;
import com.chua.starter.geo.model.TileSource;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * 瓦片下载服务，负责从指定图源下载瓦片图像。
 *
 * @author CH
 * @since 1.0.0
 */
public interface TileDownloaderService {

    /**
     * 下载指定区域内所有瓦片。
     *
     * @param source    图源配置
     * @param bounds    地理边界
     * @param zoom      缩放级别
     * @param proxy     代理地址（可为 null）
     * @param parallel  是否并行下载
     * @return 下载结果
     */
    DownloadResult downloadTiles(TileSource source, com.chua.starter.geo.model.Bounds bounds,
                                 int zoom, String proxy, boolean parallel);

    /**
     * 瓦片下载结果，包含下载的瓦片列表和统计信息。
     *
     * @param tiles      下载成功的瓦片图像列表
     * @param total      总瓦片数
     * @param completed  成功完成数
     * @param failed     失败数
     */
    record DownloadResult(List<BufferedImage> tiles, int total, int completed, int failed) {
    }
}
