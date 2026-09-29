package com.chua.starter.system.support;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

/**
 * 客户端真实 IP 解析。
 *
 * <p><b>为什么抽成公共的</b>：限流过滤器和接口停用/转发过滤器都要按来源 IP 做
 * 命中统计与名单匹配。两处各写一份「先看哪个头、逗号怎么截」的逻辑，早晚会分叉，
 * 表现为「同一次请求，限流统计记成 A、名单统计记成 B」，排查时极难定位。</p>
 *
 * <p>顺序：{@code X-Forwarded-For} → {@code Proxy-Client-IP} → {@code WL-Proxy-Client-IP}
 * → {@code HTTP_CLIENT_IP} → {@code request.getRemoteAddr()}；取到多值时只取第一个
 * （最靠近客户端的那个）。都没有时返回 {@code "unknown"} 而不是 null，
 * 让统计侧不必再判空。</p>
 *
 * @since 4.0.0.43
 * @author CH
 */
public final class ClientIpResolver {

    /**
     * 取不到时返回的占位值
     */
    public static final String UNKNOWN = "unknown";

    private ClientIpResolver() {
    }

    /**
     * 解析客户端 IP。
     *
     * @param request 请求，可为 null
     * @return 客户端 IP；解析不到时返回 {@link #UNKNOWN}
     */
    public static String resolve(HttpServletRequest request) {
        if (null == request) {
            return UNKNOWN;
        }
        String ip = firstNonBlank(
                request.getHeader("X-Forwarded-For"),
                request.getHeader("Proxy-Client-IP"),
                request.getHeader("WL-Proxy-Client-IP"),
                request.getHeader("HTTP_CLIENT_IP"));
        if (!StringUtils.hasText(ip)) {
            ip = request.getRemoteAddr();
        }
        if (!StringUtils.hasText(ip)) {
            return UNKNOWN;
        }
        int comma = ip.indexOf(',');
        if (comma >= 0) {
            ip = ip.substring(0, comma);
        }
        ip = ip.trim();
        return ip.isEmpty() || "unknown".equalsIgnoreCase(ip) ? UNKNOWN : ip;
    }

    /**
     * 取第一个有效值。
     *
     * @param values 候选值
     * @return 第一个非空白值；都没有返回 null
     */
    private static String firstNonBlank(String... values) {
        for (String v : values) {
            if (StringUtils.hasText(v) && !"unknown".equalsIgnoreCase(v)) {
                return v;
            }
        }
        return null;
    }
}
