package com.chua.starter.service.configuration.trace;

import java.time.Instant;
import java.util.List;
import java.util.Set;

/**
 * 请求追踪分析快照。
 *
 * <p>对 {@link RequestTraceEvent} 在一个时间窗口内的聚合统计结果，供监控看板或告警消费：
 * 总量类指标用基本类型直接给出，分布类指标用嵌套的 {@link MetricBucket} 列表给出 Top-N，
 * 命中信号用 {@link SignalBucket} 按编码聚合，最近的可疑请求逐条列出 {@link SuspiciousTrace}。</p>
 *
 * <p>可空性：五个列表组件在紧凑构造器里都由 {@code null} 归一为 {@link List#of()}，
 * 并用 {@link List#copyOf} 做不可变快照，因此访问器**保证非 null**；元素不允许为
 * {@code null}（{@code List.copyOf} 会拒绝空元素）。其余组件均为基本类型或
 * {@link Instant}，不存在空值语义。</p>
 *
 * @param generatedAt 快照 生成时刻，即聚合统计的截止时间点，由聚合方在统计开始时取
 *                     {@link Instant#now()} 得出。与各事件的 {@code timestamp}
 *                     （epoch 毫秒）相比，本组件为带时区的 {@link Instant}，可直接用于
 *                     展示或与 {@code windowMinutes} 一起推出窗口起点；不允许为
 *                     {@code null}（原始类型），也不做归一化
 * @param windowMinutes 统计 窗口 长度，单位分钟，标明 {@code totalRequests} 等指标覆盖的
 *                        时间跨度。取值来源为 {@link RequestTraceStrategyConfig} 的
 *                        {@code defaultWindowMinutes}（默认 10），并受
 *                        {@code maxWindowMinutes}（默认 60）上界约束；原始 int，
 *                        不允许为 {@code null}，语义上必须为正数分钟数
 * @param totalRequests 窗口 内 纳入统计的请求总数，单位「条」，包含命中信号与未命中信号的
 *                      全部入站请求。取值来源为对窗口内 {@link RequestTraceEvent} 的计数；
 *                      原始 long，非负，无结果时为 0
 * @param suspiciousRequests 窗口 内 至少命中一个 {@link RequestTraceSignal} 的请求数，
 *                           判定口径与 {@link RequestTraceEvent#suspicious()}
 *                           （信号集合非空）一致。原始 long，非负，无可疑请求时为 0，
 *                           恒不大于 {@code totalRequests}
 * @param uniqueUsers 窗口 内 去重后的 已认证用户数，单位「人」，按 {@code userId} 去重
 *                    （匿名请求无 {@code userId}，不计入）。原始 long，非负；
 *                    全部为匿名请求时为 0
 * @param uniqueIps 窗口 内 去重后的 客户端 IP 数，单位「个 IP」，按 {@code clientIp} 去重。
 *                  原始 long，非负；未采集到 IP 的事件不计入，全部缺失时为 0
 * @param signals 各 命中信号 的聚合分布，按 {@link RequestTraceSignal} 的 code 分桶，
 *                元素为 {@link SignalBucket}（编码、描述、命中次数）。紧凑构造器已把
 *                {@code null} 归一为 {@link List#of()} 并做不可变快照；元素不允许为
 *                {@code null}。无任何命中时为空列表
 * @param topPaths 请求量 Top-N 的 路由键，取值为 {@link RequestTraceEvent#routeKey()}
 *                （形如 {@code "GET /api/user/{id}"}），按命中次数降序排列，次数相同时
 *                保持聚合方原始顺序。长度上限取自 {@link RequestTraceStrategyConfig}
 *                的 {@code maxTopBuckets}（默认 10），超出部分被截断。已做不可变快照；
 *                元素不允许为 {@code null}，无请求时为空列表
 * @param topIps 请求量 Top-N 的 客户端 IP，按 {@code clientIp} 聚合，降序排列，截断口径同
 *               {@link #topPaths}。已做不可变快照；元素不允许为 {@code null}，
 *               无数据时为空列表
 * @param topUsers 请求量 Top-N 的 用户，按 {@code userId} 聚合，降序排列，截断口径同
 *                {@link #topPaths}。已做不可变快照；元素不允许为 {@code null}，
 *                全部为匿名请求时为空列表
 * @param recentSuspiciousEvents 窗口 内 最近的可疑请求明细列表，按时间倒序（最新在前）排列，
 *                               元素为 {@link SuspiciousTrace}，携带信号集合与 AI 分析结果
 *                               供人工复核。长度上限取自
 *                               {@link RequestTraceStrategyConfig} 的
 *                               {@code maxSuspiciousEvents}（默认 20），超出部分被截断。
 *                               已做不可变快照；元素不允许为 {@code null}，
 *                               无可疑请求时为空列表
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
