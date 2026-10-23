package com.chua.starter.face.reid.reid;

import com.chua.starter.face.reid.model.FaceRecord;
import com.chua.starter.face.reid.model.TrackSummary;
import com.chua.starter.face.reid.properties.FaceReidConfig;
import com.chua.starter.face.reid.spi.FaceBox;
import com.chua.starter.face.reid.spi.FaceDetection;
import com.chua.starter.face.reid.spi.FaceDetector;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 人脸重识别服务：把「一张图」变成「这个人在哪出现过」。
 *
 * <p>这是重识别（Re-ID）的业务入口，串联三件事：</p>
 * <ol>
 *   <li><b>检测</b>：调用 {@link FaceDetector} 从图片中检出人脸并提取特征；</li>
 *   <li><b>入库与归并</b>：把特征交给 {@link FaceFeatureLibrary}，由它判断
 *       「是记录里的某个人」还是「新人」，并维护跨帧轨迹；</li>
 *   <li><b>检索</b>：以图搜人（1:N），返回命中的轨迹及其出现过的所有摄像头。</li>
 * </ol>
 *
 * <p>典型调用序列——同一个人走过 A、B 两个摄像头：</p>
 * <pre>{@code
 * register("cam-A", "frame-001", bytesA);  // 新建轨迹 T1
 * register("cam-B", "frame-002", bytesB);  // 命中 T1，不新建人
 * TrackSummary t = track("T1");            // cameraIds = [cam-A, cam-B] → 跨镜成功
 * }</pre>
 *
 * @author CH
 * @since 4.0.0.43
 */
@Slf4j
public class FaceReidService {

    /**
     * 检测引擎。
     */
    private final FaceDetector detector;

    /**
     * 特征库。
     */
    private final FaceFeatureLibrary library;

    /**
     * 默认摄像头标识。
     */
    private volatile String defaultCamera;

    /**
     * 单图最大人脸数。
     */
    private volatile int maxFacesPerImage;

    /**
     * 构造服务。
     *
     * @param detector          检测引擎
     * @param library           特征库
     * @param defaultCamera     默认摄像头
     * @param maxFacesPerImage  单图最大人脸数
     */
    public FaceReidService(FaceDetector detector, FaceFeatureLibrary library,
                           String defaultCamera, int maxFacesPerImage) {
        this.detector = detector;
        this.library = library;
        this.defaultCamera = defaultCamera == null ? "" : defaultCamera;
        this.maxFacesPerImage = Math.max(1, maxFacesPerImage);
    }

    /**
     * 应用生效配置（热更新入口）。
     *
     * @param config 生效配置
     */
    public void apply(FaceReidConfig config) {
        if (config == null) {
            return;
        }
        this.maxFacesPerImage = Math.max(1, config.maxFacesPerImage());
        library.updateConfig(config);
    }

    /**
     * 检测引擎。
     *
     * @return 引擎
     */
    public FaceDetector detector() {
        return detector;
    }

    /**
     * 特征库。
     *
     * @return 特征库
     */
    public FaceFeatureLibrary library() {
        return library;
    }

    /**
     * 模块是否可用（启用且引擎就绪）。
     *
     * @return 可用返回 true
     */
    public boolean available() {
        return engineReady() && library.config().enabled();
    }

    /**
     * 引擎是否就绪。
     *
     * @return 就绪返回 true
     */
    public boolean engineReady() {
        return detector != null && detector.ready();
    }

    /**
     * 引擎不可用原因。
     *
     * <p>优先返回检测器自己给出的具体原因（例如「未找到 ArcFace 模型」），
     * 只有检测器没有说明时才回退到通用文案——排查时具体原因比「不可用」有用得多。</p>
     *
     * @return 原因，可用时为 null
     */
    public String unavailableReason() {
        if (detector == null) {
            return "人脸检测引擎未装配";
        }
        if (!detector.ready()) {
            String reason = detector instanceof FaceDetector.Unavailable unavailable
                    ? unavailable.reason()
                    : null;
            return reason == null || reason.isBlank() ? "人脸检测引擎不可用" : reason;
        }
        if (!library.config().enabled()) {
            return "人脸重识别模块未启用（spring.face.reid.enabled=false）";
        }
        return null;
    }

