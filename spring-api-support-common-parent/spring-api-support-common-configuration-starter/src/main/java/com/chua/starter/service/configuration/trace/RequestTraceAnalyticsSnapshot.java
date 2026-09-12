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

    /**
     * 标志bucket
     *
     * @param code 编码
     * @param description description
     * @param count 数量
     * @return 标志bucket的结果
     */
    public record SignalBucket(String code, String description, long count) {
    }

    /**
     * 指标bucket
     *
     * @param key 键
     * @param count 数量
     * @return 指标bucket的结果
     */
    public record MetricBucket(String key, long count) {
    }

    /**
      * suspicious追踪
     * @param timestamp 时间戳
     * @param traceId 追踪id
     * @param route route
     * @param status 状态
     * @param durationMs 持续时间ms
     * @param clientIp 客户端ip
     * @param userId 用户id
     * @param username 用户名
     * @param fingerprint fingerprint
     * @param riskLevel risk级别
     * @param summary summary
     * @param aiSummary AIsummary
     * @param aiAnalysis AIanalysis
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
