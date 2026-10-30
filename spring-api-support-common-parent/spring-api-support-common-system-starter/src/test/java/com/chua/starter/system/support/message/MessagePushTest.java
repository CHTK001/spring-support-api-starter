package com.chua.starter.system.support.message;

import com.chua.common.support.spi.ServiceProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * MessagePush.create SPI 接入锁定。
 *
 * <p>注册存在时走 SPI，别名大小写不敏感；未注册时回落原默认分支（日志模式）。</p>
 *
 * @author CH
 * @since 4.0.0.51
 */
@DisplayName("MessagePush.create SPI 接入")
class MessagePushTest {

    /**
     * aliyun 别名经 SPI 解析到阿里云实现。
     */
    @Test
    @DisplayName("aliyun走SPI-返回阿里云实现")
    void aliyun走Spi() {
        assertTrue(ServiceProvider.of(MessagePush.class).getExtensions().contains("ALIYUN"),
                "SPI 注册缺失: " + ServiceProvider.of(MessagePush.class).getExtensions());
        assertInstanceOf(AliyunSmsMessagePush.class, MessagePush.create("aliyun"));
        assertInstanceOf(AliyunSmsMessagePush.class, MessagePush.create("ALIYUN-SMS"));
    }

    /**
     * tencent 别名经 SPI 解析到腾讯云实现。
     */
    @Test
    @DisplayName("tencent走SPI-返回腾讯云实现")
    void tencent走Spi() {
        assertInstanceOf(TencentSmsMessagePush.class, MessagePush.create("tencent"));
    }

    /**
     * 未注册类型回落日志模式。
     */
    @Test
    @DisplayName("默认回退-未知类型与空类型走日志模式")
    void 默认回退() {
        assertInstanceOf(LogMessagePush.class, MessagePush.create("unknown-vendor"));
        assertInstanceOf(LogMessagePush.class, MessagePush.create(null));
        assertInstanceOf(LogMessagePush.class, MessagePush.create("  "));
    }
}
