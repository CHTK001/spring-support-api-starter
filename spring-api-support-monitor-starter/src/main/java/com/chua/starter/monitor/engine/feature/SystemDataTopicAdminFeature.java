package com.chua.starter.monitor.engine.feature;

import com.chua.common.support.data.query.definition.EngineTopicAdmin;

/**
 * Topic 管理特性。
 */
public interface SystemDataTopicAdminFeature {

    EngineTopicAdmin topicAdmin(String topicName);
}
