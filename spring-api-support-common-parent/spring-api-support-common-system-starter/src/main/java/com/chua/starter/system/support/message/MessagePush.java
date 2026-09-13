package com.chua.starter.system.support.message;

import lombok.extern.slf4j.Slf4j;

/**
 * 消息推送抽象类
 * <p>
 * 替代 utils-support-task-starter 中的 MessagePush。
 * 提供工厂方法 {@link #create(String)} 根据 appType 创建对应实现。
 * </p>
 *
 * <pre>
 * MessagePush push = MessagePush.create("aliyun");
 * MessageResponse resp = push.send(MessageRequest.builder()
 *         .to("13800138000")
 *         .content("您的验证码是：123456")
 *         .build());
 * </pre>
 *
 * @author system
 * @since 2026/09/13
 */
@Slf4j
public abstract class MessagePush {

    /**
     * 发送消息
     *
     * @param request 消息请求
     * @return 发送结果
     */
    public abstract MessageResponse send(MessageRequest request);

    /**
     * 工厂方法：根据 appType 创建对应的消息推送实现
     * <p>
     * 支持的 appType：
     * <ul>
     *   <li>aliyun / aliyun-sms — 阿里云短信（需自行实现对接）</li>
     *   <li>tencent / tencent-sms — 腾讯云短信（需自行实现对接）</li>
     *   <li>其他 / 空 — 日志模式（只记录不实际发送）</li>
     * </ul>
     * </p>
     *
     * @param appType 短信厂家类型
     * @return MessagePush 实例
     */
    public static MessagePush create(String appType) {
        if (appType == null || appType.isBlank()) {
            log.warn("未指定 appType，使用日志模式（不实际发送短信）");
            return new LogMessagePush();
        }

        String type = appType.toLowerCase().trim();
        return switch (type) {
            case "aliyun", "aliyun-sms", "ali" -> new AliyunSmsMessagePush();
            case "tencent", "tencent-sms", "tencent-cloud" -> new TencentSmsMessagePush();
            default -> {
                log.warn("未支持的 appType=[{}]，使用日志模式（不实际发送短信）", appType);
                yield new LogMessagePush();
            }
        };
    }
}
