package com.chua.starter.monitor.service.impl;

// 项目内部依赖

import com.chua.common.support.protocol.ClientSetting;
import com.chua.common.support.protocol.client.ProtocolClientFactory;
import com.chua.ssh.support.client.SshClient;
import com.chua.starter.monitor.entity.MonitorSysGenServer;
import com.chua.starter.monitor.entity.MonitorSysGenServerMetrics;
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
 * SSH服务器连接服务实现
 *
 * 注意：此实现为简化版本，主要提供基本的接口实现
 * 完整的SSH功能需要相关依赖正确配置后才能使用
 *
 * @author CH
 * @since 2024/12/18
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SshServerConnectionServiceImpl implements ServerConnectionService {

    private final MonitorSysGenServerSettingService monitorSysGenServerSettingService;

    /**
     * 连接状态映射
     */
    private final Map<Integer, SshClient> connectionStatusMap = new ConcurrentHashMap<>();

    /**
     * 服务器配置缓存
     */
    private final Map<Integer, MonitorSysGenServer> serverConfigCache = new ConcurrentHashMap<>();
    /**
     * 连接数据回调映射
     */
    private final Map<Integer, DataCallback> connectionCallbackMap = new ConcurrentHashMap<>();

    @Override
    public boolean connect(MonitorSysGenServer server) {
        try {
            Integer serverId = server.getMonitorSysGenServerId();

            // 模拟连接过程
            log.info("尝试建立SSH连接: {}@{}:{}",
                    server.getMonitorSysGenServerUsername(),
                    server.getMonitorSysGenServerHost(),
                    server.getMonitorSysGenServerPort());
            ClientSetting.ClientSettingBuilder<?, ?> config = ClientSetting.builder();
            config.host(server.getMonitorSysGenServerHost());
            config.port(server.getMonitorSysGenServerPort() != null ? server.getMonitorSysGenServerPort() : 22);
            config.username(server.getMonitorSysGenServerUsername());
            config.password(server.getMonitorSysGenServerPassword());
            config.connectTimeoutMillis(server.getMonitorSysGenServerTimeout());
            config.clientType("ssh");

            if (server.getMonitorSysGenServerTimeout() != null) {
                config.connectTimeoutMillis(server.getMonitorSysGenServerTimeout());
            }
            ClientSetting clientSetting = config.build();
            SshClient sshClient = ProtocolClientFactory.create(server.getMonitorSysGenServerId(), SshClient.class, clientSetting);
            serverConfigCache.put(serverId, server);
            connectionStatusMap.put(serverId, sshClient);

            log.info("SSH连接成功: serverId={}", serverId);
            return true;

        } catch (Exception e) {
            log.error("SSH连接失败: {}", e.getMessage(), e);
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
            log.info("测试SSH连接: {}@{}:{}",
                    server.getMonitorSysGenServerUsername(),
                    server.getMonitorSysGenServerHost(),
                    server.getMonitorSysGenServerPort());

            if(connectionStatusMap.containsKey(server.getMonitorSysGenServerId())) {
                return true;
            }

            return connect(server);
        } catch (Exception e) {
            log.error("SSH连接测试失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public void disconnect(Integer serverId) {
        try {
            // 清理连接状态
            connectionStatusMap.remove(serverId);
            ProtocolClientFactory.uncreated(serverId);
            serverConfigCache.remove(serverId);

            log.info("SSH连接已断开: serverId={}", serverId);
        } catch (Exception e) {
            log.error("断开SSH连接失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public String executeCommand(Integer serverId, String command) {
        try {
            if (!isConnected(serverId)) {
                return "SSH连接不存在或已断开";
            }
            SshClient sshClient = connectionStatusMap.get(serverId);
            sshClient.executeCommand(command);
            return "命令执行成功: " + command;

        } catch (Exception e) {
            log.error("执行SSH命令失败: {}", e.getMessage(), e);
            return "命令执行失败: " + e.getMessage();
        }
    }

    @Override
    public boolean isConnected(Integer serverId) {
        return connectionStatusMap.get(serverId) != null;
    }

    @Override
    public void sendData(Integer serverId, String data) {
        try {
            if (isConnected(serverId)) {
                log.info("发送SSH数据（模拟）: serverId={}, dataLength={}", serverId, data.length());
            } else {
                log.warn("SSH连接不存在，无法发送数据: serverId={}", serverId);
            }
            SshClient sshClient = connectionStatusMap.get(serverId);
            sshClient.executeCommand(data);
        } catch (Exception e) {
            log.error("发送数据到SSH服务器失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public MonitorSysGenServerMetrics collectMetrics(MonitorSysGenServer server) {
        try {
            Integer serverId = server.getMonitorSysGenServerId();

            if (!isConnected(serverId)) {
                log.warn("SSH连接不存在，无法收集指标: serverId={}", serverId);
                return null;
            }

            // 创建模拟指标数据
            MonitorSysGenServerMetrics metrics = new MonitorSysGenServerMetrics();
            metrics.setMonitorSysGenServerId(serverId);
            metrics.setMonitorSysGenServerMetricsCpuUsage(new BigDecimal("50.0"));
            metrics.setMonitorSysGenServerMetricsMemoryUsage(new BigDecimal("60.0"));
            metrics.setMonitorSysGenServerMetricsDiskUsage(new BigDecimal("70.0"));
            metrics.setMonitorSysGenServerMetricsNetworkIn(1024L);
            metrics.setMonitorSysGenServerMetricsNetworkOut(2048L);
            metrics.setMonitorSysGenServerMetricsCollectTime(LocalDateTime.now());

            log.info("收集SSH服务器指标（模拟）: serverId={}", serverId);
            return metrics;

        } catch (Exception e) {
            log.error("收集服务器指标失败: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public Map<String, Object> getServerInfo(Integer serverId) {
        Map<String, Object> info = new HashMap<>();
        try {
            if (isConnected(serverId)) {
                info.put("connected", true);
                info.put("protocol", "SSH");
                info.put("osInfo", "Linux模拟系统");
                info.put("hostname", "ssh-server-" + serverId);
                info.put("uptime", "模拟运行时间");
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
        return String.valueOf(serverId);
    }

    @Override
    public int[] getTerminalSize(Integer serverId) {
        // SSH默认终端大小
        return new int[]{80, 24};
    }

    @Override
    public void resizeTerminal(Integer serverId, int width, int height) {
        // SSH终端大小调整（模拟）
        log.info("调整SSH终端大小（模拟）: serverId={}, width={}, height={}", serverId, width, height);
    }

    @Override
    public Map<String, Object> listFiles(Integer serverId, String path) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (!isConnected(serverId)) {
                result.put("success", false);
                result.put("error", "SSH连接不存在或已断开");
            }
            result.put("success", true);
            result.put("files", "模拟文件列表");
            result.put("path", path);
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

            log.info("上传文件（模拟）: {} -> {}", localPath, remotePath);
            return true;
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

            log.info("下载文件（模拟）: {} -> {}", remotePath, localPath);
            return true;
        } catch (Exception e) {
            log.error("下载文件失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public void setDataCallback(Integer serverId, DataCallback callback) {
        connectionCallbackMap.put(serverId, callback);
        if(connectionStatusMap.containsKey(serverId)) {
            connectionStatusMap.get(serverId).setListener(new SshClient.SshListener() {

                @Override
                public void onOutput(String s) {
                    callback.onData(s);
                }

                @Override
                public void onException(Exception e) {

                }

                @Override
                public void onClosed() {

                }
            });
        }
    }

    /**
     * 服务销毁时清理资源
     */
    @PreDestroy
    public void destroy() {
        try {
            log.info("开始清理SSH连接资源...");

            // 清理所有资源
            connectionStatusMap.clear();
            serverConfigCache.clear();

            log.info("SSH连接资源清理完成");
        } catch (Exception e) {
            log.error("清理SSH连接资源失败: {}", e.getMessage(), e);
        }
    }
}
