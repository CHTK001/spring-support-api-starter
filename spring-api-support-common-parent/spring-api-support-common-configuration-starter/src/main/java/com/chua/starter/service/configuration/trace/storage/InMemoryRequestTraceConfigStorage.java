package com.chua.starter.service.configuration.trace.storage;

import com.chua.common.support.spi.annotations.Spi;
import com.chua.starter.service.configuration.trace.RequestTraceConfigStorage;
import com.chua.starter.service.configuration.trace.RequestTraceStrategyConfig;
import com.chua.starter.service.configuration.trace.RequestTraceStrategyConfigCodec;

import java.util.concurrent.atomic.AtomicReference;

/**
 * 内存版配置存储。
 *
 * @since 2026/03/25
 * @author CH
 */
@Spi("memory")
public class InMemoryRequestTraceConfigStorage implements RequestTraceConfigStorage {

    /** 缓存 */
    private static final AtomicReference<RequestTraceStrategyConfig> CACHE =
            new AtomicReference<>();

    @Override
    /** Type */
    public String type() {
        return "memory";
    }

    @Override
    /** 加载 */
    public RequestTraceStrategyConfig load(String groupCode) {
        RequestTraceStrategyConfig config = CACHE.get();
        return config == null ? null : config.copy();
    }

    @Override
    /** 保存 */
    public RequestTraceStrategyConfig save(String groupCode, RequestTraceStrategyConfig config) {
        RequestTraceStrategyConfig normalized = RequestTraceStrategyConfigCodec.normalize(config);
        CACHE.set(normalized);
        return normalized.copy();
    }
}
