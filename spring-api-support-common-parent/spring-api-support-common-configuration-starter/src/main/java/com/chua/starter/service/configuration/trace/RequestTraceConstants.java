package com.chua.starter.service.configuration.trace;

/**
 * 请求追踪常量。
 *
 * @since 2026/03/25
 */
public final class RequestTraceConstants {

    /** Attribute_trace_enabled */
    public static final String ATTRIBUTE_TRACE_ENABLED =
            RequestTraceConstants.class.getName() + ".traceEnabled";
    /** Attribute_route_pattern */
    public static final String ATTRIBUTE_ROUTE_PATTERN =
            RequestTraceConstants.class.getName() + ".routePattern";
    /** Attribute_strategy_code */
    public static final String ATTRIBUTE_STRATEGY_CODE =
            RequestTraceConstants.class.getName() + ".strategyCode";

    /** Mdc_trace_id */
    public static final String MDC_TRACE_ID = "traceId";
    /** Mdc_user_id */
    public static final String MDC_USER_ID = "userId";
    /** Mdc_request_ip */
    public static final String MDC_REQUEST_IP = "requestIp";
    /** Mdc_elapsed_ms */
    public static final String MDC_ELAPSED_MS = "elapsedMs";

    /** Group_code */
    public static final String GROUP_CODE = "request_trace_strategy";

    private RequestTraceConstants() {
    }
}
