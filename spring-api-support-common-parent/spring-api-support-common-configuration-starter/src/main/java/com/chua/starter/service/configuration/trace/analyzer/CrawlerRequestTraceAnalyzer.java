package com.chua.starter.service.configuration.trace.analyzer;

import com.chua.starter.service.configuration.trace.RequestTraceAnalysisContext;
import com.chua.starter.service.configuration.trace.RequestTraceAnalyzer;
import com.chua.starter.service.configuration.trace.RequestTraceEvent;
import com.chua.starter.service.configuration.trace.RequestTraceSignal;
import com.chua.starter.service.configuration.trace.RequestTraceSupport;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 爬虫扫描分析器。
 *
 * @since 2026/03/25
 * @author CH
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 3)
public class CrawlerRequestTraceAnalyzer implements RequestTraceAnalyzer {

    @Override
    /** Analyze */
    public Collection<RequestTraceSignal> analyze(RequestTraceAnalysisContext context) {
        if (!Boolean.TRUE.equals(context.config().getCrawlerEnabled())) {
            return Set.of();
        }
        RequestTraceEvent currentEvent = context.currentEvent();
        String actorKey = currentEvent == null ? null : currentEvent.actorKey();
        if (currentEvent == null || actorKey == null) {
            return Set.of();
        }

        List<RequestTraceEvent> history = context.historyWithin(
                Duration.ofMinutes(context.config().getCrawlerWindowMinutes()),
                event -> actorKey.equals(event.actorKey()));

        long requestCount = history.size() + 1L;
        long distinctPathCount = history.stream()
                .map(RequestTraceEvent::path)
                .filter(path -> path != null && !path.isBlank())
                .distinct()
                .count();
        if (currentEvent.path() != null
                && history.stream().noneMatch(event -> currentEvent.path().equals(event.path()))) {
            distinctPathCount += 1L;
        }

        boolean thresholdMatched = requestCount >= context.config().getCrawlerRequestThreshold()
                && distinctPathCount >= context.config().getCrawlerDistinctPathThreshold();
        boolean userAgentMatched = RequestTraceSupport.isCrawlerUserAgent(currentEvent.userAgent())
                && distinctPathCount >= Math.max(3, context.config().getCrawlerDistinctPathThreshold() / 2);

        if (thresholdMatched || userAgentMatched) {
            return Set.of(RequestTraceSignal.CRAWLER);
        }
        return Set.of();
    }
}
