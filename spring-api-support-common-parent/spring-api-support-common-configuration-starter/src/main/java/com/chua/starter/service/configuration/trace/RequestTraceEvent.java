package com.chua.starter.service.configuration.trace;

import java.util.Set;

/**
 * 请求追踪事件。
 *
 * <p>描述一次入站请求的完整快照，是请求追踪体系的最小单元：由请求入口采集后，
 * 交由各个 {@link RequestTraceAnalyzer} 与历史事件比对得出命中的 {@link RequestTraceSignal}，
 * 再经 {@link RequestTraceRawRecordStorage} 落盘。字符串类组件都在紧凑构造器里经过
 * {@link RequestTraceSupport#normalize(String)} 处理（去首尾空白，空白串归一为 null），
 * 因此访问器返回的永远是已归一化文本或 null。</p>
 *
 * @param timestamp 请求发生时刻，Unix 纪元毫秒时间戳（{@code System.currentTimeMillis()}）。
 *                  原始类型不允许为 null，也不会被归一化；分析器用它做时间窗口过滤
 *                  （见 {@link RequestTraceAnalysisContext#historyWithin}），落盘时按系统时区换算成日期分文件
 * @param traceId 单次请求的链路追踪 ID，用于把同一次请求的多条记录串起来。
 *                允许为 null（无追踪头或未启用透传时），允许任意长度文本
 * @param method HTTP 请求方法，如 {@code GET} / {@code POST}。允许为 null（未采集到时）
 * @param path 实际请求路径（不含查询串），如 {@code /api/user/1001}。允许为 null
 * @param routePattern 匹配到的路由模板（路径变量已归一为占位符），如 {@code /api/user/{id}}。
 *                     允许为 null；{@link #routeKey()} 优先取它，取不到时回退到 {@link #path}
 * @param query 查询字符串，不含前导 {@code ?}。允许为 null；{@link #requestUrl()} 用它拼接完整 URL
 * @param status HTTP 响应状态码，如 200 / 404 / 500。原始 int，默认无异常状态时为 0；
 *                {@code >= 400} 被 {@link RequestTraceSignal#ERROR_BURST} 判定为异常请求
 * @param durationMs 请求处理耗时，单位毫秒。原始 long，不允许为 null；
 *                   与 {@code RequestTraceStrategyConfig.slowRequestThresholdMs}（默认 1500）比较判定
 *                   {@link RequestTraceSignal#SLOW_REQUEST}
 * @param clientIp 客户端真实 IP，多级代理时取第一段。允许为 null；
 *                 {@link #actorKey()} 在没有用户身份时以它作为行为主体兜底
 * @param userAgent 原始 {@code User-Agent} 请求头文本。允许为 null，可能很长
 * @param browser 由 {@link RequestTraceSupport#resolveBrowser(String)} 从 {@link #userAgent} 解析出的浏览器名，
 *                取值范围 Edge / Opera / Chrome / Firefox / Safari / IE / Postman / Curl / Wget / Bot / Other。
 *                允许为 null（{@link #userAgent} 为空时无法解析）
 * @param appName 客户端应用名，取自追踪头（如 {@code x-trace-app-name}）。允许为 null
 * @param userId 已认证用户 ID，未认证时为 null
 * @param username 已认证用户名，未认证时为 null
 * @param fingerprint 浏览器/终端指纹。允许为 null；与 {@link #userId} 一起构成行为主体维度
 * @param tokenHash 访问令牌的**哈希值**（不是明文令牌），由令牌原文做不可逆摘要后得到；
 *                  允许为 null（未携带令牌时）。明文令牌禁止写入本记录，
 *                  仅用该哈希做 {@link RequestTraceSignal#TOKEN_REUSE} 的同令牌比对
 * @param strategyCode 命中并执行本次追踪的策略编码，用于按策略分组统计。允许为 null
 * @param signals 本次请求命中的信号集合。允许传 null 或空集合（紧凑构造器统一归一为
 *                {@link Set#of()} 的不可变空集），归一后访问器**保证非 null**；
 *                元素不允许为 null（{@link Set#copyOf(Set)} 会拒绝 null 元素）；
 *                {@link #suspicious()} 以其是否为空判定本次请求是否可疑
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

    /**
     * withsignals
     *
     * @param newSignals 新signals
     * @return withSignals的结果
     */
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

    /**
     * Suspicious
     *
     * @return suspicious的结果
     */
    public boolean suspicious() {
        return !signals.isEmpty();
    }

    /**
     * route键
     *
     * @return route键的结果
     */
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

    /**
     * 请求url
     *
     * @return 请求url的结果
     */
    public String requestUrl() {
        if (path == null) {
            return null;
        }
        if (query == null) {
            return path;
        }
        return path + "?" + query;
    }

    /**
     * actor键
     *
     * @return actor键的结果
     */
    public String actorKey() {
        return RequestTraceSupport.firstNonBlank(userId, fingerprint, clientIp);
    }

    /**
     * principal键
     *
     * @return principal键的结果
     */
    public String principalKey() {
        return RequestTraceSupport.firstNonBlank(userId, tokenHash, fingerprint, clientIp);
    }
}
