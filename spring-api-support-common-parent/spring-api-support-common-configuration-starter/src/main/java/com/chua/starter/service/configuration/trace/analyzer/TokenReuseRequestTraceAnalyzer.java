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
 * 令牌复用分析器
 *
 * @since 2026/03/25
 * @author CH
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class TokenReuseRequestTraceAnalyzer implements RequestTraceAnalyzer {

    @Override
    public Collection<RequestTraceSignal> analyze(RequestTraceAnalysisContext context) {
        RequestTraceEvent currentEvent = context.currentEvent();
        if (!Boolean.TRUE.equals(context.config().getTokenReuseEnabled())) {
            return Set.of();
        }
        if (currentEvent == null || currentEvent.tokenHash() == null) {
            return Set.of();
        }
        Duration window = Duration.ofMinutes(context.config().getTokenReuseWindowMinutes());
        int minEventCount = context.config().getTokenReuseMinEvents();

        List<RequestTraceEvent> matchedEvents = context.historyWithin(window, event ->
                currentEvent.tokenHash().equals(event.tokenHash()));

        if (matchedEvents.size() + 1 < minEventCount) {
            return Set.of();
        }

        Set<String> ips = new HashSet<>();
        Set<String> fingerprints = new HashSet<>();
        Set<String> users = new HashSet<>();
        collectContext(currentEvent, ips, fingerprints, users);
        for (RequestTraceEvent event : matchedEvents) {
            collectContext(event, ips, fingerprints, users);
        }

        boolean reusedAcrossContext = ips.size() > 1 || fingerprints.size() > 1 || users.size() > 1;
        if (reusedAcrossContext) {
            return Set.of(RequestTraceSignal.TOKEN_REUSE);
        }
        return Set.of();
    }

    /**
     * CollectContext
     * @param event event
     * @param ips ips
     * @param fingerprints fingerprints
     * @param users users
     */
    private void collectContext(
            RequestTraceEvent event,
            Set<String> ips,
            Set<String> fingerprints,
            Set<String> users
    ) {
        if (event.clientIp() != null) {
            ips.add(event.clientIp());
        }
        if (event.fingerprint() != null) {
            fingerprints.add(event.fingerprint());
        }
        if (event.userId() != null) {
            users.add(event.userId());
        }
    }
}
