package com.chua.starter.monitor.starter.task;


import com.chua.starter.monitor.starter.entity.MonitorSysGenServer;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerSetting;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerService;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerSettingService;

import com.chua.starter.monitor.starter.service.ServerLatencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


/**
 * 连接状态检测定时任务
 * 统一管理所有服务器连接状态检测相关任务
 *
 * @author CH
 * @since 2024/12/26
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConnectionStatusCheckTask {

    private final MonitorSysGenServerService serverService;
    private final MonitorSysGenServerSettingService serverSettingService;
    private final ServerLatencyService serverLatencyService;

    /**
     * 检查服务器连接状态
     * 每5分钟执行一次
     */
    @Scheduled(fixedRate = 300000) // 5分钟
    public void checkServerConnectionStatus() {
        try {
            log.debug("开始检查服务器连接状态");
            // 这里可以根据需要实现具体的连接状态检查逻辑
        } catch (Exception e) {
            log.error("检查服务器连接状态失败", e);
        }
    }

    /**
     * 检测网络延迟
     * 每分钟执行一次
     */
    @Scheduled(fixedRate = 60000) // 1分钟
    public void detectNetworkLatency() {
        try {
            log.debug("开始检测网络延迟");

            List<MonitorSysGenServer> servers = serverService.list();
            for (MonitorSysGenServer server : servers) {
                // 检查服务器是否启用监控
                boolean monitorEnabled = false;
                try {
                    ReturnResult<MonitorSysGenServerSetting> settingResult =
                        serverSettingService.getOrCreateSetting(server.getMonitorSysGenServerId());

                    if (settingResult.isOk() && settingResult.getData() != null) {
                        MonitorSysGenServerSetting setting = settingResult.getData();
                        monitorEnabled = setting.getMonitorSysGenServerSettingMonitorEnabled() != null &&
                                       setting.getMonitorSysGenServerSettingMonitorEnabled() == 1;
                    }
                } catch (Exception e) {
                    log.warn("获取服务器监控配置失败: serverId={}, error={}",
                        server.getMonitorSysGenServerId(), e.getMessage());
                }

                if (monitorEnabled) {
                    CompletableFuture.runAsync(() -> {
                        try {
                            long latency = detectServerLatency(server);

                            // 只有延迟有效时才处理
                            if (latency >= 0) {
                                // 更新到Redis缓存并检查是否有变化
                                var updateResult = serverLatencyService.updateServerLatencyToRedis(
                                    server.getMonitorSysGenServerId(), latency);

                                // 如果有变化，推送到前端
                                if (updateResult.isSuccess() && updateResult.getData()) {
                                    serverLatencyService.pushLatencyToFrontend(
                                        server.getMonitorSysGenServerId(), latency);
                                }
                            }

                        } catch (Exception e) {
                            log.error("检测服务器 {} 延迟失败: {}",
                                    server.getMonitorSysGenServerName(), e.getMessage());
                        }
                    });
                }
            }

        } catch (Exception e) {
            log.error("检测网络延迟失败", e);
        }
    }

    /**
     * 更新服务器在线状态
     * 每30秒执行一次
     */
    @Scheduled(fixedRate = 30000) // 30秒
    public void updateServerOnlineStatus() {
        try {
            log.debug("开始更新服务器在线状态");
            // 这里可以根据需要实现具体的在线状态更新逻辑
        } catch (Exception e) {
            log.error("更新服务器在线状态失败", e);
        }
    }

    /**
     * 检查连接告警
     * 每分钟执行一次
     */
    @Scheduled(fixedRate = 60000) // 1分钟
    public void checkConnectionAlerts() {
        try {
            log.debug("开始检查连接告警");
            // 这里可以根据需要实现具体的连接告警检查逻辑
        } catch (Exception e) {
            log.error("检查连接告警失败", e);
        }
    }

    /**
     * 保存延迟数据到数据库
     * 每5分钟执行一次
     */
    @Scheduled(fixedRate = 300000) // 5分钟
    public void saveLatencyDataToDatabase() {
        try {
            log.debug("开始保存延迟数据到数据库");
            var result = serverLatencyService.saveLatencyToDatabase();
            if (result.isSuccess()) {
                log.info("延迟数据保存完成，保存了 {} 个服务器数据", result.getData());
            } else {
                log.warn("延迟数据保存失败: {}", result.getMsg());
            }
        } catch (Exception e) {
            log.error("保存延迟数据到数据库失败", e);
        }
    }

    /**
     * 检测服务器延迟
     *
     * @param server 服务器配置
     * @return 延迟时间（毫秒），-1表示无法连接
     */
    private long detectServerLatency(MonitorSysGenServer server) {
        try {
            String host = server.getMonitorSysGenServerHost();
            Integer port = server.getMonitorSysGenServerPort();

            if (host == null || port == null) {
                return -1;
            }

            // 根据协议类型选择不同的延迟检测方式
            String protocol = server.getMonitorSysGenServerProtocol();
            if ("SSH".equalsIgnoreCase(protocol)) {
                return detectSshLatency(host, port);
            } else if ("RDP".equalsIgnoreCase(protocol)) {
                return detectTcpLatency(host, port != null ? port : 3389);
            } else if ("VNC".equalsIgnoreCase(protocol)) {
                return detectTcpLatency(host, port != null ? port : 5900);
            } else {
                // 默认使用TCP连接测试
                return detectTcpLatency(host, port);
            }

        } catch (Exception e) {
            log.error("检测服务器延迟异常: {}", e.getMessage());
            return -1;
        }
    }

    /**
     * 检测TCP连接延迟
     *
     * @param host 主机地址
     * @param port 端口
     * @return 延迟时间（毫秒）
     */
    private long detectTcpLatency(String host, int port) {
        long startTime = System.currentTimeMillis();

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 5000); // 5秒超时
            long endTime = System.currentTimeMillis();
            return endTime - startTime;
        } catch (IOException e) {
            log.debug("TCP连接失败: {}:{} - {}", host, port, e.getMessage());
            return -1;
        }
    }

    /**
     * 检测SSH连接延迟
     *
     * @param host 主机地址
     * @param port 端口
     * @return 延迟时间（毫秒）
     */
    private long detectSshLatency(String host, int port) {
        // SSH延迟检测可以使用TCP连接测试，或者使用SSH客户端进行更精确的测试
        return detectTcpLatency(host, port);
    }



    /**
     * 获取服务器当前延迟
     *
     * @param serverId 服务器ID
     * @return 延迟时间（毫秒），null表示未检测
     */
    public Long getServerLatency(Integer serverId) {
        var result = serverLatencyService.getServerLatencyFromRedis(serverId);
        return result.isOk() ? result.getData() : null;
    }

    /**
     * 获取所有服务器延迟统计
     *
     * @return 延迟统计数据
     */
    public Map<String, Object> getLatencyStatistics() {
        var result = serverLatencyService.getLatencyStatistics();
        return result.isOk() ? result.getData() : new HashMap<>();
    }


}
