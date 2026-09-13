package com.chua.starter.system.support.message;

import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * 日志模式消息推送
 * <p>
 * 不实际发送消息，只记录日志。用于开发/测试环境，或未配置短信服务商时的兜底。
 * </p>
 *
 * @author system
 * @since 2026/09/13
 */
@Slf4j
public class LogMessagePush extends MessagePush {

    @Override
    public MessageResponse send(MessageRequest request) {
        log.info("【日志模式-消息推送】to={}, content={}, templateId={}",
                request.getTo(), request.getContent(), request.getTemplateId());
        // 模拟发送成功，返回生成的消息ID
        return MessageResponse.ok("LOG-" + UUID.randomUUID().toString().replace("-", ""));
    }
}
