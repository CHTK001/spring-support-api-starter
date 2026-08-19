package com.chua.starter.service.configuration.trace;

import java.time.Instant;
import java.util.List;
import java.util.Set;

/**
 * 请求追踪分析快照
 *
 * @since 2026/03/25
 * @author CH
 */
public record RequestTraceAnalyticsSnapshot(
        Instant generatedAt,
        int windowMinutes,
        long totalRequests,
        long suspiciousRequests,
        long uniqueUsers,
        long uniqueIps,
        List<SignalBucket> signals,
        List<MetricBucket> topPaths,
        List<MetricBucket> topIps,
        List<MetricBucket> topUsers,
        List<SuspiciousTrace> recentSuspiciousEvents
) {

    public RequestTraceAnalyticsSnapshot {
        signals = signals == null ? List.of() : List.copyOf(signals);
        topPaths = topPaths == null ? List.of() : List.copyOf(topPaths);
        topIps = topIps == null ? List.of() : List.copyOf(topIps);
        topUsers = topUsers == null ? List.of() : List.copyOf(topUsers);
        recentSuspiciousEvents = recentSuspiciousEvents == null ? List.of() : List.copyOf(recentSuspiciousEvents);
    }

    /** SignalBucket */
    public record SignalBucket(String code, String description, long count) {
    }

    /** MetricBucket */
    public record MetricBucket(String key, long count) {
    }

    /**
     * SuspiciousTrace
     * @param timestamp timestamp
     * @param traceId traceId
     * @param route route
     * @param status status
     * @param durationMs durationMs
     * @param clientIp clientIp
     * @param userId userId
     * @param username username
     * @param fingerprint fingerprint
     * @param riskLevel riskLevel
     * @param summary summary
     * @param aiSummary aiSummary
     * @param aiAnalysis aiAnalysis
     * @param signals signals
     */
    public record SuspiciousTrace(
            long timestamp,
            String traceId,
            String route,
            int status,
            long durationMs,
            String clientIp,
            String userId,
            String username,
            String fingerprint,
            String riskLevel,
            String summary,
            String aiSummary,
            RequestTraceAiAnalysisResult aiAnalysis,
            Set<RequestTraceSignal> signals
    ) {
        public SuspiciousTrace {
            signals = signals == null || signals.isEmpty() ? Set.of() : Set.copyOf(signals);
        }
    }
}
