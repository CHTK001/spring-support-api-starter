package com.chua.starter.monitor.task.manager.step;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorSysGenServer;
import com.chua.starter.monitor.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.service.MonitorSysGenServerService;
import com.chua.starter.monitor.service.ServerLatencyService;
import com.chua.starter.monitor.service.UnifiedAlertService;
import com.chua.starter.monitor.task.manager.TaskStep;
import com.chua.starter.monitor.utils.ServerLogUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class HealthCheckStep implements TaskStep {

    private final MonitorSysGenServerService serverService;
    private final ServerLatencyService serverLatencyService;
    private final UnifiedAlertService unifiedAlertService;

    @Override
    public void run(MonitorSysGenServer server, MonitorSysGenServerSetting setting) {
        try {
            ReturnResult<Boolean> conn = serverService.testConnection(String.valueOf(server.getMonitorSysGenServerId()));
            boolean online = conn.isOk() && conn.getData() != null && conn.getData();
            serverService.updateServerOnlineStatus(server.getMonitorSysGenServerId(), online);

            long start = System.nanoTime();
            boolean reachable = isHostReachable(server.getMonitorSysGenServerHost(), 3000);
            long latency = (System.nanoTime() - start) / 1_000_000L;
            if (!reachable) {
                Integer port = server.getMonitorSysGenServerPort();
                if (port != null) {
                    start = System.nanoTime();
                    boolean ok = checkPortStatus(server.getMonitorSysGenServerHost(), port);
                    latency = (System.nanoTime() - start) / 1_000_000L;
                    online = ok;
                }
            }
            serverService.updateServerOnlineStatus(server.getMonitorSysGenServerId(), online);

            Long respThreshold = setting.getMonitorSysGenServerSettingResponseTimeAlertThreshold();
            if (respThreshold == null || respThreshold <= 0) {
                respThreshold = 5000L;
            }
            boolean healthy = online && latency < respThreshold;
            serverService.updateServerHealthStatus(server.getMonitorSysGenServerId(), healthy);

            serverLatencyService.updateServerLatencyToRedis(server.getMonitorSysGenServerId(), latency);
            serverLatencyService.pushLatencyToFrontend(server.getMonitorSysGenServerId(), latency);

            unifiedAlertService.checkAndPushConnectionAlerts(server.getMonitorSysGenServerId(), online, latency, setting);

            ServerLogUtils.logDebug(server.getMonitorSysGenServerId(), String.format("健康检查: online=%s, latency=%dms", online, latency));
        } catch (Exception e) {
            log.error("健康检查失败: serverId={}", server.getMonitorSysGenServerId(), e);
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

