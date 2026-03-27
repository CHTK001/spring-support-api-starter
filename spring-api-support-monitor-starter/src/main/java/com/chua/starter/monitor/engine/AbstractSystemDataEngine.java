package com.chua.starter.monitor.engine;

import com.chua.common.support.core.spi.ServiceProvider;
import com.chua.common.support.data.datasource.jdbc.option.DataSourceOptions;
import com.chua.common.support.data.query.FeatureSupportLevel;
import com.chua.starter.monitor.entity.SystemDataSetting;
import org.springframework.beans.BeanUtils;

/**
 * {@link SystemDataEngine} 抽象基类。
 */
public abstract class AbstractSystemDataEngine implements SystemDataEngine {

    private final SystemDataSetting setting;
    private final DataSourceOptions options;

    protected AbstractSystemDataEngine(SystemDataSetting setting, DataSourceOptions options) {
        this.setting = setting;
        this.options = options;
    }

    @Override
    public SystemDataSetting getSetting() {
        SystemDataSetting copy = new SystemDataSetting();
        BeanUtils.copyProperties(setting, copy);
        return copy;
    }

    @Override
    public DataSourceOptions getOptions() {
        DataSourceOptions copy = new DataSourceOptions();
        BeanUtils.copyProperties(options, copy);
        return copy;
    }

    @Override
    public <T> T unwrap(Class<T> targetType) {
        throw new UnsupportedOperationException("当前数据源不支持能力: "
                + (targetType == null ? "Native Engine" : targetType.getSimpleName()));
    }

    @Override
    public <T> T feature(Class<T> featureType) {
        if (featureType != null && featureType.isInstance(this)) {
            return featureType.cast(this);
        }
        return null;
    }

    protected boolean isJdbcSetting() {
        return SystemDataEngineSupport.isJdbc(setting);
    }

    protected boolean supportsPartitionManagement() {
        return SystemDataEngineSupport.supportsPartitions(setting);
    }

    protected SystemDataOrmDialect resolveOrmDialect() {
        return SystemDataEngineSupport.resolveOrmDialect(setting);
    }

    protected <T> T resolveExtension(Class<T> targetType, String... names) {
        ServiceProvider<T> provider = ServiceProvider.of(targetType);
        for (String name : names) {
            if (name == null || name.isBlank()) {
                continue;
            }
            try {
                T extension = provider.getNewExtension(name, options);
                if (extension != null) {
                    return extension;
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    protected String[] defaultExtensionNames(String... extras) {
        java.util.LinkedHashSet<String> names = new java.util.LinkedHashSet<>();
        if (extras != null) {
            java.util.Collections.addAll(names, extras);
        }
        names.add(setting.getSystemDataSettingType());
        names.add(setting.getSystemDataSettingProtocol());
        return names.stream()
                .filter(it -> it != null && !it.isBlank())
                .toArray(String[]::new);
    }

    protected void closeQuietly(AutoCloseable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Exception ignored) {
        }
    }

    protected FeatureSupportLevel resolveNativeFeatureSupport(
            Object candidate,
            com.chua.common.support.data.query.Feature nativeFeature,
            Class<?> nativeFeatureType
    ) {
        if (candidate instanceof com.chua.common.support.data.query.Engine engine) {
            if (nativeFeature != null) {
                return engine.featureSupport(nativeFeature);
            }
            if (nativeFeatureType != null) {
                return engine.featureSupport(nativeFeatureType);
            }
        }
        return nativeFeatureType != null && nativeFeatureType.isInstance(candidate)
                ? FeatureSupportLevel.FULL
                : FeatureSupportLevel.NONE;
    }

    protected FeatureSupportLevel resolveNativeFeatureSupport(Object candidate, Class<?> nativeFeatureType) {
        return resolveNativeFeatureSupport(candidate, null, nativeFeatureType);
    }

    protected FeatureSupportLevel resolveAttachedOrmSupport(
            Object candidate,
            com.chua.common.support.data.query.Feature nativeFeature,
            Class<?> nativeFeatureType,
            FeatureSupportLevel fallback
    ) {
        FeatureSupportLevel nativeLevel = resolveNativeFeatureSupport(candidate, nativeFeature, nativeFeatureType);
        return nativeLevel != FeatureSupportLevel.NONE ? nativeLevel : fallback;
    }
}
