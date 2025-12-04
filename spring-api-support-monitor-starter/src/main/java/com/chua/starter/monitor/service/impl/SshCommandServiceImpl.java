package com.chua.starter.monitor.service.impl;

import com.chua.common.support.json.Json;
import com.chua.socketio.support.constant.SocketTopics;
import com.chua.socketio.support.session.SocketSessionTemplate;
import com.chua.starter.monitor.entity.MonitorSysGenServer;
import com.chua.starter.monitor.message.ServerWebSocketMessage;
import com.chua.starter.monitor.service.ServerConnectionService;
import com.chua.starter.monitor.service.SshCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SSH 命令处理服务实现
 *
 * @author CH
 * @since 2024/12/23
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SshCommandServiceImpl implements SshCommandService {

    private final List<ServerConnectionService> serverConnectionServices;
    private final SocketSessionTemplate socketSessionTemplate;

    // 存储数据回调
    private final Map<Integer, DataCallback> dataCallbackMap = new ConcurrentHashMap<>();

    /**
     * 获取SSH连接服务
     */
    private ServerConnectionService getSshConnectionService() {
        return serverConnectionServices.stream()
                .filter(service -> "SSH".equals(service.getProtocol()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean connectSSH(MonitorSysGenServer server) {
        try {
            ServerConnectionService sshService = getSshConnectionService();
            if (sshService == null) {
                log.error("SSH服务不可用");
                return false;
            }

            boolean connected = sshService.connect(server);
            if (connected) {
                // 设置数据回调，将SSH输出发送到前端
                Integer serverId = server.getMonitorSysGenServerId();
                sshService.setDataCallback(serverId, data -> {
                    // 发送SSH输出数据到前端
                    ServerWebSocketMessage message = ServerWebSocketMessage.createSSHDataMessage(
                            serverId,
                            server.getMonitorSysGenServerName(),
                            data
                    );

                    try {
                        socketSessionTemplate.send(SocketTopics.Monitor.Server.TERMINAL, Json.toJson(message));
                    } catch (Exception e) {
                        log.error("发送SSH数据到前端失败: {}", e.getMessage(), e);
                    }
                });
                sshService.executeCommand(serverId, "\n");
                log.info("SSH连接建立成功: serverId={}, host={}", serverId, server.getMonitorSysGenServerHost());
            }

            return connected;
        } catch (Exception e) {
            log.error("建立SSH连接失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public void disconnectSSH(Integer serverId) {
        try {
            ServerConnectionService sshService = getSshConnectionService();
            if (sshService != null) {
                sshService.disconnect(serverId);
                // 清理数据回调
                dataCallbackMap.remove(serverId);
                log.info("SSH连接已断开: serverId={}", serverId);
            }
        } catch (Exception e) {
            log.error("断开SSH连接失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public boolean sendSSHInput(Integer serverId, String input) {
        try {
            ServerConnectionService sshService = getSshConnectionService();
            if (sshService == null) {
                log.error("SSH服务不可用");
                return false;
            }

            if (!sshService.isConnected(serverId)) {
                log.error("SSH连接不存在: serverId={}", serverId);
                return false;
            }

            sshService.sendData(serverId, input);
            log.debug("SSH输入发送成功: serverId={}, inputLength={}", serverId, input.length());
            return true;

        } catch (Exception e) {
            log.error("发送SSH输入失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean resizeSSHTerminal(Integer serverId, int cols, int rows) {
        try {
            ServerConnectionService sshService = getSshConnectionService();
            if (sshService == null) {
                log.error("SSH服务不可用");
                return false;
            }

            if (!sshService.isConnected(serverId)) {
                log.error("SSH连接不存在: serverId={}", serverId);
                return false;
            }

            sshService.resizeTerminal(serverId, cols, rows);
            log.debug("SSH终端大小调整成功: serverId={}, cols={}, rows={}", serverId, cols, rows);
            return true;

        } catch (Exception e) {
            log.error("调整SSH终端大小失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean isSSHConnected(Integer serverId) {
        try {
            ServerConnectionService sshService = getSshConnectionService();
            if (sshService == null) {
                return false;
            }
            return sshService.isConnected(serverId);
        } catch (Exception e) {
            log.error("检查SSH连接状态失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public String executeSSHCommand(Integer serverId, String command) {
        try {
            ServerConnectionService sshService = getSshConnectionService();
            if (sshService == null) {
                return "SSH服务不可用";
            }

            if (!sshService.isConnected(serverId)) {
                return "SSH连接不存在或已断开";
            }

            String result = sshService.executeCommand(serverId, command);
            log.debug("SSH命令执行完成: serverId={}, command={}", serverId, command);
            return result;

        } catch (Exception e) {
            log.error("执行SSH命令失败: {}", e.getMessage(), e);
            return "命令执行失败: " + e.getMessage();
        }
    }

    @Override
    public void setSSHDataCallback(Integer serverId, DataCallback callback) {
        dataCallbackMap.put(serverId, callback);

        // 同时设置到底层服务
        ServerConnectionService sshService = getSshConnectionService();
        if (sshService != null) {
            sshService.setDataCallback(serverId, callback::onData);
        }
    }
}
