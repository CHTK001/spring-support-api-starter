package com.chua.starter.service.configuration.trace;

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
 * 请求追踪风险摘要服务。
 *
 * <p>当前为纯启发式摘要实现（基于信号、路由、风险等级等规则拼接），
 * 不依赖任何大模型客户端。</p>
 *
 * @since 2026/03/25
 * @author CH
 */
@Slf4j
@Service
public class RequestTraceAiSummaryService {

    /** MASUMMARLENGTH */
    private static final int MAX_SUMMARY_LENGTH= 200;

    /** invocationTimestamps */
    private final Deque<Long> invocationTimestamps= new ArrayDeque<>();
    /** rateLimitLock */
    private final ReentrantLock rateLimitLock= new ReentrantLock();
    /** clock */
    private final LongSupplier clock;

    /** 创建 RequestTraceAiSummaryService 实例 */
    public RequestTraceAiSummaryService() {
        this(System::currentTimeMillis);
    }

    RequestTraceAiSummaryService(LongSupplier clock) {
        this.clock = clock == null ? System::currentTimeMillis : clock;
    }

    /**
     * Summarize
     * @param event event
     * @param signals signals
     * @param relatedRequestCount relatedRequestCount
     * @param distinctPathCount distinctPathCount
     * @param riskLevel riskLevel
     * @param config config
     */
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

        // 当前实现为纯启发式摘要，大模型客户端未接入，直接回退规则摘要
        return new RequestTraceAiAnalysisResult(
                clock.getAsLong(),
                RequestTraceAiAnalysisResult.STATUS_HEURISTIC_FALLBACK,
                RequestTraceAiAnalysisResult.SOURCE_HEURISTIC,
                heuristicSummary,
                heuristicSummary,
                "未配置大模型客户端，使用规则摘要",
                model,
                riskLevel,
                promptDigest,
                0L
        );
    }

    /**
     * 构建HeuristicSummary
     * @param event event
     * @param signals signals
     * @param relatedRequestCount relatedRequestCount
     * @param distinctPathCount distinctPathCount
     * @param riskLevel riskLevel
     * @param config config
     */
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

    /**
     * 构建ModelPrompt
     * @param event event
     * @param signals signals
     * @param relatedRequestCount relatedRequestCount
     * @param distinctPathCount distinctPathCount
     * @param riskLevel riskLevel
     * @param heuristicSummary heuristicSummary
     */
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
        lines.add("规则摘要: " + heuristicSummary);
        lines.add("输出要求: 一句话，包含行为判断与建议。");
        return String.join("\n", lines);
    }

    /** NormalizeSummary */
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
            return compact;
        }
        return compact.substring(0, MAX_SUMMARY_LENGTH);
    }

    /** Try获取Quota */
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

    /** HashText */
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
}
