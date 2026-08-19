package com.chua.starter.service.configuration.trace;

/**
 * 请求追踪 AI 分析结果。
 *
 * <p>用于保留模型调用状态、最终摘要来源、回退原因与提示词摘要等过程信息，方便前端展示与落库扩展。</p>
 *
 * @since 2026/03/26
 * @author CH
 */
public record RequestTraceAiAnalysisResult(
        long analyzedAt,
        String status,
        String summarySource,
        String summary,
        String heuristicSummary,
        String reason,
        String model,
        String riskLevel,
        String promptDigest,
        long latencyMs
) {

    /** Status_heuristic_fallback */
    public static final String STATUS_HEURISTIC_FALLBACK = "heuristic_fallback";
    /** Status_rate_limited */
    public static final String STATUS_RATE_LIMITED = "rate_limited";
    /** Status_disabled */
    public static final String STATUS_DISABLED = "disabled";

    /** Source_heuristic */
    public static final String SOURCE_HEURISTIC = "heuristic";

    public RequestTraceAiAnalysisResult {
        status = RequestTraceSupport.normalize(status);
        summarySource = RequestTraceSupport.normalize(summarySource);
        summary = RequestTraceSupport.normalize(summary);
        heuristicSummary = RequestTraceSupport.normalize(heuristicSummary);
        reason = RequestTraceSupport.normalize(reason);
        model = RequestTraceSupport.normalize(model);
        riskLevel = RequestTraceSupport.normalize(riskLevel);
        promptDigest = RequestTraceSupport.normalize(promptDigest);
        latencyMs = Math.max(latencyMs, 0L);
    }

    /** FinalSummary */
    public String finalSummary() {
        return RequestTraceSupport.firstNonBlank(summary, heuristicSummary);
    }
}
