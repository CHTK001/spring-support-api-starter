package com.chua.starter.system.support.message;

import com.chua.common.support.spi.annotations.Spi;
import lombok.extern.slf4j.Slf4j;

/**
 * 阿里云短信推送
 * <p>
 * 简化替代实现。当前版本仅记录日志，实际发送需接入阿里云 Dysmsapi SDK。
 * 如需真实发送，请在本类中实现阿里云短信 API 调用（使用 appId 作为 AccessKeyId，appSecret 作为 AccessKeySecret）。
 * </p>
 *
 * @author system
 * @since 2026/09/13
 */
@Slf4j
@Spi({"aliyun", "aliyun-sms", "ali"})
public class AliyunSmsMessagePush extends MessagePush {

    @Override
    public MessageResponse send(MessageRequest request) {
        log.warn("【阿里云短信-简化实现】当前为日志模拟模式，未实际调用阿里云API。to={}, content={}",
                request.getTo(), request.getContent());
        log.info("如需真实发送，请在 AliyunSmsMessagePush.send() 中接入阿里云 Dysmsapi SDK");
        // 模拟发送成功
        return MessageResponse.ok("ALIYUN-SIM-" + System.currentTimeMillis());
    }
}
