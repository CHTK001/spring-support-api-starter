package com.chua.starter.monitor.task.manager.step;

import com.chua.starter.monitor.entity.MonitorSysGenServer;
import com.chua.starter.monitor.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.service.MonitorSysGenServerService;
import com.chua.starter.monitor.service.UnifiedAlertService;
import com.chua.starter.monitor.task.manager.TaskStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 在线检查步骤
 * <p>
 * 负责检测服务器的在线状态并更新相关指标
 *
 * @author CH
 * @since 2025/09/22
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OnlineCheckStep implements TaskStep {

    private final MonitorSysGenServerService serverService;
    private final UnifiedAlertService unifiedAlertService;

    /**
     * 执行在线检查任务
     *
     * @param server  服务器信息
     * @param setting 服务器配置
     */
    @Override
    public void run(MonitorSysGenServer server, MonitorSysGenServerSetting setting) {
        Integer monitorSysGenServerSettingOnlineCheckTimeout = setting.getMonitorSysGenServerSettingOnlineCheckTimeout();
        try {
            boolean reachable;
            try {
                reachable = isHostReachable(server.getMonitorSysGenServerHost(), null == monitorSysGenServerSettingOnlineCheckTimeout ? 3000 : monitorSysGenServerSettingOnlineCheckTimeout);
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

    /**
     * 检查主机是否可达
     *
     * @param host      主机地址
     * @param timeoutMs 超时时间(毫秒)
     * @return 主机是否可达
     */
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