package com.chua.starter.service.configuration.trace;

import com.chua.deeplearning.support.ai.bigmodel.BigModelClient;
import com.chua.deeplearning.support.ai.bigmodel.BigModelRequest;
import com.chua.deeplearning.support.ai.bigmodel.BigModelResponse;
import com.chua.starter.common.support.application.GlobalSettingFactory;
import com.chua.starter.service.setting.BigModelSetting;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.LongSupplier;

/**
 * 请求追踪 AI 摘要服务。
 *
 * <p>默认使用启发式摘要，后续可继续替换为真实大模型实现。</p>
 *
 * @author CH
 * @since 2026/03/25
 * @param summary summary
 * @param reason ReasonML
 * @param model 模型
 * @return 模型尝试的结果
 * @param text 文本
 */
@Slf4j
@Service
public class RequestTraceAiSummaryService {

    private static final String DEFAULT_SYSTEM_PROMPT =
            "你是请求追踪风控助手。请基于输入的访问行为，输出一句中文风险摘要，"
                    + "需要点明风险等级、行为类型和处置建议。"
                    + "不要输出 Markdown，不要分点，控制在 80 个字以内。";
    private static final int MAX_SUMMARY_LENGTH = 200; // 最大summary长度
/**
 * 请求追踪AIsummary服务。
 */

    private final Deque<Long> invocationTimestamps = new ArrayDeque<>(); // invocation时间戳
    private final ReentrantLock rateLimitLock = new ReentrantLock(); // rate限制锁
    private final LongSupplier clock; // clock

    public RequestTraceAiSummaryService() {
        this(System::currentTimeMillis);
    }

    RequestTraceAiSummaryService(LongSupplier clock) {
        this.clock = clock == null ? System::currentTimeMillis : clock;
    }

    public RequestTraceAiAnalysisResult summarize(
            RequestTraceEvent event,
            Set<RequestTraceSignal> signals,
            long relatedRequestCount,
            long distinctPathCount,
            String riskLevel,
            RequestTraceStrategyConfig config
    ) {
        if (event == null || signals == null || signals.isEmpty()) {
            return null;
        }

        RequestTraceStrategyConfig normalizedConfig = RequestTraceStrategyConfigCodec.normalize(config);
        String heuristicSummary = buildHeuristicSummary(
                event,
                signals,
                relatedRequestCount,
                distinctPathCount,
                riskLevel,
                normalizedConfig
        );
        String prompt = buildModelPrompt(
                event,
                signals,
                relatedRequestCount,
                distinctPathCount,
                riskLevel,
                heuristicSummary
        );
        String promptDigest = hashText(prompt);
        String model = RequestTraceSupport.normalize(normalizedConfig.getAiModel());

        if (!Boolean.TRUE.equals(normalizedConfig.getAiSummaryEnabled())) {
            return new RequestTraceAiAnalysisResult(
                    clock.getAsLong(),
                    RequestTraceAiAnalysisResult.STATUS_DISABLED,
                    RequestTraceAiAnalysisResult.SOURCE_HEURISTIC,
                    heuristicSummary,
                    heuristicSummary,
                    "aiSummaryEnabled=false，未调用模型分析",
                    model,
                    riskLevel,
                    promptDigest,
                    0L
            );
        }

        if (Boolean.TRUE.equals(normalizedConfig.getAiRateLimitEnabled())
                && !tryAcquireQuota(normalizedConfig.getAiRateLimitWindowSeconds(), normalizedConfig.getAiRateLimitMaxRequests())) {
            return new RequestTraceAiAnalysisResult(
                    clock.getAsLong(),
                    RequestTraceAiAnalysisResult.STATUS_RATE_LIMITED,
                    RequestTraceAiAnalysisResult.SOURCE_HEURISTIC,
                    heuristicSummary,
                    heuristicSummary,
                    "AI 分析触发限流，已回退到规则摘要",
                    model,
                    riskLevel,
                    promptDigest,
                    0L
            );
        }

        long start = clock.getAsLong();
        ModelAttempt attempt = summarizeWithModel(
                event,
                normalizedConfig,
                prompt
        );
        long latencyMs = Math.max(clock.getAsLong() - start, 0L);
        if (attempt.summary() != null) {
            return new RequestTraceAiAnalysisResult(
                    clock.getAsLong(),
                    RequestTraceAiAnalysisResult.STATUS_MODEL_SUCCESS,
                    RequestTraceAiAnalysisResult.SOURCE_MODEL,
                    attempt.summary(),
                    heuristicSummary,
                    attempt.reason(),
                    RequestTraceSupport.firstNonBlank(attempt.model(), model),
                    riskLevel,
                    promptDigest,
                    latencyMs
            );
        }
        return new RequestTraceAiAnalysisResult(
                clock.getAsLong(),
                RequestTraceAiAnalysisResult.STATUS_HEURISTIC_FALLBACK,
                RequestTraceAiAnalysisResult.SOURCE_HEURISTIC,
                heuristicSummary,
                heuristicSummary,
                attempt.reason(),
                RequestTraceSupport.firstNonBlank(attempt.model(), model),
                riskLevel,
                promptDigest,
                latencyMs
        );
    }

