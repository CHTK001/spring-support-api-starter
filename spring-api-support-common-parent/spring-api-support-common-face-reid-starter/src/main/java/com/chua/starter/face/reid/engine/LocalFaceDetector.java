package com.chua.starter.face.reid.engine;

import com.chua.starter.face.reid.properties.FaceReidProperties;
import com.chua.starter.face.reid.spi.FaceBox;
import com.chua.starter.face.reid.spi.FaceDetection;
import com.chua.starter.face.reid.spi.FaceDetector;

import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * 本地人脸检测/特征提取引擎（纯 Java，无原生依赖）。
 *
 * <p>整体分三段，彼此独立、可单独测试：</p>
 * <ol>
 *   <li><b>肤色连通域初筛</b>：用 YCbCr 肤色判据 + 8 邻域连通域标记找出候选区域，
 *       按「近似正方形、面积占比合理、内部无大面积空洞」过滤；</li>
 *   <li><b>非极大值抑制</b>：重叠框保留质量更高者，避免同一张脸重复计数；</li>
 *   <li><b>外观描述子</b>：把人脸区域切成 4×4 网格，每格统计颜色矩 + 梯度方向直方图
 *       （HOG 式），拼成维度固定的向量并 L2 归一化。</li>
 * </ol>
 *
 * <h3>能力边界（务必了解）</h3>
 * <p>这是一个<b>确定性、可复现、零依赖</b>的参考实现，用于打通 Re-ID 全链路
 * （入库 → 检索 → 聚类 → 轨迹），并作为 SPI 的可验证样例。它不是深度学习人脸模型：
 * 对光照、侧脸、遮挡的鲁棒性远不及 ArcFace / FaceNet 一类模型，跨摄像头同人判定的
 * 准确率有限。<b>生产环境请替换为深度学习引擎</b>——实现 {@link FaceDetector}
 * 并注册即可，上层特征库与检索逻辑无需改动。</p>
 *
 * @author CH
 * @since 4.0.0.43
 */
public class LocalFaceDetector implements FaceDetector {

    /**
     * 引擎名。
     */
    public static final String ENGINE_NAME = "local";

    /**
     * 描述子维度：4×4 网格 × (6 颜色矩 + 8 梯度方向)。
     */
    public static final int DIMENSION = LocalFaceDescriptor.DIMENSION;

    /**
     * 连通域最小面积（像素），过滤噪点。
     */
    private static final int MIN_COMPONENT_AREA = 64;

    /**
     * 引擎参数。
     */
    private final FaceReidProperties.DetectorOptions options;

    /**
     * 解码器。
     */
    private final ImageDecoder decoder;

    /**
     * 单图最大人脸数。
     */
    private final int maxFacesPerImage;

    /**
     * 构造本地引擎。
     *
     * @param options          引擎参数
     * @param maxImagePixels   单图最大像素
     * @param maxFacesPerImage 单图最大人脸数
     */
    public LocalFaceDetector(FaceReidProperties.DetectorOptions options, long maxImagePixels, int maxFacesPerImage) {
        this.options = RGBImage.validate(options);
        this.decoder = new ImageDecoder(this.options, maxImagePixels);
        this.maxFacesPerImage = Math.max(1, maxFacesPerImage);
    }

    @Override
    public String name() {
        return ENGINE_NAME;
    }

    @Override
    public boolean ready() {
        return true;
    }

    @Override
    public int dimension() {
        return DIMENSION;
    }

    @Override
    public DetectionResult detect(byte[] imageBytes, String sourceName) {
        ImageDecoder.Decoded decoded = decoder.decode(imageBytes, sourceName);
        if (decoded.image() == null) {
            return DetectionResult.unsupported(decoded.reason());
        }
        RGBImage image = decoded.image();
        List<FaceBox> candidates = nms(components(image), options.nmsThreshold());
        List<FaceDetection> faces = new ArrayList<>();
        for (FaceBox box : candidates) {
            if (box.quality() < options.minQuality()) {
                continue;
            }
            float[] embedding = LocalFaceDescriptor.describe(image, box, options.flipAugment());
            if (embedding == null) {
                continue;
            }
            faces.add(new FaceDetection(box, embedding));
            if (faces.size() >= maxFacesPerImage) {
                break;
            }
        }
        faces.sort((a, b) -> Double.compare(b.box().quality(), a.box().quality()));
        return DetectionResult.of(faces);
    }

