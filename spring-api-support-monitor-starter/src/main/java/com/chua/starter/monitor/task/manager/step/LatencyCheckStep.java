package com.chua.starter.monitor.task.manager.step;

import com.chua.starter.monitor.entity.MonitorSysGenServer;
import com.chua.starter.monitor.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.service.server.MonitorSysGenServerService;
import com.chua.starter.monitor.service.server.ServerLatencyService;
import com.chua.starter.monitor.service.alert.UnifiedAlertService;
import com.chua.starter.monitor.task.manager.TaskStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 延迟检测步骤
 * <p>
 * 负责检测服务器的网络延迟并更新相关指标
 *
 * @author CH
 * @since 2025/09/22
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LatencyCheckStep implements TaskStep {

    private final MonitorSysGenServerService serverService;
    private final ServerLatencyService serverLatencyService;
    private final UnifiedAlertService unifiedAlertService;

    /**
     * 执行延迟检测任务
     *
     * @param server  服务器信息
     * @param setting 服务器配置
     */
    @Override
    public void run(MonitorSysGenServer server, MonitorSysGenServerSetting setting) {
        try {
            long start = System.nanoTime();
            boolean reachable = false;
            try {
                reachable = java.net.InetAddress.getByName(server.getMonitorSysGenServerHost()).isReachable(3000);
            } catch (Throwable ignore) {}
            long latency = (System.nanoTime() - start) / 1_000_000L;

            if (!reachable) {
                Integer port = server.getMonitorSysGenServerPort();
                if (port != null) {
                    start = System.nanoTime();
                    boolean ok = checkPortStatus(server.getMonitorSysGenServerHost(), port);
                    latency = (System.nanoTime() - start) / 1_000_000L;
                    serverService.updateServerOnlineStatus(server.getMonitorSysGenServerId(), ok);
                }
            }

            serverLatencyService.updateServerLatencyToRedis(server.getMonitorSysGenServerId(), latency);
            serverLatencyService.pushLatencyToFrontend(server.getMonitorSysGenServerId(), latency);

            Long threshold = setting.getMonitorSysGenServerSettingResponseTimeAlertThreshold();
            if (threshold != null && threshold > 0 && latency >= threshold) {
                unifiedAlertService.checkAndPushConnectionAlerts(server.getMonitorSysGenServerId(), null, latency, setting);
            }
        } catch (Exception e) {
            log.error("延迟检测失败: serverId={}", server.getMonitorSysGenServerId(), e);
        }
    }

    /**
     * 检查端口状态
     *
     * @param host 主机地址
     * @param port 端口号
     * @return 端口是否开放
     */
    private boolean checkPortStatus(String host, int port) {
        try (java.net.Socket socket = new java.net.Socket()) {
            socket.connect(new java.net.InetSocketAddress(host, port), 3000);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}