package com.chua.starter.oauth.sso.support.trace;

import com.alibaba.fastjson.JSON;
import lombok.Getter;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 服务端追踪上下文
 * 用于接收客户端传递的追踪信息并记录服务端各节点耗时
 *
 * @author CH
 * @since 2025/01/01
 */
@Getter
public class ServerTraceContext {

    private static final ThreadLocal<ServerTraceContext> CONTEXT_HOLDER = new ThreadLocal<>();

    /**
     * 追踪ID
     */
    private final String traceId;

    /**
     * 客户端开始时间戳
     */
    private final long clientStartTimestamp;

    /**
     * 服务端接收时间
     */
    private final long serverReceiveTime;

    /**
     * 客户端应用名
     */
    private String clientAppName;

    /**
     * 客户端IP
     */
    private String clientIp;

    /**
     * Span列表
     */
    private final List<TraceSpan> spans = new ArrayList<>();

    /**
     * 当前Span
     */
    private TraceSpan currentSpan;

    private ServerTraceContext(String traceId, long clientStartTimestamp) {
        this.traceId = traceId != null ? traceId : UUID.randomUUID().toString().replace("-", "");
        this.clientStartTimestamp = clientStartTimestamp;
        this.serverReceiveTime = System.currentTimeMillis();
        
        // 设置MDC
        MDC.put("traceId", this.traceId);
    }

    /**
     * 从请求头创建追踪上下文
     */
    public static ServerTraceContext create(String traceId, String timestampStr, 
                                            String clientAppName, String clientIp) {
        long clientTimestamp = 0;
        if (timestampStr != null && !timestampStr.isEmpty()) {
            try {
                clientTimestamp = Long.parseLong(timestampStr);
            } catch (NumberFormatException ignored) {
            }
        }
        
        ServerTraceContext context = new ServerTraceContext(traceId, clientTimestamp);
        context.clientAppName = clientAppName;
        context.clientIp = clientIp;
        CONTEXT_HOLDER.set(context);
        return context;
    }

    /**
     * 获取当前追踪上下文
     */
    public static ServerTraceContext current() {
        return CONTEXT_HOLDER.get();
    }

    /**
     * 清理追踪上下文
     */
    public static void clear() {
        MDC.remove("traceId");
        CONTEXT_HOLDER.remove();
    }

    /**
     * 开始一个新的Span
     */
    public TraceSpan startSpan(String spanName) {
        TraceSpan span = new TraceSpan(spanName);
        spans.add(span);
        currentSpan = span;
        return span;
    }

    /**
     * 结束当前Span
     */
    public void endSpan() {
        if (currentSpan != null) {
            currentSpan.end();
            currentSpan = null;
        }
    }

    /**
     * 获取服务端总耗时（毫秒）
     */
    public long getServerCostMs() {
        return System.currentTimeMillis() - serverReceiveTime;
    }

    /**
     * 获取从客户端开始的总耗时（如果客户端时间戳有效）
     */
    public long getTotalCostMs() {
        if (clientStartTimestamp > 0) {
            return System.currentTimeMillis() - clientStartTimestamp;
        }
        return getServerCostMs();
    }

    /**
     * 获取Span列表的JSON表示
     */
    public String getSpansJson() {
        List<SpanInfo> spanInfos = new ArrayList<>();
        for (TraceSpan span : spans) {
            spanInfos.add(new SpanInfo(span.getSpanName(), span.getCostMs()));
        }
        return JSON.toJSONString(spanInfos);
    }

    /**
     * 获取Span摘要信息
     */
    public String getSpansSummary() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < spans.size(); i++) {
            TraceSpan span = spans.get(i);
            if (i > 0) {
                sb.append(" -> ");
            }
            sb.append(span.getSpanName()).append("(").append(span.getCostMs()).append("ms)");
        }
        return sb.toString();
    }

    /**
     * Span信息（用于JSON序列化）
     */
    public record SpanInfo(String name, long costMs) {}

    /**
     * 追踪Span
     */
    @Getter
    public static class TraceSpan {
        private final String spanName;
        private final long startTime;
        private long endTime;
        private long costMs;

        public TraceSpan(String spanName) {
            this.spanName = spanName;
            this.startTime = System.currentTimeMillis();
        }

        public void end() {
            this.endTime = System.currentTimeMillis();
            this.costMs = endTime - startTime;
        }
    }
}
