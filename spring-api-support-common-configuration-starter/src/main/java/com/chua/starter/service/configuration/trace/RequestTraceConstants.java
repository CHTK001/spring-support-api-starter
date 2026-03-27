package com.chua.starter.service.configuration.trace;

/**
 * 请求追踪常量。
 *
 * @author CH
 * @since 2026/03/25
 */
public final class RequestTraceConstants {

    public static final String ATTRIBUTE_TRACE_ENABLED =
            RequestTraceConstants.class.getName() + ".traceEnabled";
    public static final String ATTRIBUTE_ROUTE_PATTERN =
            RequestTraceConstants.class.getName() + ".routePattern";
    public static final String ATTRIBUTE_STRATEGY_CODE =
            RequestTraceConstants.class.getName() + ".strategyCode";

    public static final String MDC_TRACE_ID = "traceId";
    public static final String MDC_USER_ID = "userId";
    public static final String MDC_REQUEST_IP = "requestIp";
    public static final String MDC_ELAPSED_MS = "elapsedMs";

    public static final String GROUP_CODE = "request_trace_strategy";

    private RequestTraceConstants() {
    }
}
