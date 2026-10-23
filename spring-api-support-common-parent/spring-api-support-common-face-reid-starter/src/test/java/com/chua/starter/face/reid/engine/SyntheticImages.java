package com.chua.starter.face.reid.engine;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * 合成人脸图片工具（测试专用）。
 *
 * <p>单元测试不应依赖真实照片：一来仓库不便携带，二来真实照片的检测结果受模型版本影响。
 * 这里用纯色背景 + 肤色椭圆 + 眼睛/嘴巴图形合成一张「卡通人脸」，
 * 提供两种变体：</p>
 * <ul>
 *   <li>{@link #face(String)}：固定外观，同一 {@code seed} 得到同一张脸，用于验证「同人归并」；</li>
 *   <li>{@link #differentFace(String)}：五官位置/肤色不同，用于验证「不同人不被误并」。</li>
 * </ul>
 *
 * @author CH
 * @since 4.0.0.43
 */
final class SyntheticImages {

    /**
     * 工具类。
     */
    private SyntheticImages() {
    }

    /**
     * 渲染一张合成人脸。
     *
     * @param seed 外观种子（决定五官偏移与肤色）
     * @param withFace 是否绘制人脸（false 只画背景，用于验证「无人脸」分支）
     * @return PNG 字节
     */
    static byte[] render(String seed, boolean withFace) {
        int size = 240;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        try {
            g.setColor(new Color(230, 232, 236));
            g.fillRect(0, 0, size, size);
            if (!withFace) {
                return write(image);
            }
            int hash = seed == null ? 0 : seed.hashCode();
            int skinShift = Math.abs(hash % 30);
            Color skin = new Color(200 + skinShift / 2, 150 + skinShift / 3, 120 + skinShift / 4);
            // 脸
            g.setColor(skin);
            int faceSize = 150;
            int faceX = 45;
            int faceY = 45;
            g.fillOval(faceX, faceY, faceSize, faceSize);
            // 头发（上部深色块，制造梯度）
            g.setColor(new Color(40, 30, 25));
            g.fillOval(faceX, faceY - 10, faceSize, 60);
            // 眼睛：位置随 seed 偏移，改变梯度结构
            int eyeShift = Math.abs(hash % 14) - 7;
            g.setColor(Color.WHITE);
            g.fillOval(faceX + 36 + eyeShift, faceY + 62, 26, 16);
            g.fillOval(faceX + 90 - eyeShift, faceY + 62, 26, 16);
            g.setColor(new Color(30, 30, 30));
            int pupilShift = Math.abs(hash % 6) - 3;
            g.fillOval(faceX + 44 + eyeShift + pupilShift, faceY + 66, 10, 10);
            g.fillOval(faceX + 98 - eyeShift + pupilShift, faceY + 66, 10, 10);
            // 鼻子
            g.setColor(new Color(180, 130, 105));
            g.fillOval(faceX + 66, faceY + 86, 18, 14);
            // 嘴
            g.setColor(new Color(150, 70, 70));
            g.fillOval(faceX + 50, faceY + 112, 50 + Math.abs(hash % 10), 14);
            return write(image);
        } finally {
            g.dispose();
        }
    }

    /**
     * 输出 PNG。
     *
     * @param image 图像
     * @return 字节
     */
    private static byte[] write(BufferedImage image) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * 灰度图（用于验证灰度开关）。
     *
     * @param seed 种子
     * @return PNG 字节
     */
    static byte[] grayscale(String seed) {
        BufferedImage rgb = decode(render(seed, true));
        BufferedImage gray = new BufferedImage(rgb.getWidth(), rgb.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = gray.createGraphics();
        try {
            g.drawImage(rgb, 0, 0, null);
        } finally {
            g.dispose();
        }
        return write(gray);
    }

    /**
     * 解码 PNG。
     *
     * @param bytes 字节
     * @return 图像
     */
    static BufferedImage decode(byte[] bytes) {
        try {
            return ImageIO.read(new java.io.ByteArrayInputStream(bytes));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
