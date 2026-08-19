package com.chua.starter.service.configuration.trace;

import com.chua.common.support.lang.json.Json;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 请求追踪分析服务。
 *
 * @since 2026/03/25
 * @author CH
 */
@Slf4j
@Service
public class RequestTraceAnalyticsService {

    /** Recentevents */
    private final Deque<RequestTraceEvent> recentEvents = new ArrayDeque<>();
    /** Recentbehaviorrecords */
    private final Deque<RequestTraceBehaviorRecord> recentBehaviorRecords = new ArrayDeque<>();
    /** 锁 */
    private final ReentrantLock lock = new ReentrantLock();
    /** Analyzers */
    private final List<RequestTraceAnalyzer> analyzers;
    /** 请求跟踪策略配置服务 */
    private final RequestTraceStrategyConfigService requestTraceStrategyConfigService;
    /** 请求跟踪存储router */
    private final RequestTraceStorageRouter requestTraceStorageRouter;
    /** 请求跟踪AIsummary服务 */
    private final RequestTraceAiSummaryService requestTraceAiSummaryService;

    public RequestTraceAnalyticsService(
            List<RequestTraceAnalyzer> analyzers,
            RequestTraceStrategyConfigService requestTraceStrategyConfigService,
            RequestTraceStorageRouter requestTraceStorageRouter,
            RequestTraceAiSummaryService requestTraceAiSummaryService
    ) {
        List<RequestTraceAnalyzer> orderedAnalyzers = new ArrayList<>(analyzers == null ? List.of() : analyzers);
        AnnotationAwareOrderComparator.sort(orderedAnalyzers);
        this.analyzers = List.copyOf(orderedAnalyzers);
        this.requestTraceStrategyConfigService = requestTraceStrategyConfigService;
        this.requestTraceStorageRouter = requestTraceStorageRouter;
        this.requestTraceAiSummaryService = requestTraceAiSummaryService;
    }

    public RequestTraceEvent record(RequestTraceEvent event) {
        if (event == null) {
            return null;
        }
        RequestTraceStrategyConfig config = requestTraceStrategyConfigService.currentConfig();
        if (!Boolean.TRUE.equals(config.getEnabled())) {
            return event;
        }

        RequestTraceEvent stored;
        List<RequestTraceEvent> historySnapshot;
        lock.lock();
        try {
            pruneExpired(event.timestamp(), config);
            RequestTraceAnalysisContext context = new RequestTraceAnalysisContext(
                    event,
                    List.copyOf(recentEvents),
                    Instant.ofEpochMilli(event.timestamp()),
                    config
            );
            EnumSet<RequestTraceSignal> signals = EnumSet.noneOf(RequestTraceSignal.class);
            for (RequestTraceAnalyzer analyzer : analyzers) {
                signals.addAll(analyzer.analyze(context));
            }
            stored = signals.isEmpty() ? event : event.withSignals(signals);
            recentEvents.addLast(stored);
            trimOverflow(config);
            historySnapshot = List.copyOf(recentEvents);
        } finally {
            lock.unlock();
        }

        storeRawEvent(stored, config);
        if (stored.suspicious()) {
            RequestTraceBehaviorRecord behaviorRecord = buildBehaviorRecord(stored, historySnapshot, config);
            storeBehaviorRecord(behaviorRecord, config);
            rememberBehaviorRecord(behaviorRecord, config);
            log.warn(
                    "[RequestTrace] risk={}, signals={}, traceId={}, route={}, userId={}, ip={}, browser={}, status={}, cost={}ms, ai={}",
                    behaviorRecord.riskLevel(),
                    RequestTraceSupport.signalCodes(stored.signals()),
                    stored.traceId(),
                    stored.routeKey(),
                    stored.userId(),
                    stored.clientIp(),
                    stored.browser(),
                    stored.status(),
                    stored.durationMs(),
                    RequestTraceSupport.firstNonBlank(behaviorRecord.aiSummary(), behaviorRecord.summary(), "-")
            );
        }
        return stored;
    }

