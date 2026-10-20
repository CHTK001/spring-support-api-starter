package com.chua.starter.system.support;

import com.chua.common.support.network.net.IpCidrUtils;
import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;

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
 * <p>此前为自实现，现委托 utils IpCidrUtils（@since 4.0.0.43），行为一致。</p>
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
        Map<String, String> headers = new HashMap<>(4);
        headers.put("X-Forwarded-For", request.getHeader("X-Forwarded-For"));
        headers.put("Proxy-Client-IP", request.getHeader("Proxy-Client-IP"));
        headers.put("WL-Proxy-Client-IP", request.getHeader("WL-Proxy-Client-IP"));
        headers.put("HTTP_CLIENT_IP", request.getHeader("HTTP_CLIENT_IP"));
        return IpCidrUtils.resolveClientIp(headers, request.getRemoteAddr());
    }
}
