package com.chua.starter.face.reid.properties;

import com.chua.starter.face.reid.reid.SimilarityMetric;

/**
 * 运行期生效的人脸重识别配置（不可变快照）。
 *
 * <p>由 {@link FaceReidProperties} 的出厂默认值 + 配置数据库（{@code sys_setting}）
 * 中的覆盖项组装而成，交给特征库与识别服务使用。之所以做成不可变对象而非直接
 * 读写 {@link FaceReidProperties}：阈值/容量变更要能「一次原子切换」，避免并发
 * 请求读到一半旧一半新的配置。</p>
 *
 * @param enabled          是否启用
 * @param engine           引擎名称（SPI 选择依据）
 * @param dimension        特征维度
 * @param maxFaces         特征库容量
 * @param metric           相似度度量
 * @param threshold        同人判定阈值
 * @param topK             检索返回上限
 * @param includeMerged    查询是否包含已合并副本
 * @param trackGapSeconds  轨迹消失间隙（秒）
 * @param trackMaxMisses   轨迹强制结案丢失次数
 * @param trackTtlSeconds  轨迹保留时长（秒），0 表示永久
 * @param maxFacesPerImage 单图最大人脸数
 * @author CH
 * @since 4.0.0.43
 */
public record FaceReidConfig(boolean enabled,
                             String engine,
                             int dimension,
                             int maxFaces,
                             SimilarityMetric metric,
                             double threshold,
                             int topK,
                             boolean includeMerged,
                             int trackGapSeconds,
                             int trackMaxMisses,
                             int trackTtlSeconds,
                             int maxFacesPerImage) {

    /**
     * 引擎未就绪时的兜底维度。
     */
    public static final int FALLBACK_DIMENSION = 128;

    /**
     * 规范化：把越界值收敛到安全区间。
     *
     * @return 规范化后的配置
     */
    public FaceReidConfig normalized() {
        int dim = dimension <= 0 ? FALLBACK_DIMENSION : dimension;
        int cap = maxFaces <= 0 ? 20000 : maxFaces;
        int k = topK <= 0 ? 10 : Math.min(topK, 1000);
        double t = threshold;
        if (metric == SimilarityMetric.EUCLIDEAN) {
            // 欧氏度量下得分 1/(1+d) 落在 (0,1]，阈值同样收敛到 (0,1]
            t = t <= 0 || t > 1 ? 0.5 : t;
        } else {
            t = t < -1 ? -1 : Math.min(t, 1);
        }
        return new FaceReidConfig(enabled,
                engine == null || engine.isBlank() ? "local" : engine.trim(),
                dim,
                cap,
                metric == null ? SimilarityMetric.COSINE : metric,
                t,
                k,
                includeMerged,
                Math.max(1, trackGapSeconds),
                Math.max(1, trackMaxMisses),
                Math.max(0, trackTtlSeconds),
                Math.max(1, maxFacesPerImage));
    }

    /**
     * 由出厂配置构建。
     *
     * @param properties 出厂配置
     * @return 生效配置
     */
    public static FaceReidConfig of(FaceReidProperties properties) {
        FaceReidProperties.Snapshot s = FaceReidProperties.Snapshot.of(properties);
        return new FaceReidConfig(s.enabled(), s.engine(), s.dimension(), s.maxFaces(), s.metric(),
                s.threshold(), s.topK(), s.includeMerged(), s.trackGapSeconds(),
                s.trackMaxMisses(), s.trackTtlSeconds(), s.maxFacesPerImage()).normalized();
    }
}
