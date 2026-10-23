package com.chua.starter.face.reid.engine;

import com.chua.starter.face.reid.properties.FaceReidProperties;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Locale;

/**
 * 图片解码器：字节流 → 规整的 {@link RGBImage}。
 *
 * <p>只依赖 JDK 自带 ImageIO，无模型、无原生库。所有「坏输入」都在这里被拦下并
 * 给出可读原因：非图片、扩展名不允许、灰度但配置不允许、像素超限被缩小。</p>
 *
 * <p>放大上限设为 {@code maxImagePixels}，防止一张 4 万×4 万的 PNG 把堆打穿——
 * 解码前无法得知尺寸，因此先解码再按需缩小；ImageIO 的流式解码在多数情况下
 * 不会一次性展开到最大内存，风险可接受。</p>
 *
 * @author CH
 * @since 4.0.0.43
 */
public class ImageDecoder {

    /**
     * 引擎参数。
     */
    private final FaceReidProperties.DetectorOptions options;

    /**
     * 像素上限。
     */
    private final long maxPixels;

    /**
     * 构造解码器。
     *
     * @param options   引擎参数
     * @param maxPixels 单图像素上限
     */
    public ImageDecoder(FaceReidProperties.DetectorOptions options, long maxPixels) {
        this.options = RGBImage.validate(options);
        this.maxPixels = RGBImage.safePixelLimit(maxPixels);
    }

    /**
     * 解码结果。
     *
     * @param image  解码后的图像（失败时为 null）
     * @param reason 失败原因（成功时为 null）
     * @param scaled 是否因超限被缩小
     * @author CH
     * @since 4.0.0.43
     */
    public record Decoded(RGBImage image, String reason, boolean scaled) {

        /**
         * 失败。
         *
         * @param reason 原因
         * @return 结果
         */
        public static Decoded fail(String reason) {
            return new Decoded(null, reason, false);
        }

        /**
         * 成功。
         *
         * @param image  图像
         * @param scaled 是否缩放
         * @return 结果
         */
        public static Decoded ok(RGBImage image, boolean scaled) {
            return new Decoded(image, null, scaled);
        }
    }

    /**
     * 解码图片。
     *
     * @param bytes      图片字节
     * @param sourceName 来源名（用于扩展名校验与错误信息），可为 null
     * @return 解码结果
     */
    public Decoded decode(byte[] bytes, String sourceName) {
        if (bytes == null || bytes.length == 0) {
            return Decoded.fail("图片内容为空");
        }
        if (!suffixAllowed(sourceName)) {
            return Decoded.fail("不支持的图片格式: " + suffixOf(sourceName));
        }
        BufferedImage raw;
        try (ByteArrayInputStream in = new ByteArrayInputStream(bytes)) {
            raw = ImageIO.read(in);
        } catch (IOException e) {
            return Decoded.fail("图片解码失败: " + e.getMessage());
        } catch (RuntimeException e) {
            return Decoded.fail("图片解码异常: " + e.getMessage());
        }
        if (raw == null) {
            return Decoded.fail("无法识别的图片数据");
        }
        int colorModel = raw.getColorModel().getColorSpace().getType();
        boolean gray = colorModel == java.awt.color.ColorSpace.TYPE_GRAY;
        if (gray && !options.allowGrayInput()) {
            return Decoded.fail("灰度图片已禁用（spring.face.reid.detector.allow-gray-input=false）");
        }
        RGBImage image = toRgb(raw);
        boolean scaled = false;
        long pixels = (long) image.width() * image.height();
        if (pixels > maxPixels) {
            double ratio = Math.sqrt((double) maxPixels / pixels);
            int targetWidth = Math.max(1, (int) Math.floor(image.width() * ratio));
            int targetHeight = Math.max(1, (int) Math.floor(image.height() * ratio));
            image = image.resize(targetWidth, targetHeight);
            scaled = true;
        }
        return Decoded.ok(image, scaled);
    }

    /**
     * BufferedImage → RGBImage。
     *
     * @param raw 原图
     * @return RGB 图像
     */
    private RGBImage toRgb(BufferedImage raw) {
        int width = raw.getWidth();
        int height = raw.getHeight();
        byte[] data = new byte[width * height * 3];
        int[] row = new int[width];
        for (int y = 0; y < height; y++) {
            raw.getRGB(0, y, width, 1, row, 0, width);
            int base = y * width * 3;
            for (int x = 0; x < width; x++) {
                int argb = row[x];
                int offset = base + x * 3;
                data[offset] = (byte) ((argb >> 16) & 0xFF);
                data[offset + 1] = (byte) ((argb >> 8) & 0xFF);
                data[offset + 2] = (byte) (argb & 0xFF);
            }
        }
        return new RGBImage(width, height, data);
    }

    /**
     * 扩展名是否允许；无扩展名（如直接传摄像头帧）时放行，交给内容判定。
     *
     * @param sourceName 来源名
     * @return 允许返回 true
     */
    private boolean suffixAllowed(String sourceName) {
        String suffix = suffixOf(sourceName);
        if (suffix.isEmpty()) {
            return true;
        }
        return options.allowedImageSuffix().contains(suffix);
    }

    /**
     * 取出小写扩展名（含点），无则空串。
     *
     * @param sourceName 来源名
     * @return 扩展名
     */
    private String suffixOf(String sourceName) {
        if (sourceName == null) {
            return "";
        }
        String name = sourceName.trim().toLowerCase(Locale.ROOT);
        int dot = name.lastIndexOf('.');
        int slash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
        if (dot <= slash || dot == name.length() - 1) {
            return "";
        }
        return name.substring(dot);
    }
}
