package com.chua.starter.monitor.service.impl;

import com.chua.starter.monitor.entity.MonitorSysGenAlertPushConfig;
import com.chua.starter.monitor.entity.MonitorSysGenServerAlertHistory;
import com.chua.starter.monitor.service.AlertMessagePushBridge;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 默认的消息推送桥接实现
 *
 * 当前仅打印日志，预留与基础包的 messagepush 接口集成点。
 */
@Slf4j
@Service
public class AlertMessagePushBridgeImpl implements AlertMessagePushBridge {
    @Override
    public boolean push(MonitorSysGenAlertPushConfig config, MonitorSysGenServerAlertHistory alert) {
        log.info("[AlertPush] channel={} endpoint={} title={} server={} type={}",
                config.getMonitorSysGenAlertPushConfigChannel(),
                config.getMonitorSysGenAlertPushConfigEndpoint(),
                alert.getMonitorSysGenServerAlertHistoryTitle(),
                alert.getMonitorSysGenServerAlertHistoryServerId(),
                alert.getMonitorSysGenServerAlertHistoryType());
        return true;
    }

    @Override
    public boolean pushBatch(MonitorSysGenAlertPushConfig config, List<MonitorSysGenServerAlertHistory> alerts) {
        log.info("[AlertPushBatch] channel={} endpoint={} size={}",
                config.getMonitorSysGenAlertPushConfigChannel(),
                config.getMonitorSysGenAlertPushConfigEndpoint(),
                alerts == null ? 0 : alerts.size());
        return true;
    }
}

