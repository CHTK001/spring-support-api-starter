package com.chua.starter.service.statistics;

import com.chua.starter.service.configuration.trace.RequestTraceSupport;

import java.time.Instant;
import java.util.Map;

public record RequestStatisticsRecord(
        long timestamp,
        String traceId,
        String route,
        String method,
        String path,
        int status,
        long durationMs,
        String clientIp,
        String userId,
        String username,
        String fingerprint,
        String tokenHash,
        String browser,
        String appName,
        String requestId,
        String requestUrl,
        String requestQuery,
        String requestBodyHash,
        String responseBodyHash,
        long inputTokens,
        long outputTokens,
        long cachedTokens,
        long totalTokens,
        long promptCharacters,
        long completionCharacters,
        long cacheHits,
        long cacheMisses,
        long costCents,
        String costCurrency,
        String model,
        String provider,
        String channel,
        String statusText,
        Map<String, Object> extra,
        Instant recordedAt
) {
    public RequestStatisticsRecord {
        traceId = RequestTraceSupport.normalize(traceId);
        route = RequestTraceSupport.normalize(route);
        method = RequestTraceSupport.normalize(method);
        path = RequestTraceSupport.normalize(path);
        clientIp = RequestTraceSupport.normalize(clientIp);
        userId = RequestTraceSupport.normalize(userId);
        username = RequestTraceSupport.normalize(username);
        fingerprint = RequestTraceSupport.normalize(fingerprint);
        tokenHash = RequestTraceSupport.normalize(tokenHash);
        browser = RequestTraceSupport.normalize(browser);
        appName = RequestTraceSupport.normalize(appName);
        requestId = RequestTraceSupport.normalize(requestId);
        requestUrl = RequestTraceSupport.normalize(requestUrl);
        requestQuery = RequestTraceSupport.normalize(requestQuery);
        requestBodyHash = RequestTraceSupport.normalize(requestBodyHash);
        responseBodyHash = RequestTraceSupport.normalize(responseBodyHash);
        costCurrency = RequestTraceSupport.normalize(costCurrency);
        model = RequestTraceSupport.normalize(model);
        provider = RequestTraceSupport.normalize(provider);
        channel = RequestTraceSupport.normalize(channel);
        statusText = RequestTraceSupport.normalize(statusText);
        extra = extra == null ? Map.of() : Map.copyOf(extra);
        recordedAt = recordedAt == null ? Instant.ofEpochMilli(timestamp) : recordedAt;
    }

    public String routeKey() {
        return RequestTraceSupport.firstNonBlank(route, path, requestUrl);
    }
}
