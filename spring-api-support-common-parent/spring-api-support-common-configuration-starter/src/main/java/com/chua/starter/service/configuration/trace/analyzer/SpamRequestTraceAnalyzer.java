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
 * 刷屏/重复请求分析器。
 *
 * @since 2026/03/25
 * @author CH
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
public class SpamRequestTraceAnalyzer implements RequestTraceAnalyzer {

    @Override
    /**
     * 分析
    */
    public Collection<RequestTraceSignal> analyze(RequestTraceAnalysisContext context) {
        if (!Boolean.TRUE.equals(context.config().getSpamEnabled())) {
            return Set.of();
        }
        RequestTraceEvent currentEvent = context.currentEvent();
        String actorKey = currentEvent == null ? null : currentEvent.actorKey();
        if (currentEvent == null || actorKey == null || currentEvent.path() == null) {
            return Set.of();
        }

        long count = context.historyWithin(
                        Duration.ofSeconds(context.config().getSpamWindowSeconds()),
                        event -> actorKey.equals(event.actorKey())
                                && currentEvent.routeKey().equals(event.routeKey()))
                .size() + 1L;

        if (count >= context.config().getSpamThreshold()) {
            return Set.of(RequestTraceSignal.SPAM);
        }
        return Set.of();
    }
}
