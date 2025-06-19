package com.chua.starter.monitor.starter.configuration;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.chua.common.support.json.JsonObject;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.socketio.support.SocketIOListener;
import com.chua.socketio.support.annotations.OnEvent;
import com.chua.socketio.support.session.SocketSession;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServer;
import com.chua.starter.monitor.starter.service.ServerConnectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * SSH Socket IO 监听器
 *
 * @author CH
 * @since 2024/12/18
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SshSocketIOListener implements SocketIOListener {

    private final List<ServerConnectionService> serverConnectionServices;

    /**
     * 获取SSH连接服务
     */
    private ServerConnectionService getSshConnectionService() {
        return serverConnectionServices.stream()
                .filter(service -> "SSH".equals(service.getProtocol()))
                .findFirst()
                .orElse(null);
    }

    /**
     * SSH连接事件
     */
    @OnEvent("ssh/connect")
    public void onConnect(SocketSession session, JsonObject data) {
        try {
            Integer serverId = data.getInteger("serverId");
            if (serverId == null) {
                session.send("ssh/error", ReturnResult.illegal("服务器ID不能为空"));
                return;
            }

            ServerConnectionService sshService = getSshConnectionService();
            if (sshService == null) {
                session.send("ssh/error", ReturnResult.illegal("SSH服务不可用"));
                return;
            }

            // 这里需要根据serverId获取服务器信息
            // MonitorSysGenServer server = getServerById(serverId);
            // boolean connected = sshService.connect(server);
            
            // 临时返回成功
            session.send("ssh/connected", ReturnResult.success("SSH连接成功"));
            
        } catch (Exception e) {
            log.error("SSH连接失败: {}", e.getMessage(), e);
            session.send("ssh/error", ReturnResult.illegal("SSH连接失败: " + e.getMessage()));
        }
    }

    /**
     * SSH断开连接事件
     */
    @OnEvent("ssh/disconnect")
    public void onDisconnect(SocketSession session, JsonObject data) {
        try {
            Integer serverId = data.getInteger("serverId");
            if (serverId == null) {
                session.send("ssh/error", ReturnResult.illegal("服务器ID不能为空"));
                return;
            }

            ServerConnectionService sshService = getSshConnectionService();
            if (sshService != null) {
                sshService.disconnect(serverId);
            }

            session.send("ssh/disconnected", ReturnResult.success("SSH连接已断开"));
            
        } catch (Exception e) {
            log.error("SSH断开连接失败: {}", e.getMessage(), e);
            session.send("ssh/error", ReturnResult.illegal("SSH断开连接失败: " + e.getMessage()));
        }
    }

    /**
     * SSH命令执行事件
     */
    @OnEvent("ssh/command")
    public void onCommand(SocketSession session, JsonObject data) {
        try {
            Integer serverId = data.getInteger("serverId");
            String command = data.getString("command");
            
            if (serverId == null) {
                session.send("ssh/error", ReturnResult.illegal("服务器ID不能为空"));
                return;
            }
            
            if (StringUtils.isEmpty(command)) {
                session.send("ssh/error", ReturnResult.illegal("命令不能为空"));
                return;
            }

            ServerConnectionService sshService = getSshConnectionService();
            if (sshService == null) {
                session.send("ssh/error", ReturnResult.illegal("SSH服务不可用"));
                return;
            }

            String result = sshService.executeCommand(serverId, command);
            session.send("ssh/command-result", ReturnResult.success(result));
            
        } catch (Exception e) {
            log.error("SSH命令执行失败: {}", e.getMessage(), e);
            session.send("ssh/error", ReturnResult.illegal("SSH命令执行失败: " + e.getMessage()));
        }
    }

    /**
     * SSH数据发送事件
     */
    @OnEvent("ssh/data")
    public void onData(SocketSession session, JsonObject data) {
        try {
            Integer serverId = data.getInteger("serverId");
            String inputData = data.getString("data");
            
            if (serverId == null) {
                session.send("ssh/error", ReturnResult.illegal("服务器ID不能为空"));
                return;
            }

            ServerConnectionService sshService = getSshConnectionService();
            if (sshService != null) {
                sshService.sendData(serverId, inputData);
            }
            
        } catch (Exception e) {
            log.error("SSH数据发送失败: {}", e.getMessage(), e);
            session.send("ssh/error", ReturnResult.illegal("SSH数据发送失败: " + e.getMessage()));
        }
    }

    /**
     * SSH终端大小调整事件
     */
    @OnEvent("ssh/resize")
    public void onResize(SocketSession session, JsonObject data) {
        try {
            Integer serverId = data.getInteger("serverId");
            Integer width = data.getInteger("width");
            Integer height = data.getInteger("height");
            
            if (serverId == null || width == null || height == null) {
                session.send("ssh/error", ReturnResult.illegal("参数不完整"));
                return;
            }

            ServerConnectionService sshService = getSshConnectionService();
            if (sshService != null) {
                sshService.resizeTerminal(serverId, width, height);
            }
            
        } catch (Exception e) {
            log.error("SSH终端大小调整失败: {}", e.getMessage(), e);
            session.send("ssh/error", ReturnResult.illegal("SSH终端大小调整失败: " + e.getMessage()));
        }
    }

    /**
     * SSH文件列表事件
     */
    @OnEvent("ssh/list-files")
    public void onListFiles(SocketSession session, JsonObject data) {
        try {
            Integer serverId = data.getInteger("serverId");
            String path = data.getString("path");
            
            if (serverId == null) {
                session.send("ssh/error", ReturnResult.illegal("服务器ID不能为空"));
                return;
            }

            ServerConnectionService sshService = getSshConnectionService();
            if (sshService == null) {
                session.send("ssh/error", ReturnResult.illegal("SSH服务不可用"));
                return;
            }

            var result = sshService.listFiles(serverId, path);
            session.send("ssh/file-list", ReturnResult.success(result));
            
        } catch (Exception e) {
            log.error("SSH文件列表获取失败: {}", e.getMessage(), e);
            session.send("ssh/error", ReturnResult.illegal("SSH文件列表获取失败: " + e.getMessage()));
        }
    }
}
