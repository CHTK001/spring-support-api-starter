package com.chua.starter.service.configuration.trace;

import java.util.Set;

/**
 * 请求追踪事件
 *
 * @since 2026/03/25
 * @author CH
 */
public record RequestTraceEvent(
        long timestamp,
        String traceId,
        String method,
        String path,
        String routePattern,
        String query,
        int status,
        long durationMs,
        String clientIp,
        String userAgent,
        String browser,
        String appName,
        String userId,
        String username,
        String fingerprint,
        String tokenHash,
        String strategyCode,
        Set<RequestTraceSignal> signals
) {

    public RequestTraceEvent {
        traceId = RequestTraceSupport.normalize(traceId);
        method = RequestTraceSupport.normalize(method);
        path = RequestTraceSupport.normalize(path);
        routePattern = RequestTraceSupport.normalize(routePattern);
        query = RequestTraceSupport.normalize(query);
        clientIp = RequestTraceSupport.normalize(clientIp);
        userAgent = RequestTraceSupport.normalize(userAgent);
        browser = RequestTraceSupport.normalize(browser);
        appName = RequestTraceSupport.normalize(appName);
        userId = RequestTraceSupport.normalize(userId);
        username = RequestTraceSupport.normalize(username);
        fingerprint = RequestTraceSupport.normalize(fingerprint);
        tokenHash = RequestTraceSupport.normalize(tokenHash);
        strategyCode = RequestTraceSupport.normalize(strategyCode);
        signals = signals == null || signals.isEmpty() ? Set.of() : Set.copyOf(signals);
    }

    /** WithSignals */
    public RequestTraceEvent withSignals(Set<RequestTraceSignal> newSignals) {
        return new RequestTraceEvent(
                timestamp,
                traceId,
                method,
                path,
                routePattern,
                query,
                status,
                durationMs,
                clientIp,
                userAgent,
                browser,
                appName,
                userId,
                username,
                fingerprint,
                tokenHash,
                strategyCode,
                newSignals
        );
    }

    /** Suspicious */
    public boolean suspicious() {
        return !signals.isEmpty();
    }

    /** RouteKey */
    public String routeKey() {
        if (method == null && path == null) {
            return "UNKNOWN";
        }
        String route = RequestTraceSupport.firstNonBlank(routePattern, path);
        if (method == null) {
            return route;
        }
        if (route == null) {
            return method;
        }
        return method + " " + route;
    }

    /** RequestUrl */
    public String requestUrl() {
        if (path == null) {
            return null;
        }
        if (query == null) {
            return path;
        }
        return path + "?" + query;
    }

    /** ActorKey */
    public String actorKey() {
        return RequestTraceSupport.firstNonBlank(userId, fingerprint, clientIp);
    }

    /** PrincipalKey */
    public String principalKey() {
        return RequestTraceSupport.firstNonBlank(userId, tokenHash, fingerprint, clientIp);
    }
}
