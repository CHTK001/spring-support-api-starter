package com.chua.starter.face.reid.engine;

import com.chua.starter.face.reid.properties.FaceReidProperties;
import org.springframework.util.unit.DataSize;

import java.util.List;

/**
 * 已解码的图片像素数据（RGB）。
 *
 * <p>把「读字节 → 解码 → 校验 → 缩放」从检测算法里拆出来单独实现，
 * 好处是：图像解码路径可以完全脱离模型依赖做单元测试，而检测算法只需面对
 * 一个规整的 RGB 数组。{@code data} 按行优先存放，每像素 3 字节。</p>
 *
 * @param width  宽（像素）
 * @param height 高（像素）
 * @param data   RGB 字节
 * @author CH
 * @since 4.0.0.43
 */
public record RGBImage(int width, int height, byte[] data) {

    /**
     * 像素总数。
     *
     * @return 像素数
     */
    public int pixels() {
        return width * height;
    }

    /**
     * 读取指定像素的 RGB。
     *
     * @param x X 坐标
     * @param y Y 坐标
     * @return 长度 3 的数组
     */
    public int[] rgb(int x, int y) {
        int offset = (y * width + x) * 3;
        return new int[]{
                data[offset] & 0xFF,
                data[offset + 1] & 0xFF,
                data[offset + 2] & 0xFF
        };
    }

    /**
     * 灰度值（BT.601 加权）。
     *
     * @param x X 坐标
     * @param y Y 坐标
     * @return 灰度 0~255
     */
    public int gray(int x, int y) {
        int[] rgb = rgb(x, y);
        return (int) Math.round(0.299 * rgb[0] + 0.587 * rgb[1] + 0.114 * rgb[2]);
    }

    /**
     * 按最近邻缩放。
     *
     * @param targetWidth  目标宽
     * @param targetHeight 目标高
     * @return 缩放后图像
     */
    public RGBImage resize(int targetWidth, int targetHeight) {
        if (targetWidth == width && targetHeight == height) {
            return this;
        }
        byte[] out = new byte[targetWidth * targetHeight * 3];
        for (int y = 0; y < targetHeight; y++) {
            int sy = (int) ((long) y * height / targetHeight);
            for (int x = 0; x < targetWidth; x++) {
                int sx = (int) ((long) x * width / targetWidth);
                int src = (sy * width + sx) * 3;
                int dst = (y * targetWidth + x) * 3;
                out[dst] = data[src];
                out[dst + 1] = data[src + 1];
                out[dst + 2] = data[src + 2];
            }
        }
        return new RGBImage(targetWidth, targetHeight, out);
    }

    /**
     * 裁剪矩形区域，越界部分以 0 填充。
     *
     * @param x      左上角 X
     * @param y      左上角 Y
     * @param width  宽
     * @param height 高
     * @return 裁剪结果
     */
    public RGBImage crop(int x, int y, int width, int height) {
        byte[] out = new byte[Math.max(1, width) * Math.max(1, height) * 3];
        for (int row = 0; row < height; row++) {
            int sy = y + row;
            if (sy < 0 || sy >= this.height) {
                continue;
            }
            for (int col = 0; col < width; col++) {
                int sx = x + col;
                if (sx < 0 || sx >= this.width) {
                    continue;
                }
                int src = (sy * this.width + sx) * 3;
                int dst = (row * width + col) * 3;
                out[dst] = data[src];
                out[dst + 1] = data[src + 1];
                out[dst + 2] = data[src + 2];
            }
        }
        return new RGBImage(Math.max(1, width), Math.max(1, height), out);
    }

    /**
     * 校验引擎参数，非法值在启动时就暴露，而不是等到第一帧失败。
     *
     * @param options 引擎参数
     * @return 规范化后的参数
     */
    public static FaceReidProperties.DetectorOptions validate(FaceReidProperties.DetectorOptions options) {
        FaceReidProperties.DetectorOptions target =
                options == null ? FaceReidProperties.DetectorOptions.defaults() : options;
        List<String> suffixes = target.allowedImageSuffix() == null || target.allowedImageSuffix().isEmpty()
                ? FaceReidProperties.DetectorOptions.defaults().allowedImageSuffix()
                : target.allowedImageSuffix().stream()
                .filter(s -> s != null && !s.isBlank())
                .map(s -> s.trim().toLowerCase(java.util.Locale.ROOT))
                .toList();
        double nms = target.nmsThreshold() <= 0 || target.nmsThreshold() > 1 ? 0.45 : target.nmsThreshold();
        double score = target.scoreThreshold() <= 0 || target.scoreThreshold() > 1 ? 0.65 : target.scoreThreshold();
        return new FaceReidProperties.DetectorOptions(
                Math.max(8, target.minFaceSize()),
                score,
                nms,
                Math.min(1, Math.max(0, target.minQuality())),
                Math.max(0, target.blurThreshold()),
                target.normalizeBrightness(),
                target.flipAugment(),
                target.allowGrayInput(),
                suffixes);
    }

    /**
     * 单图最大像素的安全上限（避免 DataSize 误用导致 Integer 溢出）。
     *
     * @param maxImagePixels 配置值
     * @return 实际使用的上限
     */
    public static long safePixelLimit(long maxImagePixels) {
        long configured = maxImagePixels <= 0 ? 20_000_000L : maxImagePixels;
        return Math.min(configured, DataSize.ofGigabytes(1).toBytes() / 3);
    }
}
