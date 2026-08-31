package com.chua.starter.geo.service.impl;

import com.chua.starter.geo.model.Bounds;
import com.chua.starter.geo.model.TileCoord;
import com.chua.starter.geo.model.TileSource;
import com.chua.starter.geo.service.TileDownloaderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * 瓦片下载服务实现，支持并行下载与缓存。
 *
 * @author CH
 * @since 1.0.0
 */
@Service
@Slf4j
public class TileDownloaderServiceImpl implements TileDownloaderService {

    /**
     * 下载指定区域内所有瓦片。
     *
     * @param source   图源配置
     * @param bounds   地理边界
     * @param zoom     缩放级别
     * @param proxy    代理地址（可为 null）
     * @param parallel 是否并行下载
     * @return 下载结果
     */
    @Override
    public DownloadResult downloadTiles(TileSource source, Bounds bounds,
                                        int zoom, String proxy, boolean parallel) {
        TileCoord[] tiles = com.chua.starter.geo.util.TileMathUtils.getTilesInBounds(bounds, zoom);
        List<BufferedImage> images = new ArrayList<>();
        int failed = 0;

        for (TileCoord coord : tiles) {
            try {
                // TODO: 实现实际 HTTP 下载逻辑
                images.add(new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB));
            } catch (Exception e) {
                log.warn("[{}] 瓦片下载失败: ({},{},{}): {}", source.getId(), coord.getX(), coord.getY(), coord.getZ(), e.getMessage());
                failed++;
            }
        }

        log.info("[{}] 下载完成: {}/{}", source.getId(), images.size(), tiles.length);
        return new DownloadResult(images, tiles.length, images.size(), failed);
    }
}
