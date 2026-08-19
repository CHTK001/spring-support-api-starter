package com.chua.starter.service.configuration.trace;

import java.util.Set;

/**
 * 请求追踪行为分析结果。
 *
 * @since 2026/03/25
 * @author CH
 */
public record RequestTraceBehaviorRecord(
        long analyzedAt,
        long requestAt,
        String traceId,
        String strategyCode,
        String route,
        String requestUrl,
        int status,
        long durationMs,
        String clientIp,
        String browser,
        String userId,
        String username,
        String fingerprint,
        String userAgent,
        Set<RequestTraceSignal> signals,
        long relatedRequestCount,
        long distinctPathCount,
        String riskLevel,
        String summary,
        String aiSummary,
        RequestTraceAiAnalysisResult aiAnalysis,
        String extra
) {

    public RequestTraceBehaviorRecord {
        traceId = RequestTraceSupport.normalize(traceId);
        strategyCode = RequestTraceSupport.normalize(strategyCode);
        route = RequestTraceSupport.normalize(route);
        requestUrl = RequestTraceSupport.normalize(requestUrl);
        clientIp = RequestTraceSupport.normalize(clientIp);
        browser = RequestTraceSupport.normalize(browser);
        userId = RequestTraceSupport.normalize(userId);
        username = RequestTraceSupport.normalize(username);
        fingerprint = RequestTraceSupport.normalize(fingerprint);
        userAgent = RequestTraceSupport.normalize(userAgent);
        riskLevel = RequestTraceSupport.normalize(riskLevel);
        summary = RequestTraceSupport.normalize(summary);
        aiSummary = RequestTraceSupport.normalize(aiSummary);
        extra = RequestTraceSupport.normalize(extra);
        signals = signals == null || signals.isEmpty() ? Set.of() : Set.copyOf(signals);
    }
}
