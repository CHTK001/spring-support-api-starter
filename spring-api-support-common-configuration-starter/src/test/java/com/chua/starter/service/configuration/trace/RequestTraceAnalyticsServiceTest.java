package com.chua.starter.service.configuration.trace;

import com.chua.starter.service.configuration.trace.analyzer.BurstRequestTraceAnalyzer;
import com.chua.starter.service.configuration.trace.analyzer.CrawlerRequestTraceAnalyzer;
import com.chua.starter.service.configuration.trace.analyzer.ErrorBurstRequestTraceAnalyzer;
import com.chua.starter.service.configuration.trace.analyzer.HotspotRequestTraceAnalyzer;
import com.chua.starter.service.configuration.trace.analyzer.SlowRequestTraceAnalyzer;
import com.chua.starter.service.configuration.trace.analyzer.SpamRequestTraceAnalyzer;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RequestTraceAnalyticsServiceTest {

    @Test
    void snapshotAggregatesSuspiciousTrafficAndTopMetrics() {
        RequestTraceAnalyticsService service = createService(
                List.of(new BurstRequestTraceAnalyzer()),
                config -> {
                    config.setBehaviorStorageType("memory");
                    config.setFileStoragePath("target/request-trace-tests/aggregate");
                }
        );
        long now = System.currentTimeMillis();

        for (int i = 0; i < 24; i++) {
            service.record(event(
                    now - 5_000 + i,
                    "trace-" + i,
                    "GET",
                    "/api/orders",
                    200,
                    30,
                    "10.0.0.1",
                    "user-1",
                    "alice",
                    "Mozilla/5.0"
            ));
        }
        service.record(event(
                now - 3_000,
                "trace-other",
                "GET",
                "/api/customers",
                200,
                20,
                "10.0.0.2",
                "user-2",
                "bob",
                "Mozilla/5.0"
        ));

        RequestTraceEvent suspicious = service.record(event(
                now - 2_000,
                "trace-burst",
                "GET",
                "/api/orders",
                429,
                15,
                "10.0.0.1",
                "user-1",
                "alice",
                "Mozilla/5.0"
        ));
        RequestTraceAnalyticsSnapshot snapshot = service.snapshot(5);

        assertThat(suspicious.signals()).containsExactly(RequestTraceSignal.BURST);
        assertThat(snapshot.windowMinutes()).isEqualTo(5);
        assertThat(snapshot.totalRequests()).isEqualTo(26);
        assertThat(snapshot.suspiciousRequests()).isEqualTo(1);
        assertThat(snapshot.uniqueUsers()).isEqualTo(2);
        assertThat(snapshot.uniqueIps()).isEqualTo(2);

        RequestTraceAnalyticsSnapshot.SignalBucket signal = snapshot.signals().get(0);
        assertThat(signal.code()).isEqualTo("burst");
        assertThat(signal.count()).isEqualTo(1L);

        RequestTraceAnalyticsSnapshot.MetricBucket topPath = snapshot.topPaths().get(0);
        assertThat(topPath.key()).isEqualTo("GET /api/orders");
        assertThat(topPath.count()).isEqualTo(25L);

        RequestTraceAnalyticsSnapshot.MetricBucket topIp = snapshot.topIps().get(0);
        assertThat(topIp.key()).isEqualTo("10.0.0.1");
        assertThat(topIp.count()).isEqualTo(25L);

        RequestTraceAnalyticsSnapshot.MetricBucket topUser = snapshot.topUsers().get(0);
        assertThat(topUser.key()).isEqualTo("alice");
        assertThat(topUser.count()).isEqualTo(25L);

        RequestTraceAnalyticsSnapshot.SuspiciousTrace recent = snapshot.recentSuspiciousEvents().get(0);
        assertThat(recent.traceId()).isEqualTo("trace-burst");
        assertThat(recent.route()).isEqualTo("GET /api/orders");
        assertThat(recent.signals()).containsExactly(RequestTraceSignal.BURST);
    }

    @Test
    void snapshotNormalizesWindowAndPrunesExpiredEvents() {
        RequestTraceAnalyticsService service = createService(
                List.of(),
                config -> {
                    config.setBehaviorStorageType("memory");
                    config.setFileStoragePath("target/request-trace-tests/prune");
                }
        );
        long now = System.currentTimeMillis();

        service.record(event(
                now - Duration.ofMinutes(31).toMillis(),
                "trace-old",
                "GET",
                "/api/old",
                200,
                10,
                "10.0.0.9",
                "user-old",
                "old",
                "Mozilla/5.0"
        ));
        service.record(event(
                now - 1_000,
                "trace-recent",
                "POST",
                "/api/recent",
                201,
                40,
                "10.0.0.10",
                "user-new",
                "",
                "Mozilla/5.0"
        ));

        RequestTraceAnalyticsSnapshot defaultWindow = service.snapshot(null);
        RequestTraceAnalyticsSnapshot maxWindow = service.snapshot(999);

        assertThat(defaultWindow.windowMinutes()).isEqualTo(10);
        assertThat(maxWindow.windowMinutes()).isEqualTo(60);
        assertThat(defaultWindow.totalRequests()).isEqualTo(1);
        assertThat(defaultWindow.uniqueUsers()).isEqualTo(1);
        assertThat(service.recentEvents()).extracting(RequestTraceEvent::traceId).containsExactly("trace-recent");
        assertThat(defaultWindow.topUsers()).extracting(RequestTraceAnalyticsSnapshot.MetricBucket::key)
                .containsExactly("user-new");
    }

    @Test
    void recordMarksHotspotSpamCrawlerAndSlowRequestSignals() {
        RequestTraceAnalyticsService service = createService(
                List.of(
                        new BurstRequestTraceAnalyzer(),
                        new HotspotRequestTraceAnalyzer(),
                        new SpamRequestTraceAnalyzer(),
                        new CrawlerRequestTraceAnalyzer(),
                        new SlowRequestTraceAnalyzer()
                ),
                config -> {
                    config.setBehaviorStorageType("memory");
                    config.setFileStoragePath("target/request-trace-tests/signals");
                    config.setBurstThreshold(3);
                    config.setHotspotThreshold(3);
                    config.setSpamThreshold(3);
                    config.setCrawlerRequestThreshold(5);
                    config.setCrawlerDistinctPathThreshold(3);
                    config.setSlowRequestThresholdMs(100L);
                }
        );
        long now = System.currentTimeMillis();

        service.record(event(now - 4_000, "trace-a", "GET", "/api/hot", 200, 20, "10.0.0.5", "user-9", "neo", "Mozilla/5.0"));
        service.record(event(now - 3_500, "trace-b", "GET", "/api/hot", 200, 18, "10.0.0.5", "user-9", "neo", "Mozilla/5.0"));
        service.record(event(now - 3_000, "trace-c", "GET", "/scan/a", 200, 22, "10.0.0.5", "user-9", "neo", "Mozilla/5.0"));
        service.record(event(now - 2_500, "trace-d", "GET", "/scan/b", 200, 24, "10.0.0.5", "user-9", "neo", "Mozilla/5.0"));

        RequestTraceEvent suspicious = service.record(event(
                now - 2_000,
                "trace-e",
                "GET",
                "/api/hot",
                200,
                250,
                "10.0.0.5",
                "user-9",
                "neo",
                "Googlebot/2.1"
        ));

        assertThat(suspicious.signals()).containsExactlyInAnyOrder(
                RequestTraceSignal.BURST,
                RequestTraceSignal.HOTSPOT,
                RequestTraceSignal.SPAM,
                RequestTraceSignal.CRAWLER,
                RequestTraceSignal.SLOW_REQUEST
        );

        RequestTraceAnalyticsSnapshot snapshot = service.snapshot(5);
        assertThat(snapshot.suspiciousRequests()).isEqualTo(1);
        assertThat(snapshot.recentSuspiciousEvents()).hasSize(1);
        assertThat(snapshot.recentSuspiciousEvents().get(0).signals()).containsExactlyInAnyOrder(
                RequestTraceSignal.BURST,
                RequestTraceSignal.HOTSPOT,
                RequestTraceSignal.SPAM,
                RequestTraceSignal.CRAWLER,
                RequestTraceSignal.SLOW_REQUEST
        );
    }

    @Test
    void recordMarksErrorBurstWhenSameActorRepeatedlyHitsErrors() {
        RequestTraceAnalyticsService service = createService(
                List.of(new ErrorBurstRequestTraceAnalyzer()),
                config -> {
                    config.setBehaviorStorageType("memory");
                    config.setFileStoragePath("target/request-trace-tests/error-burst");
                    config.setErrorBurstThreshold(3);
                    config.setErrorBurstWindowSeconds(60);
                }
        );
        long now = System.currentTimeMillis();

        service.record(event(now - 4_000, "trace-err-a", "POST", "/api/login", 401, 35, "10.0.0.7", "user-login", "neo", "Mozilla/5.0"));
        service.record(event(now - 3_000, "trace-err-b", "POST", "/api/login", 403, 42, "10.0.0.7", "user-login", "neo", "Mozilla/5.0"));

        RequestTraceEvent suspicious = service.record(event(
                now - 2_000,
                "trace-err-c",
                "POST",
                "/api/login",
                429,
                38,
                "10.0.0.7",
                "user-login",
                "neo",
                "Mozilla/5.0"
        ));

        assertThat(suspicious.signals()).containsExactly(RequestTraceSignal.ERROR_BURST);

        RequestTraceAnalyticsSnapshot snapshot = service.snapshot(5);
        assertThat(snapshot.suspiciousRequests()).isEqualTo(1);
        assertThat(snapshot.recentSuspiciousEvents()).hasSize(1);
        assertThat(snapshot.recentSuspiciousEvents().get(0).signals()).containsExactly(RequestTraceSignal.ERROR_BURST);
    }

    private RequestTraceAnalyticsService createService(
            List<RequestTraceAnalyzer> analyzers,
            java.util.function.Consumer<RequestTraceStrategyConfig> customizer
    ) {
        RequestTraceStrategyRuntimeProperties runtimeProperties = new RequestTraceStrategyRuntimeProperties();
        runtimeProperties.setConfigStorageType("memory");
        RequestTraceStorageRouter storageRouter = new RequestTraceStorageRouter();
        RequestTraceStrategyConfigService configService =
                new RequestTraceStrategyConfigService(runtimeProperties, storageRouter);

        RequestTraceStrategyConfig config = new RequestTraceStrategyConfig();
        config.setAiSummaryEnabled(false);
        if (customizer != null) {
            customizer.accept(config);
        }
        configService.update(config);
        return new RequestTraceAnalyticsService(
                analyzers,
                configService,
                storageRouter,
                new RequestTraceAiSummaryService()
        );
    }

    private RequestTraceEvent event(
            long timestamp,
            String traceId,
            String method,
            String path,
            int status,
            long durationMs,
            String clientIp,
            String userId,
            String username,
            String userAgent
    ) {
        return new RequestTraceEvent(
                timestamp,
                traceId,
                method,
                path,
                path,
                null,
                status,
                durationMs,
                clientIp,
                userAgent,
                RequestTraceSupport.resolveBrowser(userAgent),
                "monitor",
                userId,
                username,
                "fingerprint-" + clientIp,
                "token-" + userId,
                "strategy:test",
                Set.of()
        );
    }
}