    /**
     * 连通域检测：返回候选人脸框（未做 NMS）。
     *
     * @param image RGB 图像
     * @return 候选框
     */
    List<FaceBox> components(RGBImage image) {
        int width = image.width();
        int height = image.height();
        int total = width * height;
        boolean[] skin = new boolean[total];
        byte[] data = image.data();
        for (int i = 0, p = 0; i < total; i++, p += 3) {
            int r = data[p] & 0xFF;
            int g = data[p + 1] & 0xFF;
            int b = data[p + 2] & 0xFF;
            skin[i] = isSkin(r, g, b);
        }
        boolean[] visited = new boolean[total];
        List<FaceBox> boxes = new ArrayList<>();
        Deque<Integer> stack = new ArrayDeque<>();
        int imagePixels = width * height;
        double minSide = options.minFaceSize();
        for (int start = 0; start < total; start++) {
            if (!skin[start] || visited[start]) {
                continue;
            }
            int area = 0;
            int sumX = 0;
            int sumY = 0;
            int minX = width;
            int minY = height;
            int maxX = -1;
            int maxY = -1;
            visited[start] = true;
            stack.push(start);
            while (!stack.isEmpty()) {
                int index = stack.pop();
                int x = index % width;
                int y = index / width;
                area++;
                sumX += x;
                sumY += y;
                if (x < minX) {
                    minX = x;
                }
                if (x > maxX) {
                    maxX = x;
                }
                if (y < minY) {
                    minY = y;
                }
                if (y > maxY) {
                    maxY = y;
                }
                for (int dy = -1; dy <= 1; dy++) {
                    int ny = y + dy;
                    if (ny < 0 || ny >= height) {
                        continue;
                    }
                    for (int dx = -1; dx <= 1; dx++) {
                        int nx = x + dx;
                        if (nx < 0 || nx >= width) {
                            continue;
                        }
                        int next = ny * width + nx;
                        if (skin[next] && !visited[next]) {
                            visited[next] = true;
                            stack.push(next);
                        }
                    }
                }
            }
            FaceBox box = evaluate(minX, minY, maxX, maxY, area, imagePixels, minSide);
            if (box != null) {
                boxes.add(box);
            }
        }
        return boxes;
    }

    /**
     * 由连通域外接框评估质量并做形状过滤。
     *
     * @param minX        左
     * @param minY        上
     * @param maxX        右
     * @param maxY        下
     * @param area        连通域像素数
     * @param imagePixels 全图像素数
     * @param minSide     最小边长
     * @return 通过过滤的框；否则 null
     */
    private FaceBox evaluate(int minX, int minY, int maxX, int maxY, int area, int imagePixels, double minSide) {
        int boxWidth = maxX - minX + 1;
        int boxHeight = maxY - minY + 1;
        if (boxWidth < minSide || boxHeight < minSide || area < MIN_COMPONENT_AREA) {
            return null;
        }
        // 人脸近似方形：宽高比落在 [0.6, 1.7]；过长多半是人手/木纹/肤色墙面
        double aspect = (double) boxWidth / boxHeight;
        if (aspect < 0.6 || aspect > 1.7) {
            return null;
        }
        double fill = (double) area / ((double) boxWidth * boxHeight);
        // 实心度：人脸区域内部大部分是肤色，过低说明是空洞很多的背景
        if (fill < 0.45) {
            return null;
        }
        double areaRatio = (double) area / imagePixels;
        // 面部通常只占画面一小部分；占比过大说明整幅图偏肤色
        if (areaRatio > 0.6) {
            return null;
        }
        double sideQuality = Math.min(1.0, Math.min(boxWidth, boxHeight) / (minSide * 4));
        double score = 0.5 + 0.5 * Math.min(1.0, fill);
        double quality = Math.min(1.0, 0.4 * sideQuality + 0.6 * fill);
        return new FaceBox(minX, minY, boxWidth, boxHeight, score, quality);
    }

    /**
     * 非极大值抑制。
     *
     * @param boxes     候选框
     * @param threshold 抑制阈值
     * @return 保留的框
     */
    static List<FaceBox> nms(List<FaceBox> boxes, double threshold) {
        List<FaceBox> sorted = new ArrayList<>(boxes);
        sorted.sort((a, b) -> Double.compare(b.quality(), a.quality()));
        List<FaceBox> kept = new ArrayList<>();
        for (FaceBox candidate : sorted) {
            boolean suppressed = false;
            for (FaceBox keep : kept) {
                if (candidate.iou(keep) > threshold) {
                    suppressed = true;
                    break;
                }
            }
            if (!suppressed) {
                kept.add(candidate);
            }
        }
        return kept;
    }

    /**
     * YCbCr 肤色判据（同时约束 RGB 通道关系，减少暖色背景误检）。
     *
     * @param r 红
     * @param g 绿
     * @param b 蓝
     * @return 肤色返回 true
     */
    static boolean isSkin(int r, int g, int b) {
        int cb = 128 - (int) (0.168736 * r + 0.331264 * g - 0.5 * b);
        int cr = 128 + (int) (0.5 * r - 0.418688 * g - 0.081312 * b);
        boolean ycbcr = cr >= 133 && cr <= 173 && cb >= 77 && cb <= 127;
        boolean rgbRule = r > 95 && g > 40 && b > 20
                && Math.max(r, Math.max(g, b)) - Math.min(r, Math.min(g, b)) > 15
                && Math.abs(r - g) > 15 && r > g && r > b;
        return ycbcr && rgbRule;
    }
}
