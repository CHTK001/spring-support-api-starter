package com.chua.starter.monitor.task.manager.step;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorSysGenServer;
import com.chua.starter.monitor.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.service.MonitorSysGenServerService;
import com.chua.starter.monitor.service.UnifiedAlertService;
import com.chua.starter.monitor.task.manager.TaskStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OnlineCheckStep implements TaskStep {

    private final MonitorSysGenServerService serverService;
    private final UnifiedAlertService unifiedAlertService;

    @Override
    public void run(MonitorSysGenServer server, MonitorSysGenServerSetting setting) {
        try {
            boolean reachable;
            try {
                reachable = isHostReachable(server.getMonitorSysGenServerHost(), 3000);
            } catch (Throwable ignore) {
                reachable = false;
            }
            boolean online = reachable;
            if (!reachable) {
                Integer port = server.getMonitorSysGenServerPort();
                if (port != null) {
                    online = checkPortStatus(server.getMonitorSysGenServerHost(), port);
                }
            }
            serverService.updateServerOnlineStatus(server.getMonitorSysGenServerId(), online);
            unifiedAlertService.checkAndPushConnectionAlerts(server.getMonitorSysGenServerId(), online, null, setting);
        } catch (Exception e) {
            log.error("在线状态检测失败: serverId={}", server.getMonitorSysGenServerId(), e);
        }
    }

    private boolean isHostReachable(String host, int timeoutMs) {
        try {
            if (host == null || host.isEmpty()) {
                return false;
            }
            return java.net.InetAddress.getByName(host).isReachable(timeoutMs);
        } catch (Throwable e) {
            return false;
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

