package com.chua.starter.geo.service;

import com.chua.starter.geo.model.OutputFormat;
import java.awt.image.BufferedImage;

/**
* 图像导出服务，将拼接后的图像导出为指定格式的文件。
*
* @author CH
* @since 1.0.0
 */
public interface GeoTiffExporter {

    /**
    * 导出图像到指定路径。
    *
    * @param image          源图像
    * @param format         输出格式
    * @param outputPath     输出路径（不含扩展名）
    * @param compression    TIFF 压缩方式（无/lzw/deflate，仅 GEOTIFF 有效）
    * @param buildPyramid   是否构建影像金字塔
    * @param generateSidecars 是否生成 .prj 等侧面文件
    * @return 最终输出文件路径
    * @throws Exception 导出失败时抛出异常
     */
    String export(BufferedImage image, OutputFormat format, String outputPath,
                  String compression, boolean buildPyramid, boolean generateSidecars) throws Exception;
}
