package com.chua.starter.monitor.engine.feature;

import com.chua.common.support.data.query.definition.EngineNode;

/**
 * 节点特性。
 */
public interface SystemDataNodeFeature {

    EngineNode node(String nodeId);
}
