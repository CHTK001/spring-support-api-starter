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
 * 高频请求分析器
 *
 * @since 2026/03/25
 * @author CH
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class BurstRequestTraceAnalyzer implements RequestTraceAnalyzer {

    @Override
    /**
     * 分析
    */
    public Collection<RequestTraceSignal> analyze(RequestTraceAnalysisContext context) {
        RequestTraceEvent currentEvent = context.currentEvent();
        if (!Boolean.TRUE.equals(context.config().getBurstEnabled())) {
            return Set.of();
        }
        if (currentEvent == null || currentEvent.clientIp() == null || currentEvent.path() == null) {
            return Set.of();
        }
        Duration window = Duration.ofSeconds(context.config().getBurstWindowSeconds());
        long threshold = context.config().getBurstThreshold();

        long hitCount = context.historyWithin(window, event ->
                        currentEvent.clientIp().equals(event.clientIp())
                                && currentEvent.routeKey().equals(event.routeKey()))
                .size() + 1L;

        if (hitCount >= threshold) {
            return Set.of(RequestTraceSignal.BURST);
        }
        return Set.of();
    }
}
