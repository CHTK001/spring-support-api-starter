package com.chua.starter.geo.service;

import com.chua.starter.geo.model.TileCoord;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * 瓦片拼接服务，将多个瓦片图像合并为一张完整图像。
 *
 * @author CH
 * @since 1.0.0
 */
public interface TileMergerService {

    /**
     * 将瓦片列表拼接为完整图像。
     *
     * @param tiles   瓦片图像列表（顺序应与 getTilesInBounds 返回一致）
     * @param bounds  地理边界
     * @param zoom    缩放级别
     * @return 拼接后的完整图像
     */
    BufferedImage mergeTiles(List<BufferedImage> tiles, com.chua.starter.geo.model.Bounds bounds, int zoom);
}
