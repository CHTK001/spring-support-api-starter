package com.chua.starter.service.configuration.trace;

import java.util.Set;

/**
 * 请求追踪行为分析结果。
 *
 * <p>一条「行为分析记录」对应 {@link RequestTraceEvent} 中的一次请求在分析时刻的快照：
 * 请求本身的字段（时间、路由、状态码、耗时、客户端、身份）原样透传，
 * 分析阶段产出的字段（命中信号、关联请求数、去重路径数、风险等级、摘要）单独承载。
 * 落库目标表为 {@code sys_request_trace_behavior}。</p>
 *
 * <p>可空性：所有 {@code String} 组件都经过 {@link RequestTraceSupport#normalize(String)}，
 * 入参为 {@code null} 或去掉首尾空白后为空串时统一归一为 {@code null}；
 * {@code signals} 为 {@code null} 或空集时归一为 {@link Set#of()}，因此读取方无需判空。
 * {@code aiAnalysis} 不做归一，允许为 {@code null}（未接入 AI 分析或已禁用）。</p>
 *
 * @param analyzedAt          分析时刻，epoch 毫秒时间戳；取分析流程开始时的时间，非请求时刻
 * @param requestAt           请求发生时刻，epoch 毫秒时间戳；取自 {@link RequestTraceEvent#timestamp()}
 * @param traceId             追踪 ID，取自 MDC {@code traceId}，用于把行为记录与原始请求行关联；可为 null
 * @param strategyCode        命中接口上的 {@link RequestTraceStrategy} 策略编码；未标注或未匹配时为 null
 * @param route               命中的路由模板（形如 {@code /api/user/{id}}），优先取路由模式，回退为请求路径；可为 null
 * @param requestUrl          请求完整地址，由路径与查询串拼接而成（形如 {@code /api/user?id=1}）；查询串为空时只保留路径
 * @param status              HTTP 响应状态码，取自 {@link RequestTraceEvent#status()}；小于 100 或缺省时为 0
 * @param durationMs          请求耗时，单位毫秒，取自 {@link RequestTraceEvent#durationMs()}；未采集到时为 0
 * @param clientIp            客户端 IP，取自请求头，未取到时为 null
 * @param browser             浏览器族，由 {@link RequestTraceSupport#resolveBrowser(String)} 从 User-Agent 归一得出（Edge/Chrome/Firefox/Safari/IE/Opera/Postman/Curl/Wget/Bot/Other）；User-Agent 为空时为 null
 * @param userId              已登录用户 ID；匿名请求为 null
 * @param username            已登录用户名；匿名请求为 null
 * @param fingerprint         客户端指纹（由请求特征派生），用于在无登录态时区分同一来源；可为 null
 * @param userAgent           浏览器完整 User-Agent 头原文，长度受存储列宽限制；未取到时为 null
 * @param signals             本次命中的行为信号集合，入参为 null 或空集时归一为 {@link Set#of()}；集合元素不可为 null，落库时按编码升序拼成逗号串
 * @param relatedRequestCount 同一主体在分析窗口期内的关联请求总数（含本次），非负；窗口期长度由策略配置决定
 * @param distinctPathCount   同一主体在分析窗口期内访问过的去重路径数，非负；用于识别扫描型行为
 * @param riskLevel           风险等级，取 {@code normal} / {@code low} / {@code medium} / {@code high}，由 {@link RequestTraceSupport#resolveRiskLevel(Set)} 判定
 * @param summary             启发式（规则）摘要，无需 AI 即可生成的文字结论；为空表示未生成
 * @param aiSummary           AI 生成的摘要，来自 {@link RequestTraceAiAnalysisResult}；未接入 AI 或已禁用时为 null
 * @param aiAnalysis          AI 分析过程信息（模型调用状态、摘要来源、降级原因、提示词摘要、模型耗时等）；未接入 AI 分析时为 null
 * @param extra               扩展字段，预留给后续结构演进追加的键值对（序列化文本）；无扩展内容时为 null
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
