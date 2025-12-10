package com.chua.starter.monitor.service.impl;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.entity.service.SysMessage;
import com.chua.starter.monitor.entity.MonitorSysGenAlertPushConfig;
import com.chua.starter.monitor.entity.MonitorSysGenServerAlertHistory;
import com.chua.starter.monitor.service.alert.AlertMessagePushBridge;
import com.chua.starter.service.SysMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 告警消息推送桥接实现
 * 集成系统消息服务，实现告警推送到页面消息
 *
 * @author CH
 * @since 2024/12/25
 */
@Slf4j
@Service
public class AlertMessagePushBridgeImpl implements AlertMessagePushBridge {

    /**
     * 系统消息服务（可选注入）
     */
    @Autowired(required = false)
    private SysMessageService sysMessageService;

    /**
     * 消息业务类型：服务器告警
     */
    private static final String BIZ_TYPE_SERVER_ALERT = "SERVER_ALERT";

    /**
     * 消息类型：系统通知
     */
    private static final String MESSAGE_TYPE_SYSTEM = "SYSTEM";

    @Override
    public boolean push(MonitorSysGenAlertPushConfig config, MonitorSysGenServerAlertHistory alert) {
        String channel = config.getMonitorSysGenAlertPushConfigChannel().getCode();
        log.info("[AlertPush] channel={} endpoint={} title={} server={} type={}",
                channel,
                config.getMonitorSysGenAlertPushConfigEndpoint(),
                alert.getMonitorSysGenServerAlertHistoryTitle(),
                alert.getMonitorSysGenServerAlertHistoryServerId(),
                alert.getMonitorSysGenServerAlertHistoryType());

        // 如果是网页推送通道，调用系统消息服务
        if ("SOCKET".equalsIgnoreCase(channel)) {
            return pushToWebMessage(alert);
        }

        return true;
    }

    @Override
    public boolean pushBatch(MonitorSysGenAlertPushConfig config, List<MonitorSysGenServerAlertHistory> alerts) {
        String channel = config.getMonitorSysGenAlertPushConfigChannel().getCode();
        log.info("[AlertPushBatch] channel={} endpoint={} size={}",
                channel,
                config.getMonitorSysGenAlertPushConfigEndpoint(),
                alerts == null ? 0 : alerts.size());

        // 如果是网页推送通道，批量推送到系统消息
        if ("SOCKET".equalsIgnoreCase(channel) && alerts != null) {
            int successCount = 0;
            for (MonitorSysGenServerAlertHistory alert : alerts) {
                if (pushToWebMessage(alert)) {
                    successCount++;
                }
            }
            log.info("[AlertPushBatch] 网页推送完成: 成功={}, 总数={}", successCount, alerts.size());
            return successCount > 0;
        }

        return true;
    }

    /**
     * 推送告警到系统消息（页面消息）
     *
     * @param alert 告警记录
     * @return 是否推送成功
     */
    private boolean pushToWebMessage(MonitorSysGenServerAlertHistory alert) {
        if (sysMessageService == null) {
            log.debug("SysMessageService 未注入，跳过页面消息推送");
            return false;
        }

        try {
            // 构建消息内容
            String title = alert.getMonitorSysGenServerAlertHistoryTitle();
            String content = buildAlertContent(alert);

            // 创建系统消息
            SysMessage message = new SysMessage();
            message.setSysMessageTitle(title);
            message.setSysMessageContent(content);
            message.setSysMessageType(MESSAGE_TYPE_SYSTEM);
            // 告警级别映射到消息级别
            message.setSysMessageLevel(mapAlertLevelToMessageLevel(alert.getMonitorSysGenServerAlertHistoryLevel().getCode()));
            message.setSysMessageSenderId(0);
            message.setSysMessageSenderName("系统监控");
            // 0表示广播给所有用户
            message.setSysMessageReceiverId(0);
            message.setSysMessageBizType(BIZ_TYPE_SERVER_ALERT);
            message.setSysMessageBizId(String.valueOf(alert.getMonitorSysGenServerAlertHistoryId()));
            message.setSysMessageSendTime(LocalDateTime.now());
            // 未读状态
            message.setSysMessageRead(0);

            // 发送消息
            ReturnResult<SysMessage> result = sysMessageService.sendMessage(message);
            if (result.isOk()) {
                log.debug("告警页面消息推送成功: alertId={}, title={}", 
                    alert.getMonitorSysGenServerAlertHistoryId(), title);
                return true;
            } else {
                log.warn("告警页面消息推送失败: alertId={}, msg={}", 
                    alert.getMonitorSysGenServerAlertHistoryId(), result.getMsg());
                return false;
            }
        } catch (Exception e) {
            log.error("告警页面消息推送异常: alertId={}", 
                alert.getMonitorSysGenServerAlertHistoryId(), e);
            return false;
        }
    }

    /**
     * 构建告警消息内容
     *
     * @param alert 告警记录
     * @return 消息内容
     */
    private String buildAlertContent(MonitorSysGenServerAlertHistory alert) {
        var sb = new StringBuilder();
        sb.append("【服务器告警】").append("\n");
        sb.append("服务器ID: ").append(alert.getMonitorSysGenServerAlertHistoryServerId()).append("\n");
        sb.append("告警类型: ").append(alert.getMonitorSysGenServerAlertHistoryType()).append("\n");
        sb.append("告警级别: ").append(alert.getMonitorSysGenServerAlertHistoryLevel()).append("\n");
        if (alert.getMonitorSysGenServerAlertHistoryContent() != null) {
            sb.append("告警详情: ").append(alert.getMonitorSysGenServerAlertHistoryContent()).append("\n");
        }
        if (alert.getMonitorSysGenServerAlertHistoryCurrentValue() != null) {
            sb.append("当前值: ").append(alert.getMonitorSysGenServerAlertHistoryCurrentValue()).append("\n");
        }
        if (alert.getMonitorSysGenServerAlertHistoryThresholdValue() != null) {
            sb.append("阈值: ").append(alert.getMonitorSysGenServerAlertHistoryThresholdValue()).append("\n");
        }
        sb.append("告警时间: ").append(alert.getMonitorSysGenServerAlertHistoryTriggerTime());
        return sb.toString();
    }

    /**
     * 将告警级别映射到消息级别
     *
     * @param alertLevel 告警级别
     * @return 消息级别
     */
    private String mapAlertLevelToMessageLevel(String alertLevel) {
        if (alertLevel == null) {
            return "INFO";
        }
        return switch (alertLevel.toUpperCase()) {
            case "CRITICAL", "EMERGENCY" -> "ERROR";
            case "HIGH", "WARNING" -> "WARNING";
            default -> "INFO";
        };
    }
}

