package com.chua.starter.service.configuration.trace;

import com.chua.deeplearning.support.ai.bigmodel.BigModelCallback;
import com.chua.deeplearning.support.ai.bigmodel.BigModelClient;
import com.chua.deeplearning.support.ai.bigmodel.BigModelRequest;
import com.chua.deeplearning.support.ai.bigmodel.BigModelResponse;
import com.chua.starter.common.support.application.GlobalSettingFactory;
import com.chua.starter.service.setting.BigModelSetting;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

/** @author CH */
class RequestTraceAiSummaryServiceTest {

    @Test
    void summarizeUsesConfiguredBigModelWhenAvailable() {
        RequestTraceAiSummaryService service = new RequestTraceAiSummaryService();
        createSetting(new StubBigModelClient("AI判定为高风险刷屏，建议立即限流并二次校验。"));

        RequestTraceStrategyConfig config = new RequestTraceStrategyConfig();
        config.setAiModel("qwen-plus");

        RequestTraceAiAnalysisResult result = service.summarize(
                event("trace-ai", "/api/orders", 429, 180, "10.0.0.1", "user-1", "Mozilla/5.0"),
                Set.of(RequestTraceSignal.SPAM, RequestTraceSignal.HOTSPOT),
                18,
                2,
                "high",
                config
        );

        assertThat(result.status()).isEqualTo(RequestTraceAiAnalysisResult.STATUS_MODEL_SUCCESS);
        assertThat(result.summarySource()).isEqualTo(RequestTraceAiAnalysisResult.SOURCE_MODEL);
        assertThat(result.summary()).isEqualTo("AI判定为高风险刷屏，建议立即限流并二次校验。");
        assertThat(result.heuristicSummary()).contains("风险等级=high");
        assertThat(result.promptDigest()).isNotBlank();
    }

    @Test
    void summarizeFallsBackToHeuristicSummaryWhenModelUnavailable() {
        RequestTraceAiSummaryService service = new RequestTraceAiSummaryService();
        createSetting(new StubBigModelClient(null));

        RequestTraceStrategyConfig config = new RequestTraceStrategyConfig();
        config.setAiModel("qwen-plus");

        RequestTraceAiAnalysisResult result = service.summarize(
                event("trace-fallback", "/api/hot", 200, 95, "10.0.0.9", "user-9", "Googlebot/2.1"),
                Set.of(RequestTraceSignal.CRAWLER, RequestTraceSignal.SLOW_REQUEST),
                7,
                4,
                "high",
                config
        );

        assertThat(result.status()).isEqualTo(RequestTraceAiAnalysisResult.STATUS_HEURISTIC_FALLBACK);
        assertThat(result.summarySource()).isEqualTo(RequestTraceAiAnalysisResult.SOURCE_HEURISTIC);
        assertThat(result.summary()).contains("风险等级=high");
        assertThat(result.summary()).contains("路由=GET /api/hot");
        assertThat(result.summary()).contains("信号=crawler,slow_request");
        assertThat(result.summary()).contains("关联请求=7");
        assertThat(result.summary()).contains("扫描路径=4");
    }

    @Test
    void summarizeUsesHeuristicResultWhenAiDisabled() {
        RequestTraceAiSummaryService service = new RequestTraceAiSummaryService();
        createSetting(new StubBigModelClient("不应该被调用"));

        RequestTraceStrategyConfig config = new RequestTraceStrategyConfig();
        config.setAiSummaryEnabled(false);
        config.setAiModel("qwen-plus");

        RequestTraceAiAnalysisResult result = service.summarize(
                event("trace-disabled", "/api/risk", 403, 60, "10.0.0.3", "user-3", "Mozilla/5.0"),
                Set.of(RequestTraceSignal.BURST),
                3,
                1,
                "medium",
                config
        );

        assertThat(result.status()).isEqualTo(RequestTraceAiAnalysisResult.STATUS_DISABLED);
        assertThat(result.summarySource()).isEqualTo(RequestTraceAiAnalysisResult.SOURCE_HEURISTIC);
        assertThat(result.summary()).contains("风险等级=medium");
        assertThat(result.reason()).contains("aiSummaryEnabled=false");
    }

    @Test
    void summarizeFallsBackWhenAiQuotaIsExceeded() {
        AtomicLong now = new AtomicLong(1_000L);
        RequestTraceAiSummaryService service = new RequestTraceAiSummaryService(now::get);
        createSetting(new StubBigModelClient("模型摘要"));

        RequestTraceStrategyConfig config = new RequestTraceStrategyConfig();
        config.setAiModel("qwen-plus");
        config.setAiRateLimitWindowSeconds(60);
        config.setAiRateLimitMaxRequests(1);

        RequestTraceAiAnalysisResult first = service.summarize(
                event("trace-rate-1", "/api/orders", 429, 110, "10.0.0.8", "user-8", "Mozilla/5.0"),
                Set.of(RequestTraceSignal.SPAM),
                8,
                2,
                "medium",
                config
        );
        RequestTraceAiAnalysisResult second = service.summarize(
                event("trace-rate-2", "/api/orders", 429, 115, "10.0.0.8", "user-8", "Mozilla/5.0"),
                Set.of(RequestTraceSignal.SPAM),
                9,
                2,
                "medium",
                config
        );

        assertThat(first.status()).isEqualTo(RequestTraceAiAnalysisResult.STATUS_MODEL_SUCCESS);
        assertThat(second.status()).isEqualTo(RequestTraceAiAnalysisResult.STATUS_RATE_LIMITED);
        assertThat(second.summarySource()).isEqualTo(RequestTraceAiAnalysisResult.SOURCE_HEURISTIC);
        assertThat(second.summary()).contains("风险等级=medium");
        assertThat(second.reason()).contains("限流");
    }

    private BigModelSetting createSetting(BigModelClient client) {
        BigModelSetting setting = GlobalSettingFactory.getInstance().get("llm", BigModelSetting.class);
        setting.setAppType("mock");
        setting.setBigModelClient(client);
        return setting;
    }

    /**
     * Event
     * @param traceId traceId
     * @param path path
     * @param status status
     * @param durationMs durationMs
     * @param clientIp clientIp
     * @param userId userId
     * @param userAgent userAgent
     */
    private RequestTraceEvent event(
            String traceId,
            String path,
            int status,
            long durationMs,
            String clientIp,
            String userId,
            String userAgent
    ) {
        return new RequestTraceEvent(
                System.currentTimeMillis(),
                traceId,
                "GET",
                path,
                path,
                "page=1",
                status,
                durationMs,
                clientIp,
                userAgent,
                RequestTraceSupport.resolveBrowser(userAgent),
                "monitor",
                userId,
                "tester",
                "fingerprint-" + clientIp,
                "token-" + userId,
                "strategy:test",
                Set.of()
        );
    }

    static final class StubBigModelClient implements BigModelClient {

        /** 输出 */
        private final String output;

        StubBigModelClient(String output) {
            this.output = output;
        }

        @Override
        public List<com.chua.common.support.ai.bigmodel.BigModelMetadata> supportedModels() {
            return List.of();
        }

        @Override
        public BigModelResponse call(BigModelRequest request) {
            if (output == null) {
                return null;
            }
            return BigModelResponse.builder()
                    .output(output)
                    .done(true)
                    .build();
        }

        public void callAsync(BigModelRequest request, BigModelCallback callback) throws InterruptedException {
            throw new UnsupportedOperationException();
        }

        public void callStream(BigModelRequest request, BigModelCallback callback) {
            throw new UnsupportedOperationException();
        }
    }
}