    private ModelAttempt summarizeWithModel(
            RequestTraceEvent event,
            RequestTraceStrategyConfig config,
            String prompt
    ) {
        try {
            BigModelSetting setting = GlobalSettingFactory.getInstance().get("llm", BigModelSetting.class);
            if (setting == null) {
                return new ModelAttempt(null, "未找到 llm 全局配置", null);
            }
            BigModelClient bigModelClient = setting.getBigModelClient();
            if (bigModelClient == null) {
                return new ModelAttempt(null, "llm 客户端未初始化", null);
            }

            String model = RequestTraceSupport.normalize(config == null ? null : config.getAiModel());
            BigModelRequest.BigModelRequestBuilder builder = BigModelRequest.builder()
                    .system(DEFAULT_SYSTEM_PROMPT)
                    .prompt(prompt)
                    .tokens(256)
                    .temperature(0.1D)
                    .timeout(config == null ? 1500L : config.getAiTimeoutMs());
            if (model != null) {
                builder.model(model);
            }

            BigModelResponse response = bigModelClient.call(builder.build());
            String summary = normalizeSummary(response == null ? null : response.getOutput());
            if (summary != null) {
                return new ModelAttempt(summary, "模型输出成功", model);
            }
            return new ModelAttempt(null, "模型输出为空，已回退到规则摘要", model);
        } catch (Exception ex) {
            log.debug(
                    "调用大模型生成请求追踪摘要失败: traceId={}, model={}",
                    event.traceId(),
                    config == null ? null : config.getAiModel(),
                    ex
            );
            return new ModelAttempt(null, "模型调用异常，已回退到规则摘要", config == null ? null : config.getAiModel());
        }
    }

    private String buildHeuristicSummary(
            RequestTraceEvent event,
            Set<RequestTraceSignal> signals,
            long relatedRequestCount,
            long distinctPathCount,
            String riskLevel,
            RequestTraceStrategyConfig config
    ) {
        List<String> parts = new ArrayList<>();
        parts.add("风险等级=" + riskLevel);
        parts.add("主体=" + RequestTraceSupport.firstNonBlank(event.userId(), event.fingerprint(), event.clientIp(), "anonymous"));
        parts.add("路由=" + event.routeKey());
        parts.add("信号=" + RequestTraceSupport.signalCodes(signals));
        parts.add("关联请求=" + relatedRequestCount);
        if (distinctPathCount > 0) {
            parts.add("扫描路径=" + distinctPathCount);
        }
        if (event.durationMs() > 0) {
            parts.add("耗时=" + event.durationMs() + "ms");
        }
        String model = RequestTraceSupport.normalize(config == null ? null : config.getAiModel());
        if (model != null) {
            parts.add("摘要模型=" + model);
        }
        return String.join("；", parts);
    }

    private String buildModelPrompt(
            RequestTraceEvent event,
            Set<RequestTraceSignal> signals,
            long relatedRequestCount,
            long distinctPathCount,
            String riskLevel,
            String heuristicSummary
    ) {
        List<String> lines = new ArrayList<>();
        lines.add("请分析以下请求追踪风险事件，并输出一句中文摘要。");
        lines.add("风险等级: " + RequestTraceSupport.firstNonBlank(riskLevel, "unknown"));
        lines.add("主体: " + RequestTraceSupport.firstNonBlank(event.userId(), event.fingerprint(), event.clientIp(), "anonymous"));
        lines.add("路由: " + RequestTraceSupport.firstNonBlank(event.routeKey(), "-"));
        lines.add("请求地址: " + RequestTraceSupport.firstNonBlank(event.requestUrl(), "-"));
        lines.add("命中信号: " + RequestTraceSupport.firstNonBlank(RequestTraceSupport.signalCodes(signals), "-"));
        lines.add("浏览器: " + RequestTraceSupport.firstNonBlank(event.browser(), "unknown"));
        lines.add("状态码: " + event.status());
        lines.add("耗时: " + event.durationMs() + "ms");
        lines.add("关联请求数: " + relatedRequestCount);
        lines.add("扫描路径数: " + distinctPathCount);
        /**
         * normalizeSummary。
         * @param summary summary
         * @return normalizeSummary的结果
         */
        lines.add("规则摘要: " + heuristicSummary);
        lines.add("输出要求: 一句话，包含行为判断与建议。");
        return String.join("\n", lines);
    }

    private String normalizeSummary(String summary) {
        String normalized = RequestTraceSupport.normalize(summary);
        if (normalized == null) {
            return null;
        }
        String compact = normalized.replace('\r', ' ')
                .replace('\n', ' ')
                .replaceAll("\\s+", " ")
                .trim();
        if (compact.length() <= MAX_SUMMARY_LENGTH) {
            /**
             * 尝试acquire配额。
             * @param windowSeconds 窗口seconds
             * @param maxRequests 最大Requests
             * @return 尝试acquire配额的结果
             */
            return compact;
        }
        return compact.substring(0, MAX_SUMMARY_LENGTH);
    }

    private boolean tryAcquireQuota(int windowSeconds, int maxRequests) {
        long now = clock.getAsLong();
        long cutoff = now - Math.max(windowSeconds, 1) * 1000L;
        rateLimitLock.lock();
        try {
            while (!invocationTimestamps.isEmpty()) {
                Long first = invocationTimestamps.peekFirst();
                if (first == null || first >= cutoff) {
                    break;
                }
                invocationTimestamps.pollFirst();
            }
            if (invocationTimestamps.size() >= Math.max(maxRequests, 1)) {
                return false;
            }
            invocationTimestamps.addLast(now);
            return true;
        } finally {
            rateLimitLock.unlock();
        }
    }

    private String hashText(String text) {
        String normalized = RequestTraceSupport.normalize(text);
        if (normalized == null) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] data = digest.digest(normalized.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(data.length * 2);
            for (byte item : data) {
                builder.append(Character.forDigit((item >> 4) & 0xF, 16));
                builder.append(Character.forDigit(item & 0xF, 16));
            }
            return builder.substring(0, Math.min(builder.length(), 16));
        } catch (NoSuchAlgorithmException ex) {
            return Integer.toHexString(normalized.hashCode());
        }
    }

    private record ModelAttempt(String summary, String reason, String model) {
    }
}
