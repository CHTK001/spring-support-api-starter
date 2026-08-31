package com.chua.starter.geo.service.impl;

import com.chua.starter.geo.model.Bounds;
import com.chua.starter.geo.service.TileMergerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * 瓦片拼接服务实现，将瓦片列表按坐标顺序合并为单张图像。
 *
 * @author CH
 * @since 1.0.0
 */
@Service
@Slf4j
public class TileMergerServiceImpl implements TileMergerService {

    /** 默认瓦片像素大小 */
    private static final int DEFAULT_TILE_SIZE = 256;

    /**
     * 将瓦片列表拼接为完整图像。
     *
     * @param tiles  瓦片图像列表（顺序应与 TileMathUtils.getTilesInBounds 返回一致）
     * @param bounds 地理边界
     * @param zoom   缩放级别
     * @return 拼接后的完整图像
     */
    @Override
    public BufferedImage mergeTiles(List<BufferedImage> tiles, Bounds bounds, int zoom) {
        if (tiles == null || tiles.isEmpty()) {
            return new BufferedImage(DEFAULT_TILE_SIZE, DEFAULT_TILE_SIZE, BufferedImage.TYPE_INT_RGB);
        }
        int tileSize = tiles.get(0).getWidth();
        int cols = (int) Math.ceil(Math.sqrt((double) tiles.size()));
        int rows = (int) Math.ceil((double) tiles.size() / cols);
        BufferedImage merged = new BufferedImage(cols * tileSize, rows * tileSize, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = merged.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, merged.getWidth(), merged.getHeight());
        for (int i = 0; i < tiles.size(); i++) {
            int x = (i % cols) * tileSize;
            int y = (i / cols) * tileSize;
            g2d.drawImage(tiles.get(i), x, y, null);
        }
        g2d.dispose();
        log.info("拼接完成: {}x{} tiles, size={}", cols, rows, tiles.size());
        return merged;
    }
}
