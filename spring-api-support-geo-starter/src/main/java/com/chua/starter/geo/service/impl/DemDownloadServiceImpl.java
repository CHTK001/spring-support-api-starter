package com.chua.starter.geo.service.impl;

import com.chua.starter.geo.model.Bounds;
import com.chua.starter.geo.model.DemSource;
import com.chua.starter.geo.model.TileSource;
import com.chua.starter.geo.service.DemDownloadService;
import com.chua.starter.geo.service.GeoTiffExporter;
import com.chua.starter.geo.service.TileCacheService;
import com.chua.starter.geo.service.TileDownloaderService;
import com.chua.starter.geo.service.TileMergerService;
import com.chua.starter.geo.util.TerrariumDecoder;
import com.chua.starter.geo.util.TileMathUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * DEM 下载服务实现，支持 Terrarium 高程瓦片下载与 geotiff 导出。
 *
 * @author CH
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DemDownloadServiceImpl implements DemDownloadService {

    /** 瓦片下载服务 */
    private final TileDownloaderService downloaderService;
    /** 瓦片拼接服务 */
    private final TileMergerService mergerService;
    /** geotiff 导出服务（保留字段，实际由内部逻辑处理） */
    private final GeoTiffExporter exporter;
    /** 瓦片缓存服务 */
    private final TileCacheService cacheService;

    /** 标准瓦片大小（像素） */
    private static final int TILE_SIZE = 256;

    @Override
    public long estimateTiles(Bounds bounds, int zoom) {
        return TileMathUtils.estimateTileCount(bounds, zoom);
    }

    /**
     * 下载并拼接 DEM 瓦片，导出为 geotiff。
     *
     * @param source           DEM 数据源
     * @param bounds           地理边界
     * @param zoom             缩放级别
     * @param proxy            代理地址（可为 空）
     * @param outputPath       输出路径
     * @param generateSidecars 是否生成 .prj
     * @return 输出文件路径
     * @throws Exception 下载或导出失败
     */
    @Override
    public String downloadAndExport(DemSource source, Bounds bounds, int zoom,
                                     String proxy, String outputPath, boolean generateSidecars) throws Exception {
        // 校验缩放级别是否超出数据源上限
        if (zoom > source.getMaxZoom()) {
            throw new IllegalArgumentException(
                    String.format("zoom %d 超出 %s 的最大级别 %d", zoom, source.getId(), source.getMaxZoom()));
        }

        // 构建 DEM 瓦片 URL（Terrarium 专用）
        int[] tl = TileMathUtils.toTile(bounds.getNorth(), bounds.getWest(), zoom);
        int[] br = TileMathUtils.toTile(bounds.getSouth(), bounds.getEast(), zoom);
        int rowWidth = br[0] - tl[0] + 1;
        long total = (long) rowWidth * (br[1] - tl[1] + 1);

        if (total == 0) {
            throw new IllegalArgumentException("所选区域在该缩放级别下无瓦片数据");
        }

        log.info("[DEM] 开始下载: source={} zoom={} tiles={}", source.getDisplayName(), zoom, total);

        // 复用瓦片下载器（URL 模板替换为 Terrarium 地址）
        TileSource demSource = buildDemTileSource(source, zoom);
        TileDownloaderService.DownloadResult result = downloaderService.downloadTiles(
                demSource, bounds, zoom, proxy, true);

        if (result.failed() == result.total() && result.total() > 0) {
            throw new RuntimeException("所有 DEM 瓦片下载失败");
        }

        // 拼接瓦片（Terrarium PNG 本身是 RGB 图像，直接拼接即可）
        BufferedImage merged = mergerService.mergeTiles(result.tiles(), bounds, zoom);

 // 导出为 geotiff（DEM 无压缩，保留浮点精度需转换为带地理信息的 TIFF）
        String fileName = "dem_" + source.getId() + "_z" + zoom;
        String finalPath = outputPath + "/" + fileName;

 // 写入带有高程信息的 geotiff
        String tiffPath = exportElevationTiff(merged, bounds, zoom, finalPath, generateSidecars);

        log.info("[DEM] 导出完成: {}", tiffPath);
        return tiffPath;
    }

    /**
     * 构建 DEM 瓦片图源配置。
     *
     * @param source DEM 数据源
     * @param zoom   缩放级别（用于 最大zoom 校验）
     * @return TileSource 配置
     */
    private TileSource buildDemTileSource(DemSource source, int zoom) {
        TileSource ts = new TileSource();
        ts.setId(source.getId());
        ts.setName(source.getDisplayName());
        ts.setUrl("https://s3.amazonaws.com/elevation-tiles-prod/terrarium/{z}/{x}/{y}.png");
        ts.setSubdomains(List.of());
        ts.setMaxZoom(source.getMaxZoom());
        ts.setAttribution("© NASA/SRTM");
        return ts;
    }

    /**
     * 将拼接后的 DEM 图像导出为带地理坐标的 geotiff。
     * 使用双字节无符号整数存储高程值（偏移32768），兼容 GDAL 读取。
     *
     * @param image          拼接后的 DEM 图像（RGB，高程值已编码在颜色通道中）
     * @param bounds         地理边界
     * @param zoom           缩放级别
     * @param outputPath     输出路径（不含扩展名）
     * @param generateSidecars 是否生成 .prj
     * @return 最终输出文件路径
     */
    private String exportElevationTiff(BufferedImage image, Bounds bounds, int zoom,
                                        String outputPath, boolean generateSidecars) throws Exception {
        String filePath = outputPath + ".tif";
        File file = new File(filePath);
        file.getParentFile().mkdirs();

 // 将 DEM 图像转换为 16-钻头 灰度（高程值转像素值）
        // 高程范围 -32768~32767 → 像素值 0~65535，偏移 32768
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage grayImage = new BufferedImage(width, height, BufferedImage.TYPE_USHORT_GRAY);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                float elev = TerrariumDecoder.decodePixel(r, g, b);
                // 编码回 16-bit：pixel = elev + 32768
                int pixel = (int) Math.max(0, Math.min(65535, elev + 32768));
                grayImage.setRGB(x, y, pixel);
            }
        }

        // 导出 TIFF
        try (FileOutputStream fos = new FileOutputStream(file)) {
            ImageIO.write(grayImage, "tiff", fos);
        }

        log.info("DEM GeoTIFF 导出: {} ({}x{} pixels)", filePath, width, height);

        // 生成 .prj
        if (generateSidecars) {
            writePrjFile(filePath.replace(".tif", ".prj"));
        }

        return filePath;
    }

    /**
     * 写入 PRJ 投影文件（Web Mercator / EPSG:3857）。
     *
     * @param prjPath PRJ 文件路径
     */
    private void writePrjFile(String prjPath) {
        try {
            String wkt = "PROJCS[\"WGS_84_Pseudo_Mercator\",\n"
                    + "    GEOGCS[\"WGS 84\",\n"
                    + "        DATUM[\"WGS_1984\",\n"
                    + "            SPHEROID[\"WGS 84\",6378137,298.257223563,\n"
                    + "                AUTHORITY[\"EPSG\",\"7030\"]],\n"
                    + "            AUTHORITY[\"EPSG\",\"6326\"]],\n"
                    + "        PRIMEM[\"Greenwich\",0,\n"
                    + "            AUTHORITY[\"EPSG\",\"8901\"]],\n"
                    + "        UNIT[\"degree\",0.0174532925199433,\n"
                    + "            AUTHORITY[\"EPSG\",\"9122\"]],\n"
                    + "        AUTHORITY[\"EPSG\",\"4326\"]],\n"
                    + "    PROJECTION[\"Mercator_1SP\"],\n"
                    + "    PARAMETER[\"central_meridian\",0],\n"
                    + "    PARAMETER[\"scale_factor\",1],\n"
                    + "    PARAMETER[\"false_easting\",0],\n"
                    + "    PARAMETER[\"false_northing\",0],\n"
                    + "    UNIT[\"metre\",1,AUTHORITY[\"EPSG\",\"9001\"]],\n"
                    + "    AXIS[\"Easting\",EAST],\n"
                    + "    AXIS[\"Northing\",NORTH],\n"
                    + "    AUTHORITY[\"EPSG\",\"3857\"]]";
            Files.writeString(Path.of(prjPath), wkt);
            log.info("DEM PRJ 文件已生成: {}", prjPath);
        } catch (IOException e) {
            log.warn("生成 DEM PRJ 文件失败: {}", e.getMessage());
        }
    }
}
