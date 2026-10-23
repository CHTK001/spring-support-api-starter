package com.chua.starter.face.reid.engine;

import com.chua.starter.face.reid.spi.FaceBox;

/**
 * 人脸外观描述子。
 *
 * <p>把人脸区域按 4×4 网格切分，每格统计：</p>
 * <ul>
 *   <li>3 个通道的均值与标准差（6 维）——刻画肤色与明暗分布；</li>
 *   <li>8 个方向的梯度直方图（8 维）——刻画五官/轮廓结构，类 HOG。</li>
 * </ul>
 * <p>合计 {@value #DIMENSION} 维，L2 归一化。网格化的意义在于保留<b>空间结构</b>：
 * 同一张脸在不同摄像头下颜色分布与五官梯度走向稳定，而不同人的这些统计量有差异，
 * 这正是 Re-ID 判同的基础。</p>
 *
 * @author CH
 * @since 4.0.0.43
 */
public final class LocalFaceDescriptor {

    /**
     * 网格边长。
     *
     * <p>8×8 而非 4×4：人脸的身份信息集中在眼、鼻、口这几个小区域，
     * 4×4 时每格覆盖约半张脸，细节被平均掉，不同的人特征几乎一致。</p>
     */
    public static final int GRID = 8;

    /**
     * 每格颜色维度（3 均值 + 3 标准差）。
     */
    public static final int COLOR_DIMS = 6;

    /**
     * 每格梯度方向数。
     */
    public static final int ORIENTATIONS = 8;

    /**
     * 总维度。
     */
    public static final int DIMENSION = GRID * GRID * (COLOR_DIMS + ORIENTATIONS);

    /**
     * 工具类构造。
     */
    private LocalFaceDescriptor() {
    }

    /**
     * 生成描述子。
     *
     * @param image       整幅图像
     * @param box         人脸框
     * @param flipAugment 是否用镜像特征平均（增强左右脸一致性）
     * @return L2 归一化特征；区域非法时返回 null
     */
    public static float[] describe(RGBImage image, FaceBox box, boolean flipAugment) {
        if (image == null || box == null) {
            return null;
        }
        RGBImage face = image.crop(box.x(), box.y(), Math.max(1, box.width()), Math.max(1, box.height()));
        if (face.width() < GRID || face.height() < GRID) {
            return null;
        }
        float[] descriptor = compute(face);
        if (flipAugment) {
            float[] mirrored = compute(flip(face));
            if (mirrored != null) {
                for (int i = 0; i < descriptor.length; i++) {
                    descriptor[i] = (descriptor[i] + mirrored[i]) / 2f;
                }
            }
        }
        return l2Normalize(descriptor);
    }

    /**
     * 计算原始描述子（未归一化）。
     *
     * <p>颜色块与梯度块<b>各自</b>做一次 L2 归一化后再拼接。原因：颜色均值量级约 0.8，
     * 而归一化后的梯度直方图量级约 0.125，直接拼接会让颜色项独占 99% 的能量——
     * 余弦相似度退化成「肤色像不像」，不同的人也会得到 0.99 的高分。
     * 分块归一化让「肤色分布」与「五官结构」各占一半话语权。</p>
     *
     * @param face 人脸区域
     * @return 描述子
     */
    static float[] compute(RGBImage face) {
        int cellW = Math.max(1, face.width() / GRID);
        int cellH = Math.max(1, face.height() / GRID);
        float[] color = new float[GRID * GRID * COLOR_DIMS];
        float[] gradient = new float[GRID * GRID * ORIENTATIONS];
        int colorIndex = 0;
        int gradientIndex = 0;
        float[] cell = new float[COLOR_DIMS + ORIENTATIONS];
        for (int gy = 0; gy < GRID; gy++) {
            for (int gx = 0; gx < GRID; gx++) {
                int x0 = gx * cellW;
                int y0 = gy * cellH;
                int x1 = gx == GRID - 1 ? face.width() : x0 + cellW;
                int y1 = gy == GRID - 1 ? face.height() : y0 + cellH;
                fillCell(face, x0, y0, x1, y1, cell);
                System.arraycopy(cell, 0, color, colorIndex, COLOR_DIMS);
                colorIndex += COLOR_DIMS;
                System.arraycopy(cell, COLOR_DIMS, gradient, gradientIndex, ORIENTATIONS);
                gradientIndex += ORIENTATIONS;
            }
        }
        // 颜色块去掉「整体均值」只留相对差异：不同肤色的人均值都接近 0.8，
        // 不减均值的话颜色块内部高度相似，余弦相似度会被这层共同偏置抬到 0.99
        subtractMean(color);
        l2Normalize(color);
        l2Normalize(gradient);
        float[] out = new float[DIMENSION];
        System.arraycopy(color, 0, out, 0, color.length);
        System.arraycopy(gradient, 0, out, color.length, gradient.length);
        return out;
    }

