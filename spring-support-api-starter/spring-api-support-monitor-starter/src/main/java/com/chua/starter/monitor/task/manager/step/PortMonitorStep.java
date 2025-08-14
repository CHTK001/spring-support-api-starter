package com.chua.starter.monitor.task.manager.step;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorSysGenServer;
import com.chua.starter.monitor.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.service.ServerMetricsWebSocketService;
import com.chua.starter.monitor.task.manager.TaskStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 端口监控步骤
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PortMonitorStep implements TaskStep {

    private final ServerMetricsWebSocketService serverMetricsWebSocketService;

    @Override
    public void run(MonitorSysGenServer server, MonitorSysGenServerSetting setting) {
        try {
            String portList = setting.getMonitorSysGenServerSettingMonitorPorts();
            if (portList == null || portList.trim().isEmpty()) {
                return;
            }
            String[] ports = portList.split(",");
            Map<String, Object> portStatuses = new HashMap<>();
            for (String portStr : ports) {
                try {
                    int port = Integer.parseInt(portStr.trim());
                    boolean status = checkPortStatus(server.getMonitorSysGenServerHost(), port);
                    portStatuses.put(String.valueOf(port), status);
                } catch (NumberFormatException e) {
                    log.warn("无效的端口号: {}", portStr);
                }
            }
            Map<String, Object> message = new HashMap<>();
            message.put("type", "port_status");
            message.put("serverId", server.getMonitorSysGenServerId());
            message.put("data", portStatuses);
            message.put("timestamp", System.currentTimeMillis());
            try {
                serverMetricsWebSocketService.pushSingleServerMetrics(server, null);
            } catch (Exception e) {
                log.warn("推送端口状态信息失败: serverId={}", server.getMonitorSysGenServerId(), e);
            }
        } catch (Exception e) {
            log.error("端口监控失败: serverId={}", server.getMonitorSysGenServerId(), e);
        }
    }

    private boolean checkPortStatus(String host, int port) {
        try (java.net.Socket socket = new java.net.Socket()) {
            socket.connect(new java.net.InetSocketAddress(host, port), 3000);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

