// [P3C A20 豁免] A20/A30/A31: 含日志/界面/SQL 字符串字面量等非业务魔法值场景，豁免。
package com.chua.starter.geo.service.impl;

import com.chua.starter.geo.model.OutputFormat;
import com.chua.starter.geo.service.GeoTiffExporter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 图像导出服务实现，支持 geotiff/PNG/JPEG 格式。
 * 生成 geotiff 时同步创建 .prj 投影文件。
 *
 * @author CH
 * @since 1.0.0
 */
@Service
@Slf4j
public class GeoTiffExporterImpl implements GeoTiffExporter {

    /**
     * EPSG:3857 WKT 投影定义
    */
    private static final String EPSG3857_WKT = """
        PROJCS["WGS_84_Pseudo_Mercator",
            GEOGCS["WGS 84",
                DATUM["WGS_1984",
                    SPHEROID["WGS 84",6378137,298.257223563,
                        AUTHORITY["EPSG","7030"]],
                    AUTHORITY["EPSG","6326"]],
                PRIMEM["Greenwich",0,
                    AUTHORITY["EPSG","8901"]],
                UNIT["degree",0.0174532925199433,
                    AUTHORITY["EPSG","9122"]],
                AUTHORITY["EPSG","4326"]],
            PROJECTION["Mercator_1SP"],
            PARAMETER["central_meridian",0],
            PARAMETER["scale_factor",1],
            PARAMETER["false_easting",0],
            PARAMETER["false_northing",0],
            UNIT["metre",1,AUTHORITY["EPSG","9001"]],
            AXIS["Easting",EAST],
            AXIS["Northing",NORTH],
            EXTENSION["PROJ4","+proj=merc +a=6378137 +b=6378137 +lat_ts=0.0 +lon_0=0.0 +x_0=0.0 +y_0=0 +k=1.0 +units=m +nadgrids=@null +wktext +no_defs"],
            AUTHORITY["EPSG","3857"]]
        """;

    /**
     * 导出图像到指定路径。
     *
     * @param image          源图像
     * @param format         输出格式
     * @param outputPath     输出路径（不含扩展名）
     * @param compression    TIFF 压缩方式（无/lzw/deflate，仅 GEOTIFF 有效）
     * @param buildPyramid   是否构建影像金字塔
     * @param generateSidecars 是否生成 .prj 侧面文件
     * @return 最终输出文件路径
     * @throws Exception 导出失败时抛出异常
     */
    @Override
    public String export(BufferedImage image, OutputFormat format, String outputPath,
                         String compression, boolean buildPyramid, boolean generateSidecars) throws Exception {
        if (image == null) {
            throw new IllegalArgumentException("image must not be null");
        }
        if (format == null) {
            format = OutputFormat.GEOTIFF;
        }
        if (outputPath == null || outputPath.isBlank()) {
            throw new IllegalArgumentException("outputPath must not be blank");
        }

        String ext;
        switch (format) {
            case PNG:
                ext = ".png";
                break;
            case JPEG:
                ext = ".jpg";
                break;
            default:
                ext = ".tif";
                break;
        }

        String filePath = outputPath + ext;
        File file = new File(filePath);
        file.getParentFile().mkdirs();

        try (FileOutputStream fos = new FileOutputStream(file)) {
            ImageIO.write(image, format.name().toLowerCase(), fos);
        }

        long fileSize = file.length();
        log.info("图像导出成功: {}, format={}, size={} bytes", filePath, format.getLabel(), fileSize);

        // 生成 .prj 侧面文件
        if (generateSidecars && format == OutputFormat.GEOTIFF) {
            writePrjFile(filePath.replace(".tif", ".prj"));
        }

        // 金字塔提示（需 GDAL 支持，此处仅记录日志）
        if (buildPyramid && format == OutputFormat.GEOTIFF) {
            log.info("影像金字塔生成需 GDAL 工具支持，请手动执行: gdaladdo {} auto", filePath);
        }

        return filePath;
    }

    /**
     * 写入 PRJ 投影文件。
     *
     * @param prjPath PRJ 文件路径
     */
    private void writePrjFile(String prjPath) {
        try {
            Files.writeString(Path.of(prjPath), EPSG3857_WKT);
            log.info("PRJ 文件已生成: {}", prjPath);
        } catch (IOException e) {
            log.warn("生成 PRJ 文件失败: {}", e.getMessage());
        }
    }
}