package com.chua.starter.geo.service;

import com.chua.starter.geo.model.Bounds;
import com.chua.starter.geo.model.TileSource;

import java.util.Map;

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
     * @param source   图源配置
     * @param bounds   地理边界
     * @param zoom     缩放级别
     * @param proxy    代理地址（可为 null）
     * @param parallel 是否并行下载（当前始终并行）
     * @return 下载结果，包含瓦片字节 Map 和统计信息
     */
    DownloadResult downloadTiles(TileSource source, Bounds bounds, int zoom, String proxy, boolean parallel);

    /**
     * 瓦片下载结果记录。
     *
     * @param tiles     下载成功的瓦片 Map，key 格式 "{z}_{x}_{y}"，value 为原始字节
     * @param total     总瓦片数
     * @param completed 成功完成数
     * @param failed    失败数
     */
    record DownloadResult(Map<String, byte[]> tiles, int total, int completed, int failed) {}
}
