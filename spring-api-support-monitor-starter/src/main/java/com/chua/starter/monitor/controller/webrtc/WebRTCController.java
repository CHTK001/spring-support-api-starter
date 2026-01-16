package com.chua.starter.monitor.controller.webrtc;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.service.impl.WebRTCServiceMonitor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * WebRTC Controller
 * <p>
 * 提供WebRTC客户端访问接口和连接信息
 * </p>
 *
 * @author CH
 * @since 2026/01/03
 */
@Slf4j
@RestController
@RequestMapping("/api/monitor/webrtc")
@ConditionalOnClass(name = "org.springframework.web.bind.annotation.RestController")
public class WebRTCController {

    @Autowired(required = false)
    private WebRTCServiceMonitor webrtcService;

    /**
     * 获取WebRTC连接信息
     *
     * @param serverId 服务器ID
     * @param host     服务器地址（可选）
     * @param port     服务器端口（可选）
     * @return 连接信息
     */
    @GetMapping("/connect")
    public ReturnResult<Map<String, Object>> getConnectInfo(
            @RequestParam(required = false) Integer serverId,
            @RequestParam(required = false) String host,
            @RequestParam(required = false) Integer port
    ) {
        if (webrtcService == null) {
            return ReturnResult.illegal("WebRTC服务未启用");
        }

        try {
            if (host == null || host.isEmpty()) {
                host = webrtcService.getDefaultHost();
            }
            if (port == null || port <= 0) {
                port = webrtcService.getDefaultPort();
            }

            String clientUrl = webrtcService.getClientUrl(serverId, host, port);
            String webSocketUrl = webrtcService.getWebSocketUrl(serverId, host, port);

            Map<String, Object> result = new HashMap<>();
            result.put("host", host);
            result.put("port", port);
            result.put("clientUrl", clientUrl);
            result.put("webSocketUrl", webSocketUrl);
            result.put("enabled", true);

            return ReturnResult.ok(result);
        } catch (Exception e) {
            log.error("[WebRTC][Controller] 获取连接信息失败", e);
            return ReturnResult.illegal("获取连接信息失败: " + e.getMessage());
        }
    }

    /**
     * 检查WebRTC服务状态
     *
     * @param serverId 服务器ID
     * @return 服务状态
     */
    @GetMapping("/status")
    public ReturnResult<Map<String, Object>> getStatus(@RequestParam(required = false) Integer serverId) {
        if (webrtcService == null) {
            return ReturnResult.ok(Map.of("enabled", false));
        }

        try {
            boolean running = serverId != null && webrtcService.isServiceRunning(serverId);
            Map<String, Object> result = new HashMap<>();
            result.put("enabled", true);
            result.put("running", running);
            result.put("defaultPort", webrtcService.getDefaultPort());
            result.put("defaultHost", webrtcService.getDefaultHost());

            return ReturnResult.ok(result);
        } catch (Exception e) {
            log.error("[WebRTC][Controller] 获取服务状态失败", e);
            return ReturnResult.illegal("获取服务状态失败: " + e.getMessage());
        }
    }
}

