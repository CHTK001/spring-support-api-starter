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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
* 指纹漂移分析器
*
* @since 2026/03/25
* @author CH
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class FingerprintDriftRequestTraceAnalyzer implements RequestTraceAnalyzer {

    @Override
    /** 分析 */
    public Collection<RequestTraceSignal> analyze(RequestTraceAnalysisContext context) {
        RequestTraceEvent currentEvent = context.currentEvent();
        if (!Boolean.TRUE.equals(context.config().getFingerprintDriftEnabled())) {
            return Set.of();
        }
        if (currentEvent == null || currentEvent.fingerprint() == null) {
            return Set.of();
        }

        String principal = currentEvent.userId() != null ? currentEvent.userId() : currentEvent.tokenHash();
        if (principal == null) {
            return Set.of();
        }
        Duration window = Duration.ofMinutes(context.config().getFingerprintDriftWindowMinutes());
        int minEventCount = context.config().getFingerprintDriftMinEvents();

        List<RequestTraceEvent> matchedEvents = context.historyWithin(window, event ->
                principal.equals(event.userId() != null ? event.userId() : event.tokenHash()));

        if (matchedEvents.size() + 1 < minEventCount) {
            return Set.of();
        }

        Set<String> fingerprints = new HashSet<>();
        fingerprints.add(currentEvent.fingerprint());
        for (RequestTraceEvent event : matchedEvents) {
            if (event.fingerprint() != null) {
                fingerprints.add(event.fingerprint());
            }
        }

        if (fingerprints.size() > 1) {
            return Set.of(RequestTraceSignal.FINGERPRINT_DRIFT);
        }
        return Set.of();
    }
}
