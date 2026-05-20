package com.chua.starter.monitor.service.impl;

import com.chua.remote.support.server.NativeServer;
import com.chua.remote.support.server.ServerBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 远程桌面服务监控实现
 * <p>
 * 在Spring环境中管理远程桌面服务器生命周期
 * </p>
 *
 * @author CH
 * @since 2026/01/24
 */
@Slf4j
@Service
@ConditionalOnClass(name = "org.springframework.web.bind.annotation.RestController")
public class RemoteDesktopServiceMonitor {

    /**
     * WebSocket端口
     */
    @Value("${monitor.remote-desktop.websocket-port:21117}")
    private int websocketPort;

    /**
     * TCP端口
     */
    @Value("${monitor.remote-desktop.tcp-port:21118}")
    private int tcpPort;

    /**
     * SSH 终端端口
     */
    @Value("${monitor.remote-desktop.ssh-port:21119}")
    private int sshPort;

    /**
     * 服务器地址
     */
    @Value("${monitor.remote-desktop.host:localhost}")
    private String host;

    /**
     * 是否启用验证
     */
    @Value("${monitor.remote-desktop.auth-enabled:false}")
    private boolean authEnabled;

    /**
     * 识别码
     */
    @Value("${monitor.remote-desktop.identifier:}")
    private String identifier;

    /**
     * 验证码
     */
    @Value("${monitor.remote-desktop.verification-code:}")
    private String verificationCode;

    /**
     * 服务句柄映射（serverId -> protocol -> handle）
     */
    private final Map<Integer, Map<String, Long>> serverHandleMap = new ConcurrentHashMap<>();

    /**
     * 启动配置（fluent API）
     */
    @Data
    @Builder
    @AllArgsConstructor
    public static class StartConfig {
        private Integer tcpPort;
        private Integer sshPort;
        private boolean multiProtocol;
        private Integer websocketPort;

        public static StartConfig defaults() {
            return new StartConfig(null, null, false, null);
        }

        /** 设置 TCP 桌面端口 */
        public StartConfig withTcp(int port) { this.tcpPort = port; return this; }
        /** 设置 SSH 终端端口 */
        public StartConfig withSsh(int port) { this.sshPort = port; return this; }
        /** 开启多协议模式（同时启动 TCP 桌面 + SSH 终端） */
        public StartConfig withMutiProtocol() { this.multiProtocol = true; return this; }
        /** 设置 WebSocket 端口 */
        public StartConfig withWebsocket(int port) { this.websocketPort = port; return this; }
    }

    /**
     * 初始化远程桌面服务
     */
    @PostConstruct
    public void init() {
        try {
            if (!NativeServer.isAvailable()) {
                log.warn("[远程桌面][Monitor] 远程桌面原生库不可用");
            } else {
                log.info("[远程桌面][Monitor] 远程桌面服务初始化成功");
            }
        } catch (Exception e) {
            log.warn("[远程桌面][Monitor] 远程桌面服务初始化失败: {}", e.getMessage());
        }
    }

    /**
     * 销毁远程桌面服务
     */
    @PreDestroy
    public void destroy() {
        serverHandleMap.forEach((serverId, protocolHandles) -> {
            protocolHandles.forEach((protocol, handle) -> {
                try {
                    stopServer(serverId, protocol);
                } catch (Exception e) {
                    log.error("[远程桌面][Monitor] 停止服务失败: serverId={}, protocol={}", serverId, protocol, e);
                }
            });
        });
        serverHandleMap.clear();
        log.info("[远程桌面][Monitor] 远程桌面服务已停止");
    }

    /**
     * 启动远程桌面服务（默认配置）
     *
     * @param serverId 服务器ID
     * @param websocketPort WebSocket端口（可选，为null时使用默认值）
     * @param tcpPort TCP端口（可选，为null时使用默认值）
     * @return 服务句柄映射
     */
    public Map<String, Long> startService(Integer serverId, Integer websocketPort, Integer tcpPort) {
        return startService(serverId, websocketPort, tcpPort, null);
    }

