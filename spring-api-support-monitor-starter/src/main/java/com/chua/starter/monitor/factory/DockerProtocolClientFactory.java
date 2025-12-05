package com.chua.starter.monitor.factory;

import com.chua.common.support.lang.exception.NotSupportedException;
import com.chua.common.support.protocol.ClientSetting;
import com.chua.common.support.utils.ObjectUtils;
import com.chua.docker.support.client.CmdDockerProtocolClient;
import com.chua.docker.support.client.DockerProtocolClient;
import com.chua.starter.monitor.entity.MonitorSysGenServer;
import com.chua.starter.monitor.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.enums.DockerConnectionType;
import com.chua.starter.monitor.service.MonitorSysGenServerService;
import com.chua.starter.monitor.service.MonitorSysGenServerSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.chua.starter.monitor.pojo.DockerClientCreateResult;

import java.util.List;
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
    private final MonitorSysGenServerService serverService;

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
        if (cachedClient != null && cachedClient.isConnected()) {
            log.debug("从缓存中获取Docker客户端 - serverId: {}", serverId);
            return cachedClient;
        }

        // 获取服务器配置
        MonitorSysGenServerSetting setting = serverSettingService.getByServerId(serverId).getData();
        if (setting == null) {
            log.warn("未找到服务器配置 - serverId: {}", serverId);
            return null;
        }

        // 获取服务器名称用于错误提示
        MonitorSysGenServer server = serverService.getByServerId(serverId);
        String serverName = server != null ? server.getMonitorSysGenServerName() : "未知服务器";

        // 检查是否启用Docker
        if (setting.getMonitorSysGenServerSettingDockerEnabled() == null ||
                setting.getMonitorSysGenServerSettingDockerEnabled() != 1) {
            log.debug("服务器未启用Docker - serverId: {}, serverName: {}", serverId, serverName);
            throw new NotSupportedException(String.format("服务器[%s](ID:%d)未启用Docker", serverName, serverId));
        }

        try {
            DockerProtocolClient client = createClientFromSetting(setting);
            if (client != null) {
                client.connect();
                // 缓存客户端实例
                clientCache.put(serverId, client);
                log.info("成功创建Docker客户端 - serverId: {}, serverName: {}, connectionType: {}",
                        serverId, serverName, setting.getMonitorSysGenServerSettingDockerConnectionType());
            }
            return client;
        } catch (Exception e) {
            log.error("创建Docker客户端失败 - serverId: {}, serverName: {}, error: {}", serverId, serverName, e.getMessage(), e);
            throw new RuntimeException(String.format("服务器[%s](ID:%d)连接失败: %s", serverName, serverId, e.getMessage()), e);
        }
    }

    /**
     * 批量创建Docker协议客户端
     * 返回详细的创建结果，包括成功、失败、未启用等状态
     *
     * @param serverIds 服务器ID列表
     * @return 批量创建结果
     */
    public DockerClientCreateResult createClients(List<Integer> serverIds) {
        DockerClientCreateResult result = new DockerClientCreateResult();

        if (serverIds == null || serverIds.isEmpty()) {
            log.warn("服务器ID列表为空");
            return result;
        }

        for (Integer serverId : serverIds) {
            if (serverId == null) {
                continue;
            }

            try {
                // 先从缓存中获取
                DockerProtocolClient cachedClient = clientCache.get(serverId);
                if (cachedClient != null && cachedClient.isConnected()) {
                    result.addSuccess(serverId, cachedClient);
                    continue;
                }

                // 获取服务器配置
                MonitorSysGenServerSetting setting = serverSettingService.getByServerId(serverId).getData();
                if (setting == null) {
                    log.warn("未找到服务器配置 - serverId: {}", serverId);
                    result.addNotFound(serverId);
                    continue;
                }

                // 获取服务器名称
                MonitorSysGenServer server = serverService.getByServerId(serverId);
                String serverName = server != null ? server.getMonitorSysGenServerName() : "未知服务器(ID:" + serverId + ")";

                // 检查是否启用Docker
                if (setting.getMonitorSysGenServerSettingDockerEnabled() == null ||
                        setting.getMonitorSysGenServerSettingDockerEnabled() != 1) {
                    log.debug("服务器未启用Docker - serverId: {}, serverName: {}", serverId, serverName);
                    result.addDisabled(serverId, serverName);
                    continue;
                }

                // 创建客户端
                DockerProtocolClient client = createClientFromSetting(setting);
                if (client != null) {
                    client.connect();
                    clientCache.put(serverId, client);
                    result.addSuccess(serverId, client);
                    log.info("成功创建Docker客户端 - serverId: {}, serverName: {}", serverId, serverName);
                } else {
                    result.addFailed(serverId, serverName, "创建客户端返回null");
                }

            } catch (Exception e) {
                // 获取服务器名称用于错误信息
                MonitorSysGenServer server = serverService.getByServerId(serverId);
                String serverName = server != null ? server.getMonitorSysGenServerName() : "未知服务器";
                log.error("创建Docker客户端失败 - serverId: {}, serverName: {}, error: {}", serverId, serverName, e.getMessage());
                result.addFailed(serverId, serverName, e.getMessage());
            }
        }

        log.info("批量创建Docker客户端完成 - {}", result.getSummary());
        return result;
    }

    /**
     * 根据配置创建Docker客户端
     * <p>
     * 支持以下连接方式：
     * <ul>
     *   <li>TCP/API - TCP远程连接Docker守护进程</li>
     *   <li>UNIX_SOCKET/SOCKET - Unix Socket本地连接</li>
     *   <li>LOCAL/SHELL - 通过SSH执行docker命令（使用CmdDockerProtocolClient）</li>
     * </ul>
     * </p>
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

            return switch (dockerConnectionType) {
                case TCP, API -> createTcpClient(setting);
                case UNIX_SOCKET, SOCKET -> createUnixSocketClient(setting);
                case LOCAL, SHELL -> createShellClient(setting);
            };
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

        ClientSetting clientSetting = ClientSetting.builder()
                .url(dockerHost)
                .sslEnabled(tlsEnabled)
                .sslCertPath(setting.getMonitorSysGenServerSettingDockerCertPath())
                .username(setting.getMonitorSysGenServerSettingDockerUsername())
                .password(setting.getMonitorSysGenServerSettingDockerPassword())
                .connectTimeoutMillis(ObjectUtils.defaultIfNull(setting.getMonitorSysGenServerSettingDockerConnectTimeoutMillis(), 30_000))
                .build();
        log.debug("创建TCP Docker客户端 - host: {}", dockerHost);
        return DockerProtocolClient.of(clientSetting);
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
     * 使用CMD方式通过SSH执行docker命令
     * <p>
     * 注意：SSH连接信息（主机、端口、用户名、密码）来自MonitorSysGenServer表，
     * 而不是MonitorSysGenServerSetting表
     * </p>
     *
     * @param setting 服务器配置
     * @return Docker协议客户端实例，如果配置不完整则返回null
     */
    private DockerProtocolClient createShellClient(MonitorSysGenServerSetting setting) {
        Integer serverId = setting.getMonitorSysGenServerId();

        // 获取服务器基本信息（包含SSH连接配置）- 使用缓存方法
        MonitorSysGenServer server = serverService.getByServerId(serverId);
        if (server == null) {
            log.warn("服务器信息不存在 - serverId: {}", serverId);
            return null;
        }

        // 从服务器配置中获取SSH连接信息
        String host = server.getMonitorSysGenServerHost();
        String username = server.getMonitorSysGenServerUsername();
        String password = server.getMonitorSysGenServerPassword();
        Integer port = server.getMonitorSysGenServerPort();

        // 验证SSH连接必需参数
        if (!StringUtils.hasText(host)) {
            log.warn("服务器主机地址未配置 - serverId: {}, serverName: {}",
                    serverId, server.getMonitorSysGenServerName());
            return null;
        }

        if (!StringUtils.hasText(username)) {
            log.warn("服务器用户名未配置 - serverId: {}, host: {}",
                    serverId, host);
            return null;
        }

        if (!StringUtils.hasText(password)) {
            log.warn("服务器密码未配置 - serverId: {}, host: {}, username: {}",
                    serverId, host, username);
            return null;
        }

        // 端口默认值处理
        if (port == null || port <= 0) {
            port = 22;
            log.debug("使用默认SSH端口22 - serverId: {}", serverId);
        }

        log.debug("创建本地Docker客户端(CMD方式) - serverId: {}, serverName: {}, host: {}, port: {}, username: {}",
                serverId, server.getMonitorSysGenServerName(), host, port, username);

        // 构建SSH连接配置
        // 超时时间优先使用server配置，其次使用setting配置
        Integer timeout = server.getMonitorSysGenServerTimeout();
        if (timeout == null || timeout <= 0) {
            timeout = ObjectUtils.defaultIfNull(
                    setting.getMonitorSysGenServerSettingDockerConnectTimeoutMillis(), 30_000);
        }

        ClientSetting clientSetting = ClientSetting.builder()
                .host(host)
                .port(port)
                .username(username)
                .password(password)
                .connectTimeoutMillis(timeout)
                .build();

        try {
            // 使用CMD Docker客户端
            CmdDockerProtocolClient client = CmdDockerProtocolClient.of(clientSetting);
            log.info("CMD Docker客户端创建成功 - serverId: {}, serverName: {}, 将通过SSH({}@{}:{})执行docker命令",
                    serverId, server.getMonitorSysGenServerName(), username, host, port);
            return client;
        } catch (Exception e) {
            log.error("创建CMD Docker客户端失败 - serverId: {}, host: {}, error: {}",
                    serverId, host, e.getMessage(), e);
            return null;
        }
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
