package com.chua.starter.monitor.agent;

import lombok.Data;
import org.springframework.http.HttpMethod;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 一次系统指标接口调用。控制器从 HTTP 请求还原后交给当前 SPI 数据源处理。
 *
 * <p>{@code path} 不含 {@code /api/apm} 前缀，例如 {@code /stats}、
 * {@code /trace/{traceId}/waterfall}；查询串保留原始 {@code a=b&c=d} 形式，
 * 便于直接透传给 hotsport 后端。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Data
public class SystemMetricRequest {

    /**
     * HTTP 方法。
     */
    private HttpMethod method = HttpMethod.GET;

    /**
     * 接口路径（不含统一前缀）。
     */
    private String path = "/";

    /**
     * 原始查询串，可为 {@code null}。
     */
    private String query;

    /**
     * 请求体，可为 {@code null} 或空数组。
     */
    private byte[] body;

    /**
     * 需要透传的请求头（白名单过滤后）。
     */
    private Map<String, String> headers = new LinkedHashMap<>();

    /**
     * 根据查询参数构造可拼接的 URL 尾串。
     *
     * @return 例如 {@code ?a=b&c=d}，无查询参数时为空串
     */
    public String querySuffix() {
        return query == null || query.isEmpty() ? "" : "?" + query;
    }
}