package com.chua.starter.monitor.engine;

import com.chua.common.support.data.datasource.jdbc.option.DataSourceOptions;
import com.chua.starter.monitor.entity.SystemDataSetting;
import com.chua.starter.monitor.service.impl.SystemDataConsoleServiceImpl;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * 数据源引擎工厂。
 */
@Component
public class SystemDataEngineFactory {

    private final List<SystemDataEngineProvider> providers;

    public SystemDataEngineFactory(List<SystemDataEngineProvider> customProviders) {
        List<SystemDataEngineProvider> availableProviders = new ArrayList<>(builtinProviders());
        if (customProviders != null && !customProviders.isEmpty()) {
            availableProviders.addAll(customProviders);
        }
        AnnotationAwareOrderComparator.sort(availableProviders);
        this.providers = List.copyOf(availableProviders);
    }

    public SystemDataEngine create(SystemDataSetting setting) {
        if (setting == null) {
            throw new IllegalArgumentException("数据源配置不能为空");
        }
        DataSourceOptions options = SystemDataConsoleServiceImpl.buildOptions(setting);
        for (SystemDataEngineProvider provider : providers) {
            if (provider.supports(setting)) {
                return provider.create(setting, options);
            }
        }
        throw new IllegalStateException("未找到匹配的数据源引擎");
    }

    private List<SystemDataEngineProvider> builtinProviders() {
        return List.of(
                new BuiltinProvider(-700, SystemDataEngineSupport::isRedis, RedisSystemDataEngine::new),
                new BuiltinProvider(-600, SystemDataEngineSupport::isElasticsearch, ElasticsearchSystemDataEngine::new),
                new BuiltinProvider(-500, SystemDataEngineSupport::isMongodb, MongoSystemDataEngine::new),
                new BuiltinProvider(-400, SystemDataEngineSupport::isInfluxdb, InfluxdbSystemDataEngine::new),
                new BuiltinProvider(-300, SystemDataEngineSupport::isKafka, KafkaSystemDataEngine::new),
                new BuiltinProvider(-200, SystemDataEngineSupport::isZookeeper, ZookeeperSystemDataEngine::new),
                new BuiltinProvider(-100, SystemDataEngineSupport::isHttp, HttpSystemDataEngine::new),
                new BuiltinProvider(0, SystemDataEngineSupport::isJdbc, JdbcSystemDataEngine::new),
                new BuiltinProvider(Ordered.LOWEST_PRECEDENCE, setting -> true, LegacySpiSystemDataEngine::new)
        );
    }

    private record BuiltinProvider(
            int order,
            Predicate<SystemDataSetting> matcher,
            BiFunction<SystemDataSetting, DataSourceOptions, SystemDataEngine> factory
    ) implements SystemDataEngineProvider {

        @Override
        public boolean supports(SystemDataSetting setting) {
            return matcher.test(setting);
        }

        @Override
        public SystemDataEngine create(SystemDataSetting setting, DataSourceOptions options) {
            return factory.apply(setting, options);
        }

        @Override
        public int getOrder() {
            return order;
        }
    }
}
