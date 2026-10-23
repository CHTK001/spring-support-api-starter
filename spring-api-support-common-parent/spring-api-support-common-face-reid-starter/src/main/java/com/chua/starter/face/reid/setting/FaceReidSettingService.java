package com.chua.starter.face.reid.setting;

import com.chua.starter.face.reid.properties.FaceReidConfig;
import com.chua.starter.face.reid.properties.FaceReidProperties;
import com.chua.starter.face.reid.reid.SimilarityMetric;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 人脸重识别运行期配置服务。
 *
 * <p>配置分两层：</p>
 * <ol>
 *   <li><b>出厂值</b>：{@code application.yml} 的 {@code spring.face.reid.*}；</li>
 *   <li><b>运行期覆盖</b>：配置数据库 {@code sys_setting}（group={@value #GROUP}）。</li>
 * </ol>
 * <p>后者优先。首次启动用出厂值播种，之后以数据库为准——这样运维在管理端改阈值
 * 即可生效，不必改配置文件重启。</p>
 *
 * <p>「添加重识别配置」在代码层面的落点：{@link #DESCRIPTORS} 定义了全部可调项及其
 * 类型/说明，管理端可直接据此渲染表单项，也可通过 {@link #update(Map)} 批量提交。</p>
 *
 * @author CH
 * @since 4.0.0.43
 */
@Slf4j
public class FaceReidSettingService {

    /**
     * 配置分组。
     */
    public static final String GROUP = "faceReid";

    /**
     * 是否启用。
     */
    public static final String NAME_ENABLED = "enabled";

    /**
     * 引擎名称。
     */
    public static final String NAME_ENGINE = "engine";

    /**
     * 特征维度（0=引擎决定）。
     */
    public static final String NAME_DIMENSION = "dimension";

    /**
     * 特征库容量。
     */
    public static final String NAME_MAX_FACES = "maxFaces";

    /**
     * 相似度度量。
     */
    public static final String NAME_METRIC = "metric";

    /**
     * 同人判定阈值。
     */
    public static final String NAME_THRESHOLD = "threshold";

    /**
     * 检索返回上限。
     */
    public static final String NAME_TOP_K = "topK";

    /**
     * 查询是否包含已合并副本。
     */
    public static final String NAME_INCLUDE_MERGED = "includeMerged";

    /**
     * 轨迹消失间隙（秒）。
     */
    public static final String NAME_TRACK_GAP_SECONDS = "trackGapSeconds";

    /**
     * 轨迹强制结案丢失次数。
     */
    public static final String NAME_TRACK_MAX_MISSES = "trackMaxMisses";

    /**
     * 轨迹保留时长（秒），0=永久。
     */
    public static final String NAME_TRACK_TTL_SECONDS = "trackTtlSeconds";

    /**
     * 单图最大人脸数。
     */
    public static final String NAME_MAX_FACES_PER_IMAGE = "maxFacesPerImage";

    /**
     * 全部可配置项描述（名称 / 类型 / 说明 / 排序）。
     */
    public static final List<Descriptor> DESCRIPTORS = List.of(
            new Descriptor(NAME_ENABLED, "Bool", "是否启用人脸重识别", 1),
            new Descriptor(NAME_ENGINE, "String", "检测引擎（SPI 名称，默认 local）", 2),
            new Descriptor(NAME_DIMENSION, "Number", "特征维度，0 表示由引擎决定", 3),
            new Descriptor(NAME_MAX_FACES, "Number", "特征库最大容量（人脸条数）", 4),
            new Descriptor(NAME_METRIC, "String", "相似度度量：COSINE / EUCLIDEAN", 5),
            new Descriptor(NAME_THRESHOLD, "Number", "同人判定阈值，越大越严格（余弦 0.55~0.60）", 6),
            new Descriptor(NAME_TOP_K, "Number", "1:N 检索返回上限", 7),
            new Descriptor(NAME_INCLUDE_MERGED, "Bool", "检索是否包含已合并的重复人脸", 8),
            new Descriptor(NAME_TRACK_GAP_SECONDS, "Number", "同一轨迹两次出现的最长间隔（秒）", 9),
            new Descriptor(NAME_TRACK_MAX_MISSES, "Number", "轨迹连续丢失多少次后结案", 10),
            new Descriptor(NAME_TRACK_TTL_SECONDS, "Number", "轨迹保留时长（秒），0 表示永久", 11),
            new Descriptor(NAME_MAX_FACES_PER_IMAGE, "Number", "单张图片最多处理的人脸数", 12));

    /**
     * 配置存储。
     */
    private final FaceReidSettingStore store;

    /**
     * 出厂配置。
     */
    private final FaceReidProperties properties;

    /**
     * 变更监听（通常为特征库热更新）。
     */
    private final List<java.util.function.Consumer<FaceReidConfig>> listeners = new ArrayList<>();

    /**
     * 当前生效配置。
     */
    private volatile FaceReidConfig current;

    /**
     * 上层覆盖（例如测试或宿主强制指定）。
     */
    private final Map<String, String> overrides = new LinkedHashMap<>();

    /**
     * 构造配置服务。
     *
     * @param store      配置存储
     * @param properties 出厂配置
     */
    public FaceReidSettingService(FaceReidSettingStore store, FaceReidProperties properties) {
        this.store = store == null ? new FaceReidSettingStore.Noop() : store;
        this.properties = properties == null ? new FaceReidProperties() : properties;
        this.current = FaceReidConfig.of(this.properties);
    }

    /**
     * 存储是否可用。
     *
     * @return 可用返回 true
     */
    public boolean available() {
        return store.available();
    }

    /**
     * 注册配置变更监听。
     *
     * @param listener 监听器
     */
    public void addListener(java.util.function.Consumer<FaceReidConfig> listener) {
        if (listener != null) {
            synchronized (listeners) {
                listeners.add(listener);
            }
        }
    }

    /**
     * 当前生效配置。
     *
     * @return 配置
     */
    public FaceReidConfig config() {
        return current;
    }

    /**
     * 初始化：播种出厂值（仅缺失项）并加载生效配置。
     */
    public synchronized void initialize() {
        if (available()) {
            try {
                seedIfAbsent();
            } catch (Exception e) {
                log.warn("[FaceReid] 重识别配置播种失败，回退出厂配置: {}", e.getMessage());
            }
        }
        reload();
    }

    /**
     * 重新加载运行期配置并通知监听者。
     */
    public synchronized void reload() {
        Map<String, String> values = new LinkedHashMap<>();
        if (available()) {
            try {
                for (FaceReidSettingStore.Row row : store.list(GROUP)) {
                    if (row.name() != null && row.value() != null) {
                        values.put(row.name(), row.value());
                    }
                }
            } catch (Exception e) {
                log.warn("[FaceReid] 重识别配置加载失败，回退出厂配置: {}", e.getMessage());
            }
        }
        values.putAll(overrides);
        this.current = resolve(values).normalized();
        notifyListeners(current);
        log.info("[FaceReid] 生效配置: engine={}, threshold={}, maxFaces={}, topK={}",
                current.engine(), current.threshold(), current.maxFaces(), current.topK());
    }

    /**
     * 读取原始配置值（调试 / 前端表单回填）。
     *
     * @return 名称 → 值
     */
    public synchronized Map<String, String> raw() {
        Map<String, String> values = new LinkedHashMap<>();
        if (available()) {
            for (FaceReidSettingStore.Row row : store.list(GROUP)) {
                if (row.name() != null && row.value() != null) {
                    values.put(row.name(), row.value());
                }
            }
        }
        for (Map.Entry<String, String> entry : toMap(current).entrySet()) {
            values.putIfAbsent(entry.getKey(), entry.getValue());
        }
        values.putAll(overrides);
        return values;
    }

    /**
     * 批量更新配置：写入存储（可用时）后立即热加载。
     *
     * <p>未知键被忽略并记录日志；非法数值静默跳过（保留旧值），
     * 避免一个手误让整个识别链路停摆。</p>
     *
     * @param updates 名称 → 值
     * @return 是否发生了有效变更
     */
    public synchronized boolean update(Map<String, String> updates) {
        if (updates == null || updates.isEmpty()) {
            return false;
        }
        boolean changed = false;
        for (Descriptor descriptor : DESCRIPTORS) {
            String value = updates.get(descriptor.name());
            if (value == null) {
                continue;
            }
            if (store.available()) {
                store.upsert(GROUP, descriptor.name(), value.trim(), descriptor.type(),
                        descriptor.remark(), descriptor.sort());
            } else {
                overrides.put(descriptor.name(), value.trim());
            }
            changed = true;
        }
        if (!changed) {
            log.warn("[FaceReid] 配置更新未命中任何已知配置项: {}", updates.keySet());
            return false;
        }
        reload();
        return true;
    }

    /**
     * 重置为出厂配置（删除分组内全部覆盖项）。
     *
     * @return 是否执行成功
     */
    public synchronized boolean reset() {
        overrides.clear();
        if (!store.available()) {
            reload();
            return true;
        }
        try {
            for (Descriptor descriptor : DESCRIPTORS) {
                store.deleteByPrefix(GROUP, descriptor.name());
            }
            reload();
            return true;
        } catch (Exception e) {
            log.warn("[FaceReid] 重置配置失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 用出厂值播种缺失项。
     */
    private void seedIfAbsent() {
        Map<String, String> existing = new LinkedHashMap<>();
        for (FaceReidSettingStore.Row row : store.list(GROUP)) {
            if (row.name() != null) {
                existing.put(row.name(), row.value());
            }
        }
        for (Map.Entry<String, String> entry : toMap(FaceReidConfig.of(properties)).entrySet()) {
            if (existing.containsKey(entry.getKey())) {
                continue;
            }
            Descriptor descriptor = descriptorOf(entry.getKey());
            if (descriptor == null) {
                continue;
            }
            store.upsert(GROUP, descriptor.name(), entry.getValue(), descriptor.type(),
                    descriptor.remark(), descriptor.sort());
        }
    }

    /**
     * 按名称查描述项。
     *
     * @param name 名称
     * @return 描述项，未知返回 null
     */
    public static Descriptor descriptorOf(String name) {
        for (Descriptor descriptor : DESCRIPTORS) {
            if (descriptor.name().equals(name)) {
                return descriptor;
            }
        }
        return null;
    }

    /**
     * 名称 → 值 解析为生效配置。
     *
     * @param values 名称 → 值
     * @return 配置
     */
    FaceReidConfig resolve(Map<String, String> values) {
        FaceReidConfig base = FaceReidConfig.of(properties);
        if (values == null || values.isEmpty()) {
            return base;
        }
        return new FaceReidConfig(
                bool(values.get(NAME_ENABLED), base.enabled()),
                text(values.get(NAME_ENGINE), base.engine()),
                integer(values.get(NAME_DIMENSION), base.dimension()),
                integer(values.get(NAME_MAX_FACES), base.maxFaces()),
                values.containsKey(NAME_METRIC)
                        ? SimilarityMetric.parse(values.get(NAME_METRIC)) : base.metric(),
                decimal(values.get(NAME_THRESHOLD), base.threshold()),
                integer(values.get(NAME_TOP_K), base.topK()),
                bool(values.get(NAME_INCLUDE_MERGED), base.includeMerged()),
                integer(values.get(NAME_TRACK_GAP_SECONDS), base.trackGapSeconds()),
                integer(values.get(NAME_TRACK_MAX_MISSES), base.trackMaxMisses()),
                integer(values.get(NAME_TRACK_TTL_SECONDS), base.trackTtlSeconds()),
                integer(values.get(NAME_MAX_FACES_PER_IMAGE), base.maxFacesPerImage()));
    }

    /**
     * 生效配置 → 名称 → 值。
     *
     * @param config 配置
     * @return 名称 → 值
     */
    public static Map<String, String> toMap(FaceReidConfig config) {
        Map<String, String> map = new LinkedHashMap<>();
        if (config == null) {
            return map;
        }
        map.put(NAME_ENABLED, String.valueOf(config.enabled()));
        map.put(NAME_ENGINE, config.engine());
        map.put(NAME_DIMENSION, String.valueOf(config.dimension()));
        map.put(NAME_MAX_FACES, String.valueOf(config.maxFaces()));
        map.put(NAME_METRIC, config.metric().name());
        map.put(NAME_THRESHOLD, String.valueOf(config.threshold()));
        map.put(NAME_TOP_K, String.valueOf(config.topK()));
        map.put(NAME_INCLUDE_MERGED, String.valueOf(config.includeMerged()));
        map.put(NAME_TRACK_GAP_SECONDS, String.valueOf(config.trackGapSeconds()));
        map.put(NAME_TRACK_MAX_MISSES, String.valueOf(config.trackMaxMisses()));
        map.put(NAME_TRACK_TTL_SECONDS, String.valueOf(config.trackTtlSeconds()));
        map.put(NAME_MAX_FACES_PER_IMAGE, String.valueOf(config.maxFacesPerImage()));
        return Collections.unmodifiableMap(map);
    }

    /**
     * 通知监听者。
     *
     * @param config 新配置
     */
    private void notifyListeners(FaceReidConfig config) {
        List<java.util.function.Consumer<FaceReidConfig>> snapshot;
        synchronized (listeners) {
            snapshot = new ArrayList<>(listeners);
        }
        for (java.util.function.Consumer<FaceReidConfig> listener : snapshot) {
            try {
                listener.accept(config);
            } catch (Exception e) {
                log.warn("[FaceReid] 配置监听回调失败: {}", e.getMessage());
            }
        }
    }

    /**
     * 解析布尔。
     */
    private static boolean bool(String raw, boolean fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        return Boolean.parseBoolean(raw.trim());
    }

    /**
     * 解析整数。
     */
    private static int integer(String raw, int fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    /**
     * 解析浮点。
     */
    private static double decimal(String raw, double fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        try {
            return Double.parseDouble(raw.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    /**
     * 解析文本。
     */
    private static String text(String raw, String fallback) {
        return raw == null || raw.isBlank() ? fallback : raw.trim();
    }

    /**
     * 配置项描述。
     *
     * @param name   名称
     * @param type   类型（String/Number/Bool）
     * @param remark 说明
     * @param sort   排序号
     * @author CH
     * @since 4.0.0.43
     */
    public record Descriptor(String name, String type, String remark, int sort) {
    }
}
