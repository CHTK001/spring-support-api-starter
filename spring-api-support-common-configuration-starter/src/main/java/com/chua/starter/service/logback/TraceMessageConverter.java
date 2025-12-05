package com.chua.starter.service.logback;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.slf4j.MDC;

/**
 * Logback日志TraceId转换器
 * <p>
 * 用于在日志中输出追踪ID，支持分布式链路追踪
 * </p>
 *
 * @author CH
 * @since 2025/12/05
 * @version 1.0.0
 */
public class TraceMessageConverter extends ClassicConverter {

    /**
     * 追踪ID的MDC键名
     */
    private static final String TRACE_ID_KEY = "traceId";

    /**
     * 默认追踪ID（当MDC中不存在时使用）
     */
    private static final String DEFAULT_TRACE_ID = "";

    /**
     * 转换日志事件，获取追踪ID
     *
     * @param event 日志事件
     * @return 追踪ID字符串
     */
    @Override
    public String convert(ILoggingEvent event) {
        // 优先从MDC中获取traceId
        String traceId = MDC.get(TRACE_ID_KEY);
        if (traceId != null && !traceId.isEmpty()) {
            return traceId;
        }

        // 尝试从事件的MDC属性中获取
        if (event.getMDCPropertyMap() != null) {
            traceId = event.getMDCPropertyMap().get(TRACE_ID_KEY);
            if (traceId != null && !traceId.isEmpty()) {
                return traceId;
            }
        }

        return DEFAULT_TRACE_ID;
    }
}
