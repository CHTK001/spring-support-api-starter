package com.chua.starter.service.configuration.trace;

import com.chua.common.support.spi.ServiceProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 请求追踪配置服务。
 *
 * @since 2026/03/25
 * @author CH
 */
@Service
@RequiredArgsConstructor
public class RequestTraceStrategyConfigService {

    /**
     * 日志
     */
    private static final System.Logger LOG = System.getLogger(RequestTraceStrategyConfigService.class.getName());

    /**
     * 配置_storage_默认
    */
    private static final List<String> CONFIG_STORAGE_DEFAULTS = List.of("database", "redis", "memory");
    /**
     * Raw_storage_默认
    */
    private static final List<String> RAW_STORAGE_DEFAULTS = List.of("file", "database");
    /**
     * 行为_storage_默认
    */
    private static final List<String> BEHAVIOR_STORAGE_DEFAULTS = List.of("database", "memory");

    /**
     * Runtime属性
    */
    private final RequestTraceStrategyRuntimeProperties runtimeProperties;
    /**
     * 存储router
    */
    private final RequestTraceStorageRouter storageRouter;

    private volatile RequestTraceStrategyConfig cachedConfig; // 缓存配置

    /**
     * 当前配置
     *
     * @return 当前配置的结果
     */
    public RequestTraceStrategyConfig currentConfig() {
        RequestTraceStrategyConfig local = cachedConfig;
        if (local != null) {
            return local.copy();
        }
        return refresh();
    }

    /**
     * Refresh
     *
     * @return refresh的结果
     */
    public synchronized RequestTraceStrategyConfig refresh() {
        RequestTraceConfigStorage storage = storageRouter.configStorage(runtimeProperties.getConfigStorageType());
        RequestTraceStrategyConfig loaded = storage.load(RequestTraceConstants.GROUP_CODE);
        RequestTraceStrategyConfig normalized = loaded == null
                ? resolveRuntimeDefaultConfig()
                : sanitizeLoadedConfig(RequestTraceStrategyConfigCodec.normalize(loaded));
        cachedConfig = normalized;
        return normalized.copy();
    }

    /**
     * 更新
     *
     * @param config 配置
     * @return 更新的结果
     */
    public synchronized RequestTraceStrategyConfig update(RequestTraceStrategyConfig config) {
        RequestTraceStrategyConfig normalized = validateWritableConfig(RequestTraceStrategyConfigCodec.normalize(config));
        RequestTraceConfigStorage storage = storageRouter.configStorage(runtimeProperties.getConfigStorageType());
        RequestTraceStrategyConfig saved = storage.save(RequestTraceConstants.GROUP_CODE, normalized);
        RequestTraceStrategyConfig target = sanitizeLoadedConfig(RequestTraceStrategyConfigCodec.normalize(saved));
        cachedConfig = target;
        return target.copy();
    }

    /**
     * 配置storage类型
     *
     * @return 配置storage类型的结果
     */
    public String configStorageType() {
        return sanitizeStorageType(runtimeProperties.getConfigStorageType(), configStorageTypes(), "database");
    }

    /**
     * 配置storage类型
     *
     * @return 配置storage类型的结果
     */
    public List<String> configStorageTypes() {
        return storageTypes(RequestTraceConfigStorage.class, CONFIG_STORAGE_DEFAULTS);
    }

    /**
     * rawstorage类型
     *
     * @return rawstorage类型的结果
     */
    public List<String> rawStorageTypes() {
        return storageTypes(RequestTraceRawRecordStorage.class, RAW_STORAGE_DEFAULTS);
    }

    /**
     * 行为storage类型
     *
     * @return 行为storage类型的结果
     */
    public List<String> behaviorStorageTypes() {
        return storageTypes(RequestTraceBehaviorStorage.class, BEHAVIOR_STORAGE_DEFAULTS);
    }

