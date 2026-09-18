package com.chua.starter.service.configuration.trace;

import com.chua.common.support.spi.annotations.SpiDescribe;

/**
 * 请求追踪配置存储 SPI。
 *
 * @since 2026/03/25
 * @author CH
 */
@SpiDescribe("请求追踪策略配置存储SPI")
public interface RequestTraceConfigStorage {

    /**
     * 类型。
     *
     * @return 结果字符串
     */
    String type();

    /**
     * 加载。
     *
     * @param groupCode 分组编码，不允许为 null
     * @return 请求TraceStrategy配置 对象
     */
    RequestTraceStrategyConfig load(String groupCode);

    /**
     * 保存。
     *
     * @param groupCode 分组编码，不允许为 null
     * @param config 配置，不允许为 null
     * @return 请求TraceStrategy配置 对象
     */
    RequestTraceStrategyConfig save(String groupCode, RequestTraceStrategyConfig config);
}
