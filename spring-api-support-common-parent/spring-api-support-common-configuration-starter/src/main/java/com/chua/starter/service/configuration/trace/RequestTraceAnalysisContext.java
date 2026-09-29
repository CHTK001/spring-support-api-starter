package com.chua.starter.service.configuration.trace;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.function.Predicate;

/**
 * 请求追踪分析上下文。
 *
 * <p>交给各 {@link RequestTraceAnalyzer} 实现的分析输入包：一条待判定的当前事件
 * {@link #currentEvent}、一段历史事件缓冲 {@link #recentEvents}、时间基准 {@link #now}
 * 与策略配置 {@link #config}，分析器据此产出 {@link RequestTraceSignal}。
 *
 * <p>归一化约定（见紧凑构造器）：{@code recentEvents} 传 null 会被规整为空列表、
 * {@code now} 传 null 会被规整为构造时刻、{@code config} 传 null 会被替换为一份全默认值的
 * 新配置，故这三者访问器恒非 null；只有 {@code currentEvent} 保留 null 语义——
 * 各分析器都自行判空并直接返回空信号集。</p>
 *
 * @param currentEvent 当前待判定的请求追踪事件，即「本次要出信号的那条事件」。可为 null：
 *                    各分析器都以 {@code currentEvent == null} 作为提前返回条件
 *                    （无锚点事件时所有信号都不成立），本 record 也不对它做任何非空校验
 * @param recentEvents 历史事件缓冲，供 {@link #historyWithin(Duration)} 做时间窗口过滤；
 *                    应为当前事件之前已发生的事件，不含 {@code currentEvent} 本身
 *                    （各分析器比较阈值时把当前事件以 {@code +1} 单独计入）。
 *                    传 null 会被规整为空列表；元素不允许为 null（{@link List#copyOf} 会拒绝）。
 *                    不要求预先按时间排序：窗口过滤只逐条比较 {@code timestamp}，
 *                    输出顺序即列表原顺序
 * @param now         时间窗口的判定基准时刻，由 {@link #historyWithin(Duration)} 据此反推截止
 *                    时间（{@code now.minus(duration)}）；传 null 时取构造那一刻的
 *                    {@link Instant#now()}。恒非 null，且是构造时刻的一次性快照，
 *                    同一实例内多次调用不会刷新，以保证单次分析过程中时间基准不漂移
 * @param config      请求追踪策略配置，承载各检测项的开关、统计窗口与阈值（单位：毫秒 / 分钟 /
 *                    条）；传 null 时由 {@link RequestTraceStrategyConfigCodec#normalize} 补一份
 *                    全默认值的新配置，故恒非 null。存入的是 {@code copy().normalize()} 的结果
 *                    而非原对象引用，调用方事后修改原配置不会影响本上下文
 * @since 2026/03/25
 * @author CH
 */
public record RequestTraceAnalysisContext(
        RequestTraceEvent currentEvent,
        List<RequestTraceEvent> recentEvents,
        Instant now,
        RequestTraceStrategyConfig config
) {

    public RequestTraceAnalysisContext {
        recentEvents = recentEvents == null ? List.of() : List.copyOf(recentEvents);
        now = now == null ? Instant.now() : now;
        config = RequestTraceStrategyConfigCodec.normalize(config);
    }

    /**
     * 历史within
     *
     * @param duration 持续时间
     * @return 历史within的结果
     */
    public List<RequestTraceEvent> historyWithin(Duration duration) {
        return historyWithin(duration, event -> true);
    }

    /**
     * 历史within
     *
     * @param duration 持续时间
     * @param predicate predicate
     * @return 历史within的结果
     */
    public List<RequestTraceEvent> historyWithin(Duration duration, Predicate<RequestTraceEvent> predicate) {
        long cutoff = now.minus(duration).toEpochMilli();
        return recentEvents.stream()
                .filter(event -> event.timestamp() >= cutoff)
                .filter(predicate)
                .toList();
    }
}