    /**
     * 注册（检测 + 入库 + 归并）一张图片中的全部人脸。
     *
     * @param cameraId   摄像头 ID，空则使用默认值
     * @param sourceId   来源图片 ID，空则自动生成
     * @param imageBytes 图片字节
     * @return 注册结果
     */
    public RegisterResult register(String cameraId, String sourceId, byte[] imageBytes) {
        String reason = unavailableReason();
        if (reason != null) {
            return RegisterResult.fail(reason);
        }
        String camera = cameraId == null || cameraId.isBlank() ? defaultCamera : cameraId.trim();
        String source = sourceId == null || sourceId.isBlank()
                ? "S" + UUID.randomUUID().toString().replace("-", "").substring(0, 16) : sourceId.trim();
        FaceDetector.DetectionResult detection = detector.detect(imageBytes, source);
        if (!detection.ok()) {
            return RegisterResult.fail(detection.reason());
        }
        List<FaceDetection> faces = detection.faces();
        if (faces.size() > maxFacesPerImage) {
            faces = faces.subList(0, maxFacesPerImage);
        }
        List<Enrolled> enrolled = new ArrayList<>();
        int created = 0;
        for (FaceDetection face : faces) {
            try {
                FaceFeatureLibrary.EnrollResult result = library.enroll(camera, source,
                        face.box(), face.box().quality(), face.embedding());
                if (result.created()) {
                    created++;
                }
                enrolled.add(new Enrolled(result.record(), result.created(), face.box()));
            } catch (Exception e) {
                // 单张人脸入库失败不影响同图其它人脸
                log.warn("[FaceReid] 人脸入库失败: source={}, err={}", source, e.getMessage());
            }
        }
        return RegisterResult.ok(camera, source, enrolled, created);
    }

    /**
     * 以图搜人（1:N 重识别）。
     *
     * @param imageBytes  查询图片
     * @param topK        返回上限，≤0 用配置值
     * @param matchedOnly 是否只返回达阈值结果
     * @return 检索结果
     */
    public IdentifyResult identify(byte[] imageBytes, int topK, boolean matchedOnly) {
        String reason = unavailableReason();
        if (reason != null) {
            return IdentifyResult.fail(reason);
        }
        FaceDetector.DetectionResult detection = detector.detect(imageBytes, "query");
        if (!detection.ok()) {
            return IdentifyResult.fail(detection.reason());
        }
        List<FaceIdentification> identifications = new ArrayList<>();
        for (FaceDetection face : detection.faces()) {
            List<FaceFeatureLibrary.SearchHit> hits = library.search(face.embedding(), topK, matchedOnly);
            List<Hit> mapped = new ArrayList<>();
            for (FaceFeatureLibrary.SearchHit hit : hits) {
                TrackSummary track = library.track(hit.record().trackId());
                mapped.add(new Hit(hit.record(), hit.score(), hit.matched(), track));
            }
            identifications.add(new FaceIdentification(face.box(), mapped));
        }
        return IdentifyResult.ok(identifications);
    }

    /**
     * 直接用特征检索（离线比对、批量任务用）。
     *
     * @param embedding   特征
     * @param topK        返回上限
     * @param matchedOnly 是否只返回达阈值结果
     * @return 候选
     */
    public List<FaceFeatureLibrary.SearchHit> search(float[] embedding, int topK, boolean matchedOnly) {
        return library.search(embedding, topK, matchedOnly);
    }

    /**
     * 读取轨迹。
     *
     * @param trackId 轨迹 ID
     * @return 轨迹摘要，不存在返回 null
     */
    public TrackSummary track(String trackId) {
        return library.track(trackId);
    }

    /**
     * 列出轨迹。
     *
     * @param cameraId 摄像头过滤（null 不限）
     * @param crossOnly 是否只返回跨镜轨迹
     * @param limit    上限
     * @return 轨迹列表
     */
    public List<TrackSummary> tracks(String cameraId, boolean crossOnly, int limit) {
        return library.tracks(cameraId, crossOnly, limit);
    }

