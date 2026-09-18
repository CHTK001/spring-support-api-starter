package com.chua.starter.geo.service.impl;

import com.chua.starter.geo.model.Bounds;
import com.chua.starter.geo.service.TileMergerService;
import com.chua.starter.geo.util.TileMathUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 瓦片拼接服务实现，将瓦片 映射 按坐标顺序合并为单张图像。
 *
 * @author CH
 * @since 1.0.0
 * @param x x
 * @param data 数据
 * @return 瓷砖entry的结果
 */
@Service
@Slf4j
public class TileMergerServiceImpl implements TileMergerService {

    /**
     * 合并瓷砖。
     * @param tiles 瓷砖
     * @param bounds bounds
     * @param zoom zoom
     * @return 合并瓷砖的结果
     */
    private static final int DEFAULT_TILE_SIZE = 256;

    @Override
    public BufferedImage mergeTiles(Map<String, byte[]> tiles, Bounds bounds, int zoom) {
        if (tiles == null || tiles.isEmpty()) {
            return new BufferedImage(DEFAULT_TILE_SIZE, DEFAULT_TILE_SIZE, BufferedImage.TYPE_INT_RGB);
        }
        int[] tl = TileMathUtils.toTile(bounds.getNorth(), bounds.getWest(), zoom);
        int[] br = TileMathUtils.toTile(bounds.getSouth(), bounds.getEast(), zoom);
        int cols = br[0] - tl[0] + 1;
        int rows = br[1] - tl[1] + 1;

        TreeMap<Integer, List<TileEntry>> rowMap = new TreeMap<>();
        for (Map.Entry<String, byte[]> entry : tiles.entrySet()) {
            String[] parts = entry.getKey().split("_");
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            rowMap.computeIfAbsent(y, k -> new ArrayList<>()).add(new TileEntry(x, entry.getValue()));
        }

        int tileSize = DEFAULT_TILE_SIZE;
        BufferedImage merged = new BufferedImage(cols * tileSize, rows * tileSize, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = merged.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, merged.getWidth(), merged.getHeight());

        int rowIdx = 0;
        for (List<TileEntry> row : rowMap.values()) {
            row.sort((a, b) -> Integer.compare(a.x, b.x));
            int xIdx = 0;
            for (TileEntry entry : row) {
                try {
                    ByteArrayInputStream bais = new ByteArrayInputStream(entry.data);
                    BufferedImage tile = ImageIO.read(bais);
                    if (tile != null) {
                        g2d.drawImage(tile, xIdx * tileSize, rowIdx * tileSize, null);
                    }
                } catch (Exception e) {
                    log.warn("瓦片解码失败: {}", e.getMessage());
                }
                xIdx++;
            }
            rowIdx++;
        }
        g2d.dispose();
        log.info("拼接完成: {}x{} tiles, size={}", cols, rows, tiles.size());
        return merged;
    }

    private record TileEntry(int x, byte[] data) {}
}
