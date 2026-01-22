package com.chua.starter.monitor.spi;

import com.chua.common.support.core.annotation.SpiDescribe;
import com.chua.starter.monitor.entity.MonitorSysGenMessagePushTemplate;
import com.chua.starter.monitor.entity.MonitorSysGenServerAlertHistory;

import java.util.List;

/**
 * 消息推送 SPI 接口
 * 使用 @Spi("渠道编码") 提供不同通道的实现（如: email/webhook/dingtalk/wechat/sms）。
 */
@SpiDescribe("消息推送SPI")
public interface MessagePushSender {

    /**
     * 通道编码（与 AlertNotificationMethod.code 对应）
     */
    String channel();

    /**
     * 发送单个告警
     */
    boolean send(MonitorSysGenMessagePushTemplate template, MonitorSysGenServerAlertHistory alert);

    /**
     * 批量发送告警
     */
    default boolean sendBatch(MonitorSysGenMessagePushTemplate template, List<MonitorSysGenServerAlertHistory> alerts) {
        boolean ok = true;
        if (alerts == null) return false;
        for (MonitorSysGenServerAlertHistory a : alerts) {
            ok = send(template, a) && ok;
        }
        return ok;
    }
}