    /**
     * 启动远程桌面服务（含多协议配置）
     *
     * @param serverId 服务器ID
     * @param websocketPort WebSocket端口（可选，为null时使用默认值）
     * @param tcpPort TCP端口（可选，为null时使用默认值）
     * @param config 多协议配置（可选，为null时使用单协议模式）
     * @return 服务句柄映射
     */
    public Map<String, Long> startService(Integer serverId, Integer websocketPort, Integer tcpPort, StartConfig config) {
        if (!NativeServer.isAvailable()) {
            log.error("[远程桌面][Monitor] 远程桌面原生库不可用");
            return Map.of();
        }

        try {
            var builder = ServerBuilder.create();

            var wsPort = websocketPort != null ? websocketPort : this.websocketPort;
            var tcp = tcpPort != null ? tcpPort : this.tcpPort;

            builder.websocket(wsPort);
            if (tcp > 0) {
                builder.tcp(tcp);
            }

            if (authEnabled && identifier != null && !identifier.isEmpty()
                    && verificationCode != null && !verificationCode.isEmpty()) {
                builder.auth(identifier, verificationCode);
            }

            // 多协议配置
            if (config != null && config.isMultiProtocol()) {
                int ssh = config.getSshPort() != null ? config.getSshPort() : this.sshPort;
                builder.tcp(config.getTcpPort() != null ? config.getTcpPort() : tcp);
                log.info("[远程桌面][Monitor] 多协议模式启用: tcp_port={}, ssh_port={}", tcp, ssh);
                // ServerBuilder 原生支持通过这些方法配置
            }

            var handles = builder.start();

            if (!handles.isEmpty()) {
                serverHandleMap.put(serverId, handles);
                log.info("[远程桌面][Monitor] 远程桌面服务启动成功: serverId={}, handles={}", serverId, handles);
            } else {
                log.error("[远程桌面][Monitor] 远程桌面服务启动失败: serverId={}", serverId);
            }

            return handles;
        } catch (Exception e) {
            log.error("[远程桌面][Monitor] 远程桌面服务启动异常: serverId={}", serverId, e);
            return Map.of();
        }
    }

    /**
     * 停止远程桌面服务
     *
     * @param serverId 服务器ID
     * @param protocol 协议名称（tcp/websocket/wss/vnc），为null时停止所有协议
     */
    public void stopService(Integer serverId, String protocol) {
        var protocolHandles = serverHandleMap.get(serverId);
        if (protocolHandles == null || protocolHandles.isEmpty()) {
            return;
        }

        if (protocol == null || protocol.isEmpty()) {
            // 停止所有协议
            protocolHandles.forEach((proto, handle) -> stopServer(serverId, proto));
            serverHandleMap.remove(serverId);
        } else {
            // 停止指定协议
            stopServer(serverId, protocol);
            protocolHandles.remove(protocol);
            if (protocolHandles.isEmpty()) {
                serverHandleMap.remove(serverId);
            }
        }
    }

    /**
     * 停止服务器
     *
     * @param serverId 服务器ID
     * @param protocol 协议名称
     */
    private void stopServer(Integer serverId, String protocol) {
        var protocolHandles = serverHandleMap.get(serverId);
        if (protocolHandles == null) {
            return;
        }

        var handle = protocolHandles.get(protocol);
        if (handle == null || handle == 0) {
            return;
        }

        var success = switch (protocol) {
            case "tcp" -> NativeServer.stopServer(handle);
            case "websocket" -> NativeServer.stopServer(handle);
            case "wss" -> NativeServer.stopServer(handle);
            case "vnc" -> NativeServer.stopVncServer(handle);
            default -> false;
        };

        if (success) {
            log.info("[远程桌面][Monitor] 远程桌面服务已停止: serverId={}, protocol={}, handle={}", serverId, protocol, handle);
        } else {
            log.error("[远程桌面][Monitor] 远程桌面服务停止失败: serverId={}, protocol={}, handle={}", serverId, protocol, handle);
        }
    }

    /**
     * 检查服务是否运行中
     *
     * @param serverId 服务器ID
     * @param protocol 协议名称（可选）
     * @return true表示运行中
     */
    public boolean isServiceRunning(Integer serverId, String protocol) {
        var protocolHandles = serverHandleMap.get(serverId);
        if (protocolHandles == null || protocolHandles.isEmpty()) {
            return false;
        }

        if (protocol == null || protocol.isEmpty()) {
            return !protocolHandles.isEmpty();
        }

        return protocolHandles.containsKey(protocol) && protocolHandles.get(protocol) > 0;
    }

    /**
     * 获取WebSocket连接URL
     *
     * @param serverId 服务器ID
     * @param host     服务器地址
     * @param port     服务器端口
     * @return WebSocket URL
     */
    public String getWebSocketUrl(Integer serverId, String host, int port) {
        var protocol = "ws";
        if (host.startsWith("https://")) {
            protocol = "wss";
            host = host.replace("https://", "");
        } else if (host.startsWith("http://")) {
            host = host.replace("http://", "");
        }
        return String.format("%s://%s:%d", protocol, host, port);
    }

    /**
     * 获取默认WebSocket端口
     *
     * @return 默认端口
     */
    public int getDefaultWebSocketPort() {
        return websocketPort;
    }

    /**
     * 获取默认TCP端口
     *
     * @return 默认端口
     */
    public int getDefaultTcpPort() {
        return tcpPort;
    }

    /**
     * 获取默认 SSH 端口
     *
     * @return 默认端口
     */
    public int getDefaultSshPort() {
        return sshPort;
    }

    /**
     * 获取默认主机
     *
     * @return 默认主机
     */
    public String getDefaultHost() {
        return host;
    }
}