    /**
     * sanitize加载配置
     *
     * @param config 配置
     * @return sanitize加载配置的结果
     */
    private RequestTraceStrategyConfig sanitizeLoadedConfig(RequestTraceStrategyConfig config) {
        config.setRawStorageType(sanitizeStorageType(config.getRawStorageType(), rawStorageTypes(), "file"));
        config.setBehaviorStorageType(sanitizeStorageType(config.getBehaviorStorageType(), behaviorStorageTypes(), "database"));
        return config;
    }

    /**
     * 解析runtime默认配置
     *
     * @return resolveruntime默认配置的结果
     */
    private RequestTraceStrategyConfig resolveRuntimeDefaultConfig() {
        RequestTraceStrategyConfig config = new RequestTraceStrategyConfig();
        config.setAiSummaryEnabled(runtimeProperties.getAiSummaryEnabled());
        config.setAiModel(runtimeProperties.getAiModel());
        config.setAiRateLimitEnabled(runtimeProperties.getAiRateLimitEnabled());
        config.setAiRateLimitWindowSeconds(runtimeProperties.getAiRateLimitWindowSeconds());
        config.setAiRateLimitMaxRequests(runtimeProperties.getAiRateLimitMaxRequests());
        config.setAiTimeoutMs(runtimeProperties.getAiTimeoutMs());
        return sanitizeLoadedConfig(RequestTraceStrategyConfigCodec.normalize(config));
    }

    /**
     * 校验writable配置
     *
     * @param config 配置
     * @return 校验writable配置的结果
     */
    private RequestTraceStrategyConfig validateWritableConfig(RequestTraceStrategyConfig config) {
        validateStorageType("rawStorageType", config.getRawStorageType(), rawStorageTypes(), "file");
        validateStorageType("behaviorStorageType", config.getBehaviorStorageType(), behaviorStorageTypes(), "database");
        return config;
    }

    /**
     * 校验storage类型
     *
     * @param fieldName 字段名称
     * @param value 值
     * @param supportedTypes 支持类型
     * @param defaultType 默认类型
     */
    private void validateStorageType(String fieldName, String value, List<String> supportedTypes, String defaultType) {
        String normalized = lowerCase(value);
        if (normalized == null || normalized.equals(defaultType)) {
            return;
        }
        if (supportedTypes.contains(normalized)) {
            return;
        }
        throw new IllegalArgumentException("不支持的 " + fieldName + "=" + value + "，可选值: " + supportedTypes);
    }

    /**
     * sanitizestorage类型
     *
     * @param value 值
     * @param supportedTypes 支持类型
     * @param defaultType 默认类型
     * @return sanitizestorage类型的结果
     */
    private String sanitizeStorageType(String value, List<String> supportedTypes, String defaultType) {
        String normalized = lowerCase(value);
        if (normalized == null) {
            return defaultType;
        }
        return supportedTypes.contains(normalized) ? normalized : defaultType;
    }

    /**
     * storage类型
     *
     * @param spiType spi类型
     * @param defaults 默认
     * @return storage类型的结果
     */
    private List<String> storageTypes(Class<?> spiType, List<String> defaults) {
        Set<String> values = new LinkedHashSet<>();
        defaults.stream()
                .map(this::lowerCase)
                .filter(item -> item != null && !item.isBlank())
                .forEach(values::add);
        try {
            ServiceProvider.of(spiType).list().values().stream().map(String::valueOf)
                    .map(this::lowerCase)
                    .filter(item -> item != null && !item.isBlank())
                    .forEach(values::add);
        } catch (Exception e) {
            LOG.log(System.Logger.Level.DEBUG, "[trace] 加载 SPI 策略失败，已忽略: " + e.getMessage(), e);
        }
        return List.copyOf(values);
    }

    /**
     * 降低大小写
     *
     * @param value 值
     * @return 降低大小写的结果
     */
    private String lowerCase(String value) {
        String normalized = RequestTraceSupport.normalize(value);
        return normalized == null ? null : normalized.toLowerCase(Locale.ROOT);
    }
}
