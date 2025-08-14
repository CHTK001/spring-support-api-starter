package com.chua.starter.monitor.task.manager.step;

import com.chua.starter.monitor.entity.MonitorSysGenServer;
import com.chua.starter.monitor.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.service.ServerMetricsWebSocketService;
import com.chua.starter.monitor.task.manager.TaskStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketCleanupStep implements TaskStep {

    private final ServerMetricsWebSocketService serverMetricsWebSocketService;

    @Override
    public void run(MonitorSysGenServer server, MonitorSysGenServerSetting setting) {
        try {
            serverMetricsWebSocketService.pushConnectionStatistics();
        } catch (Exception e) {
            log.error("WebSocket清理失败: serverId={}", server.getMonitorSysGenServerId(), e);
        }
    }
}

