package com.chua.starter.face.reid.model;

import java.util.List;

/**
 * 特征库中的人脸记录。
 *
 * <p>一条记录 = 一次「在某个摄像头某一帧中检出并入库的人脸」。Re-ID 的关键信息是：
 * 哪张图（{@code sourceId}）、哪个摄像头（{@code cameraId}）、什么时候（{@code timestamp}）、
 * 属于哪条轨迹（{@code trackId}）、是不是主记录（{@code primary}）。</p>
 *
 * <p>跨帧聚类命中已有轨迹时不会新增记录，而是把命中记录标记为 {@code observationCount++}
 * 并刷新 {@code lastSeen}；只有轨迹首次出现才新建记录。因此
 * {@code primary=true} 的记录数量即「不同的人/独立轨迹」数量，
 * 这也是 1:N 检索的候选集。</p>
 *
 * @param faceId           人脸记录 ID
 * @param trackId          所属轨迹 ID（Re-ID 结果，同一条轨迹即同一个人）
 * @param primary          是否为主记录（非合并副本）
 * @param cameraId         摄像头/来源标识
 * @param sourceId         图片来源 ID（注册时由调用方提供，便于回溯）
 * @param timestamp        入队时间戳（毫秒）
 * @param lastSeen         最近一次命中时间戳（毫秒）
 * @param observationCount 命中次数（含首次）
 * @param box              首次检出时的检测框
 * @param quality          入库质量
 * @param embedding        归一化特征
 * @author CH
 * @since 4.0.0.43
 */
public record FaceRecord(String faceId,
                         String trackId,
                         boolean primary,
                         String cameraId,
                         String sourceId,
                         long timestamp,
                         long lastSeen,
                         int observationCount,
                         com.chua.starter.face.reid.spi.FaceBox box,
                         double quality,
                         float[] embedding) {

    /**
     * 特征维度。
     *
     * @return 维度
     */
    public int dimension() {
        return embedding == null ? 0 : embedding.length;
    }

    /**
     * 生成一条新的观测记录，并入既有轨迹。
     *
     * <p>每次观测都是一条<b>独立记录</b>：它带着<b>本次</b>的摄像头、来源帧、检测框与特征，
     * 只是与首条记录共享 {@code trackId}。这样轨迹才能回答「这个人被哪些摄像头在何时拍到」；
     * 若复用首条的摄像头信息覆盖写入，跨镜证据就会丢失，Re-ID 也就失去了意义。</p>
     *
     * @param newFaceId  新记录 ID
     * @param newCameraId 本次观测的摄像头
     * @param newSourceId 本次观测的来源帧
     * @param seen       本次观测时间
     * @param newBox     本次检测框
     * @param newQuality 本次质量
     * @param newEmbedding 本次特征
     * @return 新记录
     */
    public FaceRecord observe(String newFaceId, String newCameraId, String newSourceId, long seen,
                              com.chua.starter.face.reid.spi.FaceBox newBox, double newQuality,
                              float[] newEmbedding) {
        return new FaceRecord(newFaceId, trackId, true, newCameraId, newSourceId,
                seen, seen, 1, newBox, newQuality, newEmbedding);
    }

    /**
     * 轨迹摘要：把同一轨迹下的多条记录归并为一条「人」。
     *
     * @param records 同一轨迹的记录（至少一条）
     * @return 摘要
     */
    public static TrackSummary summarize(List<FaceRecord> records) {
        if (records == null || records.isEmpty()) {
            return null;
        }
        List<String> faceIds = records.stream().map(FaceRecord::faceId).toList();
        List<String> cameras = records.stream()
                .map(FaceRecord::cameraId)
                .filter(camera -> camera != null && !camera.isBlank())
                .distinct()
                .toList();
        List<String> sources = records.stream()
                .map(FaceRecord::sourceId)
                .filter(source -> source != null && !source.isBlank())
                .distinct()
                .toList();
        long firstSeen = records.stream().mapToLong(FaceRecord::timestamp).min().orElse(0);
        long lastSeen = records.stream().mapToLong(FaceRecord::lastSeen).max().orElse(firstSeen);
        int observations = records.stream().mapToInt(FaceRecord::observationCount).sum();
        double quality = records.stream()
                .filter(FaceRecord::primary)
                .mapToDouble(FaceRecord::quality)
                .max()
                .orElseGet(() -> records.stream().mapToDouble(FaceRecord::quality).max().orElse(0));
        return new TrackSummary(records.get(0).trackId(), faceIds, sources, cameras,
                firstSeen, lastSeen, observations, records.size(), quality);
    }
}
