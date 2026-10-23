package com.chua.starter.face.reid.spi;

/**
 * 一张检出的人脸：检测框 + 归一化特征向量。
 *
 * <p>特征已 L2 归一化，长度与引擎维度一致。该对象不落盘，由
 * {@link FaceDetector#detect(byte[], String)} 生成后交给特征库。</p>
 *
 * @param box       检测框与置信度
 * @param embedding 归一化特征向量（不可变，调用方不应修改）
 * @author CH
 * @since 4.0.0.43
 */
public record FaceDetection(FaceBox box, float[] embedding) {

    /**
     * 拷贝构造，切断外部对内部数组的引用。
     *
     * @param box       检测框
     * @param embedding 特征
     */
    public FaceDetection {
        embedding = embedding == null ? null : embedding.clone();
    }

    /**
     * 取出特征副本。
     *
     * @return 特征副本
     */
    @Override
    public float[] embedding() {
        return embedding == null ? null : embedding.clone();
    }

    /**
     * 特征维度。
     *
     * @return 维度，无特征时为 0
     */
    public int dimension() {
        return embedding == null ? 0 : embedding.length;
    }
}