    public RequestTraceAnalyticsSnapshot snapshot(Integer requestedWindowMinutes) {
        RequestTraceStrategyConfig config = requestTraceStrategyConfigService.currentConfig();
        int windowMinutes = normalizeWindowMinutes(requestedWindowMinutes, config);
        lock.lock();
        try {
            pruneExpired(System.currentTimeMillis(), config);
            long cutoff = Instant.now().minus(Duration.ofMinutes(windowMinutes)).toEpochMilli();
            List<RequestTraceEvent> windowEvents = recentEvents.stream()
                    .filter(event -> event.timestamp() >= cutoff)
                    .toList();
            Map<String, RequestTraceBehaviorRecord> behaviorByTraceId = recentBehaviorRecords.stream()
                    .filter(item -> item.requestAt() >= cutoff)
                    .collect(LinkedHashMap::new, (map, item) -> map.put(item.traceId(), item), LinkedHashMap::putAll);

            long suspiciousRequests = windowEvents.stream().filter(RequestTraceEvent::suspicious).count();
            long uniqueUsers = windowEvents.stream()
                    .map(RequestTraceEvent::userId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .count();
            long uniqueIps = windowEvents.stream()
                    .map(RequestTraceEvent::clientIp)
                    .filter(Objects::nonNull)
                    .distinct()
                    .count();

            Map<RequestTraceSignal, Long> signalCounter = new EnumMap<>(RequestTraceSignal.class);
            for (RequestTraceEvent item : windowEvents) {
                for (RequestTraceSignal signal : item.signals()) {
                    signalCounter.merge(signal, 1L, Long::sum);
                }
            }

            List<RequestTraceAnalyticsSnapshot.SignalBucket> signals = signalCounter.entrySet().stream()
                    .sorted(Map.Entry.<RequestTraceSignal, Long>comparingByValue().reversed())
                    .map(entry -> new RequestTraceAnalyticsSnapshot.SignalBucket(
                            entry.getKey().getCode(),
                            entry.getKey().getDescription(),
                            entry.getValue()
                    ))
                    .toList();

            List<RequestTraceAnalyticsSnapshot.MetricBucket> topPaths = toMetricBuckets(
                    windowEvents,
                    RequestTraceEvent::routeKey,
                    config.getMaxTopBuckets()
            );
            List<RequestTraceAnalyticsSnapshot.MetricBucket> topIps = toMetricBuckets(
                    windowEvents,
                    RequestTraceEvent::clientIp,
                    config.getMaxTopBuckets()
            );
            List<RequestTraceAnalyticsSnapshot.MetricBucket> topUsers = toMetricBuckets(
                    windowEvents,
                    item -> firstNonBlank(item.username(), item.userId()),
                    config.getMaxTopBuckets()
            );

            List<RequestTraceAnalyticsSnapshot.SuspiciousTrace> suspiciousTraces = windowEvents.stream()
                    .filter(RequestTraceEvent::suspicious)
                    .sorted(Comparator.comparingLong(RequestTraceEvent::timestamp).reversed())
                    .limit(config.getMaxSuspiciousEvents())
                    .map(item -> new RequestTraceAnalyticsSnapshot.SuspiciousTrace(
                            item.timestamp(),
                            item.traceId(),
                            item.routeKey(),
                            item.status(),
                            item.durationMs(),
                            item.clientIp(),
                            item.userId(),
                            item.username(),
                            item.fingerprint(),
                            riskLevelOf(behaviorByTraceId.get(item.traceId()), item),
                            summaryOf(behaviorByTraceId.get(item.traceId())),
                            aiSummaryOf(behaviorByTraceId.get(item.traceId())),
                            aiAnalysisOf(behaviorByTraceId.get(item.traceId())),
                            item.signals()
                    ))
                    .toList();

            return new RequestTraceAnalyticsSnapshot(
                    Instant.now(),
                    windowMinutes,
                    windowEvents.size(),
                    suspiciousRequests,
                    uniqueUsers,
                    uniqueIps,
                    signals,
                    topPaths,
                    topIps,
                    topUsers,
                    suspiciousTraces
            );
        } finally {
            lock.unlock();
        }
    }

    public List<RequestTraceEvent> recentEvents() {
        RequestTraceStrategyConfig config = requestTraceStrategyConfigService.currentConfig();
        lock.lock();
        try {
            pruneExpired(System.currentTimeMillis(), config);
            return List.copyOf(recentEvents);
        } finally {
            lock.unlock();
        }
    }

    private RequestTraceBehaviorRecord buildBehaviorRecord(
            RequestTraceEvent event,
            List<RequestTraceEvent> history,
            RequestTraceStrategyConfig config
    ) {
        String actorKey = RequestTraceSupport.firstNonBlank(event.actorKey(), event.traceId());
        List<RequestTraceEvent> relatedEvents = history.stream()
                .filter(candidate -> actorKey.equals(RequestTraceSupport.firstNonBlank(candidate.actorKey(), candidate.traceId())))
                .toList();
        long relatedRequestCount = relatedEvents.size();
        long distinctPathCount = relatedEvents.stream()
                .map(RequestTraceEvent::path)
                .filter(path -> path != null && !path.isBlank())
                .distinct()
                .count();
        String riskLevel = RequestTraceSupport.resolveRiskLevel(event.signals());
        String summary = buildSummary(event, relatedRequestCount, distinctPathCount, riskLevel);
        RequestTraceAiAnalysisResult aiAnalysis = requestTraceAiSummaryService.summarize(
                event,
                event.signals(),
                relatedRequestCount,
                distinctPathCount,
                riskLevel,
                config
        );
        String aiSummary = aiAnalysis == null ? null : aiAnalysis.finalSummary();

        Map<String, Object> extra = new LinkedHashMap<>();
        extra.put("signalCodes", RequestTraceSupport.signalCodes(event.signals()));
        extra.put("appName", event.appName());
        extra.put("method", event.method());
        extra.put("browser", event.browser());
        extra.put("routePattern", event.routePattern());
        extra.put("status", event.status());
        extra.put("ai", toAiMap(aiAnalysis));

        return new RequestTraceBehaviorRecord(
                System.currentTimeMillis(),
                event.timestamp(),
                event.traceId(),
                event.strategyCode(),
                event.routeKey(),
                event.requestUrl(),
                event.status(),
                event.durationMs(),
                event.clientIp(),
                event.browser(),
                event.userId(),
                event.username(),
                event.fingerprint(),
                event.userAgent(),
                event.signals(),
                relatedRequestCount,
                distinctPathCount,
                riskLevel,
                summary,
                aiSummary,
                aiAnalysis,
                Json.toJson(extra)
        );
    }

    private String buildSummary(
            RequestTraceEvent event,
            long relatedRequestCount,
            long distinctPathCount,
            String riskLevel
    ) {
        List<String> parts = new ArrayList<>();
        parts.add("风险等级=" + riskLevel);
        parts.add("主体=" + RequestTraceSupport.firstNonBlank(event.userId(), event.fingerprint(), event.clientIp(), "anonymous"));
        parts.add("路由=" + event.routeKey());
        parts.add("信号=" + RequestTraceSupport.signalCodes(event.signals()));
        parts.add("浏览器=" + RequestTraceSupport.firstNonBlank(event.browser(), "unknown"));
        parts.add("关联请求=" + relatedRequestCount);
        if (distinctPathCount > 0) {
            parts.add("扫描路径=" + distinctPathCount);
        }
        return String.join("；", parts);
    }

    private void storeRawEvent(RequestTraceEvent event, RequestTraceStrategyConfig config) {
        try {
            requestTraceStorageRouter.rawStorage(config.getRawStorageType()).store(event, config);
        } catch (Exception ex) {
            log.debug("存储请求追踪原始记录失败: traceId={}", event.traceId(), ex);
        }
    }

    private void storeBehaviorRecord(RequestTraceBehaviorRecord record, RequestTraceStrategyConfig config) {
        try {
            requestTraceStorageRouter.behaviorStorage(config.getBehaviorStorageType()).store(record, config);
        } catch (Exception ex) {
            log.debug("存储请求追踪行为记录失败: traceId={}", record.traceId(), ex);
        }
    }

    private void trimOverflow(RequestTraceStrategyConfig config) {
        while (recentEvents.size() > config.getMaxEvents()) {
            recentEvents.pollFirst();
        }
        while (recentBehaviorRecords.size() > config.getMaxEvents()) {
            recentBehaviorRecords.pollFirst();
        }
    }

    private void pruneExpired(long now, RequestTraceStrategyConfig config) {
        long cutoff = now - Duration.ofMinutes(config.getRetentionMinutes()).toMillis();
        while (!recentEvents.isEmpty()) {
            RequestTraceEvent first = recentEvents.peekFirst();
            if (first == null || first.timestamp() >= cutoff) {
                break;
            }
            recentEvents.pollFirst();
        }
        while (!recentBehaviorRecords.isEmpty()) {
            RequestTraceBehaviorRecord first = recentBehaviorRecords.peekFirst();
            if (first == null || first.requestAt() >= cutoff) {
                break;
            }
            recentBehaviorRecords.pollFirst();
        }
    }

    private int normalizeWindowMinutes(Integer requestedWindowMinutes, RequestTraceStrategyConfig config) {
        if (requestedWindowMinutes == null || requestedWindowMinutes <= 0) {
            return config.getDefaultWindowMinutes();
        }
        return Math.min(requestedWindowMinutes, config.getMaxWindowMinutes());
    }

    private List<RequestTraceAnalyticsSnapshot.MetricBucket> toMetricBuckets(
            List<RequestTraceEvent> events,
            java.util.function.Function<RequestTraceEvent, String> keyFunction,
            int limit
    ) {
        Map<String, Long> counter = new HashMap<>();
        for (RequestTraceEvent event : events) {
            String key = keyFunction.apply(event);
            if (key == null || key.isBlank()) {
                continue;
            }
            counter.merge(key, 1L, Long::sum);
        }
        return counter.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> new RequestTraceAnalyticsSnapshot.MetricBucket(entry.getKey(), entry.getValue()))
                .toList();
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        return second;
    }

