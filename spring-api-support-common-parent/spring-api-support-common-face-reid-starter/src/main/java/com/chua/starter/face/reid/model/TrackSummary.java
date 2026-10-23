package com.chua.starter.face.reid.model;

import java.util.List;

/**
 * 轨迹摘要：「一个人」在特征库中的聚合视图。
 *
 * <p>Re-ID 输出的最终结果形态。一条轨迹由若干次观测（{@link FaceRecord}）组成，
 * {@code cameraIds} 的个数即「跨镜」数量——大于 1 说明同一个人被多个摄像头捕捉到，
 * 这正是重识别要证明的事情。</p>
 *
 * @param trackId          轨迹 ID
 * @param faceIds          轨迹下全部人脸记录 ID
 * @param sourceIds        轨迹下全部来源图片 ID
 * @param cameraIds        轨迹出现过的摄像头（跨镜证据）
 * @param firstSeen        首次出现时间（毫秒）
 * @param lastSeen         最近出现时间（毫秒）
 * @param observations     累计观测次数
 * @param recordCount      记录条数（含合并副本）
 * @param quality          最高质量
 * @author CH
 * @since 4.0.0.43
 */
public record TrackSummary(String trackId,
                           List<String> faceIds,
                           List<String> sourceIds,
                           List<String> cameraIds,
                           long firstSeen,
                           long lastSeen,
                           int observations,
                           int recordCount,
                           double quality) {

    /**
     * 是否跨越了多个摄像头。
     *
     * @return 摄像头数大于 1 返回 true
     */
    public boolean crossCamera() {
        return cameraIds != null && cameraIds.size() > 1;
    }
}
