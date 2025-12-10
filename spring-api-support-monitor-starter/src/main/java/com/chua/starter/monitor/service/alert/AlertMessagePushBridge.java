package com.chua.starter.monitor.service.alert;

import com.chua.starter.monitor.entity.MonitorSysGenAlertPushConfig;
import com.chua.starter.monitor.entity.MonitorSysGenServerAlertHistory;

import java.util.List;

/**
 * 消息推送桥接接口
 *
 * 用于对接“基础包的messagepush接口”。本项目中提供空实现，后续接入基础包时仅需替换Bean。
 */
public interface AlertMessagePushBridge {

    /**
     * 推送单条告警
     */
    boolean push(MonitorSysGenAlertPushConfig config, MonitorSysGenServerAlertHistory alert);

    /**
     * 批量推送告警
     */
    boolean pushBatch(MonitorSysGenAlertPushConfig config, List<MonitorSysGenServerAlertHistory> alerts);
}

