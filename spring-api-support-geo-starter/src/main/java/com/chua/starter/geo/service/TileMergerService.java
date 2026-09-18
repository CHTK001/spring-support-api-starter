package com.chua.starter.geo.service;

import com.chua.starter.geo.model.Bounds;

import java.awt.image.BufferedImage;
import java.util.Map;

/**
 * 瓦片拼接服务，将多个瓦片图像合并为一张完整图像。
 *
 * @author CH
 * @since 1.0.0
 */
public interface TileMergerService {

    /**
     * 将瓦片字节 映射 拼接为完整 RGB 图像。
     *
     * @param tileData 瓦片数据 映射，键 格式 "{z}_{x}_{y}"，值 为瓦片原始字节
     * @param bounds   地理边界
     * @param zoom     缩放级别
     * @return 拼接后的完整图像
     */
    BufferedImage mergeTiles(Map<String, byte[]> tileData, Bounds bounds, int zoom);
}
