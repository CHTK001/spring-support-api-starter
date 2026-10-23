package com.chua.starter.oauth.sso.support.protocol;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 客户端地址可信解析测试。
 *
 * <p>守护「来源 IP 可伪造」：只有传输层地址是回环/可信代理时，才允许采信
 * {@code X-Forwarded-For}；否则伪造的转发头必须被忽略。</p>
 *
 * @since 4.0.0.42
 * @author CH
 */
@DisplayName("ClientAddressResolver: 只采信可信来源")
class ClientAddressResolverTest {

    /**
     * 无转发头时用传输层地址。
     */
    @Test
    @DisplayName("无转发头: 使用传输层地址")
    void shouldUseRemoteWhenNoForwardedHeader() {
        assertEquals("1.2.3.4", ClientAddressResolver.resolve(null, "1.2.3.4", null));
    }

    /**
     * 非可信来源伪造转发头必须被忽略。
     */
    @Test
    @DisplayName("不可信来源伪造 XFF: 忽略")
    void shouldIgnoreSpoofedHeader() {
        assertEquals("1.2.3.4",
                ClientAddressResolver.resolve("9.9.9.9", "1.2.3.4", null));
    }

    /**
     * 回环来源（本机代理）信任转发头。
     */
    @Test
    @DisplayName("回环来源: 信任 XFF")
    void shouldTrustLoopbackProxy() {
        assertEquals("9.9.9.9",
                ClientAddressResolver.resolve("9.9.9.9", "127.0.0.1", null));
        assertEquals("9.9.9.9",
                ClientAddressResolver.resolve("9.9.9.9", "::1", null));
    }

    /**
     * 显式配置的可信代理信任转发头，并取链最左端。
     */
    @Test
    @DisplayName("可信代理: 取 XFF 最左端")
    void shouldTrustConfiguredProxyAndTakeLeftmost() {
        assertEquals("9.9.9.9",
                ClientAddressResolver.resolve("9.9.9.9, 10.0.0.1", "10.0.0.10", List.of("10.0.0.10")));
    }

    /**
     * 消息型协议没有对端 IP：返回 null，避免全员落进同一个限流桶。
     */
    @Test
    @DisplayName("无传输层地址: 返回 null")
    void shouldReturnNullWhenRemoteMissing() {
        assertNull(ClientAddressResolver.resolve(null, null, null));
    }

    /**
     * IPv4 带端口时剥离端口。
     */
    @Test
    @DisplayName("剥离 IPv4 端口")
    void shouldStripPort() {
        assertEquals("1.2.3.4", ClientAddressResolver.resolve(null, "1.2.3.4:5678", null));
    }
}
