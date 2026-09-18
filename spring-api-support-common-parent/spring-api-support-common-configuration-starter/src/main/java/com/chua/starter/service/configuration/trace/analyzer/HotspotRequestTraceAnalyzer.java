package com.chua.starter.service.configuration.trace.analyzer;

import com.chua.starter.service.configuration.trace.RequestTraceAnalysisContext;
import com.chua.starter.service.configuration.trace.RequestTraceAnalyzer;
import com.chua.starter.service.configuration.trace.RequestTraceEvent;
import com.chua.starter.service.configuration.trace.RequestTraceSignal;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collection;
import java.util.Set;

/**
 * 热点路由分析器。
 *
 * @since 2026/03/25
 * @author CH
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class HotspotRequestTraceAnalyzer implements RequestTraceAnalyzer {

    @Override
    /** 分析 */
    public Collection<RequestTraceSignal> analyze(RequestTraceAnalysisContext context) {
        if (!Boolean.TRUE.equals(context.config().getHotspotEnabled())) {
            return Set.of();
        }
        RequestTraceEvent currentEvent = context.currentEvent();
        if (currentEvent == null || currentEvent.path() == null) {
            return Set.of();
        }

        long count = context.historyWithin(
                        Duration.ofMinutes(context.config().getHotspotWindowMinutes()),
                        event -> currentEvent.routeKey().equals(event.routeKey()))
                .size() + 1L;

        if (count >= context.config().getHotspotThreshold()) {
            return Set.of(RequestTraceSignal.HOTSPOT);
        }
        return Set.of();
    }
}
