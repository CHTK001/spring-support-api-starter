package com.chua.starter.monitor.engine;

import com.chua.common.support.data.datasource.jdbc.option.DataSourceOptions;
import com.chua.common.support.data.query.FeatureSupportLevel;
import com.chua.common.support.data.query.definition.EngineTopic;
import com.chua.common.support.data.query.definition.EngineTopicAdmin;
import com.chua.common.support.data.query.definition.query.Search;
import com.chua.common.support.data.query.definition.query.Update;
import com.chua.common.support.data.query.feature.SearchFeature;
import com.chua.common.support.data.query.feature.TopicAdminFeature;
import com.chua.common.support.data.query.feature.TopicFeature;
import com.chua.common.support.data.query.feature.UpdateFeature;
import com.chua.starter.monitor.engine.feature.SystemDataTopicAdminFeature;
import com.chua.starter.monitor.engine.feature.SystemDataTopicFeature;
import com.chua.starter.monitor.entity.SystemDataSetting;

/**
 * Kafka 专用引擎。
 */
public class KafkaSystemDataEngine extends LegacySpiSystemDataEngine
        implements SystemDataTopicFeature, SystemDataTopicAdminFeature {

    private TopicFeature topicFeature;

    public KafkaSystemDataEngine(SystemDataSetting setting, DataSourceOptions options) {
        super(setting, options);
    }

    @Override
    public FeatureSupportLevel capabilitySupport(SystemDataCapability capability) {
        if (capability == null) {
            return FeatureSupportLevel.NONE;
        }
        return switch (capability) {
            case ORM_SEARCH -> resolveAttachedOrmSupport(
                    ensureTopicFeature(),
                    com.chua.common.support.data.query.Feature.SEARCH,
                    SearchFeature.class,
                    super.capabilitySupport(capability)
            );
            case ORM_UPDATE -> resolveAttachedOrmSupport(
                    ensureTopicFeature(),
                    com.chua.common.support.data.query.Feature.UPDATE,
                    UpdateFeature.class,
                    super.capabilitySupport(capability)
            );
            case TOPIC -> resolveNativeFeatureSupport(
                    ensureTopicFeature(),
                    com.chua.common.support.data.query.Feature.TOPIC,
                    TopicFeature.class
            );
            case TOPIC_ADMIN -> resolveNativeFeatureSupport(
                    ensureTopicFeature(),
                    com.chua.common.support.data.query.Feature.TOPIC_ADMIN,
                    TopicAdminFeature.class
            );
            default -> super.capabilitySupport(capability);
        };
    }

    @Override
    public <T> Search<T> search(Class<T> targetType) {
        TopicFeature feature = ensureTopicFeature();
        if (feature instanceof SearchFeature searchFeature) {
            return searchFeature.search(targetType, Search.class);
        }
        return super.search(targetType);
    }

    public <T> Update<T> update(Class<T> targetType) {
        TopicFeature feature = ensureTopicFeature();
        if (feature instanceof UpdateFeature updateFeature) {
            return updateFeature.update(targetType, Update.class);
        }
        return super.update(targetType);
    }

    @Override
    public EngineTopic topic(String topicName) {
        TopicFeature feature = ensureTopicFeature();
        if (feature == null) {
            throw new UnsupportedOperationException("当前数据源不支持能力: Topic");
        }
        return feature.topic(topicName);
    }

    @Override
    public EngineTopicAdmin topicAdmin(String topicName) {
        TopicFeature feature = ensureTopicFeature();
        if (feature instanceof TopicAdminFeature topicAdminFeature) {
            return topicAdminFeature.topicAdmin(topicName);
        }
        throw new UnsupportedOperationException("当前数据源不支持能力: Topic Admin");
    }

    @Override
    public <T> T unwrap(Class<T> targetType) {
        if (targetType != null && targetType.isInstance(ensureTopicFeature())) {
            return targetType.cast(ensureTopicFeature());
        }
        return super.unwrap(targetType);
    }

    @Override
    public void close() throws Exception {
        if (topicFeature != null) {
            closeQuietly(topicFeature);
        }
        super.close();
    }

    private TopicFeature ensureTopicFeature() {
        if (topicFeature == null) {
            topicFeature = resolveExtension(TopicFeature.class, defaultExtensionNames("kafka"));
        }
        return topicFeature;
    }
}
