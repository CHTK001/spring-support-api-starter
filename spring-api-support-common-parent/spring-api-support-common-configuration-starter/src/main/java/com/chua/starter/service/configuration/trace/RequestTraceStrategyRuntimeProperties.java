package com.chua.starter.service.configuration.trace;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
* 请求追踪运行时属性。
*
* @since 2026/03/25
* @author CH
 */
@Data
@Component
@ConfigurationProperties(prefix = "request.trace.strategy")
public class RequestTraceStrategyRuntimeProperties {

    /**
    * 配置存储类型：database / Redis / 内存
     */
    private String configStorageType = "database";

    /**
    * 当持久化配置不存在时使用的 AI 分析默认开关。
     */
    private Boolean aiSummaryEnabled = Boolean.TRUE;

    /**
    * 当持久化配置不存在时使用的默认模型名。
     */
    private String aiModel;

    /**
    * 当持久化配置不存在时使用的 AI 限流开关。
     */
    private Boolean aiRateLimitEnabled = Boolean.TRUE;

    /**
    * 当持久化配置不存在时使用的 AI 限流窗口秒数。
     */
    private Integer aiRateLimitWindowSeconds = 60;

    /**
    * 当持久化配置不存在时使用的 AI 限流窗口最大调用数。
     */
    private Integer aiRateLimitMaxRequests = 30;

    /**
    * 当持久化配置不存在时使用的 AI 调用超时时间。
     */
    private Long aiTimeoutMs = 1500L;
}
