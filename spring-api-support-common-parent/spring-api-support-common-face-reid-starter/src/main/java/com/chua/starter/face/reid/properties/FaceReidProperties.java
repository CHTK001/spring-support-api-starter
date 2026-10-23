package com.chua.starter.face.reid.properties;

import com.chua.starter.face.reid.reid.SimilarityMetric;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 人脸重识别(Re-ID)配置。
 *
 * <p>前缀 {@code spring.face.reid}。描述「特征库 + 1:N 跨镜检索」的行为：
 * 特征库容量与维度、相似度度量与判定阈值、跨帧聚类窗口、各摄像头客流区域的
 * 合并策略，以及检测引擎的启用与参数。</p>
 *
 * <p>本类只承载<b>出厂默认值</b>；运行期以配置数据库（{@code sys_setting}，
 * group={@code faceReid}）为准，由 {@code FaceReidSettingService} 解析后
 * 组装成 {@link FaceReidConfig} 下发给服务，见该类的字段说明。</p>
 *
 * @author CH
 * @since 4.0.0.43
 */
@Data
@ConfigurationProperties(prefix = FaceReidProperties.PRE, ignoreInvalidFields = true)
public class FaceReidProperties {

    /**
     * 配置前缀。
     */
    public static final String PRE = "spring.face.reid";

    /**
     * 是否启用人脸重识别模块。
     */
    private boolean enabled = true;

    /**
     * 检测/特征提取引擎：{@code local} 本地实现 / {@code none} 关闭（仅保留特征库能力）。
     *
     * <p>实现类由 SPI 注册，取第一个 {@code supports(engine)} 为真的实现；
     * 均不匹配时降级为 no-op，模块仍可启动，只是识别接口返回「引擎不可用」。</p>
     */
    private String engine = "local";

    /**
     * 特征维度；0 表示由引擎决定（引擎未就绪时按 128 处理）。
     */
    private int dimension = 0;

    /**
     * 特征库最大容量（人脸条数），超出按最久未出现淘汰。
     */
    private int maxFaces = 20000;

    /**
     * 相似度度量；默认余弦（L2 归一化后与内积等价）。
     */
    private SimilarityMetric metric = SimilarityMetric.COSINE;

    /**
     * 判定为同一人的阈值（越大越严格）。
     *
     * <p><b>该阈值与所用引擎强相关，必须按引擎标定：</b></p>
     * <ul>
     *   <li>深度模型（ArcFace / FaceNet 等，经 SPI 接入）：同一人通常 ≥ 0.70，
     *       不同人通常 ≤ 0.45，0.55~0.60 是常用工作点；</li>
     *   <li>内置 {@code local} 引擎：手工特征，余弦值被压缩在很窄的区间
     *       （实测同人 ≈ 0.999+，不同人 ≈ 0.97~0.99），需取 0.995 以上的高阈值。</li>
     * </ul>
     *
     * <p>接入新引擎时请用真实样本跑一遍 {@code /v2/face/reid/search}，
     * 观察同人/不同人得分分布后再定值，切勿直接套用本文档的示例数字。</p>
     */
    private double threshold = 0.58;

    /**
     * 1:N 检索返回的最大候选数（含未达阈值的结果，便于人工复核）。
     */
    private int topK = 10;

    /**
     * 查询时是否包含已合并到主轨迹的重复人脸记录。
     */
    private boolean includeMerged = false;

    /**
     * 跨帧聚类：同一轨迹两次出现被间隙超过该秒数视为「消失」，下一帧匹配不到即结案。
     */
    private int trackGapSeconds = 30;

    /**
     * 跨帧聚类：同一轨迹连续丢弃该次数后强制结案，避免长期占用活动轨迹槽位。
     */
    private int trackMaxMisses = 5;

    /**
     * 跨帧聚类：已结案轨迹在特征库中保留的秒数，0 表示永久保留。
     */
    private int trackTtlSeconds = 0;

    /**
     * 单张图片最多处理的人脸数，防止大图放大后误检风暴。
     */
    private int maxFacesPerImage = 20;

    /**
     * 单张图片最大像素（宽 × 高），超出按比例缩小后再检测。
     */
    private long maxImagePixels = 20_000_000L;

    /**
     * 单帧图片默认属于哪个摄像头（多帧注册时可显式指定）。
     */
    private String defaultCameraId = "";

    /**
     * 运行期变换参数（配置数据库改这里即可生效，无需改 application 配置）。
     *
     * @param enabled         是否启用
     * @param engine          引擎名称
     * @param dimension       特征维度，0 表示由引擎决定
     * @param maxFaces        特征库容量
     * @param metric          相似度度量
     * @param threshold       同人判定阈值
     * @param topK            检索返回上限
     * @param includeMerged   查询是否包含已合并副本
     * @param trackGapSeconds 轨迹消失间隙（秒）
     * @param trackMaxMisses  轨迹强制结案丢失次数
     * @param trackTtlSeconds 轨迹保留时长（秒），0 表示永久
     * @param maxFacesPerImage 单图最大人脸数
     */
    public record Snapshot(boolean enabled,
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
         * 由配置对象的出厂默认值生成快照。
         *
         * @param properties 配置对象
         * @return 快照
         */
        public static Snapshot of(FaceReidProperties properties) {
            return new Snapshot(properties.isEnabled(),
                    properties.getEngine(),
                    properties.getDimension(),
                    properties.getMaxFaces(),
                    properties.getMetric(),
                    properties.getThreshold(),
                    properties.getTopK(),
                    properties.isIncludeMerged(),
                    properties.getTrackGapSeconds(),
                    properties.getTrackMaxMisses(),
                    properties.getTrackTtlSeconds(),
                    properties.getMaxFacesPerImage());
        }
    }

    /**
     * 检测引擎参数（本地实现的阈值，单位随参数含义）。
     *
     * @param minFaceSize        最小人脸边长（像素）
     * @param scoreThreshold     检测置信度阈值
     * @param nmsThreshold       重叠框抑制阈值
     * @param minQuality         入库最小质量（0~1）
     * @param blurThreshold      模糊度下限（归一化后越小越清晰）
     * @param normalizeBrightness 是否做亮度归一化
     * @param flipAugment        提取时是否用镜像特征平均以增强左右脸一致性
     * @param allowGrayInput     是否允许灰度输入（否则仅接受彩色）
     * @param allowedImageSuffix 允许的图片扩展名（小写，含点）
     */
    public record DetectorOptions(double minFaceSize,
                                  double scoreThreshold,
                                  double nmsThreshold,
                                  double minQuality,
                                  double blurThreshold,
                                  boolean normalizeBrightness,
                                  boolean flipAugment,
                                  boolean allowGrayInput,
                                  List<String> allowedImageSuffix) {

        /**
         * 缺省引擎参数。
         *
         * @return 参数
         */
        public static DetectorOptions defaults() {
            return new DetectorOptions(20.0, 0.65, 0.45, 0.35, 0.45, true, false, true,
                    new ArrayList<>(List.of(".jpg", ".jpeg", ".png", ".bmp", ".webp")));
        }
    }
}
