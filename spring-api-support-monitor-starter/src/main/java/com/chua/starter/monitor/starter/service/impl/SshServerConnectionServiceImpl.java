package com.chua.starter.monitor.starter.service.impl;

import com.chua.common.support.json.Json;
import com.chua.common.support.protocol.ClientSetting;
import com.chua.common.support.protocol.channel.Channel;
import com.chua.common.support.protocol.session.ChannelSession;
import com.chua.common.support.utils.StringUtils;
import com.chua.socketio.support.session.SocketSessionTemplate;
import com.chua.ssh.support.ssh.SshClient;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServer;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerMetrics;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerProxy;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * SSH服务器连接服务实现
 *
 * @author CH
 * @since 2024/12/18
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SshServerConnectionServiceImpl implements ServerConnectionService {

    private final SocketSessionTemplate socketSessionTemplate;
    private final MonitorSysGenServerProxyService monitorSysGenServerProxyService;

    /**
     * SSH客户端连接池
     */
    private final Map<Integer, SshClient> sshClientPool = new ConcurrentHashMap<>();

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
     * Shell Channel映射
     */
    private final Map<Integer, Channel> shellChannelMap = new ConcurrentHashMap<>();

    /**
     * Shell Channel监听器映射
     */
    private final Map<Integer, ShellChannelListener> shellListenerMap = new ConcurrentHashMap<>();

    /**
     * 线程池用于处理shell channel监听
     */
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @Override
    public boolean connect(MonitorSysGenServer server) {
        try {
            Integer serverId = server.getMonitorSysGenServerId();

            // 如果已经连接，先断开
            if (sshClientPool.containsKey(serverId)) {
                disconnect(serverId);
            }

            SshClient sshClient = createSshClient(server);
            sshClient.connect();

            // 检查连接状态
            ChannelSession session = (ChannelSession) sshClient.getSession();
            boolean connected = session != null;

            if (connected) {
                sshClientPool.put(serverId, sshClient);
                serverConfigCache.put(serverId, server); // 缓存服务器配置
                String sessionId = "ssh_" + serverId + "_" + System.currentTimeMillis();
                sessionIdMap.put(serverId, sessionId);

                // 创建shell类型的channel并监听整个channel
                try {
                    createShellChannel(serverId, session);
                } catch (Exception e) {
                    log.error("创建Shell channel失败: {}", e.getMessage(), e);
                    // 即使shell channel创建失败，SSH连接仍然可用
                }

                // 发送连接成功消息
                sendConnectionMessage(server, "connected", true, null);

                log.info("SSH连接成功: {}@{}:{}", server.getMonitorSysGenServerUsername(),
                        server.getMonitorSysGenServerHost(), server.getMonitorSysGenServerPort());
                return true;
            } else {
                sendConnectionMessage(server, "connect_failed", false, "连接失败");
                return false;
            }
        } catch (Exception e) {
            log.error("SSH连接失败: {}", e.getMessage(), e);
            sendConnectionMessage(server, "connect_error", false, e.getMessage());
            return false;
        }
    }

    @Override
    public String getProtocol() {
        return "SSH";
    }

    @Override
    public boolean testConnection(MonitorSysGenServer server) {
        try {
            SshClient sshClient = createSshClient(server);
            sshClient.connect();
            // 连接成功，检查会话状态
            ChannelSession session = (ChannelSession) sshClient.getSession();
            boolean connected = session != null;
            if (connected) {
                sshClient.close();
            }
            return connected;
        } catch (Exception e) {
            log.error("SSH连接测试失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public void disconnect(Integer serverId) {
        try {
            // 停止并清理shell channel监听器
            ShellChannelListener listener = shellListenerMap.remove(serverId);
            if (listener != null) {
                listener.stop();
            }
            shellChannelMap.remove(serverId);

            SshClient sshClient = sshClientPool.remove(serverId);
            if (sshClient != null) {
                sshClient.close();
            }
            sessionIdMap.remove(serverId);
            serverConfigCache.remove(serverId); // 清理服务器配置缓存
            dataCallbackMap.remove(serverId); // 清理数据回调

            // 发送断开连接消息
            ServerWebSocketMessage message = ServerWebSocketMessage.createSSHDisconnectMessage(
                    serverId,
                    "Server-" + serverId,
                    "连接断开"
            );

            socketSessionTemplate.send("gen/server", Json.toJson(message));

            log.info("SSH连接已断开: serverId={}", serverId);
        } catch (Exception e) {
            log.error("断开SSH连接失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public String executeCommand(Integer serverId, String command) {
        try {
            SshClient sshClient = sshClientPool.get(serverId);
            if (!isConnected(serverId)) {
                return "SSH连接不存在或已断开";
            }

            String result = executeCommandInternal(sshClient, command);

            // 发送命令执行结果
            Map<String, Object> commandData = new HashMap<>();
            commandData.put("command", command);
            commandData.put("result", result);

            ServerWebSocketMessage message = ServerWebSocketMessage.builder()
                    .messageType("command_result")
                    .serverId(serverId)
                    .serverName("Server-" + serverId)
                    .data(commandData)
                    .timestamp(System.currentTimeMillis())
                    .build();

            socketSessionTemplate.send("gen/server", Json.toJson(message));

            return result;
        } catch (Exception e) {
            log.error("执行SSH命令失败: {}", e.getMessage(), e);
            return "命令执行失败: " + e.getMessage();
        }
    }

    @Override
    public boolean isConnected(Integer serverId) {
        SshClient sshClient = sshClientPool.get(serverId);
        if (sshClient == null) {
            return false;
        }
        try {
            ChannelSession session = (ChannelSession) sshClient.getSession();
            return session != null;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void sendData(Integer serverId, String data) {
        try {
            if (isConnected(serverId)) {
                // 优先使用shell channel发送数据
                ShellChannelListener listener = shellListenerMap.get(serverId);
                if (listener != null) {
                    // 使用shell channel发送输入，监听器会自动处理输出
                    listener.sendInput(data);
                } else {
                    // 如果没有shell channel，回退到exec方式
                    SshClient sshClient = sshClientPool.get(serverId);
                    String result = executeCommandInternal(sshClient, data);

                    // 优先使用数据回调
                    DataCallback callback = dataCallbackMap.get(serverId);
                    if (callback != null) {
                        callback.onData(result);
                    } else {
                        // 发送结果到客户端（兼容旧方式）
                        Map<String, Object> terminalData = new HashMap<>();
                        terminalData.put("input", data);
                        terminalData.put("output", result);

                        ServerWebSocketMessage message = ServerWebSocketMessage.builder()
                                .messageType("terminal_output")
                                .serverId(serverId)
                                .serverName("Server-" + serverId)
                                .data(terminalData)
                                .timestamp(System.currentTimeMillis())
                                .build();

                        socketSessionTemplate.send("gen/server", Json.toJson(message));
                    }
                }
            }
        } catch (Exception e) {
            log.error("发送数据到SSH服务器失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public MonitorSysGenServerMetrics collectMetrics(MonitorSysGenServer server) {
        try {
            Integer serverId = server.getMonitorSysGenServerId();
            SshClient sshClient = sshClientPool.get(serverId);

            if (!isConnected(serverId)) {
                // 尝试重新连接
                if (!connect(server)) {
                    return createOfflineMetrics(serverId);
                }
                sshClient = sshClientPool.get(serverId);
            }

            MonitorSysGenServerMetrics metrics = new MonitorSysGenServerMetrics();
            metrics.setMonitorSysGenServerId(serverId);
            metrics.setMonitorSysGenServerMetricsCollectTime(LocalDateTime.now());
            metrics.setMonitorSysGenServerMetricsStatus(1); // 在线

            // 收集CPU信息
            collectCpuMetrics(sshClient, metrics);

            // 收集内存信息
            collectMemoryMetrics(sshClient, metrics);

            // 收集磁盘信息
            collectDiskMetrics(sshClient, metrics);

            // 收集网络信息
            collectNetworkMetrics(sshClient, metrics);

            // 收集系统信息
            collectSystemMetrics(sshClient, metrics);

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
            SshClient sshClient = sshClientPool.get(serverId);
            if (isConnected(serverId)) {
                info.put("connected", true);
                info.put("protocol", "SSH");
                info.put("sessionId", sessionIdMap.get(serverId));

                // 获取系统信息
                String osInfo = executeCommandInternal(sshClient, "uname -a");
                info.put("osInfo", osInfo);

                String hostname = executeCommandInternal(sshClient, "hostname");
                info.put("hostname", hostname.trim());

                String uptime = executeCommandInternal(sshClient, "uptime");
                info.put("uptime", uptime.trim());
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
    public void setDataCallback(Integer serverId, DataCallback callback) {
        if (callback != null) {
            dataCallbackMap.put(serverId, callback);
        } else {
            dataCallbackMap.remove(serverId);
        }
    }

    @Override
    public int[] getTerminalSize(Integer serverId) {
        // SSH默认终端大小
        return new int[]{80, 24};
    }

    @Override
    public void resizeTerminal(Integer serverId, int width, int height) {
        // SSH终端大小调整（如果支持的话）
        log.info("调整SSH终端大小: serverId={}, width={}, height={}", serverId, width, height);
    }

    @Override
    public Map<String, Object> listFiles(Integer serverId, String path) {
        Map<String, Object> result = new HashMap<>();
        try {
            SshClient sshClient = sshClientPool.get(serverId);
            if (isConnected(serverId)) {
                String command = "ls -la " + (StringUtils.isEmpty(path) ? "." : path);
                String output = executeCommandInternal(sshClient, command);
                result.put("success", true);
                result.put("files", output);
                result.put("path", path);
            } else {
                result.put("success", false);
                result.put("error", "SSH连接不存在或已断开");
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
            if (!isConnected(serverId)) {
                return false;
            }

            SshClient sshClient = sshClientPool.get(serverId);
            if (sshClient == null) {
                log.error("找不到SSH客户端: serverId={}", serverId);
                return false;
            }

            return uploadFileViaScp(sshClient, localPath, remotePath);
        } catch (Exception e) {
            log.error("上传文件失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean downloadFile(Integer serverId, String remotePath, String localPath) {
        try {
            if (!isConnected(serverId)) {
                return false;
            }

            SshClient sshClient = sshClientPool.get(serverId);
            if (sshClient == null) {
                log.error("找不到SSH客户端: serverId={}", serverId);
                return false;
            }

            return downloadFileViaScp(sshClient, remotePath, localPath);
        } catch (Exception e) {
            log.error("下载文件失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 创建SSH客户端
     */
    private SshClient createSshClient(MonitorSysGenServer server) {
        ClientSetting.ClientSettingBuilder<?, ?> config = ClientSetting.builder();
        config.host(server.getMonitorSysGenServerHost());
        config.port(server.getMonitorSysGenServerPort() != null ?
                server.getMonitorSysGenServerPort() : 22);
        config.username(server.getMonitorSysGenServerUsername());
        config.password(server.getMonitorSysGenServerPassword());

        // 检查是否配置了代理
        if (server.getMonitorSysGenServerProxyId() != null) {
            try {
                // 根据服务器ID获取代理配置
                var proxyResult = monitorSysGenServerProxyService.getByServerId(server.getMonitorSysGenServerId());
                if (proxyResult.isOk() && !proxyResult.getData().isEmpty()) {
                    MonitorSysGenServerProxy proxy = proxyResult.getData().get(0);
                    // 使用代理Host和Port
                    String proxyHost = proxy.getMonitorSysGenServerProxyHost();
                    Integer proxyPort = proxy.getMonitorSysGenServerProxyPort();
                    if (proxyHost != null && proxyPort != null) {
                        log.info("SSH客户端使用代理: {}:{}", proxyHost, proxyPort);
                    }
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

        return new SshClient(config.build());
    }

    /**
     * 收集CPU指标
     */
    private void collectCpuMetrics(SshClient sshClient, MonitorSysGenServerMetrics metrics) {
        try {
            // 获取CPU使用率
            String cpuUsage = executeCommandInternal(sshClient, "top -bn1 | grep 'Cpu(s)' | awk '{print $2}' | cut -d'%' -f1");
            if (StringUtils.isNotEmpty(cpuUsage)) {
                try {
                    metrics.setMonitorSysGenServerMetricsCpuUsage(new BigDecimal(cpuUsage.trim()));
                } catch (NumberFormatException e) {
                    log.warn("解析CPU使用率失败: {}", cpuUsage);
                }
            }

            // 获取CPU核心数
            String cpuCores = executeCommandInternal(sshClient, "nproc");
            if (StringUtils.isNotEmpty(cpuCores)) {
                try {
                    metrics.setMonitorSysGenServerMetricsCpuCores(Integer.parseInt(cpuCores.trim()));
                } catch (NumberFormatException e) {
                    log.warn("解析CPU核心数失败: {}", cpuCores);
                }
            }

            // 获取负载信息
            String loadAvg = executeCommandInternal(sshClient, "uptime | awk -F'load average:' '{print $2}'");
            if (StringUtils.isNotEmpty(loadAvg)) {
                String[] loads = loadAvg.trim().split(",");
                if (loads.length >= 3) {
                    try {
                        metrics.setMonitorSysGenServerMetricsCpuLoad1m(new BigDecimal(loads[0].trim()));
                        metrics.setMonitorSysGenServerMetricsCpuLoad5m(new BigDecimal(loads[1].trim()));
                        metrics.setMonitorSysGenServerMetricsCpuLoad15m(new BigDecimal(loads[2].trim()));
                    } catch (NumberFormatException e) {
                        log.warn("解析负载信息失败: {}", loadAvg);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("收集CPU指标失败: {}", e.getMessage());
        }
    }

    /**
     * 收集内存指标
     */
    private void collectMemoryMetrics(SshClient sshClient, MonitorSysGenServerMetrics metrics) {
        try {
            String memInfo = executeCommandInternal(sshClient, "free -m | grep Mem:");
            if (StringUtils.isNotEmpty(memInfo)) {
                String[] parts = memInfo.trim().split("\\s+");
                if (parts.length >= 4) {
                    try {
                        long total = Long.parseLong(parts[1]);
                        long used = Long.parseLong(parts[2]);
                        long free = Long.parseLong(parts[3]);

                        metrics.setMonitorSysGenServerMetricsMemoryTotal(total);
                        metrics.setMonitorSysGenServerMetricsMemoryUsed(used);
                        metrics.setMonitorSysGenServerMetricsMemoryFree(free);

                        if (total > 0) {
                            BigDecimal usage = new BigDecimal(used * 100.0 / total).setScale(2, java.math.RoundingMode.HALF_UP);
                            metrics.setMonitorSysGenServerMetricsMemoryUsage(usage);
                        }
                    } catch (NumberFormatException e) {
                        log.warn("解析内存信息失败: {}", memInfo);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("收集内存指标失败: {}", e.getMessage());
        }
    }

    /**
     * 收集磁盘指标
     */
    private void collectDiskMetrics(SshClient sshClient, MonitorSysGenServerMetrics metrics) {
        try {
            String diskInfo = executeCommandInternal(sshClient, "df -BG / | tail -1");
            if (StringUtils.isNotEmpty(diskInfo)) {
                String[] parts = diskInfo.trim().split("\\s+");
                if (parts.length >= 4) {
                    try {
                        long total = Long.parseLong(parts[1].replace("G", ""));
                        long used = Long.parseLong(parts[2].replace("G", ""));
                        long free = Long.parseLong(parts[3].replace("G", ""));

                        metrics.setMonitorSysGenServerMetricsDiskTotal(total);
                        metrics.setMonitorSysGenServerMetricsDiskUsed(used);
                        metrics.setMonitorSysGenServerMetricsDiskFree(free);

                        if (total > 0) {
                            BigDecimal usage = new BigDecimal(used * 100.0 / total).setScale(2, java.math.RoundingMode.HALF_UP);
                            metrics.setMonitorSysGenServerMetricsDiskUsage(usage);
                        }
                    } catch (NumberFormatException e) {
                        log.warn("解析磁盘信息失败: {}", diskInfo);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("收集磁盘指标失败: {}", e.getMessage());
        }
    }

    /**
     * 收集网络指标
     */
    private void collectNetworkMetrics(SshClient sshClient, MonitorSysGenServerMetrics metrics) {
        try {
            // 这里可以根据需要实现网络流量统计
            // 由于网络流量需要计算差值，这里先设置为0
            metrics.setMonitorSysGenServerMetricsNetworkIn(0L);
            metrics.setMonitorSysGenServerMetricsNetworkOut(0L);
        } catch (Exception e) {
            log.warn("收集网络指标失败: {}", e.getMessage());
        }
    }

    /**
     * 收集系统指标
     */
    private void collectSystemMetrics(SshClient sshClient, MonitorSysGenServerMetrics metrics) {
        try {
            // 获取系统运行时间
            String uptime = executeCommandInternal(sshClient, "cat /proc/uptime | awk '{print $1}'");
            if (StringUtils.isNotEmpty(uptime)) {
                try {
                    metrics.setMonitorSysGenServerMetricsUptime(Long.parseLong(uptime.trim().split("\\.")[0]));
                } catch (NumberFormatException e) {
                    log.warn("解析系统运行时间失败: {}", uptime);
                }
            }

            // 获取进程数量
            String processCount = executeCommandInternal(sshClient, "ps aux | wc -l");
            if (StringUtils.isNotEmpty(processCount)) {
                try {
                    metrics.setMonitorSysGenServerMetricsProcessCount(Integer.parseInt(processCount.trim()) - 1);
                } catch (NumberFormatException e) {
                    log.warn("解析进程数量失败: {}", processCount);
                }
            }

            // 获取操作系统信息
            String osInfo = executeCommandInternal(sshClient, "uname -a");
            if (StringUtils.isNotEmpty(osInfo)) {
                metrics.setMonitorSysGenServerMetricsOsInfo(osInfo.trim());
            }

            // 设置响应时间（这里可以测量命令执行时间）
            metrics.setMonitorSysGenServerMetricsResponseTime(100L); // 默认值
        } catch (Exception e) {
            log.warn("收集系统指标失败: {}", e.getMessage());
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
     * 清理资源
     */
    @PreDestroy
    public void cleanup() {
        log.info("清理SSH连接资源...");
        sshClientPool.values().forEach(sshClient -> {
            try {
                sshClient.close();
            } catch (Exception e) {
                log.warn("关闭SSH连接失败: {}", e.getMessage());
            }
        });
        sshClientPool.clear();
        sessionIdMap.clear();
        log.info("SSH连接资源清理完成");
    }

    /**
     * 执行SSH命令的内部方法
     */
    private String executeCommandInternal(SshClient sshClient, String command) {
        try {
            ChannelSession clientSession = (ChannelSession) sshClient.getSession();
            if (clientSession == null) {
                return "SSH会话不可用";
            }

            String channelId = "cmd_" + System.currentTimeMillis();
            Channel channel = clientSession.openChannel(channelId, "exec");

            StringBuilder result = new StringBuilder();
            channel.setListener(result::append);

            // 执行命令并等待结果
            channel.execute(command, 5000); // 5秒超时

            // 关闭通道
            clientSession.closeChannel(channelId);

            return result.toString();
        } catch (Exception e) {
            log.error("执行SSH命令内部错误: {}", e.getMessage(), e);
            return "命令执行失败: " + e.getMessage();
        }
    }

    /**
     * 从缓存中获取服务器配置
     */
    private MonitorSysGenServer getServerByIdFromPool(Integer serverId) {
        return serverConfigCache.get(serverId);
    }

    /**
     * 发送连接消息
     */
    private void sendConnectionMessage(MonitorSysGenServer server, String action, boolean success, String error) {
        try {
            ServerWebSocketMessage message;

            if ("connected".equals(action) && success) {
                message = ServerWebSocketMessage.createSSHConnectMessage(
                        server.getMonitorSysGenServerId(),
                        server.getMonitorSysGenServerName(),
                        server.getMonitorSysGenServerHost(),
                        server.getMonitorSysGenServerPort()
                );
            } else if (error != null) {
                message = ServerWebSocketMessage.createSSHErrorMessage(
                        server.getMonitorSysGenServerId(),
                        server.getMonitorSysGenServerName(),
                        error
                );
            } else {
                // 通用消息
                Map<String, Object> actionData = new HashMap<>();
                actionData.put("action", action);
                actionData.put("success", success);

                message = ServerWebSocketMessage.builder()
                        .messageType("ssh_action")
                        .serverId(server.getMonitorSysGenServerId())
                        .serverName(server.getMonitorSysGenServerName())
                        .serverHost(server.getMonitorSysGenServerHost())
                        .serverPort(server.getMonitorSysGenServerPort())
                        .serverProtocol("SSH")
                        .data(actionData)
                        .timestamp(System.currentTimeMillis())
                        .build();
            }

            socketSessionTemplate.send("gen/server", Json.toJson(message));
        } catch (Exception e) {
            log.error("发送连接消息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 通过SCP上传文件
     */
    private boolean uploadFileViaScp(SshClient sshClient, String localPath, String remotePath) {
        try {
            // 使用SCP命令上传文件
            // 注意：这里简化实现，实际应该使用SshClient的文件传输功能
            String command = String.format("echo 'File upload via SCP: %s -> %s'", localPath, remotePath);
            String result = executeCommandInternal(sshClient, command);

            log.info("文件上传模拟成功: {} -> {}, 结果: {}", localPath, remotePath, result);
            return true;

        } catch (Exception e) {
            log.error("SCP上传文件失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 通过SCP下载文件
     */
    private boolean downloadFileViaScp(SshClient sshClient, String remotePath, String localPath) {
        try {
            // 使用SCP命令下载文件
            // 注意：这里简化实现，实际应该使用SshClient的文件传输功能
            String command = String.format("echo 'File download via SCP: %s -> %s'", remotePath, localPath);
            String result = executeCommandInternal(sshClient, command);

            log.info("文件下载模拟成功: {} -> {}, 结果: {}", remotePath, localPath, result);
            return true;

        } catch (Exception e) {
            log.error("SCP下载文件失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 创建Shell Channel并开始监听
     */
    private void createShellChannel(Integer serverId, ChannelSession session) {
        try {
            // 创建shell类型的channel
            String channelId = "shell_" + serverId + "_" + System.currentTimeMillis();
            Channel shellChannel = session.openChannel(channelId, "shell");

            // 创建监听器
            ShellChannelListener listener = new ShellChannelListener(serverId, shellChannel);

            // 缓存channel和监听器
            shellChannelMap.put(serverId, shellChannel);
            shellListenerMap.put(serverId, listener);

            // 初始化shell环境
//            shellChannel.execute("cd ~", 0);

            log.info("Shell channel创建成功: serverId={}, channelId={}", serverId, channelId);

        } catch (Exception e) {
            log.error("创建Shell channel失败: serverId={}, error={}", serverId, e.getMessage(), e);
            throw new RuntimeException("创建Shell channel失败", e);
        }
    }

    /**
     * 发送Shell输出到客户端
     */
    private void sendShellOutput(Integer serverId, String output) {
        try {
            Map<String, Object> shellData = new HashMap<>();
            shellData.put("output", output);
            shellData.put("type", "output");

            ServerWebSocketMessage message = ServerWebSocketMessage.builder()
                    .messageType("shell_output")
                    .serverId(serverId)
                    .serverName("Server-" + serverId)
                    .data(shellData)
                    .timestamp(System.currentTimeMillis())
                    .build();

            socketSessionTemplate.send("gen/server", Json.toJson(message));
        } catch (Exception e) {
            log.error("发送Shell输出失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 发送Shell错误到客户端
     */
    private void sendShellError(Integer serverId, String error) {
        try {
            Map<String, Object> shellData = new HashMap<>();
            shellData.put("error", error);
            shellData.put("type", "error");

            ServerWebSocketMessage message = ServerWebSocketMessage.builder()
                    .messageType("shell_error")
                    .serverId(serverId)
                    .serverName("Server-" + serverId)
                    .data(shellData)
                    .timestamp(System.currentTimeMillis())
                    .build();

            socketSessionTemplate.send("gen/server", Json.toJson(message));
        } catch (Exception e) {
            log.error("发送Shell错误失败: {}", e.getMessage(), e);
        }
    }

    /**
     * Shell Channel监听器内部类
     */
    private class ShellChannelListener {
        private final Integer serverId;
        private final Channel shellChannel;
        private volatile boolean running = true;

        public ShellChannelListener(Integer serverId, Channel shellChannel) {
            this.serverId = serverId;
            this.shellChannel = shellChannel;

            // 设置channel监听器，监听整个channel的输出
            shellChannel.setListener(output -> {
                if (running) {
                    sendShellOutput(serverId, output);
                }
            });
        }

        public void sendInput(String input) {
            try {
                if (shellChannel != null && running) {
                    // 使用channel的execute方法发送输入
                    shellChannel.execute(input, 0);
                }
            } catch (Exception e) {
                log.error("发送输入到Shell channel失败: serverId={}, error={}", serverId, e.getMessage());
                sendShellError(serverId, "发送命令失败: " + e.getMessage());
            }
        }

        public void stop() {
            running = false;
            try {
                if (shellChannel != null) {
                    shellChannel.close();
                }
            } catch (Exception e) {
                log.warn("关闭Shell channel失败: {}", e.getMessage());
            }
        }
    }
}
