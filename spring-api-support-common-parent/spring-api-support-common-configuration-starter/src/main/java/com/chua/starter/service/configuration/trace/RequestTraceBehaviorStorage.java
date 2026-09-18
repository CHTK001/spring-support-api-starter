package com.chua.starter.service.configuration.trace;

import com.chua.common.support.spi.annotations.SpiDescribe;

/**
* 行为分析结果存储 SPI。
*
* @since 2026/03/25
* @author CH
 */
@SpiDescribe("请求追踪行为分析结果存储SPI")
public interface RequestTraceBehaviorStorage {

    /**
     * 类型。
     *
     * @return 结果字符串
     */
    String type();

    /**
     * store。
     *
     * @param record 记录，不允许为 null
     * @param config 配置，不允许为 null
     */
    void store(RequestTraceBehaviorRecord record, RequestTraceStrategyConfig config);
}
