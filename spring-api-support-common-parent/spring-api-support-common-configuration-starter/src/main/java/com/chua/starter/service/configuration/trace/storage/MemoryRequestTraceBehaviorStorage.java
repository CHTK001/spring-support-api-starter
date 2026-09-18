package com.chua.starter.service.configuration.trace.storage;

import com.chua.common.support.spi.annotations.Spi;
import com.chua.starter.service.configuration.trace.RequestTraceBehaviorRecord;
import com.chua.starter.service.configuration.trace.RequestTraceBehaviorStorage;
import com.chua.starter.service.configuration.trace.RequestTraceStrategyConfig;

/**
 * 内存占位行为结果存储。
 *
 * @since 2026/03/25
 * @author CH
 */
@Spi("memory")
public class MemoryRequestTraceBehaviorStorage implements RequestTraceBehaviorStorage {

    @Override
    /** 类型 */
    public String type() {
        return "memory";
    }

    @Override
    /** 存储 */
    public void store(RequestTraceBehaviorRecord record, RequestTraceStrategyConfig config) {
    }
}
