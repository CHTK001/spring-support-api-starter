package com.chua.starter.monitor.service.impl;

import com.chua.common.support.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.net.NetUtils;
import com.chua.guacamole.support.client.GuacamoleClient;
import com.chua.socketio.support.constant.SocketTopics;
import com.chua.socket.support.session.SocketSessionTemplate;
import com.chua.starter.monitor.entity.MonitorSysGenServer;
import com.chua.starter.monitor.entity.MonitorSysGenServerMetrics;
import com.chua.starter.monitor.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.message.ServerWebSocketMessage;
import com.chua.starter.monitor.service.server.MonitorSysGenServerSettingService;
import com.chua.starter.monitor.service.server.ServerConnectionService;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * VNC服务器连接服务实现
 *
 * @author CH
 * @since 2024/12/18
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class VncServerConnectionServiceImpl implements ServerConnectionService {

    private final SocketSessionTemplate socketSessionTemplate;

    /**
     * VNC连接池 (使用GuacamoleClient)
     */
    private final Map<Integer, GuacamoleClient> vncConnectionPool = new ConcurrentHashMap<>();

    /**
     * 会话ID映射
     */
    private final Map<Integer, String> sessionIdMap = new ConcurrentHashMap<>();

    /**
     * 服务器配置缓存
     */
    private final Map<Integer, MonitorSysGenServer> serverConfigCache = new ConcurrentHashMap<>();
    private final MonitorSysGenServerSettingService monitorSysGenServerSettingService;

    @Override
    public String getProtocol() {
        return "VNC";
    }

    @Override
    public boolean testConnection(MonitorSysGenServer server) {
        return connect(server);
    }

    @Override
    public boolean connect(MonitorSysGenServer server) {
        try {
            Integer monitorSysGenServerProxyId = server.getMonitorSysGenServerProxyId();
            if(null == monitorSysGenServerProxyId) {
                return NetUtils.isConnectable(server.getMonitorSysGenServerHost(), server.getMonitorSysGenServerPort());
            }
            ReturnResult<MonitorSysGenServerSetting> serverConfigResult = monitorSysGenServerSettingService.getByServerId(server.getMonitorSysGenServerId());
            if(!serverConfigResult.isOk()) {
                return false;
            }
            MonitorSysGenServerSetting serverConfig = serverConfigResult.getData();
            return NetUtils.isConnectable(serverConfig.getMonitorSysGenServerSettingProxyHost(), serverConfig.getMonitorSysGenServerSettingProxyPort());
        } catch (Exception e) {
            log.error("VNC连接失败: {}", e.getMessage(), e);
            sendConnectionMessage(server, "connect_error", false, e.getMessage());
            return false;
        }
    }


    @Override
    public void disconnect(Integer serverId) {
        try {
            GuacamoleClient client = vncConnectionPool.remove(serverId);
            if (client != null) {
                client.close();
            }
            sessionIdMap.remove(serverId);
            serverConfigCache.remove(serverId); // 清理服务器配置缓存

            // 发送断开连接消息
            ServerWebSocketMessage message = ServerWebSocketMessage.builder()
                    .messageType("vnc_disconnect")
                    .serverId(serverId)
                    .serverName("VNC服务器")
                    .data(Map.of("action", "disconnected"))
                    .timestamp(System.currentTimeMillis())
                    .build();

            String topic = SocketTopics.Monitor.Server.VNC_CONNECT;
            socketSessionTemplate.send(topic, Json.toJson(message));

            log.info("VNC连接已断开: serverId={}", serverId);
        } catch (Exception e) {
            log.error("断开VNC连接失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public boolean isConnected(Integer serverId) {
        GuacamoleClient client = vncConnectionPool.get(serverId);
        return client != null; // 简化实现，假设存在即连接
    }

    @Override
    public String executeCommand(Integer serverId, String command) {
        try {
            if (!isConnected(serverId)) {
                return "VNC连接不存在或已断开";
            }

            // VNC主要用于图形界面，不支持直接命令执行
            String result = "VNC协议不支持直接命令执行，请使用图形界面操作";

            // 发送命令执行结果
            ServerWebSocketMessage message = ServerWebSocketMessage.createVncDataMessage(
                    serverId,
                    "VNC服务器",
                    Map.of("command", command, "result", result, "type", "command_result")
            );

            String topic = SocketTopics.Monitor.Server.VNC_CONNECT;
            socketSessionTemplate.send(topic, Json.toJson(message));

            return result;
        } catch (Exception e) {
            log.error("执行VNC命令失败: {}", e.getMessage(), e);
            return "命令执行失败: " + e.getMessage();
        }
    }

    @Override
    public MonitorSysGenServerMetrics collectMetrics(MonitorSysGenServer server) {
        try {
            Integer serverId = server.getMonitorSysGenServerId();

            if (!isConnected(serverId)) {
                // 尝试重新连接
                if (!connect(server)) {
                    return createOfflineMetrics(serverId);
                }
            }

            MonitorSysGenServerMetrics metrics = new MonitorSysGenServerMetrics();
            metrics.setMonitorSysGenServerId(serverId);
            metrics.setMonitorSysGenServerMetricsCollectTime(LocalDateTime.now());
            metrics.setMonitorSysGenServerMetricsStatus(1); // 在线

            // VNC连接无法直接收集系统指标，设置默认值
            collectDefaultMetrics(metrics);

            return metrics;
        } catch (Exception e) {
            log.error("收集服务器指标失败: {}", e.getMessage(), e);
            return createOfflineMetrics(server.getMonitorSysGenServerId());
        }
    }

    @Override
    public Map<String, Object> getServerInfo(Integer serverId) {
        Map<String, Object> info = new HashMap<>();
        try {
            if (isConnected(serverId)) {
                info.put("connected", true);
                info.put("protocol", "VNC");
                info.put("sessionId", sessionIdMap.get(serverId));

                // 从缓存获取服务器信息
                MonitorSysGenServer server = serverConfigCache.get(serverId);
                if (server != null) {
                    info.put("hostname", server.getMonitorSysGenServerHost());
                    info.put("port", server.getMonitorSysGenServerPort());
                }

                info.put("screenWidth", 1024);
                info.put("screenHeight", 768);
                info.put("osInfo", "VNC Server");
            } else {
                info.put("connected", false);
            }
        } catch (Exception e) {
            log.error("获取服务器信息失败: {}", e.getMessage(), e);
            info.put("connected", false);
            info.put("error", e.getMessage());
        }
        return info;
    }

    @Override
    public String getSessionId(Integer serverId) {
        return sessionIdMap.get(serverId);
    }

    @Override
    public void sendData(Integer serverId, String data) {
        try {
            GuacamoleClient client = vncConnectionPool.get(serverId);
            if (client != null) {
                // 通过GuacamoleClient发送数据
                // 注意：这里需要根据GuacamoleClient的实际API来实现
                log.info("发送数据到VNC服务器: serverId={}, data={}", serverId, data);

                // 发送确认消息到客户端
                ServerWebSocketMessage message = ServerWebSocketMessage.createVncInputMessage(
                        serverId,
                        "VNC服务器",
                        Map.of("input", data, "type", "input_sent")
                );

                String topic = SocketTopics.Monitor.Server.VNC_CONNECT;
                socketSessionTemplate.send(topic, Json.toJson(message));
            }
        } catch (Exception e) {
            log.error("发送数据到VNC服务器失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public int[] getTerminalSize(Integer serverId) {
        if (isConnected(serverId)) {
            // 从缓存获取服务器配置，返回默认屏幕大小
            return new int[]{1024, 768};
        }
        // VNC默认屏幕大小
        return new int[]{1024, 768};
    }

    @Override
    public void resizeTerminal(Integer serverId, int width, int height) {
        try {
            if (isConnected(serverId)) {
                // Guacamole VNC屏幕大小调整需要重新创建连接
                log.info("VNC屏幕大小调整需要重新连接: serverId={}, width={}, height={}", serverId, width, height);
            }
            log.info("调整VNC屏幕大小: serverId={}, width={}, height={}", serverId, width, height);
        } catch (Exception e) {
            log.error("调整VNC屏幕大小失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> listFiles(Integer serverId, String path) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (isConnected(serverId)) {
                // VNC协议不支持直接文件浏览，需要通过图形界面操作
                result.put("success", false);
                result.put("error", "VNC协议不支持直接文件浏览，请使用图形界面的文件管理器");
                result.put("path", path);
            } else {
                result.put("success", false);
                result.put("error", "VNC连接不存在或已断开");
            }
        } catch (Exception e) {
            log.error("获取文件列表失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @Override
    public boolean uploadFile(Integer serverId, String localPath, String remotePath) {
        try {
            if (isConnected(serverId)) {
                // VNC协议不支持直接文件传输，需要通过剪贴板或共享文件夹
                log.warn("VNC协议不支持直接文件传输: {} -> {}", localPath, remotePath);
                return false;
            }
            return false;
        } catch (Exception e) {
            log.error("上传文件失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean downloadFile(Integer serverId, String remotePath, String localPath) {
        try {
            if (isConnected(serverId)) {
                // VNC协议不支持直接文件传输，需要通过剪贴板或共享文件夹
                log.warn("VNC协议不支持直接文件传输: {} -> {}", remotePath, localPath);
                return false;
            }
            return false;
        } catch (Exception e) {
            log.error("下载文件失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public void setDataCallback(Integer serverId, DataCallback callback) {

    }

    /**
     * 收集默认指标
     */
    private void collectDefaultMetrics(MonitorSysGenServerMetrics metrics) {
        try {
            // VNC连接无法直接收集系统指标，设置默认值
            metrics.setMonitorSysGenServerMetricsCpuUsage(BigDecimal.ZERO);
            metrics.setMonitorSysGenServerMetricsCpuCores(4);
            metrics.setMonitorSysGenServerMetricsCpuLoad1m(BigDecimal.ZERO);
            metrics.setMonitorSysGenServerMetricsCpuLoad5m(BigDecimal.ZERO);
            metrics.setMonitorSysGenServerMetricsCpuLoad15m(BigDecimal.ZERO);

            metrics.setMonitorSysGenServerMetricsMemoryUsage(BigDecimal.ZERO);
            metrics.setMonitorSysGenServerMetricsMemoryTotal(8192L); // 8GB默认
            metrics.setMonitorSysGenServerMetricsMemoryUsed(0L);
            metrics.setMonitorSysGenServerMetricsMemoryFree(8192L);

            metrics.setMonitorSysGenServerMetricsDiskUsage(BigDecimal.ZERO);
            metrics.setMonitorSysGenServerMetricsDiskTotal(100L); // 100GB默认
            metrics.setMonitorSysGenServerMetricsDiskUsed(0L);
            metrics.setMonitorSysGenServerMetricsDiskFree(100L);

            metrics.setMonitorSysGenServerMetricsUptime(0L);
            metrics.setMonitorSysGenServerMetricsProcessCount(0);
            metrics.setMonitorSysGenServerMetricsOsInfo("VNC Server");
            metrics.setMonitorSysGenServerMetricsResponseTime(100L);

        } catch (Exception e) {
            log.warn("设置默认指标失败: {}", e.getMessage());
        }
    }

    /**
     * 创建离线指标
     */
    private MonitorSysGenServerMetrics createOfflineMetrics(Integer serverId) {
        MonitorSysGenServerMetrics metrics = new MonitorSysGenServerMetrics();
        metrics.setMonitorSysGenServerId(serverId);
        metrics.setMonitorSysGenServerMetricsCollectTime(LocalDateTime.now());
        metrics.setMonitorSysGenServerMetricsStatus(0); // 离线
        return metrics;
    }

    /**
     * 发送连接消息
     */
    private void sendConnectionMessage(MonitorSysGenServer server, String action, boolean success, String error) {
        try {
            ServerWebSocketMessage message = ServerWebSocketMessage.builder()
                    .messageType("vnc_" + action)
                    .serverId(server.getMonitorSysGenServerId())
                    .serverName(server.getMonitorSysGenServerName())
                    .serverHost(server.getMonitorSysGenServerHost())
                    .serverPort(server.getMonitorSysGenServerPort())
                    .serverProtocol("VNC")
                    .errorMessage(error)
                    .data(Map.of("action", action, "success", success))
                    .timestamp(System.currentTimeMillis())
                    .build();

            String topic = SocketTopics.Monitor.Server.VNC_CONNECT;
            socketSessionTemplate.send(topic, Json.toJson(message));
        } catch (Exception e) {
            log.error("发送连接消息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 清理资源
     */
    @PreDestroy
    public void cleanup() {
        log.info("清理VNC连接资源...");
        vncConnectionPool.values().forEach(tunnel -> {
            try {
                tunnel.close();
            } catch (Exception e) {
                log.warn("关闭VNC连接失败: {}", e.getMessage());
            }
        });
        vncConnectionPool.clear();
        sessionIdMap.clear();
        serverConfigCache.clear();
        log.info("VNC连接资源清理完成");
    }
}
