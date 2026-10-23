package com.chua.starter.oauth.sso.support.protocol;

import java.util.Collection;
import java.util.Locale;

/**
 * 认证入口的客户端地址解析。
 *
 * <p>登录限流、审计日志都依赖「来源 IP」，而这个值必须来自传输层，不能直接采信请求头：
 * 调用方可以随便写 {@code X-Forwarded-For} 或请求体里的 {@code x-oauth-param-address}，
 * 一旦采信，按 IP 的限流可被逐次换 IP 绕过，审计日志也会被污染。</p>
 *
 * <p>规则：</p>
 * <ol>
 *   <li>没有转发头 → 用传输层地址；</li>
 *   <li>有转发头，但传输层地址不在可信代理列表（且不是回环）→ 仍然用传输层地址；</li>
 *   <li>传输层地址是回环/可信代理 → 取转发链最左端的客户端地址。</li>
 * </ol>
 *
 * @since 4.0.0.42
 * @author CH
 */
public final class ClientAddressResolver {

    /**
     * 转发头名称
     */
    public static final String FORWARDED_FOR = "X-Forwarded-For";

    /**
     * 工具类禁止实例化
     */
    private ClientAddressResolver() {
    }

    /**
     * 解析可信客户端地址。
     *
     * @param forwardedFor    {@code X-Forwarded-For} 原始值，可为空
     * @param remoteAddress   传输层来源地址，可为空（消息型协议没有 IP 概念）
     * @param trustedProxies  可信代理地址列表，可为空
     * @return 可信客户端地址；无法确定时返回 {@code null}
     */
    public static String resolve(String forwardedFor, String remoteAddress, Collection<String> trustedProxies) {
        String remote = normalizeSingle(remoteAddress);
        String forwarded = firstForwarded(forwardedFor);
        if (forwarded == null) {
            return remote;
        }
        if (isTrustedProxy(remote, trustedProxies)) {
            return forwarded;
        }
        return remote;
    }

    /**
     * 是否可信代理（回环或显式配置）。
     *
     * @param remote 传输层地址
     * @param trustedProxies 可信代理列表
     * @return 可信返回 true
     */
    private static boolean isTrustedProxy(String remote, Collection<String> trustedProxies) {
        if (remote == null) {
            return false;
        }
        if (isLoopback(remote)) {
            return true;
        }
        if (trustedProxies == null) {
            return false;
        }
        for (String proxy : trustedProxies) {
            if (proxy != null && remote.equalsIgnoreCase(proxy.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否回环地址。
     *
     * @param address 地址
     * @return 回环返回 true
     */
    private static boolean isLoopback(String address) {
        String value = address.toLowerCase(Locale.ROOT);
        return "127.0.0.1".equals(value)
                || "::1".equals(value)
                || "0:0:0:0:0:0:0:1".equals(value)
                || "localhost".equals(value);
    }

    /**
     * 取转发链最左端的地址。
     *
     * @param forwardedFor 转发头
     * @return 客户端地址，缺失返回 {@code null}
     */
    private static String firstForwarded(String forwardedFor) {
        if (forwardedFor == null || forwardedFor.isBlank()) {
            return null;
        }
        for (String part : forwardedFor.split(",")) {
            String candidate = normalizeSingle(part);
            if (candidate != null && !"unknown".equalsIgnoreCase(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    /**
     * 归一化单个地址：去空白、去方括号、去端口。
     *
     * @param address 原始地址
     * @return 归一化地址，缺失返回 {@code null}
     */
    private static String normalizeSingle(String address) {
        if (address == null) {
            return null;
        }
        String value = address.trim();
        if (value.isEmpty()) {
            return null;
        }
        if (value.startsWith("[")) {
            int end = value.indexOf(']');
            if (end > 0) {
                return value.substring(1, end);
            }
        }
        // IPv4:port —— 只剥离「恰好 4 段数字 + 端口」的形式，避免误伤 IPv6
        if (value.matches("^\\d{1,3}(\\.\\d{1,3}){3}:\\d{1,5}$")) {
            return value.substring(0, value.lastIndexOf(':'));
        }
        return value;
    }
}