    /**
     * 统计信息。
     *
     * @return 统计
     */
    public Map<String, Object> stats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        FaceReidConfig config = library.config();
        stats.put("enabled", config.enabled());
        stats.put("available", available());
        stats.put("engine", detector == null ? "none" : detector.name());
        stats.put("engineReady", engineReady());
        stats.put("dimension", detector == null ? 0 : detector.dimension());
        stats.put("faces", library.size());
        stats.put("tracks", library.trackCount());
        stats.put("threshold", config.threshold());
        stats.put("metric", config.metric().name());
        stats.put("maxFaces", config.maxFaces());
        stats.put("topK", config.topK());
        return stats;
    }

    /**
     * 清空特征库。
     *
     * @return 清空的记录数
     */
    public int clear() {
        return library.clear();
    }

    /**
     * 设置默认摄像头。
     *
     * @param camera 摄像头
     */
    public void setDefaultCamera(String camera) {
        this.defaultCamera = camera == null ? "" : camera;
    }

    /**
     * 单次注册出的人脸。
     *
     * @param record  人脸记录
     * @param created 是否新建轨迹（true=新人）
     * @param box     检测框
     * @author CH
     * @since 4.0.0.43
     */
    public record Enrolled(FaceRecord record, boolean created, FaceBox box) {

        /**
         * 轨迹 ID。
         *
         * @return 轨迹 ID
         */
        public String trackId() {
            return record.trackId();
        }
    }

    /**
     * 注册结果。
     *
     * @param ok       是否成功
     * @param reason   失败原因
     * @param cameraId 摄像头
     * @param sourceId 来源图片 ID
     * @param faces    入库人脸
     * @param created  新建轨迹数
     * @author CH
     * @since 4.0.0.43
     */
    public record RegisterResult(boolean ok, String reason, String cameraId, String sourceId,
                                 List<Enrolled> faces, int created) {

        /**
         * 成功。
         *
         * @param cameraId 摄像头
         * @param sourceId 来源
         * @param faces    人脸
         * @param created  新建数
         * @return 结果
         */
        public static RegisterResult ok(String cameraId, String sourceId, List<Enrolled> faces, int created) {
            return new RegisterResult(true, null, cameraId, sourceId, List.copyOf(faces), created);
        }

        /**
         * 失败。
         *
         * @param reason 原因
         * @return 结果
         */
        public static RegisterResult fail(String reason) {
            return new RegisterResult(false, reason, null, null, List.of(), 0);
        }
    }

    /**
     * 单张人脸的检索命中。
     *
     * @param record 命中记录
     * @param score  相似度
     * @param matched 是否达阈值
     * @param track  命中记录所属轨迹
     * @author CH
     * @since 4.0.0.43
     */
    public record Hit(FaceRecord record, double score, boolean matched, TrackSummary track) {
    }

    /**
     * 单张查询人脸的识别结果。
     *
     * @param box  查询人脸的检测框
     * @param hits 命中候选（降序）
     * @author CH
     * @since 4.0.0.43
     */
    public record FaceIdentification(FaceBox box, List<Hit> hits) {

        /**
         * 最佳命中（最高分，不保证达阈值）。
         *
         * @return 命中，无候选返回 null
         */
        public Hit best() {
            return hits == null || hits.isEmpty() ? null : hits.get(0);
        }

        /**
         * 是否已经认出一个人（最佳命中达阈值）。
         *
         * @return 认出返回 true
         */
        public boolean identified() {
            Hit best = best();
            return best != null && best.matched();
        }
    }

    /**
     * 识别结果。
     *
     * @param ok   是否成功
     * @param reason 失败原因
     * @param faces 每张查询人脸的结果
     * @author CH
     * @since 4.0.0.43
     */
    public record IdentifyResult(boolean ok, String reason, List<FaceIdentification> faces) {

        /**
         * 成功。
         *
         * @param faces 结果
         * @return 结果
         */
        public static IdentifyResult ok(List<FaceIdentification> faces) {
            return new IdentifyResult(true, null, List.copyOf(faces));
        }

        /**
         * 失败。
         *
         * @param reason 原因
         * @return 结果
         */
        public static IdentifyResult fail(String reason) {
            return new IdentifyResult(false, reason, List.of());
        }
    }
}
