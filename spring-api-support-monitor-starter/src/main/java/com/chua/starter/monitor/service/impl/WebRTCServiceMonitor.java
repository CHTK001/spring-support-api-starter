package com.chua.starter.monitor.service.impl;

import com.chua.common.support.core.spi.ServiceProvider;
import com.chua.remote.support.webrtc.WebRTCService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebRTC服务监控实现
 * <p>
 * 在Spring环境中管理WebRTC服务生命周期，通过SPI获取WebRTC服务实现
 * </p>
 *
 * @author CH
 * @since 2026/01/03
 */
@Slf4j
@Service
@ConditionalOnClass(name = "org.springframework.web.bind.annotation.RestController")
public class WebRTCServiceMonitor {

    /**
     * WebRTC服务端口
     */
    @Value("${monitor.webrtc.port:21116}")
    private int port;

    /**
     * 服务器地址
     */
    @Value("${monitor.webrtc.host:localhost}")
    private String host;

    /**
     * WebRTC服务实现（通过SPI获取）
     */
    private WebRTCService webrtcService;

    /**
     * 服务句柄映射
     */
    private final Map<Integer, Long> serverHandleMap = new ConcurrentHashMap<>();

    /**
     * 初始化WebRTC服务
     */
    @PostConstruct
    public void init() {
        try {
            var provider = ServiceProvider.of(WebRTCService.class);
            this.webrtcService = provider.getExtension();
            log.info("[WebRTC][Monitor] WebRTC服务初始化成功");
        } catch (Exception e) {
            log.warn("[WebRTC][Monitor] 无法通过SPI获取WebRTC服务实现: {}", e.getMessage());
        }
    }

    /**
     * 销毁WebRTC服务
     */
    @PreDestroy
    public void destroy() {
        if (webrtcService != null) {
            serverHandleMap.values().forEach(handle -> {
                if (webrtcService.isRunning(handle)) {
                    webrtcService.stop(handle);
                }
            });
            serverHandleMap.clear();
            log.info("[WebRTC][Monitor] WebRTC服务已停止");
        }
    }

    /**
     * 启动WebRTC服务
     *
     * @param serverId 服务器ID
     * @param port     服务端口
     * @return 服务句柄，0表示失败
     */
    public long startService(Integer serverId, int port) {
        if (webrtcService == null) {
            log.error("[WebRTC][Monitor] WebRTC服务未初始化");
            return 0;
        }

        try {
            long handle = webrtcService.start(port);
            if (handle != 0) {
                serverHandleMap.put(serverId, handle);
                log.info("[WebRTC][Monitor] WebRTC服务启动成功: serverId={}, handle={}, port={}", serverId, handle, port);
            } else {
                log.error("[WebRTC][Monitor] WebRTC服务启动失败: serverId={}, port={}", serverId, port);
            }
            return handle;
        } catch (Exception e) {
            log.error("[WebRTC][Monitor] WebRTC服务启动异常: serverId={}, port={}", serverId, port, e);
            return 0;
        }
    }

    /**
     * 停止WebRTC服务
     *
     * @param serverId 服务器ID
     */
    public void stopService(Integer serverId) {
        if (webrtcService == null) {
            return;
        }

        Long handle = serverHandleMap.remove(serverId);
        if (handle != null && webrtcService.isRunning(handle)) {
            webrtcService.stop(handle);
            log.info("[WebRTC][Monitor] WebRTC服务已停止: serverId={}, handle={}", serverId, handle);
        }
    }

    /**
     * 检查服务是否运行中
     *
     * @param serverId 服务器ID
     * @return true表示运行中
     */
    public boolean isServiceRunning(Integer serverId) {
        if (webrtcService == null) {
            return false;
        }

        Long handle = serverHandleMap.get(serverId);
        return handle != null && webrtcService.isRunning(handle);
    }

    /**
     * 获取客户端URL
     *
     * @param serverId 服务器ID
     * @param host     服务器地址
     * @param port     服务器端口
     * @return 客户端URL
     */
    public String getClientUrl(Integer serverId, String host, int port) {
        if (webrtcService == null) {
            return null;
        }
        return webrtcService.getClientUrl(host, port);
    }

    /**
     * 获取WebSocket URL
     *
     * @param serverId 服务器ID
     * @param host      服务器地址
     * @param port      服务器端口
     * @return WebSocket URL
     */
    public String getWebSocketUrl(Integer serverId, String host, int port) {
        if (webrtcService == null) {
            return null;
        }
        return webrtcService.getWebSocketUrl(host, port);
    }

    /**
     * 获取默认端口
     *
     * @return 默认端口
     */
    public int getDefaultPort() {
        return port;
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

