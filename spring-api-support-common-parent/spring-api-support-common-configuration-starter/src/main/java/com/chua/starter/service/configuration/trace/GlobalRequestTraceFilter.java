package com.chua.starter.service.configuration.trace;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 全局请求追踪过滤器。
 *
 * <p>仅对带有 {@link RequestTraceStrategy} 注解的控制器或方法记录行为数据。</p>
 *
 * @author CH
 * @since 2026/03/25
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(name = {
        "org.springframework.web.filter.OncePerRequestFilter",
        "jakarta.servlet.http.HttpServletRequest"
})
public class GlobalRequestTraceFilter extends OncePerRequestFilter {

    private static final Set<String> IGNORE_PATH_PREFIXES = Set.of(
            "/v1/request-trace/analytics",
            "/actuator"
    );
    private static final Set<String> STATIC_SUFFIXES = Set.of(
            ".js", ".css", ".map", ".png", ".jpg", ".jpeg", ".gif", ".svg", ".ico", ".woff", ".woff2"
    );
    private static final List<String> IP_HEADERS = List.of(
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR"
    );
    private static final List<String> FINGERPRINT_HEADERS = List.of(
            "x-oauth-fingerprint",
            "x-req-fingerprint",
            "x-request-fingerprint",
            "x-fingerprint",
            "fingerprint"
    );

