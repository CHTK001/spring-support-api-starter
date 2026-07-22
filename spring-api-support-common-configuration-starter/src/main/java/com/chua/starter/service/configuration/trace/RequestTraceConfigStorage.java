package com.chua.starter.service.configuration.trace;

import com.chua.common.support.spi.annotations.SpiDescribe;

/**
 * 请求追踪配置存储 SPI。
 *
 * @author CH
 * @since 2026/03/25
 */
@SpiDescribe("请求追踪策略配置存储SPI")
public interface RequestTraceConfigStorage {

    String type();

    RequestTraceStrategyConfig load(String groupCode);

    RequestTraceStrategyConfig save(String groupCode, RequestTraceStrategyConfig config);
}
