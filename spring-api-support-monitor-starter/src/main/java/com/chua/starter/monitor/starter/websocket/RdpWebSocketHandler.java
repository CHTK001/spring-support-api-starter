package com.chua.starter.monitor.starter.websocket;

import com.chua.common.support.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.IoUtils;
import com.chua.socketio.support.session.SocketSessionTemplate;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServer;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerService;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.guacamole.GuacamoleConnectionClosedException;
import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.io.GuacamoleReader;
import org.apache.guacamole.io.GuacamoleWriter;
import org.apache.guacamole.net.GuacamoleTunnel;
import org.apache.guacamole.net.InetGuacamoleSocket;
import org.apache.guacamole.net.SimpleGuacamoleTunnel;
import org.apache.guacamole.protocol.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * RDP WebSocket处理器
 *
 * @author CH
 * @since 2024/12/18
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RdpWebSocketHandler extends BinaryWebSocketHandler {
    @Autowired
    private MonitorSysGenServerService serverService;

    @Autowired
    private MonitorSysGenServerSettingService serverSettingService;


    @Autowired
    private SocketSessionTemplate socketSessionTemplate;
    /**
     * 会话到隧道的映射
     */
    private final Map<String, GuacamoleTunnel> sessionTunnelMap = new ConcurrentHashMap<>();

    /**
     * 会话到服务器ID的映射
     */
    private final Map<String, String> sessionServerMap = new ConcurrentHashMap<>();

    /**
     * 线程池用于处理隧道数据
     */
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private static final String PING_OPCODE = "ping";


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        URI uri = session.getUri();
        log.info("Guacamole WebSocket连接建立: sessionId={}, uri={}", sessionId, uri);

        try {
            // 从URL参数获取服务器配置
            String serverId = getServerIdFromSession(session);
            if (serverId == null) {
                log.error("无法获取服务器ID: sessionId={}, uri={}", sessionId, uri);
                session.close(CloseStatus.BAD_DATA.withReason("Missing serverId"));
                return;
            }

            log.info("获取到服务器ID: serverId={}", serverId);

            // 获取服务器信息
            MonitorSysGenServer server = serverService.getById(serverId);
            if (server == null) {
                log.error("服务器不存在: serverId={}", serverId);
                session.close(CloseStatus.BAD_DATA.withReason("Server not found"));
                return;
            }

            log.info("获取到服务器信息: serverId={}, host={}, port={}, protocol={}",
                    serverId, server.getMonitorSysGenServerHost(),
                    server.getMonitorSysGenServerPort(), server.getMonitorSysGenServerProtocol());

            // 创建Guacamole隧道
            GuacamoleTunnel tunnel = createGuacamoleTunnel(server, session);
            if (tunnel == null) {
                log.error("创建Guacamole隧道失败: serverId={}", serverId);
                session.close(CloseStatus.SERVER_ERROR.withReason("Failed to create tunnel"));
                return;
            }

            // 保存映射关系
            sessionTunnelMap.put(sessionId, tunnel);
            sessionServerMap.put(sessionId, serverId);

            // 启动隧道数据读取线程
            startTunnelReader(session, tunnel);

            log.info("Guacamole隧道创建成功: serverId={}, sessionId={}", serverId, sessionId);

        } catch (Exception e) {
            log.error("建立Guacamole连接失败: sessionId={}, error={}", sessionId, e.getMessage(), e);
            session.close(CloseStatus.SERVER_ERROR.withReason("Connection failed"));
        }
    }

    /**
     * 创建Guacamole隧道
     */
    private GuacamoleTunnel createGuacamoleTunnel(MonitorSysGenServer server, WebSocketSession session) {
        try {
            log.info("开始创建Guacamole隧道: serverId={}", server.getMonitorSysGenServerId());

            // 创建Guacamole配置，如果不存在则创建默认配置
            ReturnResult<MonitorSysGenServerSetting> byServerId = serverSettingService.getOrCreateSetting(server.getMonitorSysGenServerId());
            if (!byServerId.isOk()) {
                log.error("获取服务器设置失败: serverId={}, result={}",
                        server.getMonitorSysGenServerId(), byServerId);
                try {
                    afterConnectionClosed(session, CloseStatus.SERVER_ERROR);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return null;
            }

            MonitorSysGenServerSetting sysGenServerSetting = byServerId.getData();
            log.info("获取到服务器设置: proxyHost={}, proxyPort={}",
                    sysGenServerSetting.getMonitorSysGenServerSettingProxyHost(),
                    sysGenServerSetting.getMonitorSysGenServerSettingProxyPort());

            GuacamoleConfiguration config = createGuacamoleConfiguration(server, session, sysGenServerSetting);
            log.info("创建Guacamole配置完成: protocol={}, hostname={}, port={}",
                    config.getProtocol(), config.getParameter("hostname"), config.getParameter("port"));

            // 检查代理服务是否可用
            String proxyHost = sysGenServerSetting.getMonitorSysGenServerSettingProxyHost();
            Integer proxyPort = sysGenServerSetting.getMonitorSysGenServerSettingProxyPort();

            log.info("尝试连接到Guacamole代理: {}:{}", proxyHost, proxyPort);

            // 创建Guacamole Socket
            InetGuacamoleSocket socket;
            try {
                socket = new InetGuacamoleSocket(proxyHost, proxyPort);
                log.info("创建InetGuacamoleSocket完成");
            } catch (Exception e) {
                log.error("无法连接到Guacamole代理 {}:{}, 错误: {}", proxyHost, proxyPort, e.getMessage());
                // 如果无法连接到guacd，返回一个提示性的错误
                throw new GuacamoleException("无法连接到Guacamole代理服务。请确保guacd服务正在运行在 " + proxyHost + ":" + proxyPort);
            }

            ConfiguredGuacamoleSocket configuredSocket = new ConfiguredGuacamoleSocket(socket, config);
            log.info("创建ConfiguredGuacamoleSocket完成");

            // 创建隧道
            SimpleGuacamoleTunnel tunnel = new SimpleGuacamoleTunnel(configuredSocket);
            log.info("创建SimpleGuacamoleTunnel完成");

            log.info("Guacamole隧道创建成功: serverId={}, protocol={}",
                    server.getMonitorSysGenServerId(), server.getMonitorSysGenServerProtocol());

            return tunnel;

        } catch (GuacamoleException e) {
            log.error("创建Guacamole隧道失败: serverId={}, error={}",
                    server.getMonitorSysGenServerId(), e.getMessage(), e);
            return null;
        } catch (Exception e) {
            log.error("创建Guacamole隧道异常: serverId={}, error={}",
                    server.getMonitorSysGenServerId(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * 创建Guacamole配置
     */
    private GuacamoleConfiguration createGuacamoleConfiguration(MonitorSysGenServer server, WebSocketSession session, MonitorSysGenServerSetting sysGenServerSetting) {
        GuacamoleConfiguration config = new GuacamoleConfiguration();

        String protocol = server.getMonitorSysGenServerProtocol().toLowerCase();
        config.setProtocol(protocol);

        // 基本连接参数
        config.setParameter("hostname", server.getMonitorSysGenServerHost());
        config.setParameter("port", String.valueOf(server.getMonitorSysGenServerPort()));

        if (server.getMonitorSysGenServerUsername() != null) {
            config.setParameter("username", server.getMonitorSysGenServerUsername());
        }

        if (server.getMonitorSysGenServerPassword() != null) {
            config.setParameter("password", server.getMonitorSysGenServerPassword());
        }

        // 协议特定参数
        if ("rdp".equals(protocol)) {
            configureRdpParameters(config, server, session);
        } else if ("vnc".equals(protocol)) {
            configureVncParameters(config, server, session);
        } else if ("ssh".equals(protocol)) {
            configureSshParameters(config, server, session);
        }

        return config;
    }

    /**
     * 配置RDP参数
     */
    private void configureRdpParameters(GuacamoleConfiguration config, MonitorSysGenServer server, WebSocketSession session) {
        // 从URL参数或服务器设置获取配置
        Map<String, String> queryParams = getQueryParameters(session);

        // 基本连接参数
        config.setParameter("security", queryParams.getOrDefault("security", "any"));
        config.setParameter("ignore-cert", queryParams.getOrDefault("ignore-cert", "true"));
        config.setParameter("disable-auth", queryParams.getOrDefault("disable-auth", "false"));
        config.setParameter("server-layout", queryParams.getOrDefault("server-layout", "en-us-qwerty"));

        // 分辨率和显示设置
        String width = queryParams.getOrDefault("width", "1920");
        String height = queryParams.getOrDefault("height", "1080");
        config.setParameter("width", width);
        config.setParameter("height", height);
        config.setParameter("dpi", queryParams.getOrDefault("dpi", "96"));
        config.setParameter("color-depth", queryParams.getOrDefault("color-depth", "32"));
        config.setParameter("resize-method", queryParams.getOrDefault("resize-method", "reconnect"));

        // 性能优化参数
        config.setParameter("enable-wallpaper", queryParams.getOrDefault("enable-wallpaper", "false"));
        config.setParameter("enable-theming", queryParams.getOrDefault("enable-theming", "false"));
        config.setParameter("enable-font-smoothing", queryParams.getOrDefault("enable-font-smoothing", "false"));
        config.setParameter("enable-full-window-drag", queryParams.getOrDefault("enable-full-window-drag", "false"));
        config.setParameter("enable-desktop-composition", queryParams.getOrDefault("enable-desktop-composition", "false"));
        config.setParameter("enable-menu-animations", queryParams.getOrDefault("enable-menu-animations", "false"));

        // 音频设置
        config.setParameter("enable-audio", queryParams.getOrDefault("enable-audio", "true"));
        config.setParameter("audio-servername", queryParams.getOrDefault("audio-servername", ""));

        // 打印设置
        config.setParameter("enable-printing", queryParams.getOrDefault("enable-printing", "false"));
        config.setParameter("printer-name", queryParams.getOrDefault("printer-name", "Guacamole Printer"));

        // 驱动器映射设置
        config.setParameter("enable-drive", queryParams.getOrDefault("enable-drive", "false"));
        config.setParameter("drive-name", queryParams.getOrDefault("drive-name", "Guacamole Disk"));
        config.setParameter("drive-path", queryParams.getOrDefault("drive-path", "/tmp/guacamole-drive"));
        config.setParameter("create-drive-path", queryParams.getOrDefault("create-drive-path", "true"));

        // 剪贴板设置
        config.setParameter("disable-copy", queryParams.getOrDefault("disable-copy", "false"));
        config.setParameter("disable-paste", queryParams.getOrDefault("disable-paste", "false"));

        // 网关设置
        config.setParameter("gateway-hostname", queryParams.getOrDefault("gateway-hostname", ""));
        config.setParameter("gateway-port", queryParams.getOrDefault("gateway-port", "443"));
        config.setParameter("gateway-username", queryParams.getOrDefault("gateway-username", ""));
        config.setParameter("gateway-password", queryParams.getOrDefault("gateway-password", ""));
        config.setParameter("gateway-domain", queryParams.getOrDefault("gateway-domain", ""));

        // 负载均衡设置
        config.setParameter("load-balance-info", queryParams.getOrDefault("load-balance-info", ""));

        // 预连接设置
        config.setParameter("preconnection-id", queryParams.getOrDefault("preconnection-id", ""));
        config.setParameter("preconnection-blob", queryParams.getOrDefault("preconnection-blob", ""));

        // 连接超时设置
        config.setParameter("connect-timeout", queryParams.getOrDefault("connect-timeout", "10000"));

        // 心跳设置
        String heartbeatInterval = queryParams.getOrDefault("heartbeat-interval", "30000");
        config.setParameter("heartbeat-interval", heartbeatInterval);

        // 重连设置
        config.setParameter("auto-retry", queryParams.getOrDefault("auto-retry", "true"));
        config.setParameter("max-retry-attempts", queryParams.getOrDefault("max-retry-attempts", "3"));
        config.setParameter("retry-interval", queryParams.getOrDefault("retry-interval", "5000"));

        // 域设置
        config.setParameter("domain", queryParams.getOrDefault("domain", ""));

        // 应用程序设置
        config.setParameter("initial-program", queryParams.getOrDefault("initial-program", ""));
        config.setParameter("client-name", queryParams.getOrDefault("client-name", "Guacamole RDP"));

        // 控制台设置
        config.setParameter("console", queryParams.getOrDefault("console", "false"));
        config.setParameter("console-audio", queryParams.getOrDefault("console-audio", "false"));

        log.info("RDP配置: host={}, port={}, security={}, resolution={}x{}, heartbeat={}, layout={}",
                server.getMonitorSysGenServerHost(),
                server.getMonitorSysGenServerPort(),
                config.getParameter("security"),
                width, height,
                heartbeatInterval,
                config.getParameter("server-layout"));
    }

    /**
     * 配置VNC参数
     */
    private void configureVncParameters(GuacamoleConfiguration config, MonitorSysGenServer server, WebSocketSession session) {
        Map<String, String> queryParams = getQueryParameters(session);

        // 基本显示参数
        config.setParameter("color-depth", queryParams.getOrDefault("color-depth", "32"));
        config.setParameter("swap-red-blue", queryParams.getOrDefault("swap-red-blue", "false"));
        config.setParameter("cursor", queryParams.getOrDefault("cursor", "local"));
        config.setParameter("read-only", queryParams.getOrDefault("read-only", "false"));

        // 分辨率设置
        String width = queryParams.getOrDefault("width", "1024");
        String height = queryParams.getOrDefault("height", "768");
        config.setParameter("width", width);
        config.setParameter("height", height);

        // 编码设置
        config.setParameter("encodings", queryParams.getOrDefault("encodings", "zrle ultra copyrect hextile zlib corre rre raw"));

        // 性能优化参数
        config.setParameter("enable-wallpaper", queryParams.getOrDefault("enable-wallpaper", "false"));
        config.setParameter("enable-theming", queryParams.getOrDefault("enable-theming", "false"));
        config.setParameter("enable-font-smoothing", queryParams.getOrDefault("enable-font-smoothing", "false"));
        config.setParameter("enable-full-window-drag", queryParams.getOrDefault("enable-full-window-drag", "false"));
        config.setParameter("enable-desktop-composition", queryParams.getOrDefault("enable-desktop-composition", "false"));
        config.setParameter("enable-menu-animations", queryParams.getOrDefault("enable-menu-animations", "false"));

        // 剪贴板设置
        config.setParameter("disable-copy", queryParams.getOrDefault("disable-copy", "false"));
        config.setParameter("disable-paste", queryParams.getOrDefault("disable-paste", "false"));

        // 音频设置
        config.setParameter("enable-audio", queryParams.getOrDefault("enable-audio", "false"));
        config.setParameter("audio-servername", queryParams.getOrDefault("audio-servername", ""));

        // 文件传输设置
        config.setParameter("enable-sftp", queryParams.getOrDefault("enable-sftp", "false"));
        config.setParameter("sftp-hostname", queryParams.getOrDefault("sftp-hostname", server.getMonitorSysGenServerHost()));
        config.setParameter("sftp-port", queryParams.getOrDefault("sftp-port", "22"));
        config.setParameter("sftp-username", queryParams.getOrDefault("sftp-username", server.getMonitorSysGenServerUsername()));
        config.setParameter("sftp-password", queryParams.getOrDefault("sftp-password", server.getMonitorSysGenServerPassword()));

        // 安全设置
        config.setParameter("security", queryParams.getOrDefault("security", "any"));
        config.setParameter("ignore-cert", queryParams.getOrDefault("ignore-cert", "true"));

        // 连接超时设置
        config.setParameter("connect-timeout", queryParams.getOrDefault("connect-timeout", "10000"));

        // 心跳设置
        String heartbeatInterval = queryParams.getOrDefault("heartbeat-interval", "30000");
        config.setParameter("heartbeat-interval", heartbeatInterval);

        // 重连设置
        config.setParameter("auto-retry", queryParams.getOrDefault("auto-retry", "true"));
        config.setParameter("max-retry-attempts", queryParams.getOrDefault("max-retry-attempts", "3"));
        config.setParameter("retry-interval", queryParams.getOrDefault("retry-interval", "5000"));

        log.info("VNC配置: host={}, port={}, resolution={}x{}, color-depth={}, heartbeat={}",
                server.getMonitorSysGenServerHost(),
                server.getMonitorSysGenServerPort(),
                width, height,
                config.getParameter("color-depth"),
                heartbeatInterval);
    }

    /**
     * 配置SSH参数
     */
    private void configureSshParameters(GuacamoleConfiguration config, MonitorSysGenServer server, WebSocketSession session) {
        Map<String, String> queryParams = getQueryParameters(session);

        config.setParameter("font-name", queryParams.getOrDefault("font-name", "monospace"));
        config.setParameter("font-size", queryParams.getOrDefault("font-size", "12"));
        config.setParameter("color-scheme", queryParams.getOrDefault("color-scheme", "gray-black"));
        config.setParameter("terminal-type", queryParams.getOrDefault("terminal-type", "xterm"));

        log.info("SSH配置: host={}, port={}, font={}",
                server.getMonitorSysGenServerHost(),
                server.getMonitorSysGenServerPort(),
                config.getParameter("font-name"));
    }

    /**
     * 启动隧道数据读取线程
     */
    private void startTunnelReader(WebSocketSession session, GuacamoleTunnel tunnel) {
        executorService.submit(() -> {
            String sessionId = session.getId();

            try {
                GuacamoleReader reader = tunnel.acquireReader();

                while (session.isOpen() && !Thread.currentThread().isInterrupted()) {
                    // 使用GuacamoleReader的正确方法读取指令
                    org.apache.guacamole.protocol.GuacamoleInstruction instruction = reader.readInstruction();
                    if (instruction != null) {
                        // 将指令转换为字符串并发送到WebSocket
                        String instructionString = instruction.toString();
                        session.sendMessage(new TextMessage(instructionString));
                    }
                }

                tunnel.releaseReader();

            } catch (Exception e) {
                if (session.isOpen()) {
                    log.error("隧道数据读取失败: sessionId={}, error={}", sessionId, e.getMessage(), e);
                    try {
                        session.close(CloseStatus.SERVER_ERROR);
                    } catch (IOException ioException) {
                        log.error("关闭会话失败: sessionId={}", sessionId, ioException);
                    }
                }
            }
        });
    }

    /**
     * 清理会话资源
     */
    private void cleanupSession(String sessionId) {
        try {
            // 关闭隧道
            GuacamoleTunnel tunnel = sessionTunnelMap.remove(sessionId);
            if (tunnel != null) {
                tunnel.close();
            }

            // 清理映射
            String serverId = sessionServerMap.remove(sessionId);

            log.info("会话资源清理完成: sessionId={}, serverId={}", sessionId, serverId);

        } catch (Exception e) {
            log.error("清理会话资源失败: sessionId={}, error={}", sessionId, e.getMessage(), e);
        }
    }

    /**
     * 获取URL查询参数
     */
    private Map<String, String> getQueryParameters(WebSocketSession session) {
        Map<String, String> params = new ConcurrentHashMap<>();
        try {
            URI uri = session.getUri();
            if (uri != null && uri.getQuery() != null) {
                String[] queryParams = uri.getQuery().split("&");
                for (String param : queryParams) {
                    String[] keyValue = param.split("=");
                    if (keyValue.length == 2) {
                        params.put(keyValue[0], keyValue[1]);
                    }
                }
            }
        } catch (Exception e) {
            log.error("解析查询参数失败: {}", e.getMessage(), e);
        }
        return params;
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        String serverId = sessionServerMap.get(session.getId());
        if (serverId == null) {
            return;
        }
        GuacamoleTunnel tunnel = sessionTunnelMap.get(serverId);
        // Ignore inbound messages if there is no associated tunnel
        if (tunnel == null)
            return;

        // Filter received instructions, handling tunnel-internal instructions
        // without passing through to guacd
        GuacamoleWriter writer = new FilteredGuacamoleWriter(tunnel.acquireWriter(), new GuacamoleFilter() {

            @Override
            public GuacamoleInstruction filter(GuacamoleInstruction instruction)
                    throws GuacamoleException {

                // Filter out all tunnel-internal instructions
                if (instruction.getOpcode().equals(GuacamoleTunnel.INTERNAL_DATA_OPCODE)) {

                    List<String> args = instruction.getArgs();
                    if (args.size() >= 2 && args.get(0).equals(PING_OPCODE)) {

                        try {
                            sendInstruction(session, new GuacamoleInstruction(
                                    GuacamoleTunnel.INTERNAL_DATA_OPCODE,
                                    PING_OPCODE, args.get(1)
                            ));
                        } catch (IOException e) {
                            log.debug("Unable to send \"ping\" response for WebSocket tunnel.", e);
                        }

                    }

                    return null;

                }

                // Pass through all non-internal instructions untouched
                return instruction;

            }

        });

        try {
            // Write received message
            writer.write(message.getPayload().toString().toCharArray());
        } catch (GuacamoleConnectionClosedException e) {
            log.debug("Connection to guacd closed.", e);
        } catch (GuacamoleException e) {
            log.debug("WebSocket tunnel write failed.", e);
        }

        tunnel.releaseWriter();
    }


    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("RDP WebSocket传输错误: sessionId={}, error={}", session.getId(), exception.getMessage(), exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        String sessionId = session.getId();
        log.info("RDP WebSocket连接关闭: sessionId={}, status={}", sessionId, closeStatus);

        // 清理会话
        GuacamoleTunnel guacamoleTunnel = sessionTunnelMap.remove(sessionId);
        IoUtils.closeQuietly(guacamoleTunnel);
        sessionServerMap.remove(sessionId);
        cleanupSession(sessionId);
    }

    /**
     * 发送消息
     */
    private void sendMessage(String type, Object data) {
        Map<String, Object> message = Map.of(
                "type", type,
                "data", data,
                "success", true,
                "timestamp", System.currentTimeMillis()
        );
        socketSessionTemplate.send("gen/server", Json.toJson(message));
    }

    /**
     * 发送错误消息
     */
    private void sendErrorMessage(WebSocketSession session, String error) {
        sendMessage("error", Map.of("message", error));
    }

    /**
     * 从WebSocket会话中获取服务器ID
     */
    private String getServerIdFromSession(WebSocketSession session) {
        try {
            String query = session.getUri().getQuery();
            log.debug("原始查询字符串: {}", query);

            if (query != null && !query.isEmpty()) {
                // 直接解析查询参数，不需要替换?为&
                String[] params = query.split("&");
                for (String param : params) {
                    if (param.contains("=")) {
                        String[] keyValue = param.split("=", 2);
                        if (keyValue.length == 2 && "serverId".equals(keyValue[0].trim())) {
                            String serverId = keyValue[1].trim();
                            log.debug("解析到服务器ID: {}", serverId);
                            return serverId;
                        }
                    }
                }
            }
            log.warn("未找到serverId参数，查询字符串: {}", query);
        } catch (Exception e) {
            log.error("获取服务器ID失败: {}", e.getMessage(), e);
        }
        return null;
    }

    private void sendInstruction(WebSocketSession session, GuacamoleInstruction instruction)
            throws IOException {
        sendInstruction(session, instruction.toString());
    }

    private void sendInstruction(WebSocketSession session, String instruction)
            throws IOException {
        session.sendMessage(new TextMessage(instruction));
        sendMessage("rdp-data", Map.of("data", instruction));
    }
}
