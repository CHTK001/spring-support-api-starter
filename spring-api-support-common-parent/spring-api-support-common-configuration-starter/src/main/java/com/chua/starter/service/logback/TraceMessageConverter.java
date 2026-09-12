package com.chua.starter.service.logback;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

/**
   * Logback日志追踪id转换器
 * <p>
   * 用于在日志中输出追踪标识，支持分布式链路追踪
 * </p>
 *
 * @since 2025/12/05
   * @版本 1.0.0
 * @author CH
 */
@ConditionalOnClass(name = "ch.qos.logback.classic.pattern.ClassicConverter")
public class TraceMessageConverter extends ClassicConverter {

    /**
      * 追踪标识的MDC键名
     */
    private static final String TRACE_ID_KEY = "traceId";

    /**
      * 默认追踪标识（当MDC中不存在时使用）
     */
    private static final String DEFAULT_TRACE_ID = "";

    /**
      * 转换日志事件，获取追踪标识
     *
     * @param event 日志事件
     * @return 追踪ID字符串
     */
    @Override
    public String convert(ILoggingEvent event) {
 // 优先从MDC中获取追踪id
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
