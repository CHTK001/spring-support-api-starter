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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 用户近期访问偏好分析器
 *
 * @author CH
 * @since 2026/03/25
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 30)
public class UserPreferenceRequestTraceAnalyzer implements RequestTraceAnalyzer {

    @Override
    public Collection<RequestTraceSignal> analyze(RequestTraceAnalysisContext context) {
        RequestTraceEvent currentEvent = context.currentEvent();
        if (!Boolean.TRUE.equals(context.config().getUserPreferenceDriftEnabled())) {
            return Set.of();
        }
        if (currentEvent == null || currentEvent.userId() == null) {
            return Set.of();
        }
        Duration window = Duration.ofMinutes(context.config().getUserPreferenceWindowMinutes());
        int baselineCount = context.config().getUserPreferenceBaselineCount();
        int dominantCount = context.config().getUserPreferenceDominantCount();

        List<RequestTraceEvent> history = context.historyWithin(window, event ->
                currentEvent.userId().equals(event.userId()));
        if (history.size() < baselineCount) {
            return Set.of();
        }

        DominantValue dominantIp = dominant(history, RequestTraceEvent::clientIp);
        DominantValue dominantFingerprint = dominant(history, RequestTraceEvent::fingerprint);

        boolean ipChanged = dominantIp != null
                && dominantIp.count >= dominantCount
                && dominantIp.value != null
                && currentEvent.clientIp() != null
                && !dominantIp.value.equals(currentEvent.clientIp());

        boolean fingerprintChanged = dominantFingerprint != null
                && dominantFingerprint.count >= dominantCount
                && dominantFingerprint.value != null
                && currentEvent.fingerprint() != null
                && !dominantFingerprint.value.equals(currentEvent.fingerprint());

        if (ipChanged || fingerprintChanged) {
            return Set.of(RequestTraceSignal.USER_PREFERENCE_DRIFT);
        }
        return Set.of();
    }

    private DominantValue dominant(
            List<RequestTraceEvent> history,
            java.util.function.Function<RequestTraceEvent, String> valueExtractor
    ) {
        Map<String, Integer> counter = new HashMap<>();
        for (RequestTraceEvent event : history) {
            String value = valueExtractor.apply(event);
            if (value == null || value.isBlank()) {
                continue;
            }
            counter.merge(value, 1, Integer::sum);
        }

        return counter.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> new DominantValue(entry.getKey(), entry.getValue()))
                .orElse(null);
    }

    private record DominantValue(String value, int count) {
    }
}
