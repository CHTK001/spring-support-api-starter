package com.chua.starter.service.configuration.trace.storage;

import com.chua.common.support.core.annotation.Spi;
import com.chua.starter.service.configuration.trace.RequestTraceBehaviorRecord;
import com.chua.starter.service.configuration.trace.RequestTraceBehaviorStorage;
import com.chua.starter.service.configuration.trace.RequestTraceStrategyConfig;

/**
 * 内存占位行为结果存储。
 *
 * @author CH
 * @since 2026/03/25
 */
@Spi("memory")
public class MemoryRequestTraceBehaviorStorage implements RequestTraceBehaviorStorage {

    @Override
    public String type() {
        return "memory";
    }

    @Override
    public void store(RequestTraceBehaviorRecord record, RequestTraceStrategyConfig config) {
    }
}
