package com.chua.starter.service.configuration.trace;

import com.chua.common.support.core.spi.ServiceProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 请求追踪配置服务。
 *
 * @author CH
 * @since 2026/03/25
 */
@Service
@RequiredArgsConstructor
public class RequestTraceStrategyConfigService {

    private static final List<String> CONFIG_STORAGE_DEFAULTS = List.of("database", "redis", "memory");
    private static final List<String> RAW_STORAGE_DEFAULTS = List.of("file", "database");
    private static final List<String> BEHAVIOR_STORAGE_DEFAULTS = List.of("database", "memory");

    private final RequestTraceStrategyRuntimeProperties runtimeProperties;
    private final RequestTraceStorageRouter storageRouter;

    private volatile RequestTraceStrategyConfig cachedConfig;

    public RequestTraceStrategyConfig currentConfig() {
        RequestTraceStrategyConfig local = cachedConfig;
        if (local != null) {
            return local.copy();
        }
        return refresh();
    }

    public synchronized RequestTraceStrategyConfig refresh() {
        RequestTraceConfigStorage storage = storageRouter.configStorage(runtimeProperties.getConfigStorageType());
        RequestTraceStrategyConfig loaded = storage.load(RequestTraceConstants.GROUP_CODE);
        RequestTraceStrategyConfig normalized = loaded == null
                ? resolveRuntimeDefaultConfig()
                : sanitizeLoadedConfig(RequestTraceStrategyConfigCodec.normalize(loaded));
        cachedConfig = normalized;
        return normalized.copy();
    }

    public synchronized RequestTraceStrategyConfig update(RequestTraceStrategyConfig config) {
        RequestTraceStrategyConfig normalized = validateWritableConfig(RequestTraceStrategyConfigCodec.normalize(config));
        RequestTraceConfigStorage storage = storageRouter.configStorage(runtimeProperties.getConfigStorageType());
        RequestTraceStrategyConfig saved = storage.save(RequestTraceConstants.GROUP_CODE, normalized);
        RequestTraceStrategyConfig target = sanitizeLoadedConfig(RequestTraceStrategyConfigCodec.normalize(saved));
        cachedConfig = target;
        return target.copy();
    }

    public String configStorageType() {
        return sanitizeStorageType(runtimeProperties.getConfigStorageType(), configStorageTypes(), "database");
    }

    public List<String> configStorageTypes() {
        return storageTypes(RequestTraceConfigStorage.class, CONFIG_STORAGE_DEFAULTS);
    }

    public List<String> rawStorageTypes() {
        return storageTypes(RequestTraceRawRecordStorage.class, RAW_STORAGE_DEFAULTS);
    }

    public List<String> behaviorStorageTypes() {
        return storageTypes(RequestTraceBehaviorStorage.class, BEHAVIOR_STORAGE_DEFAULTS);
    }

    private RequestTraceStrategyConfig sanitizeLoadedConfig(RequestTraceStrategyConfig config) {
        config.setRawStorageType(sanitizeStorageType(config.getRawStorageType(), rawStorageTypes(), "file"));
        config.setBehaviorStorageType(sanitizeStorageType(config.getBehaviorStorageType(), behaviorStorageTypes(), "database"));
        return config;
    }

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

    private RequestTraceStrategyConfig validateWritableConfig(RequestTraceStrategyConfig config) {
        validateStorageType("rawStorageType", config.getRawStorageType(), rawStorageTypes(), "file");
        validateStorageType("behaviorStorageType", config.getBehaviorStorageType(), behaviorStorageTypes(), "database");
        return config;
    }

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

    private String sanitizeStorageType(String value, List<String> supportedTypes, String defaultType) {
        String normalized = lowerCase(value);
        if (normalized == null) {
            return defaultType;
        }
        return supportedTypes.contains(normalized) ? normalized : defaultType;
    }

    private List<String> storageTypes(Class<?> spiType, List<String> defaults) {
        Set<String> values = new LinkedHashSet<>();
        defaults.stream()
                .map(this::lowerCase)
                .filter(item -> item != null && !item.isBlank())
                .forEach(values::add);
        try {
            ServiceProvider.of(spiType).getExtensions().stream()
                    .map(this::lowerCase)
                    .filter(item -> item != null && !item.isBlank())
                    .forEach(values::add);
        } catch (Exception ignored) {
        }
        return List.copyOf(values);
    }

    private String lowerCase(String value) {
        String normalized = RequestTraceSupport.normalize(value);
        return normalized == null ? null : normalized.toLowerCase(Locale.ROOT);
    }
}
