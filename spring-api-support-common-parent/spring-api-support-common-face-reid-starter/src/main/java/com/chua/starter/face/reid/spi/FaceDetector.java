package com.chua.starter.face.reid.spi;

/**
 * 人脸检测与特征提取引擎 SPI。
 *
 * <p>重识别（Re-ID）的核心是「同一张人脸在不同摄像头/不同帧之间具有相近的特征向量」。
 * 本接口只负责最底层的一步：<b>输入一张图片字节流，输出若干人脸及其归一化特征</b>；
 * 特征库、相似度检索、跨帧聚类等重识别逻辑全部在 {@code reid} 包内实现，
 * 与具体引擎解耦，便于后续替换为 OpenCV / ONNX Runtime / 云厂商 API。</p>
 *
 * <p>实现约定：</p>
 * <ul>
 *   <li>返回的特征<b>必须</b>已 L2 归一化，且同一实现的不同实例、不同调用之间维度一致；</li>
 *   <li>输入非法（非图片、尺寸过大、灰度且配置不允许）时返回 {@link DetectionResult#unsupported(String)}，
 *       而非抛异常，以免单帧坏图拖垮批量任务；</li>
 *   <li>检测到的框应通过 {@link FaceBox#quality()} 表达入库质量，低质量人脸由调用方过滤。</li>
 * </ul>
 *
 * <p>注册方式：实现类放入 {@code META-INF/faces/reid-engines.list}（每行一个全限定类名），
 * 由 {@code FaceReidAutoConfiguration} 发现并按 {@link #supports(String)} 选择；
 * 宿主也可直接注册为 Spring Bean 覆盖默认引擎。</p>
 *
 * @author CH
 * @since 4.0.0.43
 */
public interface FaceDetector {

    /**
     * 引擎名称，与配置 {@code spring.face.reid.engine} 对应。
     *
     * @return 名称，如 {@code local}
     */
    String name();

    /**
     * 是否支持指定的引擎名。
     *
     * @param engine 配置中的引擎名
     * @return 支持返回 true
     */
    default boolean supports(String engine) {
        return engine != null && name().equalsIgnoreCase(engine.trim());
    }

    /**
     * 引擎是否已就绪（例如依赖的原生库可用、模型已加载）。
     *
     * @return 就绪返回 true
     */
    default boolean ready() {
        return true;
    }

    /**
     * 特征维度。
     *
     * @return 维度
     */
    int dimension();

    /**
     * 检测并提取人脸特征。
     *
     * @param imageBytes 图片字节流（jpg/png/bmp/webp）
     * @param sourceName 来源名称（文件名或摄像头 ID），仅用于日志与错误信息，可为 null
     * @return 检测结果；永不返回 null
     */
    DetectionResult detect(byte[] imageBytes, String sourceName);

    /**
     * 检测结果。
     *
     * <p>{@code faces} 与 {@code reason} 互斥：成功时 {@code reason} 为 null，
     * 失败时 {@code faces} 为空列表。</p>
     *
     * @param faces  检出的人脸（按质量降序）
     * @param reason 失败原因（成功时为 null）
     * @author CH
     * @since 4.0.0.43
     */
    record DetectionResult(java.util.List<FaceDetection> faces, String reason) {

        /**
         * 统一为空成功结果。
         */
        private static final DetectionResult EMPTY = new DetectionResult(java.util.List.of(), null);

        /**
         * 成功但未检出人脸。
         *
         * @return 结果
         */
        public static DetectionResult empty() {
            return EMPTY;
        }

        /**
         * 成功检出人脸。
         *
         * @param faces 人脸
         * @return 结果
         */
        public static DetectionResult of(java.util.List<FaceDetection> faces) {
            return new DetectionResult(faces == null ? java.util.List.of() : java.util.List.copyOf(faces), null);
        }

        /**
         * 输入不受支持。
         *
         * @param reason 原因
         * @return 结果
         */
        public static DetectionResult unsupported(String reason) {
            return new DetectionResult(java.util.List.of(), reason == null ? "unsupported input" : reason);
        }

        /**
         * 是否成功（未报错）。
         *
         * @return 成功返回 true
         */
        public boolean ok() {
            return reason == null;
        }
    }

    /**
     * 引擎不可用时的降级实现：始终返回「引擎不可用」，模块仍可正常启动。
     *
     * <p>保留特征库的读写与检索能力（可加载历史特征做离线比对），
     * 只是不能再从图片中提取新特征。</p>
     *
     * @author CH
     * @since 4.0.0.43
     */
    final class Unavailable implements FaceDetector {

        /**
         * 不可用原因。
         */
        private final String reason;

        /**
         * 构造降级引擎。
         *
         * @param reason 不可用原因
         */
        public Unavailable(String reason) {
            this.reason = reason == null ? "人脸检测引擎不可用" : reason;
        }

        @Override
        public String name() {
            return "none";
        }

        /**
         * 不可用原因。
         *
         * @return 原因
         */
        public String reason() {
            return reason;
        }

        @Override
        public boolean supports(String engine) {
            return true;
        }

        @Override
        public boolean ready() {
            return false;
        }

        @Override
        public int dimension() {
            return 0;
        }

        @Override
        public DetectionResult detect(byte[] imageBytes, String sourceName) {
            return DetectionResult.unsupported(reason);
        }
    }
}
