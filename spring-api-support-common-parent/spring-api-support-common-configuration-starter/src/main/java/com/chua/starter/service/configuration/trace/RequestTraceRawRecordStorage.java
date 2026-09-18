package com.chua.starter.service.configuration.trace;

import com.chua.common.support.spi.annotations.SpiDescribe;

/**
 * 请求原始记录存储 SPI。
 *
 * @since 2026/03/25
 * @author CH
 */
@SpiDescribe("请求追踪原始记录存储SPI")
public interface RequestTraceRawRecordStorage {

    /**
     * 类型。
     *
     * @return 结果字符串
     */
    String type();

    /**
     * store。
     *
     * @param event 方法入参 event
     * @param config 配置，不允许为 null
     */
    void store(RequestTraceEvent event, RequestTraceStrategyConfig config);
}
