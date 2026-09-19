package com.chua.starter.service.configuration.trace;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 请求追踪工具方法。
 *
 * @since 2026/03/25
 * @author CH
 */
public final class RequestTraceSupport {

    /**
     * 创建 请求追踪支持 实例
    */
    private RequestTraceSupport() {
    }

    /**
     * Normalize
     *
     * @param value 值
     * @return normalize的结果
     */
    public static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * 第一个nonblank
     *
     * @param values 值
     * @return 第一个nonblank的结果
     */
    public static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            String normalized = normalize(value);
            if (normalized != null) {
                return normalized;
            }
        }
        return null;
    }

    /**
     * 标志编码
     *
     * @param signals signals
     * @return 标志编码的结果
     */
    public static String signalCodes(Set<RequestTraceSignal> signals) {
        if (signals == null || signals.isEmpty()) {
            return null;
        }
        return signals.stream()
                .map(RequestTraceSignal::getCode)
                .sorted()
                .collect(Collectors.joining(","));
    }

    /**
     * 是否爬虫用户智能体
     *
     * @param userAgent 用户智能体
     * @return 是否爬虫用户智能体的结果
     */
    public static boolean isCrawlerUserAgent(String userAgent) {
        String normalized = normalize(userAgent);
        if (normalized == null) {
            return false;
        }
        String lower = normalized.toLowerCase();
        return lower.contains("bot")
                || lower.contains("spider")
                || lower.contains("crawler")
                || lower.contains("scrapy")
                || lower.contains("wget")
                || lower.contains("curl");
    }

    /**
     * 解析Browser
     *
     * @param userAgent 用户智能体
     * @return resolveBrowser的结果
     */
    public static String resolveBrowser(String userAgent) {
        String normalized = normalize(userAgent);
        if (normalized == null) {
            return null;
        }
        String lower = normalized.toLowerCase();
        if (lower.contains("edg/") || lower.contains(" edge/")) {
            return "Edge";
        }
        if (lower.contains("opr/") || lower.contains("opera")) {
            return "Opera";
        }
        if (lower.contains("chrome/") && !lower.contains("edg/") && !lower.contains("opr/")) {
            return "Chrome";
        }
        if (lower.contains("firefox/")) {
            return "Firefox";
        }
        if (lower.contains("safari/") && !lower.contains("chrome/")) {
            return "Safari";
        }
        if (lower.contains("msie") || lower.contains("trident/")) {
            return "IE";
        }
        if (lower.contains("postmanruntime")) {
            return "Postman";
        }
        if (lower.contains("curl/")) {
            return "Curl";
        }
        if (lower.contains("wget/")) {
            return "Wget";
        }
        if (isCrawlerUserAgent(normalized)) {
            return "Bot";
        }
        return "Other";
    }

    /**
     * 解析risk级别
     *
     * @param signals signals
     * @return resolverisk级别的结果
     */
    public static String resolveRiskLevel(Set<RequestTraceSignal> signals) {
        if (signals == null || signals.isEmpty()) {
            return "normal";
        }
        if (signals.contains(RequestTraceSignal.CRAWLER)
                || signals.contains(RequestTraceSignal.TOKEN_REUSE)
                || signals.size() >= 3) {
            return "high";
        }
        if (signals.contains(RequestTraceSignal.BURST)
                || signals.contains(RequestTraceSignal.HOTSPOT)
                || signals.contains(RequestTraceSignal.SPAM)
                || signals.contains(RequestTraceSignal.ERROR_BURST)
                || signals.contains(RequestTraceSignal.FINGERPRINT_DRIFT)
                || signals.contains(RequestTraceSignal.USER_PREFERENCE_DRIFT)) {
            return "medium";
        }
        return "low";
    }
}
