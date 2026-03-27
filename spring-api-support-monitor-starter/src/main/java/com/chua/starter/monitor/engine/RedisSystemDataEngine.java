package com.chua.starter.monitor.engine;

import com.chua.common.support.data.datasource.jdbc.option.DataSourceOptions;
import com.chua.common.support.data.query.ClusterEngine;
import com.chua.common.support.data.query.FeatureSupportLevel;
import com.chua.common.support.data.query.KvEngine;
import com.chua.common.support.data.query.definition.EngineCluster;
import com.chua.common.support.data.query.definition.EngineNode;
import com.chua.common.support.data.query.definition.query.Kv;
import com.chua.common.support.data.query.definition.query.Search;
import com.chua.common.support.data.query.definition.query.TimeSeries;
import com.chua.common.support.data.query.definition.query.Update;
import com.chua.common.support.data.query.feature.ClusterFeature;
import com.chua.common.support.data.query.feature.KvFeature;
import com.chua.common.support.data.query.feature.NodeFeature;
import com.chua.common.support.data.query.feature.SearchFeature;
import com.chua.common.support.data.query.feature.TimeSeriesFeature;
import com.chua.common.support.data.query.feature.UpdateFeature;
import com.chua.starter.monitor.engine.feature.SystemDataClusterFeature;
import com.chua.starter.monitor.engine.feature.SystemDataKvFeature;
import com.chua.starter.monitor.engine.feature.SystemDataNodeFeature;
import com.chua.starter.monitor.engine.feature.SystemDataTimeSeriesFeature;
import com.chua.starter.monitor.entity.SystemDataSetting;

/**
 * Redis 专用引擎。
 */
public class RedisSystemDataEngine extends LegacySpiSystemDataEngine
        implements SystemDataKvFeature, SystemDataTimeSeriesFeature, SystemDataClusterFeature, SystemDataNodeFeature {

    private ClusterEngine clusterEngine;
    private SearchFeature searchFeature;
    private TimeSeriesFeature timeSeriesFeature;

    public RedisSystemDataEngine(SystemDataSetting setting, DataSourceOptions options) {
        super(setting, options);
    }

    @Override
    public FeatureSupportLevel capabilitySupport(SystemDataCapability capability) {
        if (capability == null) {
            return FeatureSupportLevel.NONE;
        }
        return switch (capability) {
            case ORM_SEARCH -> resolveAttachedOrmSupport(
                    ensureSearchFeature(),
                    com.chua.common.support.data.query.Feature.SEARCH,
                    SearchFeature.class,
                    super.capabilitySupport(capability)
            );
            case ORM_UPDATE -> resolveAttachedOrmSupport(
                    ensureSearchFeature(),
                    com.chua.common.support.data.query.Feature.UPDATE,
                    UpdateFeature.class,
                    super.capabilitySupport(capability)
            );
            case KV -> resolveNativeFeatureSupport(
                    ensureClusterEngine(),
                    com.chua.common.support.data.query.Feature.KV,
                    KvFeature.class
            );
            case TIME_SERIES -> resolveNativeFeatureSupport(
                    ensureTimeSeriesFeature(),
                    com.chua.common.support.data.query.Feature.TIMESERIES,
                    TimeSeriesFeature.class
            );
            case CLUSTER -> resolveNativeFeatureSupport(
                    ensureClusterEngine(),
                    com.chua.common.support.data.query.Feature.CLUSTER,
                    ClusterFeature.class
            );
            case NODE -> resolveNativeFeatureSupport(
                    ensureClusterEngine(),
                    com.chua.common.support.data.query.Feature.NODE,
                    NodeFeature.class
            );
            default -> super.capabilitySupport(capability);
        };
    }

    @Override
    public <T> Search<T> search(Class<T> targetType) {
        SearchFeature feature = ensureSearchFeature();
        if (feature != null) {
            return feature.search(targetType, Search.class);
        }
        return super.search(targetType);
    }

    public <T> Update<T> update(Class<T> targetType) {
        SearchFeature feature = ensureSearchFeature();
        if (feature instanceof UpdateFeature updateFeature) {
            return updateFeature.update(targetType, Update.class);
        }
        return super.update(targetType);
    }

    @Override
    public Kv kv(String key) {
        ClusterEngine engine = ensureClusterEngine();
        if (engine instanceof KvFeature kvFeature) {
            return kvFeature.kv(key);
        }
        throw new UnsupportedOperationException("当前数据源不支持能力: KV");
    }

    @Override
    public TimeSeries timeSeries(String key) {
        TimeSeriesFeature feature = ensureTimeSeriesFeature();
        if (feature == null) {
            throw new UnsupportedOperationException("当前数据源不支持能力: TimeSeries");
        }
        return feature.timeSeries(key);
    }

    @Override
    public EngineCluster cluster(String indexName) {
        ClusterEngine engine = ensureClusterEngine();
        if (engine instanceof ClusterFeature clusterFeature) {
            return clusterFeature.cluster(indexName);
        }
        throw new UnsupportedOperationException("当前数据源不支持能力: Cluster");
    }

    @Override
    public EngineNode node(String nodeId) {
        ClusterEngine engine = ensureClusterEngine();
        if (engine instanceof NodeFeature nodeFeature) {
            return nodeFeature.node(nodeId);
        }
        throw new UnsupportedOperationException("当前数据源不支持能力: Node");
    }

    @Override
    public <T> T unwrap(Class<T> targetType) {
        Object[] candidates = {
                ensureClusterEngine(),
                ensureSearchFeature(),
                ensureTimeSeriesFeature()
        };
        for (Object candidate : candidates) {
            if (candidate != null && targetType.isInstance(candidate)) {
                return targetType.cast(candidate);
            }
        }
        return super.unwrap(targetType);
    }

    @Override
    public void close() throws Exception {
        java.util.LinkedHashSet<AutoCloseable> closeables = new java.util.LinkedHashSet<>();
        if (clusterEngine != null) {
            closeables.add(clusterEngine);
        }
        if (searchFeature instanceof AutoCloseable closeable) {
            closeables.add(closeable);
        }
        if (timeSeriesFeature != null) {
            closeables.add(timeSeriesFeature);
        }
        for (AutoCloseable closeable : closeables) {
            closeQuietly(closeable);
        }
        super.close();
    }

    private ClusterEngine ensureClusterEngine() {
        if (clusterEngine == null) {
            clusterEngine = resolveExtension(ClusterEngine.class, defaultExtensionNames("redis"));
        }
        return clusterEngine;
    }

    private SearchFeature ensureSearchFeature() {
        if (searchFeature == null) {
            searchFeature = resolveExtension(
                    SearchFeature.class,
                    defaultExtensionNames("REDISEARCH", "redisearch", "REDIS", "redis")
            );
        }
        return searchFeature;
    }

    private TimeSeriesFeature ensureTimeSeriesFeature() {
        if (timeSeriesFeature == null) {
            timeSeriesFeature = resolveExtension(
                    TimeSeriesFeature.class,
                    defaultExtensionNames("REDIS_TIMESERIES", "redis_timeseries", "REDIS", "redis")
            );
        }
        return timeSeriesFeature;
    }
}
