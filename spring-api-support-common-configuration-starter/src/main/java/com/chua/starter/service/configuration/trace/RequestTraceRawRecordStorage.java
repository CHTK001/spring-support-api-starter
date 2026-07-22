package com.chua.starter.service.configuration.trace;

import com.chua.common.support.spi.annotations.SpiDescribe;

/**
 * 请求原始记录存储 SPI。
 *
 * @author CH
 * @since 2026/03/25
 */
@SpiDescribe("请求追踪原始记录存储SPI")
public interface RequestTraceRawRecordStorage {

    String type();

    void store(RequestTraceEvent event, RequestTraceStrategyConfig config);
}
