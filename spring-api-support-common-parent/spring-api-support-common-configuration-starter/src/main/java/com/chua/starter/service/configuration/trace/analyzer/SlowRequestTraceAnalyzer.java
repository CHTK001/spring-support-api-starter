package com.chua.starter.service.configuration.trace.analyzer;

import com.chua.starter.service.configuration.trace.RequestTraceAnalysisContext;
import com.chua.starter.service.configuration.trace.RequestTraceAnalyzer;
import com.chua.starter.service.configuration.trace.RequestTraceEvent;
import com.chua.starter.service.configuration.trace.RequestTraceSignal;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;

/**
 * 慢请求分析器。
 *
 * @since 2026/03/25
 * @author CH
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 4)
public class SlowRequestTraceAnalyzer implements RequestTraceAnalyzer {

    @Override
    /**
     * 分析
    */
    public Collection<RequestTraceSignal> analyze(RequestTraceAnalysisContext context) {
        if (!Boolean.TRUE.equals(context.config().getSlowRequestEnabled())) {
            return Set.of();
        }
        RequestTraceEvent currentEvent = context.currentEvent();
        if (currentEvent == null) {
            return Set.of();
        }
        if (currentEvent.durationMs() >= context.config().getSlowRequestThresholdMs()) {
            return Set.of(RequestTraceSignal.SLOW_REQUEST);
        }
        return Set.of();
    }
}
