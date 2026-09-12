package com.chua.starter.service.configuration.trace;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.function.Predicate;

/**
* 请求追踪分析上下文
*
* @since 2026/03/25
* @author CH
 */
public record RequestTraceAnalysisContext(
        RequestTraceEvent currentEvent,
        List<RequestTraceEvent> recentEvents,
        Instant now,
        RequestTraceStrategyConfig config
) {

    public RequestTraceAnalysisContext {
        recentEvents = recentEvents == null ? List.of() : List.copyOf(recentEvents);
        now = now == null ? Instant.now() : now;
        config = RequestTraceStrategyConfigCodec.normalize(config);
    }

    /**
    * 历史within
    *
    * @param duration 持续时间
    * @return 历史within的结果
     */
    public List<RequestTraceEvent> historyWithin(Duration duration) {
        return historyWithin(duration, event -> true);
    }

    /**
    * 历史within
    *
    * @param duration 持续时间
    * @param predicate predicate
    * @return 历史within的结果
     */
    public List<RequestTraceEvent> historyWithin(Duration duration, Predicate<RequestTraceEvent> predicate) {
        long cutoff = now.minus(duration).toEpochMilli();
        return recentEvents.stream()
                .filter(event -> event.timestamp() >= cutoff)
                .filter(predicate)
                .toList();
    }
}
