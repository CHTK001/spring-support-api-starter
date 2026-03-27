package com.chua.starter.monitor.engine.feature;

import com.chua.common.support.data.query.definition.EngineCluster;

/**
 * 集群特性。
 */
public interface SystemDataClusterFeature {

    EngineCluster cluster(String indexName);
}
