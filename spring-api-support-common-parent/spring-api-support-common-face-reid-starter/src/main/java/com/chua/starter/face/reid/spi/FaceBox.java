package com.chua.starter.face.reid.spi;

/**
 * 人脸检测结果中的单个检测框。
 *
 * <p>坐标以原图像素为准，原点在左上角。评分 {@code score} 为检测置信度（0~1），
 * 质量 {@code quality} 为入库适用性（0~1，综合考虑尺寸/清晰度/姿态），
 * 只有通过质量门限的检测才会被提取特征。</p>
 *
 * @param x       左上角 X
 * @param y       左上角 Y
 * @param width   宽度
 * @param height  高度
 * @param score   检测置信度
 * @param quality 入库质量
 * @author CH
 * @since 4.0.0.43
 */
public record FaceBox(int x, int y, int width, int height, double score, double quality) {

    /**
     * 检测框面积（像素）。
     *
     * @return 面积
     */
    public long area() {
        return (long) width * height;
    }

    /**
     * 中心点 X。
     *
     * @return 中心 X
     */
    public double centerX() {
        return x + width / 2.0;
    }

    /**
     * 中心点 Y。
     *
     * @return 中心 Y
     */
    public double centerY() {
        return y + height / 2.0;
    }

    /**
     * 两个框的交并比（IoU）。
     *
     * @param other 另一个框
     * @return 交并比，范围 [0,1]
     */
    public double iou(FaceBox other) {
        if (other == null) {
            return 0;
        }
        int left = Math.max(x, other.x);
        int top = Math.max(y, other.y);
        int right = Math.min(x + width, other.x + other.width);
        int bottom = Math.min(y + height, other.y + other.height);
        if (right <= left || bottom <= top) {
            return 0;
        }
        long inter = (long) (right - left) * (bottom - top);
        long union = area() + other.area() - inter;
        return union <= 0 ? 0 : (double) inter / union;
    }
}
