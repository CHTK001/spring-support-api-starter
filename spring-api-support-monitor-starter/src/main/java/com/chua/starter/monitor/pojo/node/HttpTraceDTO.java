package com.chua.starter.monitor.pojo.node;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * HTTP 追踪信息 DTO
 * <p>
 * 用于记录 HTTP 请求的详细信息，包括请求方法、URI、状态码、耗时等
 * </p>
 *
 * @author CH
 * @version 1.0.0
 * @since 2025/01/17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "HTTP 追踪信息")
public class HttpTraceDTO {

    /**
     * 请求时间戳
     */
    @Schema(description = "请求时间戳")
    private Long timestamp;

    /**
     * 请求方法
     */
    @Schema(description = "请求方法: GET, POST, PUT, DELETE 等")
    private String method;

    /**
     * 请求 URI
     */
    @Schema(description = "请求 URI")
    private String uri;

    /**
     * HTTP 状态码
     */
    @Schema(description = "HTTP 状态码")
    private Integer statusCode;

    /**
     * 请求耗时（毫秒）
     */
    @Schema(description = "请求耗时（毫秒）")
    private Long timeTaken;

    /**
     * 客户端 IP 地址
     */
    @Schema(description = "客户端 IP 地址")
    private String remoteAddress;

    /**
     * 会话 ID
     */
    @Schema(description = "会话 ID")
    private String sessionId;

    /**
     * 请求头
     */
    @Schema(description = "请求头")
    private Map<String, String> requestHeaders;

    /**
     * 响应头
     */
    @Schema(description = "响应头")
    private Map<String, String> responseHeaders;

    /**
     * 用户代理
     */
    @Schema(description = "用户代理")
    private String userAgent;

    /**
     * 请求内容类型
     */
    @Schema(description = "请求内容类型")
    private String contentType;
}
