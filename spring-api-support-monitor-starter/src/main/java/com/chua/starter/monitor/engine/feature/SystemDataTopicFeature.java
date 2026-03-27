package com.chua.starter.monitor.engine.feature;

import com.chua.common.support.data.query.definition.EngineTopic;

/**
 * Topic 特性。
 */
public interface SystemDataTopicFeature {

    EngineTopic topic(String topicName);
}
