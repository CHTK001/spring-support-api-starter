package com.chua.starter.geo.service;

import com.chua.starter.geo.model.Bounds;
import com.chua.starter.geo.model.TileSource;

import java.util.Map;
import java.util.Objects;

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
     * @param proxy    代理地址（可为 空）
     * @param parallel 是否并行下载（当前始终并行）
     * @return 下载结果，包含瓦片字节 映射 和统计信息
     */
    DownloadResult downloadTiles(TileSource source, Bounds bounds, int zoom, String proxy, boolean parallel);

    /**
     * 瓦片下载结果记录。
     *
     * @param tiles     下载成功的瓦片 映射，键 格式 "{z}_{x}_{y}"，值 为原始字节
     * @param total     总瓦片数
     * @param completed 成功完成数
     * @param failed    失败数
     */
    record DownloadResult(Map<String, byte[]> tiles, int total, int completed, int failed) {
        /**
         * 规范构造器：对瓦片 映射 做防御性拷贝。
         *
         * <p>value class 前置条件——集合组件必须深不可变；下载实现保证仅写入键 与 字节 均非空的条目，
         * 故可使用 {@code Map.copyOf}。</p>
         *
         * @param tiles     下载成功的瓦片 映射，键 格式 "{z}_{x}_{y}"，值 为原始字节
         * @param total     总瓦片数
         * @param completed 成功完成数
         * @param failed    失败数
         */
        public DownloadResult {
            tiles = Map.copyOf(Objects.requireNonNull(tiles, "tiles 不能为 null"));
        }
    }
}
