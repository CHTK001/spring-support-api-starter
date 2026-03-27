package com.chua.starter.monitor.engine;

import com.chua.common.support.data.datasource.jdbc.option.DataSourceOptions;
import com.chua.common.support.data.query.FeatureSupportLevel;
import com.chua.common.support.data.query.definition.query.Search;
import com.chua.common.support.data.query.definition.query.Update;
import com.chua.common.support.data.query.feature.SearchFeature;
import com.chua.common.support.data.query.feature.TableFeature;
import com.chua.common.support.data.query.feature.UpdateFeature;
import com.chua.starter.monitor.entity.SystemDataSetting;

/**
 * MongoDB 专用引擎。
 */
public class MongoSystemDataEngine extends LegacySpiSystemDataEngine {

    private TableFeature tableFeature;

    public MongoSystemDataEngine(SystemDataSetting setting, DataSourceOptions options) {
        super(setting, options);
    }

    @Override
    public FeatureSupportLevel capabilitySupport(SystemDataCapability capability) {
        if (capability == null) {
            return FeatureSupportLevel.NONE;
        }
        return switch (capability) {
            case ORM_SEARCH -> resolveAttachedOrmSupport(
                    ensureTableFeature(),
                    com.chua.common.support.data.query.Feature.SEARCH,
                    SearchFeature.class,
                    super.capabilitySupport(capability)
            );
            case ORM_UPDATE -> resolveAttachedOrmSupport(
                    ensureTableFeature(),
                    com.chua.common.support.data.query.Feature.UPDATE,
                    UpdateFeature.class,
                    super.capabilitySupport(capability)
            );
            default -> super.capabilitySupport(capability);
        };
    }

    @Override
    public <T> Search<T> search(Class<T> targetType) {
        TableFeature feature = ensureTableFeature();
        if (feature instanceof SearchFeature searchFeature) {
            return searchFeature.search(targetType, Search.class);
        }
        return super.search(targetType);
    }

    public <T> Update<T> update(Class<T> targetType) {
        TableFeature feature = ensureTableFeature();
        if (feature instanceof UpdateFeature updateFeature) {
            return updateFeature.update(targetType, Update.class);
        }
        return super.update(targetType);
    }

    @Override
    public <T> T unwrap(Class<T> targetType) {
        if (targetType != null && targetType.isInstance(ensureTableFeature())) {
            return targetType.cast(ensureTableFeature());
        }
        return super.unwrap(targetType);
    }

    @Override
    public void close() throws Exception {
        if (tableFeature != null) {
            closeQuietly(tableFeature);
        }
        super.close();
    }

    private TableFeature ensureTableFeature() {
        if (tableFeature == null) {
            tableFeature = resolveExtension(TableFeature.class, defaultExtensionNames("mongodb", "mongo"));
        }
        return tableFeature;
    }
}
