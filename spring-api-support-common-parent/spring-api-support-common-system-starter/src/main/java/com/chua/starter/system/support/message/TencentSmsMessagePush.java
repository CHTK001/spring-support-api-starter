package com.chua.starter.system.support.message;

import lombok.extern.slf4j.Slf4j;

/**
 * 腾讯云短信推送
 * <p>
 * 简化替代实现。当前版本仅记录日志，实际发送需接入腾讯云 SMS SDK。
 * 如需真实发送，请在本类中实现腾讯云短信 API 调用。
 * </p>
 *
 * @author system
 * @since 2026/09/13
 */
@Slf4j
public class TencentSmsMessagePush extends MessagePush {

    @Override
    public MessageResponse send(MessageRequest request) {
        log.warn("【腾讯云短信-简化实现】当前为日志模拟模式，未实际调用腾讯云API。to={}, content={}",
                request.getTo(), request.getContent());
        log.info("如需真实发送，请在 TencentSmsMessagePush.send() 中接入腾讯云 SMS SDK");
        // 模拟发送成功
        return MessageResponse.ok("TENCENT-SIM-" + System.currentTimeMillis());
    }
}
