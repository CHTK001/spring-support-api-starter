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
import java.util.Map;

/**
 * 瓦片拼接服务实现，按瓦片坐标精确放置每张图片。
 *
 * @author CH
 * @since 1.0.0
 */
@Service
@Slf4j
public class TileMergerServiceImpl implements TileMergerService {

    /** 标准瓦片大小（像素） */
    private static final int TILE_SIZE = 256;

    /**
     * 将瓦片字节 Map 拼接为完整 RGB 图像。
     *
     * @param tileData 瓦片数据 Map，key 格式 "{z}_{x}_{y}"
     * @param bounds   地理边界
     * @param zoom     缩放级别
     * @return 拼接后的完整图像
     */
    @Override
    public BufferedImage mergeTiles(Map<String, byte[]> tileData, Bounds bounds, int zoom) {
        int[] tl = TileMathUtils.toTile(bounds.getNorth(), bounds.getWest(), zoom);
        int[] br = TileMathUtils.toTile(bounds.getSouth(), bounds.getEast(), zoom);
        int cols = br[0] - tl[0] + 1;
        int rows = br[1] - tl[1] + 1;
        int width = cols * TILE_SIZE;
        int height = rows * TILE_SIZE;

        BufferedImage canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = canvas.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);

        int placed = 0;
        for (Map.Entry<String, byte[]> entry : tileData.entrySet()) {
            String[] parts = entry.getKey().split("_");
            if (parts.length != 3) {
                continue;
            }
            try {
                int z = Integer.parseInt(parts[0]);
                int x = Integer.parseInt(parts[1]);
                int y = Integer.parseInt(parts[2]);
                if (z != zoom || x < tl[0] || x > br[0] || y < tl[1] || y > br[1]) {
                    continue;
                }
                int px = (x - tl[0]) * TILE_SIZE;
                int py = (y - tl[1]) * TILE_SIZE;
                try (ByteArrayInputStream bis = new ByteArrayInputStream(entry.getValue())) {
                    BufferedImage tile = ImageIO.read(bis);
                    if (tile != null) {
                        g2d.drawImage(tile, px, py, null);
                        placed++;
                    }
                }
            } catch (Exception e) {
                log.warn("[{}] 瓦片处理异常: {}", entry.getKey(), e.getMessage());
            }
        }
        g2d.dispose();
        log.info("瓦片拼接完成: {} 张瓦片放入 {}x{} 画布", placed, width, height);
        return canvas;
    }
}
