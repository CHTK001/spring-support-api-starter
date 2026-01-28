package com.chua.starter.monitor.controller.remote;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.service.impl.RemoteDesktopServiceMonitor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 远程桌面 Controller
 * <p>
 * 提供远程桌面服务器管理接口和连接信息
 * </p>
 *
 * @author CH
 * @since 2026/01/24
 */
@Slf4j
@RestController
@RequestMapping("/api/monitor/remote-desktop")
@ConditionalOnClass(name = "org.springframework.web.bind.annotation.RestController")
public class RemoteDesktopController {

    @Autowired(required = false)
    private RemoteDesktopServiceMonitor remoteDesktopService;

    /**
     * 启动远程桌面服务
     *
     * @param serverId 服务器ID
     * @param websocketPort WebSocket端口（可选）
     * @param tcpPort TCP端口（可选）
     * @return 启动结果
     */
    @PostMapping("/start")
    public ReturnResult<Map<String, Object>> startService(
            @RequestParam(required = false) Integer serverId,
            @RequestParam(required = false) Integer websocketPort,
            @RequestParam(required = false) Integer tcpPort
    ) {
        if (remoteDesktopService == null) {
            return ReturnResult.illegal("远程桌面服务未启用");
        }

        if (serverId == null) {
            return ReturnResult.illegal("服务器ID不能为空");
        }

        try {
            var handles = remoteDesktopService.startService(serverId, websocketPort, tcpPort);
            
            if (handles.isEmpty()) {
                return ReturnResult.illegal("远程桌面服务启动失败");
            }

            var wsPort = websocketPort != null ? websocketPort : remoteDesktopService.getDefaultWebSocketPort();
            var host = remoteDesktopService.getDefaultHost();
            var wsUrl = remoteDesktopService.getWebSocketUrl(serverId, host, wsPort);

            Map<String, Object> result = new HashMap<>();
            result.put("serverId", serverId);
            result.put("handles", handles);
            result.put("websocketUrl", wsUrl);
            result.put("websocketPort", wsPort);
            result.put("host", host);
            result.put("enabled", true);

            return ReturnResult.ok(result);
        } catch (Exception e) {
            log.error("[远程桌面][Controller] 启动服务失败", e);
            return ReturnResult.illegal("启动服务失败: " + e.getMessage());
        }
    }

    /**
     * 停止远程桌面服务
     *
     * @param serverId 服务器ID
     * @param protocol 协议名称（可选，为null时停止所有协议）
     * @return 停止结果
     */
    @PostMapping("/stop")
    public ReturnResult<Map<String, Object>> stopService(
            @RequestParam Integer serverId,
            @RequestParam(required = false) String protocol
    ) {
        if (remoteDesktopService == null) {
            return ReturnResult.illegal("远程桌面服务未启用");
        }

        try {
            remoteDesktopService.stopService(serverId, protocol);
            
            Map<String, Object> result = new HashMap<>();
            result.put("serverId", serverId);
            result.put("protocol", protocol);
            result.put("stopped", true);

            return ReturnResult.ok(result);
        } catch (Exception e) {
            log.error("[远程桌面][Controller] 停止服务失败", e);
            return ReturnResult.illegal("停止服务失败: " + e.getMessage());
        }
    }

    /**
     * 获取远程桌面连接信息
     *
     * @param serverId 服务器ID
     * @param host     服务器地址（可选）
     * @param websocketPort WebSocket端口（可选）
     * @return 连接信息
     */
    @GetMapping("/connect")
    public ReturnResult<Map<String, Object>> getConnectInfo(
            @RequestParam(required = false) Integer serverId,
            @RequestParam(required = false) String host,
            @RequestParam(required = false) Integer websocketPort
    ) {
        if (remoteDesktopService == null) {
            return ReturnResult.illegal("远程桌面服务未启用");
        }

        try {
            if (host == null || host.isEmpty()) {
                host = remoteDesktopService.getDefaultHost();
            }
            if (websocketPort == null || websocketPort <= 0) {
                websocketPort = remoteDesktopService.getDefaultWebSocketPort();
            }

            var wsUrl = remoteDesktopService.getWebSocketUrl(serverId, host, websocketPort);
            var running = serverId != null && remoteDesktopService.isServiceRunning(serverId, null);

            Map<String, Object> result = new HashMap<>();
            result.put("host", host);
            result.put("websocketPort", websocketPort);
            result.put("websocketUrl", wsUrl);
            result.put("running", running);
            result.put("enabled", true);

            return ReturnResult.ok(result);
        } catch (Exception e) {
            log.error("[远程桌面][Controller] 获取连接信息失败", e);
            return ReturnResult.illegal("获取连接信息失败: " + e.getMessage());
        }
    }

    /**
     * 检查远程桌面服务状态
     *
     * @param serverId 服务器ID
     * @param protocol 协议名称（可选）
     * @return 服务状态
     */
    @GetMapping("/status")
    public ReturnResult<Map<String, Object>> getStatus(
            @RequestParam(required = false) Integer serverId,
            @RequestParam(required = false) String protocol
    ) {
        if (remoteDesktopService == null) {
            return ReturnResult.ok(Map.of("enabled", false));
        }

        try {
            var running = serverId != null && remoteDesktopService.isServiceRunning(serverId, protocol);
            
            Map<String, Object> result = new HashMap<>();
            result.put("enabled", true);
            result.put("running", running);
            result.put("defaultWebSocketPort", remoteDesktopService.getDefaultWebSocketPort());
            result.put("defaultTcpPort", remoteDesktopService.getDefaultTcpPort());
            result.put("defaultHost", remoteDesktopService.getDefaultHost());

            return ReturnResult.ok(result);
        } catch (Exception e) {
            log.error("[远程桌面][Controller] 获取服务状态失败", e);
            return ReturnResult.illegal("获取服务状态失败: " + e.getMessage());
        }
    }
}