    private final RequestTraceAnalyticsService requestTraceAnalyticsService;
    private final ApplicationContext applicationContext;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        if (requestUri == null || requestUri.isBlank()) {
            return true;
        }
        for (String prefix : IGNORE_PATH_PREFIXES) {
            if (requestUri.startsWith(prefix)) {
                return true;
            }
        }
        for (String suffix : STATIC_SUFFIXES) {
            if (requestUri.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        String traceId = resolveTraceId(request);
        CurrentUser currentUser = resolveCurrentUser();
        String clientIp = resolveClientIp(request);

        String previousTraceId = MDC.get(RequestTraceConstants.MDC_TRACE_ID);
        String previousUserId = MDC.get(RequestTraceConstants.MDC_USER_ID);
        String previousRequestIp = MDC.get(RequestTraceConstants.MDC_REQUEST_IP);
        String previousElapsedMs = MDC.get(RequestTraceConstants.MDC_ELAPSED_MS);

        setMdcValue(RequestTraceConstants.MDC_TRACE_ID, traceId);
        setMdcValue(RequestTraceConstants.MDC_USER_ID, currentUser.userId());
        setMdcValue(RequestTraceConstants.MDC_REQUEST_IP, clientIp);
        response.setHeader("x-trace-id", traceId);

        int status = HttpServletResponse.SC_OK;
        try {
            filterChain.doFilter(request, response);
            status = response.getStatus();
        } catch (IOException | ServletException | RuntimeException ex) {
            status = response.getStatus() >= 400 ? response.getStatus() : HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            throw ex;
        } finally {
            long durationMs = System.currentTimeMillis() - startTime;
            setMdcValue(RequestTraceConstants.MDC_ELAPSED_MS, String.valueOf(durationMs));
            try {
                TraceTarget traceTarget = resolveTraceTarget(request);
                request.setAttribute(RequestTraceConstants.ATTRIBUTE_TRACE_ENABLED, traceTarget.enabled());
                String tokenHash = null;
                if (traceTarget.enabled()) {
                    request.setAttribute(RequestTraceConstants.ATTRIBUTE_ROUTE_PATTERN, traceTarget.routePattern());
                    request.setAttribute(RequestTraceConstants.ATTRIBUTE_STRATEGY_CODE, traceTarget.strategyCode());
                    tokenHash = hashToken(resolveToken(request));
                    recordRequestTrace(request, traceTarget, traceId, status, durationMs, currentUser, clientIp, tokenHash);
                }
                enqueueRequestStatistics(buildStatisticsRecord(request, traceTarget, traceId, status, durationMs, currentUser, clientIp, tokenHash));
            } catch (Exception ex) {
                log.debug("记录请求追踪失败: traceId={}", traceId, ex);
            } finally {
                restoreMdcValue(RequestTraceConstants.MDC_TRACE_ID, previousTraceId);
                restoreMdcValue(RequestTraceConstants.MDC_USER_ID, previousUserId);
                restoreMdcValue(RequestTraceConstants.MDC_REQUEST_IP, previousRequestIp);
                restoreMdcValue(RequestTraceConstants.MDC_ELAPSED_MS, previousElapsedMs);
            }
        }
    }

    private void recordRequestTrace(
            HttpServletRequest request,
            TraceTarget traceTarget,
            String traceId,
            int status,
            long durationMs,
            CurrentUser currentUser,
            String clientIp,
            String tokenHash
    ) {
        String fingerprint = firstNonBlank(resolveFingerprint(request), currentUser.fingerprint());
        String userAgent = truncate(request.getHeader("User-Agent"), 256);
        requestTraceAnalyticsService.record(new RequestTraceEvent(
                System.currentTimeMillis(),
                traceId,
                request.getMethod(),
                request.getRequestURI(),
                traceTarget.routePattern(),
                sanitizeQuery(request.getQueryString()),
                status,
                durationMs,
                clientIp,
                userAgent,
                RequestTraceSupport.resolveBrowser(userAgent),
                truncate(request.getHeader("x-trace-app-name"), 64),
                truncate(currentUser.userId(), 64),
                truncate(currentUser.username(), 64),
                truncate(fingerprint, 128),
                tokenHash,
                traceTarget.strategyCode(),
                Set.of()
        ));

    }

    private Object buildStatisticsRecord(
            HttpServletRequest request,
            TraceTarget traceTarget,
            String traceId,
            int status,
            long durationMs,
            CurrentUser currentUser,
            String clientIp,
            String tokenHash
    ) {
        String userAgent = truncate(request.getHeader("User-Agent"), 256);
        String fingerprint = firstNonBlank(resolveFingerprint(request), currentUser.fingerprint());
        String requestUrl = request.getRequestURI();
        String query = sanitizeQuery(request.getQueryString());
        String fullUrl = query == null ? requestUrl : requestUrl + "?" + query;
        try {
            Class<?> recordClass = Class.forName("com.chua.starter.service.statistics.RequestStatisticsRecord");
            return recordClass.getConstructor(
                    long.class,
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    int.class,
                    long.class,
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    long.class,
                    long.class,
                    long.class,
                    long.class,
                    long.class,
                    long.class,
                    long.class,
                    long.class,
                    long.class,
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    java.util.Map.class,
                    java.time.Instant.class
            ).newInstance(
                    System.currentTimeMillis(),
                    traceId,
                    traceTarget.routePattern(),
                    request.getMethod(),
                    request.getRequestURI(),
                    status,
                    durationMs,
                    clientIp,
                    currentUser.userId(),
                    currentUser.username(),
                    truncate(fingerprint, 128),
                    tokenHash,
                    userAgent,
                    truncate(request.getHeader("x-trace-app-name"), 64),
                    traceId,
                    fullUrl,
                    query,
                    null,
                    null,
                    0L,
                    0L,
                    0L,
                    0L,
                    0L,
                    0L,
                    0L,
                    0L,
                    0L,
                    null,
                    null,
                    null,
                    traceTarget.strategyCode(),
                    String.valueOf(status),
                    java.util.Map.of("routeEnabled", traceTarget.enabled()),
                    java.time.Instant.now()
            );
        } catch (ClassNotFoundException ex) {
            return null;
        } catch (Exception ex) {
            log.debug("build request statistics record failed", ex);
            return null;
        }
    }

    private void enqueueRequestStatistics(Object record) {
        if (record == null) {
            return;
        }
        try {
            if (!applicationContext.containsBean("requestStatisticsIngestionService")) {
                return;
            }
            Object service = applicationContext.getBean("requestStatisticsIngestionService");
            service.getClass().getMethod("enqueue", Class.forName("com.chua.starter.service.statistics.RequestStatisticsRecord"))
                    .invoke(service, record);
        } catch (ClassNotFoundException ex) {
            // statistics module is optional
        } catch (Exception ex) {
            log.debug("enqueue request statistics failed", ex);
        }
    }

    private TraceTarget resolveTraceTarget(HttpServletRequest request) {
        Object handler = request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE);
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return TraceTarget.disabled();
        }
        RequestTraceStrategy methodAnnotation =
                AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getMethod(), RequestTraceStrategy.class);
        RequestTraceStrategy typeAnnotation =
                AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getBeanType(), RequestTraceStrategy.class);
        RequestTraceStrategy annotation = methodAnnotation != null ? methodAnnotation : typeAnnotation;
        if (annotation == null) {
            return TraceTarget.disabled();
        }
        String routePattern = normalizeRoutePattern(request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE));
        String strategyCode = RequestTraceSupport.firstNonBlank(
                annotation.value(),
                routePattern,
                handlerMethod.getBeanType().getSimpleName() + "#" + handlerMethod.getMethod().getName()
        );
        return new TraceTarget(true, routePattern, strategyCode);
    }

    private String normalizeRoutePattern(Object routePattern) {
        if (routePattern == null) {
            return null;
        }
        return truncate(String.valueOf(routePattern), 255);
    }

    private String resolveTraceId(HttpServletRequest request) {
        String traceId = firstNonBlank(request.getHeader("x-trace-id"), MDC.get(RequestTraceConstants.MDC_TRACE_ID));
        if (traceId != null) {
            return truncate(traceId, 64);
        }
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String resolveClientIp(HttpServletRequest request) {
        for (String header : IP_HEADERS) {
            String ip = request.getHeader(header);
            if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
                continue;
            }
            if (ip.contains(",")) {
                return truncate(ip.split(",")[0].trim(), 64);
            }
            return truncate(ip.trim(), 64);
        }
        return truncate(request.getRemoteAddr(), 64);
    }

    private String resolveToken(HttpServletRequest request) {
        String token = request.getHeader("x-oauth-token");
        if (token != null && !token.isBlank()) {
            return token;
        }

        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return authorization.substring(7).trim();
        }

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if ("x-oauth-cookie".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private String resolveFingerprint(HttpServletRequest request) {
        for (String header : FINGERPRINT_HEADERS) {
            String value = request.getHeader(header);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        String parameter = request.getParameter("fingerprint");
        if (parameter != null && !parameter.isBlank()) {
            return parameter;
        }
        String oauthFingerprint = request.getParameter("x-oauth-fingerprint");
        if (oauthFingerprint != null && !oauthFingerprint.isBlank()) {
            return oauthFingerprint;
        }
        return null;
    }

    private CurrentUser resolveCurrentUser() {
        try {
            Class<?> authClientExecuteClass = Class.forName("com.chua.starter.oauth.client.support.execute.AuthClientExecute");
            Object authClientExecute = authClientExecuteClass.getMethod("getInstance").invoke(null);
            if (authClientExecute == null) {
                return CurrentUser.EMPTY;
            }
            Boolean whiteList = invokeBoolean(authClientExecute, "whiteList");
            if (Boolean.TRUE.equals(whiteList)) {
                return CurrentUser.EMPTY;
            }

            Object userResult = authClientExecuteClass.getMethod("getUserResult").invoke(authClientExecute);
            if (userResult == null) {
                return CurrentUser.EMPTY;
            }
            String userId = firstNonBlank(invokeOption(userResult, "sysUserId"), invokeString(userResult, "getUserId"));
            String username = firstNonBlank(invokeOption(userResult, "userRealName"), invokeString(userResult, "getUsername"));
            String fingerprint = firstNonBlank(invokeOption(userResult, "fingerprint"), invokeString(userResult, "getFingerprint"));
            return new CurrentUser(userId, username, fingerprint);
        } catch (ClassNotFoundException ex) {
            return CurrentUser.EMPTY;
        } catch (LinkageError ex) {
            log.debug("读取当前登录用户失败: oauth 客户端初始化异常", ex);
            return CurrentUser.EMPTY;
        } catch (Exception ex) {
            log.debug("读取当前登录用户失败", ex);
            return CurrentUser.EMPTY;
        }
    }

    private Boolean invokeBoolean(Object target, String methodName) {
        Object value = invoke(target, methodName);
        if (value instanceof Boolean result) {
            return result;
        }
        if (value == null) {
            return Boolean.FALSE;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }

    private String invokeOption(Object target, String key) {
        Object value = invoke(target, "getOption", new Class<?>[]{String.class}, key);
        if (value == null) {
            return null;
        }
        return String.valueOf(value);
    }

    private String invokeString(Object target, String methodName) {
        Object value = invoke(target, methodName);
        if (value == null) {
            return null;
        }
        return String.valueOf(value);
    }

    private Object invoke(Object target, String methodName) {
        return invoke(target, methodName, new Class<?>[0]);
    }

    private Object invoke(Object target, String methodName, Class<?>[] parameterTypes, Object... args) {
        if (target == null) {
            return null;
        }
        try {
            Method method = target.getClass().getMethod(methodName, parameterTypes);
            return method.invoke(target, args);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String sanitizeQuery(String query) {
        if (query == null || query.isBlank()) {
            return null;
        }

        StringBuilder builder = new StringBuilder();
        String[] parts = query.split("&");
        for (String part : parts) {
            if (builder.length() > 0) {
                builder.append('&');
            }

            int separatorIndex = part.indexOf('=');
            String key = separatorIndex >= 0 ? part.substring(0, separatorIndex) : part;
            String value = separatorIndex >= 0 ? part.substring(separatorIndex + 1) : "";

            builder.append(key);
            if (separatorIndex >= 0) {
                builder.append('=');
                builder.append(isSensitiveKey(key) ? "***" : truncate(value, 64));
            }
        }
        return truncate(builder.toString(), 256);
    }

    private boolean isSensitiveKey(String key) {
        String normalized = key == null ? "" : key.toLowerCase(Locale.ROOT);
        return normalized.contains("token")
                || normalized.contains("cookie")
                || normalized.contains("authorization")
                || normalized.contains("password")
                || normalized.contains("secret");
    }

    private String hashToken(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(hashed.length * 2);
            for (byte value : hashed) {
                builder.append(Character.forDigit((value >> 4) & 0xF, 16));
                builder.append(Character.forDigit(value & 0xF, 16));
            }
            return builder.substring(0, Math.min(builder.length(), 24));
        } catch (NoSuchAlgorithmException ex) {
            return Integer.toHexString(token.hashCode());
        }
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        return second;
    }

    private void setMdcValue(String key, String value) {
        if (value == null || value.isBlank()) {
            MDC.remove(key);
            return;
        }
        MDC.put(key, value);
    }

    private void restoreMdcValue(String key, String value) {
        if (value == null) {
            MDC.remove(key);
            return;
        }
        MDC.put(key, value);
    }

    private record CurrentUser(String userId, String username, String fingerprint) {
        private static final CurrentUser EMPTY = new CurrentUser(null, null, null);
    }

    private record TraceTarget(boolean enabled, String routePattern, String strategyCode) {
        private static TraceTarget disabled() {
            return new TraceTarget(false, null, null);
        }
    }
}
