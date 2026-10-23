package com.chua.starter.face.reid.reid;

import com.chua.starter.face.reid.properties.FaceReidProperties;

/**
 * 相似度度量。
 *
 * <p>特征统一 L2 归一化后，余弦相似度与内积等价；欧氏距离与余弦存在单调关系
 * （{@code d² = 2 - 2·cos}），因此 {@code score} 一律换算为「越大越相似」的
 * 统一口径，阈值判定与排序无需分支。</p>
 *
 * @author CH
 * @since 4.0.0.43
 */
public enum SimilarityMetric {

    /**
     * 余弦相似度：取值 [-1, 1]，人脸特征通常落在 [0, 1]。
     */
    COSINE {
        @Override
        public double score(float[] a, float[] b) {
            double dot = 0;
            double na = 0;
            double nb = 0;
            for (int i = 0; i < a.length; i++) {
                dot += (double) a[i] * b[i];
                na += (double) a[i] * a[i];
                nb += (double) b[i] * b[i];
            }
            if (na <= 0 || nb <= 0) {
                return 0;
            }
            return dot / (Math.sqrt(na) * Math.sqrt(nb));
        }

        @Override
        public double thresholdToDistance(double threshold) {
            // cos = t 时欧氏距离的平方
            return Math.sqrt(Math.max(0, 2 - 2 * threshold));
        }
    },

    /**
     * 欧氏距离：换算为 {@code 1 / (1 + d)} 后作为得分，保持「越大越相似」。
     */
    EUCLIDEAN {
        @Override
        public double score(float[] a, float[] b) {
            double sum = 0;
            for (int i = 0; i < a.length; i++) {
                double d = (double) a[i] - b[i];
                sum += d * d;
            }
            return 1.0 / (1.0 + Math.sqrt(sum));
        }

        @Override
        public double thresholdToDistance(double threshold) {
            // score = 1/(1+d) = t  =>  d = 1/t - 1
            if (threshold <= 0) {
                return Double.MAX_VALUE;
            }
            return Math.max(0, 1.0 / threshold - 1.0);
        }
    };

    /**
     * 计算相似度得分（越大越相似，理论上限 1）。
     *
     * @param a 特征 A
     * @param b 特征 B
     * @return 得分
     */
    public abstract double score(float[] a, float[] b);

    /**
     * 把阈值换算为欧氏空间下的最大距离，供 `search` 做预筛上界。
     *
     * @param threshold 阈值
     * @return 距离上界
     */
    public abstract double thresholdToDistance(double threshold);

    /**
     * 按名称解析度量，非法或空值回退余弦。
     *
     * @param name 名称
     * @return 度量
     */
    public static SimilarityMetric parse(String name) {
        if (name == null || name.isBlank()) {
            return COSINE;
        }
        try {
            return valueOf(name.trim().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return COSINE;
        }
    }

    /**
     * 从配置快照解析度量。
     *
     * @param snapshot 配置快照
     * @return 度量
     */
    public static SimilarityMetric from(FaceReidProperties.Snapshot snapshot) {
        return snapshot == null || snapshot.metric() == null ? COSINE : snapshot.metric();
    }
}
