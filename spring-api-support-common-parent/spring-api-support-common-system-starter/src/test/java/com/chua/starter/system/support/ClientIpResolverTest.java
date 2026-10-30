package com.chua.starter.system.support;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link ClientIpResolver} 头链顺序与 unknown 口径锁定。
 *
 * <p>沿用 {@code PolicyRateLimitFilterTest.TestRequest} 的 Proxy 风格构造
 * {@link HttpServletRequest}，只覆盖头链语义：顺序、多值首段、unknown 跳过、回退。</p>
 *
 * @author CH
 * @since 4.0.0.51
 */
@DisplayName("ClientIpResolver 头链语义")
class ClientIpResolverTest {

    @Test
    @DisplayName("头链顺序：XFF优先，其次各级代理头")
    void headerOrder() {
        assertEquals("1.2.3.4", ClientIpResolver.resolve(request(
                Map.of("X-Forwarded-For", "1.2.3.4, 5.6.7.8", "Proxy-Client-IP", "6.6.6.6"), "9.9.9.9")));
        assertEquals("6.6.6.6", ClientIpResolver.resolve(request(
                Map.of("Proxy-Client-IP", "6.6.6.6"), "9.9.9.9")));
        assertEquals("7.7.7.7", ClientIpResolver.resolve(request(
                Map.of("WL-Proxy-Client-IP", "7.7.7.7"), "9.9.9.9")));
        assertEquals("8.8.8.8", ClientIpResolver.resolve(request(
                Map.of("HTTP_CLIENT_IP", "8.8.8.8"), "9.9.9.9")));
    }

    @Test
    @DisplayName("多值取逗号首段")
    void firstSegment() {
        assertEquals("1.2.3.4", ClientIpResolver.resolve(request(
                Map.of("X-Forwarded-For", "1.2.3.4, 5.6.7.8"), "9.9.9.9")));
    }

    @Test
    @DisplayName("整头unknown跳过取下一级、首段unknown回落unknown、无头回退remoteAddr、缺失回unknown")
    void unknownAndFallback() {
        assertEquals("6.6.6.6", ClientIpResolver.resolve(request(
                Map.of("X-Forwarded-For", "UNKNOWN", "Proxy-Client-IP", "6.6.6.6"), "9.9.9.9")));
        assertEquals("unknown", ClientIpResolver.resolve(request(
                Map.of("X-Forwarded-For", "unknown, 1.2.3.4"), "9.9.9.9")));
        assertEquals("9.9.9.9", ClientIpResolver.resolve(request(Map.of(), "9.9.9.9")));
        assertEquals("unknown", ClientIpResolver.resolve(request(Map.of(), null)));
        assertEquals("unknown", ClientIpResolver.resolve(null));
    }

    private static HttpServletRequest request(Map<String, String> headers, String remoteAddr) {
        Map<String, String> copy = new HashMap<>(headers);
        return (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getHeader" -> copy.get(String.valueOf(args[0]));
                    case "getRemoteAddr" -> remoteAddr;
                    default -> null;
                });
    }
}
