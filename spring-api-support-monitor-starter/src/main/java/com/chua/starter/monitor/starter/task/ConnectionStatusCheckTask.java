package com.chua.starter.monitor.starter.task;


import com.chua.starter.monitor.starter.entity.MonitorSysGenServer;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerSetting;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerService;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerSettingService;

import com.chua.starter.monitor.starter.service.ServerLatencyService;
import com.chua.starter.monitor.starter.service.AlertDetectionService;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerAlertHistory;
import com.chua.starter.monitor.starter.websocket.SshWebSocketHandler;
import com.chua.starter.monitor.starter.websocket.VncWebSocketHandler;
import com.chua.starter.monitor.starter.websocket.RdpWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Iterator;


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
    private final AlertDetectionService alertDetectionService;

    @Autowired(required = false)
    private SshWebSocketHandler sshWebSocketHandler;

    @Autowired(required = false)
    private VncWebSocketHandler vncWebSocketHandler;

    @Autowired(required = false)
    private RdpWebSocketHandler rdpWebSocketHandler;

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

            List<MonitorSysGenServer> servers = serverService.list();
            for (MonitorSysGenServer server : servers) {
                try {
                    // 检查服务器是否启用监控
                    ReturnResult<MonitorSysGenServerSetting> settingResult =
                        serverSettingService.getOrCreateSetting(server.getMonitorSysGenServerId());

                    if (settingResult.isOk() && settingResult.getData() != null) {
                        MonitorSysGenServerSetting setting = settingResult.getData();
                        boolean monitorEnabled = setting.getMonitorSysGenServerSettingMonitorEnabled() != null &&
                                               setting.getMonitorSysGenServerSettingMonitorEnabled() == 1;

                        if (monitorEnabled) {
                            // 获取服务器当前延迟
                            Long latency = getServerLatency(server.getMonitorSysGenServerId());
                            boolean isOnline = latency != null && latency >= 0;

                            // 检测连接状态告警
                            ReturnResult<MonitorSysGenServerAlertHistory> alertResult =
                                alertDetectionService.detectConnectionAlert(
                                    server.getMonitorSysGenServerId(), isOnline, latency);

                            if (alertResult.isOk() && alertResult.getData() != null) {
                                log.warn("检测到连接告警: serverId={}, isOnline={}, latency={}ms",
                                    server.getMonitorSysGenServerId(), isOnline, latency);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("检查服务器连接告警失败: serverId={}", server.getMonitorSysGenServerId(), e);
                }
            }

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

    /**
     * 检查WebSocket会话状态并清理无效连接
     * 每30秒执行一次
     */
    @Scheduled(fixedRate = 30000) // 30秒
    public void checkWebSocketSessionStatus() {
        try {
            log.debug("开始检查WebSocket会话状态");

            int cleanedSessions = 0;

            // 检查SSH会话
            if (sshWebSocketHandler != null) {
                cleanedSessions += checkAndCleanSshSessions();
            }

            // 检查VNC会话
            if (vncWebSocketHandler != null) {
                cleanedSessions += checkAndCleanVncSessions();
            }

            // 检查RDP会话
            if (rdpWebSocketHandler != null) {
                cleanedSessions += checkAndCleanRdpSessions();
            }

            if (cleanedSessions > 0) {
                log.info("清理了 {} 个无效的WebSocket会话", cleanedSessions);
            }

        } catch (Exception e) {
            log.error("检查WebSocket会话状态失败", e);
        }
    }

    /**
     * 检查并清理SSH会话
     */
    private int checkAndCleanSshSessions() {
        int cleanedCount = 0;
        try {
            // 获取SSH会话映射
            ConcurrentHashMap<String, WebSocketSession> sessions = getSshSessions();
            ConcurrentHashMap<String, Integer> sessionServerMap = getSshSessionServerMap();
            ConcurrentHashMap<Integer, WebSocketSession> serverSessionMap = getSshServerSessionMap();

            if (sessions != null && sessionServerMap != null && serverSessionMap != null) {
                Iterator<Map.Entry<String, WebSocketSession>> iterator = sessions.entrySet().iterator();

                while (iterator.hasNext()) {
                    Map.Entry<String, WebSocketSession> entry = iterator.next();
                    String sessionId = entry.getKey();
                    WebSocketSession session = entry.getValue();

                    // 检查会话是否已关闭
                    if (session == null || !session.isOpen()) {
                        log.warn("发现已关闭的SSH会话，开始清理: sessionId={}", sessionId);

                        // 清理会话映射
                        iterator.remove();
                        Integer serverId = sessionServerMap.remove(sessionId);

                        if (serverId != null) {
                            serverSessionMap.remove(serverId);

                            // 断开服务器连接
                            try {
                                // 调用SSH连接服务断开连接
                                cleanupSshServerConnection(serverId);
                                log.info("已清理SSH服务器连接: serverId={}", serverId);
                            } catch (Exception e) {
                                log.warn("清理SSH服务器连接失败: serverId={}, error={}", serverId, e.getMessage());
                            }
                        }

                        cleanedCount++;
                    }
                }
            }
        } catch (Exception e) {
            log.error("检查SSH会话失败", e);
        }
        return cleanedCount;
    }

    /**
     * 检查并清理VNC会话
     */
    private int checkAndCleanVncSessions() {
        int cleanedCount = 0;
        try {
            // 获取VNC会话映射
            ConcurrentHashMap<String, WebSocketSession> sessions = getVncSessions();
            ConcurrentHashMap<String, Integer> sessionServerMap = getVncSessionServerMap();
            ConcurrentHashMap<Integer, WebSocketSession> serverSessionMap = getVncServerSessionMap();

            if (sessions != null && sessionServerMap != null && serverSessionMap != null) {
                Iterator<Map.Entry<String, WebSocketSession>> iterator = sessions.entrySet().iterator();

                while (iterator.hasNext()) {
                    Map.Entry<String, WebSocketSession> entry = iterator.next();
                    String sessionId = entry.getKey();
                    WebSocketSession session = entry.getValue();

                    // 检查会话是否已关闭
                    if (session == null || !session.isOpen()) {
                        log.warn("发现已关闭的VNC会话，开始清理: sessionId={}", sessionId);

                        // 清理会话映射
                        iterator.remove();
                        Integer serverId = sessionServerMap.remove(sessionId);

                        if (serverId != null) {
                            serverSessionMap.remove(serverId);

                            // 断开服务器连接和Guacamole隧道
                            try {
                                cleanupVncServerConnection(serverId);
                                log.info("已清理VNC服务器连接: serverId={}", serverId);
                            } catch (Exception e) {
                                log.warn("清理VNC服务器连接失败: serverId={}, error={}", serverId, e.getMessage());
                            }
                        }

                        cleanedCount++;
                    }
                }
            }
        } catch (Exception e) {
            log.error("检查VNC会话失败", e);
        }
        return cleanedCount;
    }

    /**
     * 检查并清理RDP会话
     */
    private int checkAndCleanRdpSessions() {
        int cleanedCount = 0;
        try {
            // 获取RDP会话映射
            ConcurrentHashMap<String, WebSocketSession> sessions = getRdpSessions();
            ConcurrentHashMap<String, Integer> sessionServerMap = getRdpSessionServerMap();
            ConcurrentHashMap<Integer, WebSocketSession> serverSessionMap = getRdpServerSessionMap();

            if (sessions != null && sessionServerMap != null && serverSessionMap != null) {
                Iterator<Map.Entry<String, WebSocketSession>> iterator = sessions.entrySet().iterator();

                while (iterator.hasNext()) {
                    Map.Entry<String, WebSocketSession> entry = iterator.next();
                    String sessionId = entry.getKey();
                    WebSocketSession session = entry.getValue();

                    // 检查会话是否已关闭
                    if (session == null || !session.isOpen()) {
                        log.warn("发现已关闭的RDP会话，开始清理: sessionId={}", sessionId);

                        // 清理会话映射
                        iterator.remove();
                        Integer serverId = sessionServerMap.remove(sessionId);

                        if (serverId != null) {
                            serverSessionMap.remove(serverId);

                            // 断开服务器连接和Guacamole隧道
                            try {
                                cleanupRdpServerConnection(serverId);
                                log.info("已清理RDP服务器连接: serverId={}", serverId);
                            } catch (Exception e) {
                                log.warn("清理RDP服务器连接失败: serverId={}, error={}", serverId, e.getMessage());
                            }
                        }

                        cleanedCount++;
                    }
                }
            }
        } catch (Exception e) {
            log.error("检查RDP会话失败", e);
        }
        return cleanedCount;
    }

    /**
     * 获取SSH会话映射（通过反射访问私有字段）
     */
    @SuppressWarnings("unchecked")
    private ConcurrentHashMap<String, WebSocketSession> getSshSessions() {
        try {
            java.lang.reflect.Field field = SshWebSocketHandler.class.getDeclaredField("sessions");
            field.setAccessible(true);
            return (ConcurrentHashMap<String, WebSocketSession>) field.get(sshWebSocketHandler);
        } catch (Exception e) {
            log.warn("无法获取SSH会话映射: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取SSH会话-服务器映射
     */
    @SuppressWarnings("unchecked")
    private ConcurrentHashMap<String, Integer> getSshSessionServerMap() {
        try {
            java.lang.reflect.Field field = SshWebSocketHandler.class.getDeclaredField("sessionServerMap");
            field.setAccessible(true);
            return (ConcurrentHashMap<String, Integer>) field.get(sshWebSocketHandler);
        } catch (Exception e) {
            log.warn("无法获取SSH会话-服务器映射: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取SSH服务器-会话映射
     */
    @SuppressWarnings("unchecked")
    private ConcurrentHashMap<Integer, WebSocketSession> getSshServerSessionMap() {
        try {
            java.lang.reflect.Field field = SshWebSocketHandler.class.getDeclaredField("serverSessionMap");
            field.setAccessible(true);
            return (ConcurrentHashMap<Integer, WebSocketSession>) field.get(sshWebSocketHandler);
        } catch (Exception e) {
            log.warn("无法获取SSH服务器-会话映射: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取VNC会话映射
     */
    @SuppressWarnings("unchecked")
    private ConcurrentHashMap<String, WebSocketSession> getVncSessions() {
        try {
            java.lang.reflect.Field field = VncWebSocketHandler.class.getDeclaredField("sessions");
            field.setAccessible(true);
            return (ConcurrentHashMap<String, WebSocketSession>) field.get(vncWebSocketHandler);
        } catch (Exception e) {
            log.warn("无法获取VNC会话映射: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取VNC会话-服务器映射
     */
    @SuppressWarnings("unchecked")
    private ConcurrentHashMap<String, Integer> getVncSessionServerMap() {
        try {
            java.lang.reflect.Field field = VncWebSocketHandler.class.getDeclaredField("sessionServerMap");
            field.setAccessible(true);
            return (ConcurrentHashMap<String, Integer>) field.get(vncWebSocketHandler);
        } catch (Exception e) {
            log.warn("无法获取VNC会话-服务器映射: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取VNC服务器-会话映射
     */
    @SuppressWarnings("unchecked")
    private ConcurrentHashMap<Integer, WebSocketSession> getVncServerSessionMap() {
        try {
            java.lang.reflect.Field field = VncWebSocketHandler.class.getDeclaredField("serverSessionMap");
            field.setAccessible(true);
            return (ConcurrentHashMap<Integer, WebSocketSession>) field.get(vncWebSocketHandler);
        } catch (Exception e) {
            log.warn("无法获取VNC服务器-会话映射: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取RDP会话映射
     */
    @SuppressWarnings("unchecked")
    private ConcurrentHashMap<String, WebSocketSession> getRdpSessions() {
        try {
            java.lang.reflect.Field field = RdpWebSocketHandler.class.getDeclaredField("sessions");
            field.setAccessible(true);
            return (ConcurrentHashMap<String, WebSocketSession>) field.get(rdpWebSocketHandler);
        } catch (Exception e) {
            log.warn("无法获取RDP会话映射: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取RDP会话-服务器映射
     */
    @SuppressWarnings("unchecked")
    private ConcurrentHashMap<String, Integer> getRdpSessionServerMap() {
        try {
            java.lang.reflect.Field field = RdpWebSocketHandler.class.getDeclaredField("sessionServerMap");
            field.setAccessible(true);
            return (ConcurrentHashMap<String, Integer>) field.get(rdpWebSocketHandler);
        } catch (Exception e) {
            log.warn("无法获取RDP会话-服务器映射: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取RDP服务器-会话映射
     */
    @SuppressWarnings("unchecked")
    private ConcurrentHashMap<Integer, WebSocketSession> getRdpServerSessionMap() {
        try {
            java.lang.reflect.Field field = RdpWebSocketHandler.class.getDeclaredField("serverSessionMap");
            field.setAccessible(true);
            return (ConcurrentHashMap<Integer, WebSocketSession>) field.get(rdpWebSocketHandler);
        } catch (Exception e) {
            log.warn("无法获取RDP服务器-会话映射: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 清理SSH服务器连接
     */
    private void cleanupSshServerConnection(Integer serverId) {
        try {
            // 通过反射调用SSH连接服务的disconnect方法
            Object sshService = getSshConnectionService();
            if (sshService != null) {
                java.lang.reflect.Method disconnectMethod = sshService.getClass().getMethod("disconnect", Integer.class);
                disconnectMethod.invoke(sshService, serverId);
            }

            // 广播断开连接消息
            MonitorSysGenServer server = serverService.getById(String.valueOf(serverId));
            if (server != null) {
                broadcastSSHDisconnect(server, "会话超时断开");
            }
        } catch (Exception e) {
            log.error("清理SSH服务器连接失败: serverId={}", serverId, e);
        }
    }

    /**
     * 清理VNC服务器连接
     */
    private void cleanupVncServerConnection(Integer serverId) {
        try {
            // 通过反射调用VNC连接服务的disconnect方法
            Object vncService = getVncConnectionService();
            if (vncService != null) {
                java.lang.reflect.Method disconnectMethod = vncService.getClass().getMethod("disconnect", Integer.class);
                disconnectMethod.invoke(vncService, serverId);
            }

            // 清理Guacamole隧道
            cleanupVncTunnel(serverId);

        } catch (Exception e) {
            log.error("清理VNC服务器连接失败: serverId={}", serverId, e);
        }
    }

    /**
     * 清理RDP服务器连接
     */
    private void cleanupRdpServerConnection(Integer serverId) {
        try {
            // 通过反射调用RDP连接服务的disconnect方法
            Object rdpService = getRdpConnectionService();
            if (rdpService != null) {
                java.lang.reflect.Method disconnectMethod = rdpService.getClass().getMethod("disconnect", Integer.class);
                disconnectMethod.invoke(rdpService, serverId);
            }

            // 清理Guacamole隧道
            cleanupRdpTunnel(serverId);

        } catch (Exception e) {
            log.error("清理RDP服务器连接失败: serverId={}", serverId, e);
        }
    }

    /**
     * 获取SSH连接服务
     */
    private Object getSshConnectionService() {
        try {
            java.lang.reflect.Method method = SshWebSocketHandler.class.getDeclaredMethod("getSshConnectionService");
            method.setAccessible(true);
            return method.invoke(sshWebSocketHandler);
        } catch (Exception e) {
            log.warn("无法获取SSH连接服务: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取VNC连接服务
     */
    private Object getVncConnectionService() {
        try {
            java.lang.reflect.Method method = VncWebSocketHandler.class.getDeclaredMethod("getVncConnectionService");
            method.setAccessible(true);
            return method.invoke(vncWebSocketHandler);
        } catch (Exception e) {
            log.warn("无法获取VNC连接服务: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取RDP连接服务
     */
    private Object getRdpConnectionService() {
        try {
            java.lang.reflect.Method method = RdpWebSocketHandler.class.getDeclaredMethod("getRdpConnectionService");
            method.setAccessible(true);
            return method.invoke(rdpWebSocketHandler);
        } catch (Exception e) {
            log.warn("无法获取RDP连接服务: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 清理VNC Guacamole隧道
     */
    private void cleanupVncTunnel(Integer serverId) {
        try {
            java.lang.reflect.Field field = VncWebSocketHandler.class.getDeclaredField("serverTunnelMap");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            ConcurrentHashMap<Integer, Object> tunnelMap = (ConcurrentHashMap<Integer, Object>) field.get(vncWebSocketHandler);

            if (tunnelMap != null) {
                Object tunnel = tunnelMap.remove(serverId);
                if (tunnel != null) {
                    // 调用tunnel的close方法
                    java.lang.reflect.Method closeMethod = tunnel.getClass().getMethod("close");
                    closeMethod.invoke(tunnel);
                }
            }
        } catch (Exception e) {
            log.warn("清理VNC隧道失败: serverId={}, error={}", serverId, e.getMessage());
        }
    }

    /**
     * 清理RDP Guacamole隧道
     */
    private void cleanupRdpTunnel(Integer serverId) {
        try {
            java.lang.reflect.Field field = RdpWebSocketHandler.class.getDeclaredField("serverTunnelMap");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            ConcurrentHashMap<Integer, Object> tunnelMap = (ConcurrentHashMap<Integer, Object>) field.get(rdpWebSocketHandler);

            if (tunnelMap != null) {
                Object tunnel = tunnelMap.remove(serverId);
                if (tunnel != null) {
                    // 调用tunnel的close方法
                    java.lang.reflect.Method closeMethod = tunnel.getClass().getMethod("close");
                    closeMethod.invoke(tunnel);
                }
            }
        } catch (Exception e) {
            log.warn("清理RDP隧道失败: serverId={}, error={}", serverId, e.getMessage());
        }
    }

    /**
     * 广播SSH断开连接消息
     */
    private void broadcastSSHDisconnect(MonitorSysGenServer server, String reason) {
        try {
            java.lang.reflect.Method method = SshWebSocketHandler.class.getDeclaredMethod("broadcastSSHDisconnect",
                MonitorSysGenServer.class, String.class);
            method.setAccessible(true);
            method.invoke(sshWebSocketHandler, server, reason);
        } catch (Exception e) {
            log.warn("广播SSH断开连接消息失败: {}", e.getMessage());
        }
    }


}