    /**
     * 原地减去均值。
     *
     * @param vector 向量
     */
    private static void subtractMean(float[] vector) {
        if (vector.length == 0) {
            return;
        }
        double sum = 0;
        for (float value : vector) {
            sum += value;
        }
        float mean = (float) (sum / vector.length);
        for (int i = 0; i < vector.length; i++) {
            vector[i] -= mean;
        }
    }

    /**
     * 填充单个网格的统计量。
     *
     * @param face 人脸区域
     * @param x0   起始 X
     * @param y0   起始 Y
     * @param x1   结束 X
     * @param y1   结束 Y
     * @param out  输出数组（长度 ≥ {@code COLOR_DIMS + ORIENTATIONS}）
     * @return 写入的元素个数
     */
    private static int fillCell(RGBImage face, int x0, int y0, int x1, int y1, float[] out) {
        int index = 0;
        double[] sum = new double[3];
        double[] sumSq = new double[3];
        double[] hist = new double[ORIENTATIONS];
        int count = 0;
        for (int y = y0; y < y1; y++) {
            for (int x = x0; x < x1; x++) {
                int[] rgb = face.rgb(x, y);
                for (int c = 0; c < 3; c++) {
                    double v = rgb[c] / 255.0;
                    sum[c] += v;
                    sumSq[c] += v * v;
                }
                count++;
            }
        }
        if (count == 0) {
            count = 1;
        }
        for (int c = 0; c < 3; c++) {
            double mean = sum[c] / count;
            double variance = Math.max(0, sumSq[c] / count - mean * mean);
            out[index++] = (float) mean;
            out[index++] = (float) Math.sqrt(variance);
        }
        // 梯度方向直方图：中心差分，角度量化到 8 个 bin
        for (int y = Math.max(y0, 1); y < Math.min(y1, face.height() - 1); y++) {
            for (int x = Math.max(x0, 1); x < Math.min(x1, face.width() - 1); x++) {
                double gxv = face.gray(x + 1, y) - face.gray(x - 1, y);
                double gyv = face.gray(x, y + 1) - face.gray(x, y - 1);
                double magnitude = Math.hypot(gxv, gyv);
                if (magnitude < 1e-6) {
                    continue;
                }
                double angle = Math.atan2(gyv, gxv);
                double normalized = (angle + Math.PI) / (2 * Math.PI);
                int bin = (int) Math.floor(normalized * ORIENTATIONS);
                hist[Math.min(ORIENTATIONS - 1, Math.max(0, bin))] += magnitude / 255.0;
            }
        }
        double histSum = 0;
        for (double value : hist) {
            histSum += value;
        }
        for (int i = 0; i < ORIENTATIONS; i++) {
            out[index++] = (float) (histSum > 0 ? hist[i] / histSum : 0);
        }
        return index;
    }

    /**
     * 水平镜像。
     *
     * @param source 原图
     * @return 镜像图像
     */
    static RGBImage flip(RGBImage source) {
        int width = source.width();
        int height = source.height();
        byte[] data = source.data();
        byte[] out = new byte[data.length];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int src = (y * width + x) * 3;
                int dst = (y * width + (width - 1 - x)) * 3;
                out[dst] = data[src];
                out[dst + 1] = data[src + 1];
                out[dst + 2] = data[src + 2];
            }
        }
        return new RGBImage(width, height, out);
    }

    /**
     * L2 归一化（原地）。
     *
     * @param vector 向量
     * @return 归一化后的同一数组
     */
    static float[] l2Normalize(float[] vector) {
        double norm = 0;
        for (float value : vector) {
            norm += (double) value * value;
        }
        if (norm <= 1e-12) {
            return vector;
        }
        float inv = (float) (1.0 / Math.sqrt(norm));
        for (int i = 0; i < vector.length; i++) {
            vector[i] *= inv;
        }
        return vector;
    }
}
