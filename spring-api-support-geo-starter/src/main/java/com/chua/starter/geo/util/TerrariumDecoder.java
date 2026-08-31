package com.chua.starter.geo.util;

import com.chua.starter.geo.model.Bounds;

/**
 * Terrarium 高程编码解码工具。
 * Terrarium 编码：elevation_m = (R * 256 + G + B / 256) - 32768
 * 范围：-32768.0 ～ +32767.996 m，理论精度 1/256 m。
 *
 * @author CH
 * @since 1.0.0
 */
public class TerrariumDecoder {

    /** 私有构造函数，防止实例化 */
    private TerrariumDecoder() { }

    /**
     * 解码单个像素 RGB → 高程值（米）。
     *
     * @param r 红色通道值（0-255）
     * @param g 绿色通道值（0-255）
     * @param b 蓝色通道值（0-255）
     * @return 高程值（米）
     */
    public static float decodePixel(int r, int g, int b) {
        return (float) (r * 256.0 + g + b / 256.0) - 32768.0f;
    }

    /**
     * 将 RGBA 像素数组解码为高程 float 数组。
     * 输入为 BufferedImage.getRaster().getDataElements() 返回的 int[]。
     *
     * @param pixels RGBA 像素数组（每元素为一个 int，包含 R/G/B/A）
     * @param width  图像宽度
     * @param height 图像高度
     * @return 一维 float 数组，长度 width * height，行主序
     */
    public static float[] decodeToElevation(int[] pixels, int width, int height) {
        float[] elevation = new float[width * height];
        for (int i = 0; i < pixels.length; i++) {
            int p = pixels[i];
            int r = (p >> 16) & 0xFF;
            int g = (p >> 8) & 0xFF;
            int b = p & 0xFF;
            elevation[i] = decodePixel(r, g, b);
        }
        return elevation;
    }

    /**
     * 将 byte 数组（BGRA 格式）解码为高程 float 数组。
     *
     * @param bytes  原始像素字节（BGRA，每像素4字节）
     * @param width  图像宽度
     * @param height 图像高度
     * @return 高程 float 数组
     */
    public static float[] decodeFromBytes(byte[] bytes, int width, int height) {
        float[] elevation = new float[width * height];
        for (int i = 0; i < bytes.length; i += 4) {
            int b = bytes[i] & 0xFF;
            int g = bytes[i + 1] & 0xFF;
            int r = bytes[i + 2] & 0xFF;
            // alpha 在 i+3，忽略
            elevation[i / 4] = decodePixel(r, g, b);
        }
        return elevation;
    }

    /**
     * 估算 DEM 瓦片数量。
     *
     * @param bounds 地理边界
     * @param zoom   缩放级别
     * @return 瓦片数量
     */
    public static long estimateTileCount(Bounds bounds, int zoom) {
        return TileMathUtils.estimateTileCount(bounds, zoom);
    }
}
