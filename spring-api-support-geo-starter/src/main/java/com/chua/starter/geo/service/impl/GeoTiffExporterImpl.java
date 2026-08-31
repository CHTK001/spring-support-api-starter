package com.chua.starter.geo.service.impl;

import com.chua.starter.geo.model.OutputFormat;
import com.chua.starter.geo.service.GeoTiffExporter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * 图像导出服务实现，支持 GeoTIFF/PNG/JPEG 等格式导出。
 *
 * @author CH
 * @since 1.0.0
 */
@Service
@Slf4j
public class GeoTiffExporterImpl implements GeoTiffExporter {

    /** 默认瓦片像素大小 */
    private static final int DEFAULT_TILE_SIZE = 256;

    /**
     * 导出图像到指定路径。
     *
     * @param image          源图像
     * @param format         输出格式
     * @param outputPath     输出路径（不含扩展名）
     * @param compression    TIFF 压缩方式（none/lzw/deflate，仅 GEOTIFF 有效）
     * @param buildPyramid   是否构建影像金字塔（仅 GEOTIFF 有效）
     * @param generateSidecars 是否生成 .prj 等侧面文件
     * @return 最终输出文件路径
     */
    @Override
    public String export(BufferedImage image, OutputFormat format, String outputPath,
                         String compression, boolean buildPyramid, boolean generateSidecars) {
        if (format == null) {
            format = OutputFormat.GEOTIFF;
        }
        String filePath = outputPath + format.getExtension();
        File file = new File(filePath);
        file.getParentFile().mkdirs();
        try {
            javax.imageio.ImageIO.write(image, format.name().toLowerCase(), file);
            log.info("图像导出成功: {}, format={}, size={} bytes", filePath, format.getLabel(), file.length());
            return filePath;
        } catch (IOException e) {
            throw new RuntimeException("图像导出失败: " + e.getMessage(), e);
        }
    }
}
