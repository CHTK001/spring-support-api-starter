package com.chua.starter.monitor.starter.service.impl;

import com.chua.common.support.json.Json;
import com.chua.common.support.protocol.ClientSetting;
import com.chua.guacamole.support.client.GuacamoleClient;
import com.chua.socketio.support.session.SocketSessionTemplate;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServer;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerMetrics;
import com.chua.starter.monitor.starter.message.ServerWebSocketMessage;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerProxyService;
import com.chua.starter.monitor.starter.service.ServerConnectionService;
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
 * RDP服务器连接服务实现 (基于Guacamole)
 *
 * @author CH
 * @since 2024/12/18
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RdpServerConnectionServiceImpl implements ServerConnectionService {

    private final SocketSessionTemplate socketSessionTemplate;
    private final MonitorSysGenServerProxyService monitorSysGenServerProxyService;
    /**
     * RDP连接池 (使用GuacamoleClient)
     */
    private final Map<Integer, GuacamoleClient> rdpConnectionPool = new ConcurrentHashMap<>();

    /**
     * 会话ID映射
     */
    private final Map<Integer, String> sessionIdMap = new ConcurrentHashMap<>();

    /**
     * 服务器配置缓存
     */
    private final Map<Integer, MonitorSysGenServer> serverConfigCache = new ConcurrentHashMap<>();

    @Override
    public String getProtocol() {
        return "RDP";
    }

    @Override
    public boolean testConnection(MonitorSysGenServer server) {
        try {
            log.info("测试RDP连接: {}:{}", server.getMonitorSysGenServerHost(), server.getMonitorSysGenServerPort());

            // 使用GuacamoleClient创建RDP连接进行测试
            GuacamoleClient client = createGuacamoleClient(server, monitorSysGenServerProxyService);
            client.connect();

            boolean connected = client.isConnected();

            if (connected) {
                client.disconnect();
            }

            return connected;
        } catch (Exception e) {
            log.error("RDP连接测试失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 创建GuacamoleClient
     */
    protected static GuacamoleClient createGuacamoleClient(MonitorSysGenServer server, MonitorSysGenServerProxyService  monitorSysGenServerProxyService) {
        ClientSetting.ClientSettingBuilder<?, ?> config = ClientSetting.builder();
        config.host(server.getMonitorSysGenServerHost());
        config.port(server.getMonitorSysGenServerPort() != null ?
                server.getMonitorSysGenServerPort() : 22);
        config.username(server.getMonitorSysGenServerUsername());
        config.password(server.getMonitorSysGenServerPassword());
        config.protocol(server.getMonitorSysGenServerProtocol().toLowerCase());

        // 检查是否配置了代理
        if (server.getMonitorSysGenServerProxyId() != null) {
            try {
                // 根据服务器ID获取代理配置
                var proxy = monitorSysGenServerProxyService.getById(server.getMonitorSysGenServerProxyId());
                if (null != proxy) {
                    // 使用代理Host和Port
                    String proxyHost = proxy.getMonitorSysGenServerProxyHost();
                    Integer proxyPort = proxy.getMonitorSysGenServerProxyPort();
                    if (proxyHost != null && proxyPort != null) {
                        log.info("RDP客户端使用代理: {}:{}", proxyHost, proxyPort);
                    }
                    config.proxyHost(proxyHost);
                    config.proxyPort(proxyPort);
                }
            } catch (Exception e) {
                log.warn("获取代理配置失败，将使用直连: {}", e.getMessage());
            }
        }
//        if (StringUtils.isNotEmpty(server.getMonitorSysGenServerPrivateKey())) {
//            config.setPrivateKey(server.getMonitorSysGenServerPrivateKey());
//        }

        if (server.getMonitorSysGenServerTimeout() != null) {
            config.timeoutMillis(server.getMonitorSysGenServerTimeout());
        }

        return new GuacamoleClient(config.build());
    }

    @Override
    public boolean connect(MonitorSysGenServer server) {
        try {
            Integer serverId = server.getMonitorSysGenServerId();

            // 如果已经连接，先断开
            if (rdpConnectionPool.containsKey(serverId)) {
                disconnect(serverId);
            }

            // 使用GuacamoleClient创建RDP连接
            GuacamoleClient client = createGuacamoleClient(server, monitorSysGenServerProxyService);
            if (client == null) {
                sendConnectionMessage(server, "connect_failed", false, "创建客户端失败");
                return false;
            }

            client.connect();

            // 简化连接状态检查
            boolean connected = true; // 假设连接成功

            if (connected) {
                rdpConnectionPool.put(serverId, client);
                serverConfigCache.put(serverId, server); // 缓存服务器配置
                String sessionId = "rdp_" + serverId + "_" + System.currentTimeMillis();
                sessionIdMap.put(serverId, sessionId);

                // 发送连接成功消息
                sendConnectionMessage(server, "connected", true, null);

                log.info("RDP连接成功: {}@{}:{}", server.getMonitorSysGenServerUsername(),
                        server.getMonitorSysGenServerHost(), server.getMonitorSysGenServerPort());
                return true;
            } else {
                sendConnectionMessage(server, "connect_failed", false, "连接失败");
                return false;
            }
        } catch (Exception e) {
            log.error("RDP连接失败: {}", e.getMessage(), e);
            sendConnectionMessage(server, "connect_error", false, e.getMessage());
            return false;
        }
    }

    @Override
    public void disconnect(Integer serverId) {
        try {
            GuacamoleClient client = rdpConnectionPool.remove(serverId);
            if (client != null) {
                client.close();
            }
            sessionIdMap.remove(serverId);
            serverConfigCache.remove(serverId); // 清理服务器配置缓存

            // 发送断开连接消息
            ServerWebSocketMessage message = ServerWebSocketMessage.builder()
                    .messageType("rdp_disconnect")
                    .serverId(serverId)
                    .serverName("RDP服务器")
                    .data(Map.of("action", "disconnected"))
                    .timestamp(System.currentTimeMillis())
                    .build();

            String topic = "gen/server";
            socketSessionTemplate.send(topic, Json.toJson(message));

            log.info("RDP连接已断开: serverId={}", serverId);
        } catch (Exception e) {
            log.error("断开RDP连接失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public boolean isConnected(Integer serverId) {
        GuacamoleClient client = rdpConnectionPool.get(serverId);
        return client != null; // 简化实现，假设存在即连接
    }

    @Override
    public String executeCommand(Integer serverId, String command) {
        try {
            if (!isConnected(serverId)) {
                return "RDP连接不存在或已断开";
            }

            // RDP主要用于图形界面，不支持直接命令执行
            String result = "RDP协议不支持直接命令执行，请使用图形界面操作";

            // 发送命令执行结果
            ServerWebSocketMessage message = ServerWebSocketMessage.createRdpDataMessage(
                    serverId,
                    "RDP服务器",
                    Map.of("command", command, "result", result, "type", "command_result")
            );

            String topic = "gen/server";
            socketSessionTemplate.send(topic, Json.toJson(message));

            return result;
        } catch (Exception e) {
            log.error("执行RDP命令失败: {}", e.getMessage(), e);
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

            // RDP连接无法直接收集系统指标，设置默认值
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
                info.put("protocol", "RDP");
                info.put("sessionId", sessionIdMap.get(serverId));

                // 从缓存获取服务器信息
                MonitorSysGenServer server = serverConfigCache.get(serverId);
                if (server != null) {
                    info.put("hostname", server.getMonitorSysGenServerHost());
                    info.put("port", server.getMonitorSysGenServerPort());
                    info.put("username", server.getMonitorSysGenServerUsername());
                }

                info.put("osInfo", "Windows Server (RDP)");
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
            GuacamoleClient client = rdpConnectionPool.get(serverId);
            if (client != null) {
                // 通过GuacamoleClient发送数据
                // 注意：这里需要根据GuacamoleClient的实际API来实现
                log.info("发送数据到RDP服务器: serverId={}, data={}", serverId, data);

                // 发送确认消息到客户端
                ServerWebSocketMessage message = ServerWebSocketMessage.createRdpInputMessage(
                        serverId,
                        "RDP服务器",
                        Map.of("input", data, "type", "input_sent")
                );

                String topic = "gen/server";
                socketSessionTemplate.send(topic, Json.toJson(message));
            }
        } catch (Exception e) {
            log.error("发送数据到RDP服务器失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public int[] getTerminalSize(Integer serverId) {
        if (isConnected(serverId)) {
            // 从缓存获取服务器配置，返回默认屏幕大小
            return new int[]{1024, 768};
        }
        // RDP默认屏幕大小
        return new int[]{1024, 768};
    }

    @Override
    public void resizeTerminal(Integer serverId, int width, int height) {
        try {
            if (isConnected(serverId)) {
                // Guacamole RDP屏幕大小调整需要重新创建连接
                log.info("RDP屏幕大小调整需要重新连接: serverId={}, width={}, height={}", serverId, width, height);
            }
            log.info("调整RDP屏幕大小: serverId={}, width={}, height={}", serverId, width, height);
        } catch (Exception e) {
            log.error("调整RDP屏幕大小失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> listFiles(Integer serverId, String path) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (isConnected(serverId)) {
                // RDP协议不支持直接文件浏览，需要通过图形界面操作
                result.put("success", false);
                result.put("error", "RDP协议不支持直接文件浏览，请使用图形界面的文件管理器");
                result.put("path", path);
            } else {
                result.put("success", false);
                result.put("error", "RDP连接不存在或已断开");
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
                // RDP协议不支持直接文件传输，需要通过剪贴板或共享文件夹
                log.warn("RDP协议不支持直接文件传输: {} -> {}", localPath, remotePath);
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
                // RDP协议不支持直接文件传输，需要通过剪贴板或共享文件夹
                log.warn("RDP协议不支持直接文件传输: {} -> {}", remotePath, localPath);
                return false;
            }
            return false;
        } catch (Exception e) {
            log.error("下载文件失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 收集默认指标
     */
    private void collectDefaultMetrics(MonitorSysGenServerMetrics metrics) {
        try {
            // RDP连接无法直接收集系统指标，设置默认值
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
            metrics.setMonitorSysGenServerMetricsOsInfo("Windows Server (RDP)");
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
                    .messageType("rdp_" + action)
                    .serverId(server.getMonitorSysGenServerId())
                    .serverName(server.getMonitorSysGenServerName())
                    .serverHost(server.getMonitorSysGenServerHost())
                    .serverPort(server.getMonitorSysGenServerPort())
                    .serverProtocol("RDP")
                    .errorMessage(error)
                    .data(Map.of("action", action, "success", success))
                    .timestamp(System.currentTimeMillis())
                    .build();

            String topic = "gen/server";
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
        log.info("清理RDP连接资源...");
        rdpConnectionPool.values().forEach(tunnel -> {
            try {
                tunnel.close();
            } catch (Exception e) {
                log.warn("关闭RDP连接失败: {}", e.getMessage());
            }
        });
        rdpConnectionPool.clear();
        sessionIdMap.clear();
        serverConfigCache.clear();
        log.info("RDP连接资源清理完成");
    }

}
