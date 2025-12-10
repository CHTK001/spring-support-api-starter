package com.chua.starter.monitor.service.alert;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorSysGenAlertPushConfig;
import com.chua.starter.monitor.entity.MonitorSysGenServer;
import com.chua.starter.monitor.entity.MonitorSysGenServerAlertHistory;
import com.chua.starter.monitor.enums.AlertType;

import java.util.List;

/**
 * 告警通知分发器：
 * - 网页端仍使用 Socket.IO 推送（由现有逻辑处理）
 * - 其它通道根据配置通过 AlertMessagePushBridge 推送
 */
public interface AlertNotificationDispatcher {

    ReturnResult<Boolean> dispatch(MonitorSysGenServer server, List<MonitorSysGenServerAlertHistory> alerts);

    ReturnResult<Boolean> dispatchSingle(MonitorSysGenServer server, MonitorSysGenServerAlertHistory alert);

    List<MonitorSysGenAlertPushConfig> loadEnabledConfigs(Integer serverId, AlertType type);
}

