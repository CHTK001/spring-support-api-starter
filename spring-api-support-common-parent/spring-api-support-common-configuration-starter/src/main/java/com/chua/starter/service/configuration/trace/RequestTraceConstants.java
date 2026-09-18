package com.chua.starter.service.configuration.trace;

/**
 * 请求追踪常量。
 *
 * @since 2026/03/25
 * @author CH
 */
public final class RequestTraceConstants {

    /** Attribute_追踪_已启用 */
    public static final String ATTRIBUTE_TRACE_ENABLED =
            RequestTraceConstants.class.getName() + ".traceEnabled";
    /** Attribute_route_模式 */
    public static final String ATTRIBUTE_ROUTE_PATTERN =
            RequestTraceConstants.class.getName() + ".routePattern";
    /** Attribute_strategy_编码 */
    public static final String ATTRIBUTE_STRATEGY_CODE =
            RequestTraceConstants.class.getName() + ".strategyCode";

    /** Mdc_追踪_标识 */
    public static final String MDC_TRACE_ID = "traceId";
    /** Mdc_用户_标识 */
    public static final String MDC_USER_ID = "userId";
    /** Mdc_请求_ip */
    public static final String MDC_REQUEST_IP = "requestIp";
    /** Mdc_elapsed_ms */
    public static final String MDC_ELAPSED_MS = "elapsedMs";

    /** 群体_编码 */
    public static final String GROUP_CODE = "request_trace_strategy";

    /** 创建 请求追踪常量 实例 */
    private RequestTraceConstants() {
    }
}