    private void rememberBehaviorRecord(RequestTraceBehaviorRecord record, RequestTraceStrategyConfig config) {
        if (record == null) {
            return;
        }
        lock.lock();
        try {
            pruneExpired(record.requestAt(), config);
            recentBehaviorRecords.addLast(record);
            trimOverflow(config);
        } finally {
            lock.unlock();
        }
    }

    private RequestTraceAiAnalysisResult aiAnalysisOf(RequestTraceBehaviorRecord record) {
        return record == null ? null : record.aiAnalysis();
    }

    private String aiSummaryOf(RequestTraceBehaviorRecord record) {
        return record == null ? null : record.aiSummary();
    }

    private String summaryOf(RequestTraceBehaviorRecord record) {
        return record == null ? null : record.summary();
    }

    private String riskLevelOf(RequestTraceBehaviorRecord record, RequestTraceEvent event) {
        if (record != null && record.riskLevel() != null) {
            return record.riskLevel();
        }
        return event == null ? null : RequestTraceSupport.resolveRiskLevel(event.signals());
    }

    private Map<String, Object> toAiMap(RequestTraceAiAnalysisResult aiAnalysis) {
        if (aiAnalysis == null) {
            return Map.of();
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("analyzedAt", aiAnalysis.analyzedAt());
        result.put("status", aiAnalysis.status());
        result.put("summarySource", aiAnalysis.summarySource());
        result.put("summary", aiAnalysis.summary());
        result.put("heuristicSummary", aiAnalysis.heuristicSummary());
        result.put("reason", aiAnalysis.reason());
        result.put("model", aiAnalysis.model());
        result.put("riskLevel", aiAnalysis.riskLevel());
        result.put("promptDigest", aiAnalysis.promptDigest());
        result.put("latencyMs", aiAnalysis.latencyMs());
        return result;
    }
}
