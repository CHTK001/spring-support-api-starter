package com.chua.starter.monitor.engine;

import com.chua.common.support.data.datasource.jdbc.option.DataSourceOptions;
import com.chua.common.support.data.query.ClusterEngine;
import com.chua.common.support.data.query.FeatureSupportLevel;
import com.chua.common.support.data.query.definition.EngineCluster;
import com.chua.common.support.data.query.definition.EngineNode;
import com.chua.common.support.data.query.definition.query.Search;
import com.chua.common.support.data.query.definition.query.Update;
import com.chua.common.support.data.query.feature.ClusterFeature;
import com.chua.common.support.data.query.feature.NodeFeature;
import com.chua.common.support.data.query.feature.SearchFeature;
import com.chua.common.support.data.query.feature.UpdateFeature;
import com.chua.starter.monitor.engine.feature.SystemDataClusterFeature;
import com.chua.starter.monitor.engine.feature.SystemDataNodeFeature;
import com.chua.starter.monitor.entity.SystemDataSetting;

/**
 * Elasticsearch 专用引擎。
 */
public class ElasticsearchSystemDataEngine extends LegacySpiSystemDataEngine
        implements SystemDataClusterFeature, SystemDataNodeFeature {

    private ClusterEngine clusterEngine;

    public ElasticsearchSystemDataEngine(SystemDataSetting setting, DataSourceOptions options) {
        super(setting, options);
    }

    @Override
    public FeatureSupportLevel capabilitySupport(SystemDataCapability capability) {
        if (capability == null) {
            return FeatureSupportLevel.NONE;
        }
        return switch (capability) {
            case ORM_SEARCH -> resolveAttachedOrmSupport(
                    ensureClusterEngine(),
                    com.chua.common.support.data.query.Feature.SEARCH,
                    SearchFeature.class,
                    super.capabilitySupport(capability)
            );
            case ORM_UPDATE -> resolveAttachedOrmSupport(
                    ensureClusterEngine(),
                    com.chua.common.support.data.query.Feature.UPDATE,
                    UpdateFeature.class,
                    super.capabilitySupport(capability)
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
        ClusterEngine engine = ensureClusterEngine();
        if (engine instanceof SearchFeature searchFeature) {
            return searchFeature.search(targetType, Search.class);
        }
        return super.search(targetType);
    }

    public <T> Update<T> update(Class<T> targetType) {
        ClusterEngine engine = ensureClusterEngine();
        if (engine instanceof UpdateFeature updateFeature) {
            return updateFeature.update(targetType, Update.class);
        }
        return super.update(targetType);
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
        if (targetType != null && targetType.isInstance(ensureClusterEngine())) {
            return targetType.cast(ensureClusterEngine());
        }
        return super.unwrap(targetType);
    }

    @Override
    public void close() throws Exception {
        if (clusterEngine != null) {
            closeQuietly(clusterEngine);
        }
        super.close();
    }

    private ClusterEngine ensureClusterEngine() {
        if (clusterEngine == null) {
            clusterEngine = resolveExtension(ClusterEngine.class, defaultExtensionNames("elasticsearch", "elastic"));
        }
        return clusterEngine;
    }
}
