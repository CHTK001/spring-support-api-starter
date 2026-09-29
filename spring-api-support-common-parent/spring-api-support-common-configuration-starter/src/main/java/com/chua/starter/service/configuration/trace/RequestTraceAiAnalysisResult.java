package com.chua.starter.service.configuration.trace;

/**
 * 请求追踪 AI 分析结果。
 *
 * <p>用于保留模型调用状态、最终摘要来源、回退原因与提示词摘要等过程信息，方便前端展示与落库扩展。
 * 作为 {@link RequestTraceBehaviorRecord#aiAnalysis()} 的嵌套值对象存在，本身不做任何模型调用，
 * 只承载调用方（AI 摘要服务）的产出。</p>
 *
 * <p>可空性：所有 {@code String} 组件都经过 {@link RequestTraceSupport#normalize(String)}，
 * 入参为 {@code null} 或去掉首尾空白后为空串时统一归一为 {@code null}；
 * {@code latencyMs} 由紧凑构造器用 {@code Math.max(latencyMs, 0L)} 夹紧为非负。</p>
 *
 * @param analyzedAt        AI 分析时刻，epoch 毫秒时间戳（{@code System.currentTimeMillis()}）。
 *                          原始 {@code long} 不允许为 null；表示分析动作发生的时间，不是原请求的发生时刻
 * @param status            本次模型调用状态。已声明的常量取值为 {@link #STATUS_HEURISTIC_FALLBACK}
 *                          （调用失败或超时后降级为启发式）、{@link #STATUS_RATE_LIMITED}
 *                          （命中 AI 限流窗口而未真正调用）、{@link #STATUS_DISABLED}
 *                          （{@code aiSummaryEnabled} 为 false 而跳过）；
 *                          成功完成模型调用时写入调用方约定的成功标记（本类未为其声明常量）。
 *                          允许为 null（未执行分析时整体不产出本记录）
 * @param summarySource     最终摘要的来源标记。取 {@link #SOURCE_HEURISTIC}（{@code "heuristic"}）时
 *                          表示摘要来自规则/启发式逻辑；取其他非空值时表示摘要来自 AI 模型输出。
 *                          允许为 null。与 {@link #status} 配合可区分「AI 生成」与「AI 缺席时的兜底」
 * @param summary           AI 模型输出的摘要正文。允许为 null：调用被禁用、被限流、
 *                          调用异常或模型返回空内容时为 null，此时应回退看 {@link #heuristicSummary}
 * @param heuristicSummary  启发式（规则）摘要，不依赖模型即可生成的文字结论。
 *                          允许为 null；{@link #finalSummary()} 在 {@link #summary} 为空时取本值兜底
 * @param reason            降级或未生成摘要的原因说明（如「命中限流」「AI 摘要已禁用」「调用超时」等），
 *                          供前端提示与排查；正常成功时为 null
 * @param model             实际调用的 AI 模型标识，取自策略配置
 *                          {@code RequestTraceStrategyConfig.aiModel}（配置侧已做去空白并转小写）；
 *                          未真正调用模型（禁用/限流/降级）时为 null
 * @param riskLevel         风险等级，取 {@code normal} / {@code low} / {@code medium} / {@code high}，
 *                          由 {@link RequestTraceSupport#resolveRiskLevel(Set)} 依据命中信号判定；
 *                          未做风险判定时允许为 null
 * @param promptDigest      提示词的摘要指纹（提示词原文的哈希值，不是提示词正文），
 *                          用于在不泄露完整提示词的前提下做版本比对与审计；未构造提示词时为 null
 * @param latencyMs         本次 AI 分析（含模型调用）耗时，单位毫秒；紧凑构造器已用
 *                          {@code Math.max(latencyMs, 0L)} 夹紧为非负，未发生调用时为 0
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

    /**
     * 状态_heuristic_降级
    */
    public static final String STATUS_HEURISTIC_FALLBACK = "heuristic_fallback";
    /**
     * 状态_rate_限制
    */
    public static final String STATUS_RATE_LIMITED = "rate_limited";
    /**
     * 状态_已禁用
    */
    public static final String STATUS_DISABLED = "disabled";

    /**
     * 源_heuristic
    */
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

    /**
     * 最终summary
     *
     * @return 最终summary的结果
     */
    public String finalSummary() {
        return RequestTraceSupport.firstNonBlank(summary, heuristicSummary);
    }
}
