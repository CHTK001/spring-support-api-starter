package com.chua.starter.service.configuration.trace;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** @author CH */
class RequestTraceStrategyConfigServiceTest {

    @Test
    void updateRejectsUnsupportedStorageTypes() {
        RequestTraceStrategyConfigService service = createService(new StubConfigStorage(), "memory");

        RequestTraceStrategyConfig invalidRaw = new RequestTraceStrategyConfig();
        invalidRaw.setRawStorageType("oss");
        assertThatThrownBy(() -> service.update(invalidRaw))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("rawStorageType")
                .hasMessageContaining("file");

        RequestTraceStrategyConfig invalidBehavior = new RequestTraceStrategyConfig();
        invalidBehavior.setBehaviorStorageType("kafka");
        assertThatThrownBy(() -> service.update(invalidBehavior))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("behaviorStorageType")
                .hasMessageContaining("database");
    }

    @Test
    void refreshFallsBackToBuiltInStorageTypesWhenPersistedValueIsInvalid() {
        StubConfigStorage storage = new StubConfigStorage();
        RequestTraceStrategyConfig loaded = new RequestTraceStrategyConfig();
        loaded.setRawStorageType("oss");
        loaded.setBehaviorStorageType("kafka");
        storage.loaded = loaded;

        RequestTraceStrategyConfigService service = createService(storage, "memory");
        RequestTraceStrategyConfig config = service.refresh();

        assertThat(config.getRawStorageType()).isEqualTo("file");
        assertThat(config.getBehaviorStorageType()).isEqualTo("database");
    }

    @Test
    void configStorageTypeFallsBackToDatabaseWhenRuntimeValueIsInvalid() {
        RequestTraceStrategyConfigService service = createService(new StubConfigStorage(), "unknown");

        assertThat(service.configStorageType()).isEqualTo("database");
    }

    @Test
    void refreshUsesRuntimeAiDefaultsWhenNoPersistedConfigExists() {
        StubConfigStorage storage = new StubConfigStorage();
        storage.loaded = null;

        RequestTraceStrategyRuntimeProperties runtimeProperties = new RequestTraceStrategyRuntimeProperties();
        runtimeProperties.setConfigStorageType("memory");
        runtimeProperties.setAiSummaryEnabled(false);
        runtimeProperties.setAiModel("deepseek-chat");
        runtimeProperties.setAiRateLimitEnabled(true);
        runtimeProperties.setAiRateLimitWindowSeconds(15);
        runtimeProperties.setAiRateLimitMaxRequests(8);
        runtimeProperties.setAiTimeoutMs(900L);

        RequestTraceStorageRouter router = new RequestTraceStorageRouter() {
            @Override
            /** ConfigStorage */
            public RequestTraceConfigStorage configStorage(String type) {
                return storage;
            }
        };

        RequestTraceStrategyConfigService service = new RequestTraceStrategyConfigService(runtimeProperties, router);
        RequestTraceStrategyConfig config = service.refresh();

        assertThat(config.getAiSummaryEnabled()).isFalse();
        assertThat(config.getAiModel()).isEqualTo("deepseek-chat");
        assertThat(config.getAiRateLimitEnabled()).isTrue();
        assertThat(config.getAiRateLimitWindowSeconds()).isEqualTo(15);
        assertThat(config.getAiRateLimitMaxRequests()).isEqualTo(8);
        assertThat(config.getAiTimeoutMs()).isEqualTo(900L);
    }

    /** 创建Service */
    private RequestTraceStrategyConfigService createService(StubConfigStorage storage, String runtimeStorageType) {
        RequestTraceStrategyRuntimeProperties runtimeProperties = new RequestTraceStrategyRuntimeProperties();
        runtimeProperties.setConfigStorageType(runtimeStorageType);
        RequestTraceStorageRouter router = new RequestTraceStorageRouter() {
            @Override
            /** ConfigStorage */
            public RequestTraceConfigStorage configStorage(String type) {
                return storage;
            }
        };
        return new RequestTraceStrategyConfigService(runtimeProperties, router);
    }

    private static final class StubConfigStorage implements RequestTraceConfigStorage {

        /** Loaded */
        private RequestTraceStrategyConfig loaded = new RequestTraceStrategyConfig();

        @Override
        /** Type */
        public String type() {
            return "memory";
        }

        @Override
        /** 加载 */
        public RequestTraceStrategyConfig load(String groupCode) {
            return loaded == null ? null : loaded.copy();
        }

        @Override
        /** 保存 */
        public RequestTraceStrategyConfig save(String groupCode, RequestTraceStrategyConfig config) {
            loaded = config == null ? null : config.copy();
            return load(groupCode);
        }
    }
}

