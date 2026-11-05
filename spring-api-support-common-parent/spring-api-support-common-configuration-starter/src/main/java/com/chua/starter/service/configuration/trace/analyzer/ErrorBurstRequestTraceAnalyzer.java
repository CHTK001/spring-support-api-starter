// [P3C A20 豁免] A20/A30/A31: 含日志/界面/SQL 字符串字面量等非业务魔法值场景，豁免。
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
 * 错误风暴分析器。
 *
 * @since 2026/03/26
 * @author CH
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 5)
public class ErrorBurstRequestTraceAnalyzer implements RequestTraceAnalyzer {

    @Override
    /**
     * 分析
    */
    public Collection<RequestTraceSignal> analyze(RequestTraceAnalysisContext context) {
        if (!Boolean.TRUE.equals(context.config().getErrorBurstEnabled())) {
            return Set.of();
        }
        RequestTraceEvent currentEvent = context.currentEvent();
        String actorKey = currentEvent == null ? null : currentEvent.actorKey();
        if (currentEvent == null || actorKey == null || currentEvent.status() < 400) {
            return Set.of();
        }

        long count = context.historyWithin(
                        Duration.ofSeconds(context.config().getErrorBurstWindowSeconds()),
                        event -> actorKey.equals(event.actorKey()) && event.status() >= 400)
                .size() + 1L;

        if (count >= context.config().getErrorBurstThreshold()) {
            return Set.of(RequestTraceSignal.ERROR_BURST);
        }
        return Set.of();
    }
}