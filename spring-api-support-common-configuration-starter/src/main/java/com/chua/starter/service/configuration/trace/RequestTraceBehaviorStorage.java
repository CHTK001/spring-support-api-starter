package com.chua.starter.service.configuration.trace;

import com.chua.common.support.spi.annotations.SpiDescribe;

/**
 * 行为分析结果存储 SPI。
 *
 * @author CH
 * @since 2026/03/25
 */
@SpiDescribe("请求追踪行为分析结果存储SPI")
public interface RequestTraceBehaviorStorage {

    String type();

    void store(RequestTraceBehaviorRecord record, RequestTraceStrategyConfig config);
}
