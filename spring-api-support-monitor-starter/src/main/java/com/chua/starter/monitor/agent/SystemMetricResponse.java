package com.chua.starter.monitor.agent;

import lombok.Data;

import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 数据源返回的一次响应。控制器把它翻译成 HTTP 响应：
 * 普通接口写 {@code body}，SSE 实时流写 {@code streamBody}。
 *
 * @author CH
 * @since 4.0.0.42
 */
@Data
public class SystemMetricResponse {

    /**
     * HTTP 状态码。
     */
    private int status = 200;

    /**
     * 响应头。
     */
    private Map<String, String> headers = new LinkedHashMap<>();

    /**
     * 普通响应体。
     */
    private byte[] body;

    /**
     * 流式响应体（SSE / 大文件），非空时优先于 {@code body}。
     */
    private Consumer<OutputStream> streamBody;

    /**
     * 快捷构造：普通 JSON 响应。
     *
     * @param status 状态码
     * @param contentType 内容类型
     * @param body 响应体
     * @return 响应对象
     */
    public static SystemMetricResponse of(int status, String contentType, byte[] body) {
        SystemMetricResponse response = new SystemMetricResponse();
        response.setStatus(status);
        response.getHeaders().put("Content-Type", contentType);
        response.setBody(body);
        return response;
    }

    /**
     * 快捷构造：SSE 流式响应。
     *
     * @param contentType 内容类型
     * @param stream 流写入逻辑
     * @return 响应对象
     */
    public static SystemMetricResponse stream(String contentType, Consumer<OutputStream> stream) {
        SystemMetricResponse response = new SystemMetricResponse();
        response.setStatus(200);
        response.getHeaders().put("Content-Type", contentType);
        response.setStreamBody(stream);
        return response;
    }
}