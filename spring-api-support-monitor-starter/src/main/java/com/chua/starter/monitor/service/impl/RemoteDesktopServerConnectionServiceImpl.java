package com.chua.starter.monitor.service.impl;

import com.chua.common.support.network.net.NetUtils;
import com.chua.common.support.text.json.Json;
import com.chua.remote.support.client.ClientBuilder;
import com.chua.remote.support.client.RemoteClient;
// RemoteDesktopConfig 已重构，暂时注释
// import com.chua.remote.support.config.RemoteDesktopConfig.QualityMode;
import com.chua.socket.support.session.SocketSessionTemplate;
import com.chua.starter.monitor.constant.MonitorSocketTopics;
import com.chua.starter.monitor.entity.MonitorSysGenServer;
import com.chua.starter.monitor.entity.MonitorSysGenServerMetrics;
import com.chua.starter.sync.support.message.ServerWebSocketMessage;
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
 * 远程桌面服务器连接服务实现 (基于 utils-support-remote-starter)
 * <p>
 * 使用自定义的 RemoteClient 实现远程桌面连接，支持：
 * - 自动检测服务端模式（桌面/SSH）
 * - 高性能屏幕捕获和传输
 * - 鼠标和键盘控制
 * - 可配置的画质模式
 *
 * @author CH
 * @since 2024/12/28
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RemoteDesktopServerConnectionServiceImpl implements ServerConnectionService {

    /**
     * 默认远程桌面端口
     */
    private static final int DEFAULT_REMOTE_PORT = 21115;

    /**
     * 鼠标动作: 按下
     */
    private static final int MOUSE_ACTION_DOWN = 0;

    /**
     * 鼠标动作: 释放
     */
    private static final int MOUSE_ACTION_UP = 1;

    /**
     * 鼠标动作: 移动
     */
    private static final int MOUSE_ACTION_MOVE = 2;

    private final SocketSessionTemplate socketSessionTemplate;
    private final MonitorSysGenServerSettingService monitorSysGenServerSettingService;

    /**
     * 远程桌面连接池
     */
    private final Map<Integer, RemoteClient> remoteConnectionPool = new ConcurrentHashMap<>();

    /**
     * 会话ID映射
     */
    private final Map<Integer, String> sessionIdMap = new ConcurrentHashMap<>();

    /**
     * 服务器配置缓存
     */
    private final Map<Integer, MonitorSysGenServer> serverConfigCache = new ConcurrentHashMap<>();

    /**
     * 数据回调映射
     */
    private final Map<Integer, DataCallback> dataCallbackMap = new ConcurrentHashMap<>();

    /**
     * 二进制数据回调映射
     */
    private final Map<Integer, BinaryDataCallback> binaryDataCallbackMap = new ConcurrentHashMap<>();

    @Override
    public String getProtocol() {
        return "REMOTE";
    }

    @Override
    public boolean testConnection(MonitorSysGenServer server) {
        try {
            log.info("测试远程桌面连接: {}:{}",
                    server.getMonitorSysGenServerHost(),
                    server.getMonitorSysGenServerPort());

            // 检查端口是否可达
            boolean reachable = NetUtils.isConnectable(
                    server.getMonitorSysGenServerHost(),
                    server.getMonitorSysGenServerPort()
            );

            if (!reachable) {
                log.warn("远程桌面端口不可达: {}:{}",
                        server.getMonitorSysGenServerHost(),
                        server.getMonitorSysGenServerPort());
                return false;
            }

            return true;
        } catch (Exception e) {
            log.error("远程桌面连接测试失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean connect(MonitorSysGenServer server) {
        try {
            Integer serverId = server.getMonitorSysGenServerId();

            log.info("尝试建立远程桌面连接: {}@{}:{}",
                    server.getMonitorSysGenServerUsername(),
                    server.getMonitorSysGenServerHost(),
                    server.getMonitorSysGenServerPort());

            var port = server.getMonitorSysGenServerPort() != null
                    ? server.getMonitorSysGenServerPort()
                    : DEFAULT_REMOTE_PORT;
            var uri = "tcp://" + server.getMonitorSysGenServerHost() + ":" + port + "/";

            var remoteClient = ClientBuilder.create()
                    .server(uri)
                    .size(1920, 1080)
                    .mouseControl(true)
                    .keyboardControl(true)
                    .buildJavaFX();

            // 执行连接
            var connected = remoteClient.connect(uri);
            if (!connected) {
                log.error("远程桌面连接失败: serverId={}, uri={}", serverId, uri);
                sendConnectionMessage(server, "connect_error", false, "无法连接到远程桌面服务");
                return false;
            }

            // 保存连接
            remoteConnectionPool.put(serverId, remoteClient);
            serverConfigCache.put(serverId, server);
            sessionIdMap.put(serverId, "remote-" + serverId + "-" + System.currentTimeMillis());

            log.info("远程桌面连接成功: serverId={}, uri={}", serverId, uri);
            sendConnectionMessage(server, "connected", true, null);
            return true;

        } catch (Exception e) {
            log.error("远程桌面连接失败: {}", e.getMessage(), e);
            sendConnectionMessage(server, "connect_error", false, e.getMessage());
            return false;
        }
    }

    @Override
    public void disconnect(Integer serverId) {
        try {
            RemoteClient client = remoteConnectionPool.remove(serverId);
            if (client != null) {
                client.disconnect();
            }
            sessionIdMap.remove(serverId);
            serverConfigCache.remove(serverId);
            dataCallbackMap.remove(serverId);
            binaryDataCallbackMap.remove(serverId);

            // 发送断开连接消息
            ServerWebSocketMessage message = ServerWebSocketMessage.builder()
                    .messageType("remote_disconnect")
                    .serverId(serverId)
                    .serverName("远程桌面服务器")
                    .data(Map.of("action", "disconnected"))
                    .timestamp(System.currentTimeMillis())
                    .build();

            String topic = MonitorSocketTopics.Remote.Rdp.CONNECT;
            socketSessionTemplate.send(topic, Json.toJson(message));

            log.info("远程桌面连接已断开: serverId={}", serverId);
        } catch (Exception e) {
            log.error("断开远程桌面连接失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public boolean isConnected(Integer serverId) {
        RemoteClient client = remoteConnectionPool.get(serverId);
        return client != null && client.isConnected();
    }

    @Override
    public String executeCommand(Integer serverId, String command) {
        try {
            RemoteClient client = remoteConnectionPool.get(serverId);
            if (client == null || !client.isConnected()) {
                return "远程连接不存在或已断开";
            }

            // RemoteClient 已重构，不再支持直接命令执行
            // 可以通过发送键盘事件来模拟命令输入
            log.warn("RemoteClient 已重构，不再支持直接命令执行: serverId={}, command={}", serverId, command);
            return "远程桌面协议不支持直接命令执行，请使用图形界面操作";
        } catch (Exception e) {
            log.error("执行远程命令失败: {}", e.getMessage(), e);
            return "命令执行失败: " + e.getMessage();
        }
    }

    @Override
    public MonitorSysGenServerMetrics collectMetrics(MonitorSysGenServer server) {
        try {
            Integer serverId = server.getMonitorSysGenServerId();

            if (!isConnected(serverId)) {
                if (!connect(server)) {
                    return createOfflineMetrics(serverId);
                }
            }

            MonitorSysGenServerMetrics metrics = new MonitorSysGenServerMetrics();
            metrics.setMonitorSysGenServerId(serverId);
            metrics.setMonitorSysGenServerMetricsCollectTime(LocalDateTime.now());
            metrics.setMonitorSysGenServerMetricsStatus(1); // 在线

            // 设置默认指标值
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
            RemoteClient client = remoteConnectionPool.get(serverId);
            if (client != null && client.isConnected()) {
                info.put("connected", true);
                info.put("protocol", "REMOTE");
                info.put("mode", "DESKTOP");
                info.put("sessionId", sessionIdMap.get(serverId));

                MonitorSysGenServer server = serverConfigCache.get(serverId);
                if (server != null) {
                    info.put("hostname", server.getMonitorSysGenServerHost());
                    info.put("port", server.getMonitorSysGenServerPort());
                    info.put("username", server.getMonitorSysGenServerUsername());
                }

                info.put("osInfo", "Remote Server");
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
            RemoteClient client = remoteConnectionPool.get(serverId);
            if (client != null && client.isConnected()) {
                // RemoteClient 已重构，不再支持直接终端输入
                // 可以通过发送键盘事件来模拟输入
                log.debug("发送数据到远程服务器: serverId={}, dataLength={} (功能已暂时禁用)", serverId, data.length());
            }
        } catch (Exception e) {
            log.error("发送数据到远程服务器失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public int[] getTerminalSize(Integer serverId) {
        return new int[]{1920, 1080};
    }

    @Override
    public void resizeTerminal(Integer serverId, int width, int height) {
        log.info("调整远程桌面大小: serverId={}, width={}, height={}", serverId, width, height);
    }

    @Override
    public Map<String, Object> listFiles(Integer serverId, String path) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("error", "远程桌面协议不支持直接文件浏览");
        result.put("path", path);
        return result;
    }

    @Override
    public boolean uploadFile(Integer serverId, String localPath, String remotePath) {
        log.warn("远程桌面协议不支持直接文件传输: {} -> {}", localPath, remotePath);
        return false;
    }

    @Override
    public boolean downloadFile(Integer serverId, String remotePath, String localPath) {
        log.warn("远程桌面协议不支持直接文件传输: {} -> {}", remotePath, localPath);
        return false;
    }

    @Override
    public void setDataCallback(Integer serverId, DataCallback callback) {
        dataCallbackMap.put(serverId, callback);
    }

    @Override
    public void setBinaryDataCallback(Integer serverId, BinaryDataCallback callback) {
        binaryDataCallbackMap.put(serverId, callback);
    }

    // ==================== 远程桌面特有操作 ====================

    /**
     * 发送鼠标移动事件
     */
    public void sendMouseMove(Integer serverId, int x, int y) {
        try {
            var client = remoteConnectionPool.get(serverId);
            if (client != null && client.isConnected()) {
                client.sendMouseEvent(x, y, 0, MOUSE_ACTION_MOVE);
            }
        } catch (Exception e) {
            log.error("发送鼠标移动失败: {}", e.getMessage());
        }
    }

    /**
     * 发送鼠标点击事件
     */
    public void sendMouseClick(Integer serverId, int x, int y, int button, boolean pressed) {
        try {
            var client = remoteConnectionPool.get(serverId);
            if (client != null && client.isConnected()) {
                if (pressed) {
                    client.sendMouseEvent(x, y, button, MOUSE_ACTION_DOWN);
                } else {
                    client.sendMouseEvent(x, y, button, MOUSE_ACTION_UP);
                }
            }
        } catch (Exception e) {
            log.error("发送鼠标点击失败: {}", e.getMessage());
        }
    }

    /**
     * 发送鼠标滚轮事件
     */
    public void sendMouseWheel(Integer serverId, int x, int y, int amount) {
        try {
            // 当前 RemoteClient 接口不再提供滚轮事件，暂时仅记录日志
            log.info("发送鼠标滚轮事件(暂不支持具体实现): serverId={}, x={}, y={}, amount={}", serverId, x, y, amount);
        } catch (Exception e) {
            log.error("发送鼠标滚轮失败: {}", e.getMessage());
        }
    }

    /**
     * 发送键盘按下事件
     */
    public void sendKeyPress(Integer serverId, int keyCode, char keyChar, int modifiers) {
        try {
            var client = remoteConnectionPool.get(serverId);
            if (client != null && client.isConnected()) {
                client.sendKeyEvent(keyCode, true);
            }
        } catch (Exception e) {
            log.error("发送键盘按下失败: {}", e.getMessage());
        }
    }

    /**
     * 发送键盘释放事件
     */
    public void sendKeyRelease(Integer serverId, int keyCode, char keyChar, int modifiers) {
        try {
            var client = remoteConnectionPool.get(serverId);
            if (client != null && client.isConnected()) {
                client.sendKeyEvent(keyCode, false);
            }
        } catch (Exception e) {
            log.error("发送键盘释放失败: {}", e.getMessage());
        }
    }

    /**
     * 设置画质模式
     */
    public void setQualityMode(Integer serverId, String mode) {
        try {
            RemoteClient client = remoteConnectionPool.get(serverId);
            if (client != null && client.isConnected()) {
                // RemoteDesktopConfig.QualityMode 已重构，暂时注释
                // QualityMode qualityMode = QualityMode.valueOf(mode.toUpperCase());
                // client.setQualityMode(qualityMode);
                log.info("设置画质模式: serverId={}, mode={} (功能已暂时禁用)", serverId, mode);
            }
        } catch (Exception e) {
            log.error("设置画质模式失败: {}", e.getMessage());
        }
    }

    /**
     * 设置压缩器类型
     */
    public void setCompressor(Integer serverId, String type) {
        try {
            RemoteClient client = remoteConnectionPool.get(serverId);
            if (client != null && client.isConnected()) {
                // RemoteClient 已重构，不再支持直接设置压缩器
                log.info("设置压缩器类型: serverId={}, type={} (功能已暂时禁用)", serverId, type);
            }
        } catch (Exception e) {
            log.error("设置压缩器类型失败: {}", e.getMessage());
        }
    }

    /**
     * 请求重置捕获（强制服务端发送全屏）
     */
    public void requestCaptureReset(Integer serverId) {
        try {
            RemoteClient client = remoteConnectionPool.get(serverId);
            if (client != null && client.isConnected()) {
                // RemoteClient 已重构，不再支持直接重置捕获
                log.info("请求重置捕获: serverId={} (功能已暂时禁用)", serverId);
            }
        } catch (Exception e) {
            log.error("请求重置捕获失败: {}", e.getMessage());
        }
    }

    /**
     * 获取当前连接模式
     */
    public String getConnectionMode(Integer serverId) {
        RemoteClient client = remoteConnectionPool.get(serverId);
        if (client != null && client.isConnected()) {
            return "DESKTOP";
        }
        return "UNKNOWN";
    }

    // ==================== 内部方法 ====================

    /**
     * 图像处理逻辑已废弃
     * <p>
     * 当前 RemoteClient 接口不再提供图像回调，后续如果需要支持远程桌面画面传输，
     * 需要基于新的协议能力重新实现该部分逻辑。
     * </p>
     */
    @SuppressWarnings("unused")
    private void handleScreenImage(Integer serverId, Object image) {
        log.warn("当前版本远程桌面客户端不再提供屏幕图像回调, serverId={}", serverId);
    }

    /**
     * 处理终端输出
     */
    private void handleTerminalOutput(Integer serverId, String output) {
        try {
            // 发送到前端
            ServerWebSocketMessage message = ServerWebSocketMessage.builder()
                    .messageType("remote_terminal")
                    .serverId(serverId)
                    .serverName("远程桌面服务器")
                    .data(Map.of(
                            "type", "terminal",
                            "output", output
                    ))
                    .timestamp(System.currentTimeMillis())
                    .build();

            String topic = MonitorSocketTopics.Remote.Ssh.CONNECT;
            socketSessionTemplate.send(topic, Json.toJson(message));

            // 调用数据回调
            DataCallback callback = dataCallbackMap.get(serverId);
            if (callback != null) {
                callback.onData(output);
            }
        } catch (Exception e) {
            log.error("处理终端输出失败: {}", e.getMessage());
        }
    }

    /**
     * 发送模式检测消息
     */
    private void sendModeDetectedMessage(MonitorSysGenServer server, String mode) {
        try {
            ServerWebSocketMessage message = ServerWebSocketMessage.builder()
                    .messageType("remote_mode_detected")
                    .serverId(server.getMonitorSysGenServerId())
                    .serverName(server.getMonitorSysGenServerName())
                    .serverHost(server.getMonitorSysGenServerHost())
                    .serverPort(server.getMonitorSysGenServerPort())
                    .serverProtocol("REMOTE")
                    .data(Map.of("mode", mode))
                    .timestamp(System.currentTimeMillis())
                    .build();

            String topic = MonitorSocketTopics.Remote.Rdp.CONNECT;
            socketSessionTemplate.send(topic, Json.toJson(message));
        } catch (Exception e) {
            log.error("发送模式检测消息失败: {}", e.getMessage());
        }
    }

    /**
     * 发送连接消息
     */
    private void sendConnectionMessage(MonitorSysGenServer server, String action, boolean success, String error) {
        try {
            ServerWebSocketMessage message = ServerWebSocketMessage.builder()
                    .messageType("remote_" + action)
                    .serverId(server.getMonitorSysGenServerId())
                    .serverName(server.getMonitorSysGenServerName())
                    .serverHost(server.getMonitorSysGenServerHost())
                    .serverPort(server.getMonitorSysGenServerPort())
                    .serverProtocol("REMOTE")
                    .errorMessage(error)
                    .data(Map.of("action", action, "success", success))
                    .timestamp(System.currentTimeMillis())
                    .build();

            String topic = MonitorSocketTopics.Remote.Rdp.CONNECT;
            socketSessionTemplate.send(topic, Json.toJson(message));
        } catch (Exception e) {
            log.error("发送连接消息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 收集默认指标
     */
    private void collectDefaultMetrics(MonitorSysGenServerMetrics metrics) {
        try {
            metrics.setMonitorSysGenServerMetricsCpuUsage(BigDecimal.ZERO);
            metrics.setMonitorSysGenServerMetricsCpuCores(4);
            metrics.setMonitorSysGenServerMetricsCpuLoad1m(BigDecimal.ZERO);
            metrics.setMonitorSysGenServerMetricsCpuLoad5m(BigDecimal.ZERO);
            metrics.setMonitorSysGenServerMetricsCpuLoad15m(BigDecimal.ZERO);

            metrics.setMonitorSysGenServerMetricsMemoryUsage(BigDecimal.ZERO);
            metrics.setMonitorSysGenServerMetricsMemoryTotal(8192L);
            metrics.setMonitorSysGenServerMetricsMemoryUsed(0L);
            metrics.setMonitorSysGenServerMetricsMemoryFree(8192L);

            metrics.setMonitorSysGenServerMetricsDiskUsage(BigDecimal.ZERO);
            metrics.setMonitorSysGenServerMetricsDiskTotal(100L);
            metrics.setMonitorSysGenServerMetricsDiskUsed(0L);
            metrics.setMonitorSysGenServerMetricsDiskFree(100L);

            metrics.setMonitorSysGenServerMetricsUptime(0L);
            metrics.setMonitorSysGenServerMetricsProcessCount(0);
            metrics.setMonitorSysGenServerMetricsOsInfo("Remote Server");
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
        metrics.setMonitorSysGenServerMetricsStatus(0);
        return metrics;
    }

    /**
     * 清理资源
     */
    @PreDestroy
    public void cleanup() {
        log.info("清理远程桌面连接资源...");
        remoteConnectionPool.values().forEach(client -> {
            try {
                client.disconnect();
            } catch (Exception e) {
                log.warn("关闭远程桌面连接失败: {}", e.getMessage());
            }
        });
        remoteConnectionPool.clear();
        sessionIdMap.clear();
        serverConfigCache.clear();
        dataCallbackMap.clear();
        binaryDataCallbackMap.clear();
        log.info("远程桌面连接资源清理完成");
    }
}
