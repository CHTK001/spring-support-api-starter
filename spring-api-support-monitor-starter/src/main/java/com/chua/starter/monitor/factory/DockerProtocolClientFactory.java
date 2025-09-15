package com.chua.starter.monitor.factory;

import com.chua.docker.support.client.DockerProtocolClient;
import com.chua.starter.monitor.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.enums.DockerConnectionType;
import com.chua.starter.monitor.service.MonitorSysGenServerSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Docker协议客户端工厂类
 * 根据服务器ID和Docker配置动态创建客户端实例
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/12/26
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DockerProtocolClientFactory {

    private final MonitorSysGenServerSettingService serverSettingService;

    /**
     * 客户端缓存，避免重复创建
     * Key: serverId, Value: DockerProtocolClient实例
     */
    private final ConcurrentMap<Integer, DockerProtocolClient> clientCache = new ConcurrentHashMap<>();

    /**
     * 根据服务器ID创建Docker协议客户端
     *
     * @param serverId 服务器ID
     * @return Docker协议客户端实例，如果配置无效或创建失败则返回null
     */
    public DockerProtocolClient createClient(Integer serverId) {
        if (serverId == null) {
            log.warn("服务器ID不能为空");
            return null;
        }

        // 先从缓存中获取
        DockerProtocolClient cachedClient = clientCache.get(serverId);
        if (cachedClient != null) {
            log.debug("从缓存中获取Docker客户端 - serverId: {}", serverId);
            return cachedClient;
        }

        // 获取服务器配置
        MonitorSysGenServerSetting setting = serverSettingService.getByServerId(serverId).getData();
        if (setting == null) {
            log.warn("未找到服务器配置 - serverId: {}", serverId);
            return null;
        }

        // 检查是否启用Docker
        if (setting.getMonitorSysGenServerSettingDockerEnabled() == null ||
                setting.getMonitorSysGenServerSettingDockerEnabled() != 1) {
            log.debug("服务器未启用Docker - serverId: {}", serverId);
            return null;
        }

        try {
            DockerProtocolClient client = createClientFromSetting(setting);
            if (client != null) {
                // 缓存客户端实例
                clientCache.put(serverId, client);
                log.info("成功创建Docker客户端 - serverId: {}, connectionType: {}",
                        serverId, setting.getMonitorSysGenServerSettingDockerConnectionType());
            }
            return client;
        } catch (Exception e) {
            log.error("创建Docker客户端失败 - serverId: {}, error: {}", serverId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 根据配置创建Docker客户端
     *
     * @param setting 服务器配置
     * @return Docker协议客户端实例
     */
    private DockerProtocolClient createClientFromSetting(MonitorSysGenServerSetting setting) {
        String connectionType = setting.getMonitorSysGenServerSettingDockerConnectionType();

        if (!StringUtils.hasText(connectionType)) {
            log.warn("Docker连接方式未配置 - serverId: {}", setting.getMonitorSysGenServerId());
            return null;
        }

        try {
            DockerConnectionType dockerConnectionType = DockerConnectionType.valueOf(connectionType.toUpperCase());

            switch (dockerConnectionType) {
                case TCP:
                    return createTcpClient(setting);
                case UNIX_SOCKET:
                    return createUnixSocketClient(setting);
                case LOCAL:
                    return createLocalClient(setting);
                default:
                    log.warn("不支持的Docker连接方式: {} - serverId: {}",
                            connectionType, setting.getMonitorSysGenServerId());
                    return null;
            }
        } catch (IllegalArgumentException e) {
            log.error("无效的Docker连接方式: {} - serverId: {}",
                    connectionType, setting.getMonitorSysGenServerId());
            return null;
        }
    }

    /**
     * 创建TCP连接的Docker客户端
     *
     * @param setting 服务器配置
     * @return Docker协议客户端实例
     */
    private DockerProtocolClient createTcpClient(MonitorSysGenServerSetting setting) {
        String host = setting.getMonitorSysGenServerSettingDockerHost();
        Integer port = setting.getMonitorSysGenServerSettingDockerPort();

        if (!StringUtils.hasText(host) || port == null) {
            log.warn("TCP连接配置不完整 - serverId: {}, host: {}, port: {}",
                    setting.getMonitorSysGenServerId(), host, port);
            return null;
        }

        String dockerHost = String.format("tcp://%s:%d", host, port);

        // 检查是否启用TLS
        boolean tlsEnabled = setting.getMonitorSysGenServerSettingDockerTlsEnabled() != null &&
                setting.getMonitorSysGenServerSettingDockerTlsEnabled() == 1;

        if (tlsEnabled) {
            String certPath = setting.getMonitorSysGenServerSettingDockerCertPath();
            if (StringUtils.hasText(certPath)) {
                log.debug("创建TLS TCP Docker客户端 - host: {}, certPath: {}", dockerHost, certPath);
                return DockerProtocolClient.of(dockerHost);
            } else {
                log.warn("启用TLS但未配置证书路径 - serverId: {}", setting.getMonitorSysGenServerId());
                return null;
            }
        } else {
            log.debug("创建TCP Docker客户端 - host: {}", dockerHost);
            return DockerProtocolClient.of(dockerHost);
        }
    }

    /**
     * 创建Unix Socket连接的Docker客户端
     *
     * @param setting 服务器配置
     * @return Docker协议客户端实例
     */
    private DockerProtocolClient createUnixSocketClient(MonitorSysGenServerSetting setting) {
        String socketPath = setting.getMonitorSysGenServerSettingDockerSocketPath();

        if (!StringUtils.hasText(socketPath)) {
            log.warn("Unix Socket路径未配置 - serverId: {}", setting.getMonitorSysGenServerId());
            return null;
        }

        String dockerHost = "unix://" + socketPath;
        log.debug("创建Unix Socket Docker客户端 - socketPath: {}", dockerHost);
        return DockerProtocolClient.of(dockerHost);
    }

    /**
     * 创建本地连接的Docker客户端
     *
     * @param setting 服务器配置
     * @return Docker协议客户端实例
     */
    private DockerProtocolClient createLocalClient(MonitorSysGenServerSetting setting) {
        log.debug("创建本地Docker客户端 - serverId: {}", setting.getMonitorSysGenServerId());
        // 本地连接使用默认配置
        return DockerProtocolClient.of();
    }


    /**
     * 清除指定服务器的客户端缓存
     *
     * @param serverId 服务器ID
     */
    public void clearCache(Integer serverId) {
        if (serverId != null) {
            // 清除基础客户端缓存
            DockerProtocolClient removed = clientCache.remove(serverId);
            if (removed != null) {
                log.info("清除Docker客户端缓存 - serverId: {}", serverId);
                try {
                    // 关闭客户端连接
                    removed.close();
                } catch (Exception e) {
                    log.warn("关闭Docker客户端连接失败 - serverId: {}, error: {}", serverId, e.getMessage());
                }
            }
        }
    }

    /**
     * 清除所有客户端缓存
     */
    public void clearAllCache() {
        log.info("清除所有Docker客户端缓存");

        // 清除基础客户端缓存
        clientCache.forEach((serverId, client) -> {
            try {
                client.close();
            } catch (Exception e) {
                log.warn("关闭Docker客户端连接失败 - serverId: {}, error: {}", serverId, e.getMessage());
            }
        });
        clientCache.clear();
    }

    /**
     * 获取缓存中的客户端数量
     *
     * @return 缓存中的客户端数量
     */
    public int getCacheSize() {
        return clientCache.size();
    }

    /**
     * 检查指定服务器是否有缓存的客户端
     *
     * @param serverId 服务器ID
     * @return 是否有缓存的客户端
     */
    public boolean hasCachedClient(Integer serverId) {
        return serverId != null && clientCache.containsKey(serverId);
    }
}