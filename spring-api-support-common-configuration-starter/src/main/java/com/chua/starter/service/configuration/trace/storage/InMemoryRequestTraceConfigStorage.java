package com.chua.starter.service.configuration.trace.storage;

import com.chua.common.support.core.annotation.Spi;
import com.chua.starter.service.configuration.trace.RequestTraceConfigStorage;
import com.chua.starter.service.configuration.trace.RequestTraceStrategyConfig;
import com.chua.starter.service.configuration.trace.RequestTraceStrategyConfigCodec;

import java.util.concurrent.atomic.AtomicReference;

/**
 * 内存版配置存储。
 *
 * @author CH
 * @since 2026/03/25
 */
@Spi("memory")
public class InMemoryRequestTraceConfigStorage implements RequestTraceConfigStorage {

    private static final AtomicReference<RequestTraceStrategyConfig> CACHE =
            new AtomicReference<>();

    @Override
    public String type() {
        return "memory";
    }

    @Override
    public RequestTraceStrategyConfig load(String groupCode) {
        RequestTraceStrategyConfig config = CACHE.get();
        return config == null ? null : config.copy();
    }

    @Override
    public RequestTraceStrategyConfig save(String groupCode, RequestTraceStrategyConfig config) {
        RequestTraceStrategyConfig normalized = RequestTraceStrategyConfigCodec.normalize(config);
        CACHE.set(normalized);
        return normalized.copy();
    }
}
