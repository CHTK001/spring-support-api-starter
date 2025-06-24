package com.chua.starter.monitor.starter.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.StringUtils;
import com.chua.socketio.support.session.SocketSessionTemplate;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServer;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerMetrics;
import com.chua.starter.monitor.starter.entity.ServerWebSocketMessage;
import com.chua.starter.monitor.starter.mapper.MonitorSysGenServerMapper;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerService;
import com.chua.starter.monitor.starter.service.ServerConnectionService;
import com.chua.starter.monitor.starter.websocket.ServerConnectionWebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 服务器管理服务实现类
 *
 * @author CH
 * @since 2024/12/18
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MonitorSysGenServerServiceImpl extends ServiceImpl<MonitorSysGenServerMapper, MonitorSysGenServer>
        implements MonitorSysGenServerService {

    private final SocketSessionTemplate socketSessionTemplate;
    private final List<ServerConnectionService> connectionServices;
    @Autowired
    @Lazy
    private ServerConnectionWebSocketService webSocketService;

    /**
     * 重写updateById方法，在特定字段变更时关闭设备连接
     * 如果修改了地址、端口、账号、密码这四个字段中的任何一个，则关闭对应的client连接
     */
    @Override
    public boolean updateById(MonitorSysGenServer entity) {
        if (entity == null || entity.getMonitorSysGenServerId() == null) {
            return false;
        }

        try {
            // 获取更新前的服务器配置
            MonitorSysGenServer oldServer = this.getById(entity.getMonitorSysGenServerId());
            if (oldServer == null) {
                return false;
            }

            // 检查关键连接字段是否发生变化
            boolean needDisconnect = isConnectionFieldsChanged(oldServer, entity);

            // 如果关键字段发生变化，先断开连接
            if (needDisconnect) {
                try {
                    log.info("检测到服务器连接配置变更，断开现有连接: {}", oldServer.getMonitorSysGenServerName());
                    disconnect(String.valueOf(oldServer.getMonitorSysGenServerId()));
                } catch (Exception e) {
                    log.warn("断开服务器连接时发生异常: {}", e.getMessage(), e);
                    // 不阻止更新操作，只记录警告
                }
            }

            // 设置更新时间
            entity.setMonitorSysGenServerUpdateTime(LocalDateTime.now());

            // 执行更新操作
            boolean result = super.updateById(entity);

            if (result) {
                // 发送服务器更新消息
                sendServerMessage(entity, "server_updated", true, needDisconnect ? "连接配置已变更，已断开现有连接" : null);
                log.info("服务器配置更新成功: {}", entity.getMonitorSysGenServerName());
            }

            return result;
        } catch (Exception e) {
            log.error("更新服务器配置失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 检查连接相关字段是否发生变化
     * 检查字段：地址(host)、端口(port)、用户名(username)、密码(password)
     */
    private boolean isConnectionFieldsChanged(MonitorSysGenServer oldServer, MonitorSysGenServer newServer) {
        // 检查主机地址
        if (!Objects.equals(oldServer.getMonitorSysGenServerHost(), newServer.getMonitorSysGenServerHost())) {
            log.debug("服务器主机地址发生变化: {} -> {}",
                    oldServer.getMonitorSysGenServerHost(), newServer.getMonitorSysGenServerHost());
            return true;
        }

        // 检查端口
        if (!Objects.equals(oldServer.getMonitorSysGenServerPort(), newServer.getMonitorSysGenServerPort())) {
            log.debug("服务器端口发生变化: {} -> {}",
                    oldServer.getMonitorSysGenServerPort(), newServer.getMonitorSysGenServerPort());
            return true;
        }

        // 检查用户名
        if (!Objects.equals(oldServer.getMonitorSysGenServerUsername(), newServer.getMonitorSysGenServerUsername())) {
            log.debug("服务器用户名发生变化: {} -> {}",
                    oldServer.getMonitorSysGenServerUsername(), newServer.getMonitorSysGenServerUsername());
            return true;
        }

        // 检查密码
        if (!Objects.equals(oldServer.getMonitorSysGenServerPassword(), newServer.getMonitorSysGenServerPassword())) {
            log.debug("服务器密码发生变化");
            return true;
        }

        return false;
    }

    @Override
    public IPage<MonitorSysGenServer> pageFor(Page<MonitorSysGenServer> page, MonitorSysGenServer entity) {
        return this.page(page, Wrappers.<MonitorSysGenServer>lambdaQuery()
                .eq(entity.getGenId() != null, MonitorSysGenServer::getGenId, entity.getGenId())
                .like(StringUtils.isNotEmpty(entity.getMonitorSysGenServerName()),
                        MonitorSysGenServer::getMonitorSysGenServerName, entity.getMonitorSysGenServerName())
                .like(StringUtils.isNotEmpty(entity.getMonitorSysGenServerHost()),
                        MonitorSysGenServer::getMonitorSysGenServerHost, entity.getMonitorSysGenServerHost())
                .eq(StringUtils.isNotEmpty(entity.getMonitorSysGenServerProtocol()),
                        MonitorSysGenServer::getMonitorSysGenServerProtocol, entity.getMonitorSysGenServerProtocol())
                .eq(entity.getMonitorSysGenServerStatus() != null,
                        MonitorSysGenServer::getMonitorSysGenServerStatus, entity.getMonitorSysGenServerStatus())
                .orderByDesc(MonitorSysGenServer::getMonitorSysGenServerCreateTime));
    }

    @Override
    public ReturnResult<Boolean> testConnection(String id) {
        if (StringUtils.isEmpty(id)) {
            return ReturnResult.error("服务器ID不能为空");
        }

        MonitorSysGenServer server = this.getById(id);
        if (server == null) {
            return ReturnResult.error("服务器配置不存在");
        }

        try {
            ServerConnectionService connectionService = getConnectionService(server.getMonitorSysGenServerProtocol());
            if (connectionService == null) {
                return ReturnResult.error("不支持的协议类型: " + server.getMonitorSysGenServerProtocol());
            }

            boolean testResult = connectionService.testConnection(server);

            // 发送测试结果消息
            sendServerMessage(server, "test", testResult, testResult ? null : "连接测试失败");
            server.setMonitorSysGenServerConnectionStatus(testResult ? 1 : 0);
            this.updateById(server);

            return ReturnResult.of(testResult, testResult ? "连接测试成功" : "连接测试失败");
        } catch (Exception e) {
            log.error("测试服务器连接失败: {}", e.getMessage(), e);
            sendServerMessage(server, "test", false, e.getMessage());
            return ReturnResult.error("连接测试失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> connect(String id) {
        if (StringUtils.isEmpty(id)) {
            return ReturnResult.error("服务器ID不能为空");
        }

        MonitorSysGenServer server = this.getById(id);
        if (server == null) {
            return ReturnResult.error("服务器配置不存在");
        }

        try {
            // 广播连接开始状态
            webSocketService.broadcastServerConnectionStatus(
                    server.getMonitorSysGenServerId(),
                    2, // CONNECTING状态
                    "正在建立连接...",
                    null
            );

            ServerConnectionService connectionService = getConnectionService(server.getMonitorSysGenServerProtocol());
            if (connectionService == null) {
                // 广播连接失败状态
                webSocketService.broadcastServerConnectionStatus(
                        server.getMonitorSysGenServerId(),
                        3, // FAILED状态
                        "不支持的协议类型: " + server.getMonitorSysGenServerProtocol(),
                        null
                );
                return ReturnResult.error("不支持的协议类型: " + server.getMonitorSysGenServerProtocol());
            }

            long startTime = System.currentTimeMillis();
            boolean connected = connectionService.connect(server);
            long responseTime = System.currentTimeMillis() - startTime;

            if (connected) {
                // 更新服务器连接状态
                server.setMonitorSysGenServerConnectionStatus(1); // ONLINE
                server.setMonitorSysGenServerLastConnectTime(LocalDateTime.now());
                server.setMonitorSysGenServerConnectionError(null);
                this.updateById(server);

                // 广播连接成功状态
                webSocketService.broadcastServerConnectionStatus(
                        server.getMonitorSysGenServerId(),
                        1, // ONLINE状态
                        "连接成功",
                        responseTime
                );

                log.info("服务器连接成功: {}", server.getMonitorSysGenServerName());
                return ReturnResult.ok(true, "连接成功");
            } else {
                // 更新服务器连接状态
                server.setMonitorSysGenServerConnectionStatus(3); // FAILED
                server.setMonitorSysGenServerConnectionError("连接失败");
                this.updateById(server);

                // 广播连接失败状态
                webSocketService.broadcastServerConnectionStatus(
                        server.getMonitorSysGenServerId(),
                        3, // FAILED状态
                        "连接失败",
                        responseTime
                );

                return ReturnResult.error("连接失败");
            }
        } catch (Exception e) {
            log.error("连接服务器失败: {}", e.getMessage(), e);

            // 更新服务器连接状态
            server.setMonitorSysGenServerConnectionStatus(3); // FAILED
            server.setMonitorSysGenServerConnectionError("连接异常: " + e.getMessage());
            this.updateById(server);

            // 广播连接异常状态
            webSocketService.broadcastServerConnectionStatus(
                    server.getMonitorSysGenServerId(),
                    3, // FAILED状态
                    "连接异常: " + e.getMessage(),
                    null
            );

            return ReturnResult.error("连接失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> disconnect(String id) {
        if (StringUtils.isEmpty(id)) {
            return ReturnResult.error("服务器ID不能为空");
        }

        MonitorSysGenServer server = this.getById(id);
        if (server == null) {
            return ReturnResult.error("服务器配置不存在");
        }

        try {
            ServerConnectionService connectionService = getConnectionService(server.getMonitorSysGenServerProtocol());
            if (connectionService == null) {
                return ReturnResult.error("不支持的协议类型: " + server.getMonitorSysGenServerProtocol());
            }

            connectionService.disconnect(server.getMonitorSysGenServerId());

            // 更新服务器连接状态
            server.setMonitorSysGenServerConnectionStatus(0); // OFFLINE
            server.setMonitorSysGenServerConnectionError(null);
            this.updateById(server);

            // 广播断开连接状态
            webSocketService.broadcastServerConnectionStatus(
                    server.getMonitorSysGenServerId(),
                    0, // OFFLINE状态
                    "连接已断开",
                    null
            );

            log.info("服务器连接已断开: {}", server.getMonitorSysGenServerName());
            return ReturnResult.ok(true, "断开连接成功");
        } catch (Exception e) {
            log.error("断开服务器连接失败: {}", e.getMessage(), e);

            // 广播断开连接失败状态
            webSocketService.broadcastServerConnectionStatus(
                    server.getMonitorSysGenServerId(),
                    3, // FAILED状态
                    "断开连接失败: " + e.getMessage(),
                    null
            );

            return ReturnResult.error("断开连接失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> getConnectionStatus(String id) {
        if (StringUtils.isEmpty(id)) {
            return ReturnResult.error("服务器ID不能为空");
        }

        MonitorSysGenServer server = this.getById(id);
        if (server == null) {
            return ReturnResult.error("服务器配置不存在");
        }

        try {
            ServerConnectionService connectionService = getConnectionService(server.getMonitorSysGenServerProtocol());
            if (connectionService == null) {
                return ReturnResult.error("不支持的协议类型: " + server.getMonitorSysGenServerProtocol());
            }

            Map<String, Object> status = new HashMap<>();
            status.put("serverId", server.getMonitorSysGenServerId());
            status.put("serverName", server.getMonitorSysGenServerName());
            status.put("host", server.getMonitorSysGenServerHost());
            status.put("port", server.getMonitorSysGenServerPort());
            status.put("protocol", server.getMonitorSysGenServerProtocol());
            status.put("connected", connectionService.isConnected(server.getMonitorSysGenServerId()));
            status.put("sessionId", connectionService.getSessionId(server.getMonitorSysGenServerId()));
            status.put("connectionStatus", server.getMonitorSysGenServerConnectionStatus());
            status.put("statusDesc", getStatusDesc(server.getMonitorSysGenServerConnectionStatus()));
            status.put("lastConnectTime", server.getMonitorSysGenServerLastConnectTime());
            status.put("connectionError", server.getMonitorSysGenServerConnectionError());
            status.put("monitorEnabled", server.getMonitorSysGenServerMonitorEnabled());
            status.put("tags", server.getMonitorSysGenServerTags());
            status.put("timestamp", System.currentTimeMillis());

            return ReturnResult.ok(status);
        } catch (Exception e) {
            log.error("获取连接状态失败: {}", e.getMessage(), e);
            return ReturnResult.error("获取连接状态失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<String> executeCommand(String id, String command) {
        if (StringUtils.isEmpty(id)) {
            return ReturnResult.error("服务器ID不能为空");
        }

        if (StringUtils.isEmpty(command)) {
            return ReturnResult.error("命令不能为空");
        }

        MonitorSysGenServer server = this.getById(id);
        if (server == null) {
            return ReturnResult.error("服务器配置不存在");
        }

        try {
            ServerConnectionService connectionService = getConnectionService(server.getMonitorSysGenServerProtocol());
            if (connectionService == null) {
                return ReturnResult.error("不支持的协议类型: " + server.getMonitorSysGenServerProtocol());
            }

            String result = connectionService.executeCommand(server.getMonitorSysGenServerId(), command);
            return ReturnResult.ok(result);
        } catch (Exception e) {
            log.error("执行命令失败: {}", e.getMessage(), e);
            return ReturnResult.error("执行命令失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> getServerInfo(String id) {
        if (StringUtils.isEmpty(id)) {
            return ReturnResult.error("服务器ID不能为空");
        }

        MonitorSysGenServer server = this.getById(id);
        if (server == null) {
            return ReturnResult.error("服务器配置不存在");
        }

        try {
            ServerConnectionService connectionService = getConnectionService(server.getMonitorSysGenServerProtocol());
            if (connectionService == null) {
                return ReturnResult.error("不支持的协议类型: " + server.getMonitorSysGenServerProtocol());
            }

            Map<String, Object> serverInfo = connectionService.getServerInfo(server.getMonitorSysGenServerId());

            // 添加配置信息
            serverInfo.put("config", server);

            return ReturnResult.ok(serverInfo);
        } catch (Exception e) {
            log.error("获取服务器信息失败: {}", e.getMessage(), e);
            return ReturnResult.error("获取服务器信息失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> sendData(String id, String data) {
        if (StringUtils.isEmpty(id)) {
            return ReturnResult.error("服务器ID不能为空");
        }

        if (StringUtils.isEmpty(data)) {
            return ReturnResult.error("发送数据不能为空");
        }

        MonitorSysGenServer server = this.getById(id);
        if (server == null) {
            return ReturnResult.error("服务器配置不存在");
        }

        try {
            ServerConnectionService connectionService = getConnectionService(server.getMonitorSysGenServerProtocol());
            if (connectionService == null) {
                return ReturnResult.error("不支持的协议类型: " + server.getMonitorSysGenServerProtocol());
            }

            connectionService.sendData(server.getMonitorSysGenServerId(), data);
            return ReturnResult.ok(true, "数据发送成功");
        } catch (Exception e) {
            log.error("发送数据失败: {}", e.getMessage(), e);
            return ReturnResult.error("发送数据失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> resizeTerminal(String id, int width, int height) {
        if (StringUtils.isEmpty(id)) {
            return ReturnResult.error("服务器ID不能为空");
        }

        MonitorSysGenServer server = this.getById(id);
        if (server == null) {
            return ReturnResult.error("服务器配置不存在");
        }

        try {
            ServerConnectionService connectionService = getConnectionService(server.getMonitorSysGenServerProtocol());
            if (connectionService == null) {
                return ReturnResult.error("不支持的协议类型: " + server.getMonitorSysGenServerProtocol());
            }

            connectionService.resizeTerminal(server.getMonitorSysGenServerId(), width, height);
            return ReturnResult.ok(true, "终端大小调整成功");
        } catch (Exception e) {
            log.error("调整终端大小失败: {}", e.getMessage(), e);
            return ReturnResult.error("调整终端大小失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> listFiles(String id, String path) {
        if (StringUtils.isEmpty(id)) {
            return ReturnResult.error("服务器ID不能为空");
        }

        MonitorSysGenServer server = this.getById(id);
        if (server == null) {
            return ReturnResult.error("服务器配置不存在");
        }

        try {
            ServerConnectionService connectionService = getConnectionService(server.getMonitorSysGenServerProtocol());
            if (connectionService == null) {
                return ReturnResult.error("不支持的协议类型: " + server.getMonitorSysGenServerProtocol());
            }

            Map<String, Object> result = connectionService.listFiles(server.getMonitorSysGenServerId(), path);
            return ReturnResult.ok(result);
        } catch (Exception e) {
            log.error("获取文件列表失败: {}", e.getMessage(), e);
            return ReturnResult.error("获取文件列表失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> uploadFile(String id, String localPath, String remotePath) {
        if (StringUtils.isEmpty(id)) {
            return ReturnResult.error("服务器ID不能为空");
        }

        MonitorSysGenServer server = this.getById(id);
        if (server == null) {
            return ReturnResult.error("服务器配置不存在");
        }

        try {
            ServerConnectionService connectionService = getConnectionService(server.getMonitorSysGenServerProtocol());
            if (connectionService == null) {
                return ReturnResult.error("不支持的协议类型: " + server.getMonitorSysGenServerProtocol());
            }

            boolean result = connectionService.uploadFile(server.getMonitorSysGenServerId(), localPath, remotePath);
            return ReturnResult.of(result, result ? "文件上传成功" : "文件上传失败");
        } catch (Exception e) {
            log.error("上传文件失败: {}", e.getMessage(), e);
            return ReturnResult.error("上传文件失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> downloadFile(String id, String remotePath, String localPath) {
        if (StringUtils.isEmpty(id)) {
            return ReturnResult.error("服务器ID不能为空");
        }

        MonitorSysGenServer server = this.getById(id);
        if (server == null) {
            return ReturnResult.error("服务器配置不存在");
        }

        try {
            ServerConnectionService connectionService = getConnectionService(server.getMonitorSysGenServerProtocol());
            if (connectionService == null) {
                return ReturnResult.error("不支持的协议类型: " + server.getMonitorSysGenServerProtocol());
            }

            boolean result = connectionService.downloadFile(server.getMonitorSysGenServerId(), remotePath, localPath);
            return ReturnResult.of(result, result ? "文件下载成功" : "文件下载失败");
        } catch (Exception e) {
            log.error("下载文件失败: {}", e.getMessage(), e);
            return ReturnResult.error("下载文件失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> enableMonitoring(String id) {
        if (StringUtils.isEmpty(id)) {
            return ReturnResult.error("服务器ID不能为空");
        }

        MonitorSysGenServer server = this.getById(id);
        if (server == null) {
            return ReturnResult.error("服务器配置不存在");
        }

        try {
            server.setMonitorSysGenServerMonitorEnabled(1);
            server.setMonitorSysGenServerUpdateTime(LocalDateTime.now());
            boolean result = this.updateById(server);

            if (result) {
                // 发送监控启用消息
                sendServerMessage(server, "monitoring_enabled", true, null);
                log.info("服务器监控已启用: {}", server.getMonitorSysGenServerName());
            }

            return ReturnResult.of(result, result ? "监控启用成功" : "监控启用失败");
        } catch (Exception e) {
            log.error("启用服务器监控失败: {}", e.getMessage(), e);
            return ReturnResult.error("启用监控失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> disableMonitoring(String id) {
        if (StringUtils.isEmpty(id)) {
            return ReturnResult.error("服务器ID不能为空");
        }

        MonitorSysGenServer server = this.getById(id);
        if (server == null) {
            return ReturnResult.error("服务器配置不存在");
        }

        try {
            server.setMonitorSysGenServerMonitorEnabled(0);
            server.setMonitorSysGenServerUpdateTime(LocalDateTime.now());
            boolean result = this.updateById(server);

            if (result) {
                // 发送监控停用消息
                sendServerMessage(server, "monitoring_disabled", true, null);
                log.info("服务器监控已停用: {}", server.getMonitorSysGenServerName());
            }

            return ReturnResult.of(result, result ? "监控停用成功" : "监控停用失败");
        } catch (Exception e) {
            log.error("停用服务器监控失败: {}", e.getMessage(), e);
            return ReturnResult.error("停用监控失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> getMonitoringStatus(String id) {
        if (StringUtils.isEmpty(id)) {
            return ReturnResult.error("服务器ID不能为空");
        }

        MonitorSysGenServer server = this.getById(id);
        if (server == null) {
            return ReturnResult.error("服务器配置不存在");
        }

        try {
            Map<String, Object> status = new HashMap<>();
            status.put("serverId", server.getMonitorSysGenServerId());
            status.put("serverName", server.getMonitorSysGenServerName());
            status.put("monitorEnabled", server.getMonitorSysGenServerMonitorEnabled());
            status.put("monitorInterval", server.getMonitorSysGenServerMonitorInterval());
            status.put("retentionDays", server.getMonitorSysGenServerMetricsRetentionDays());
            status.put("lastUpdateTime", server.getMonitorSysGenServerUpdateTime());

            return ReturnResult.ok(status);
        } catch (Exception e) {
            log.error("获取监控状态失败: {}", e.getMessage(), e);
            return ReturnResult.error("获取监控状态失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<MonitorSysGenServerMetrics> collectMetrics(String id) {
        if (StringUtils.isEmpty(id)) {
            return ReturnResult.error("服务器ID不能为空");
        }

        MonitorSysGenServer server = this.getById(id);
        if (server == null) {
            return ReturnResult.error("服务器配置不存在");
        }

        try {
            ServerConnectionService connectionService = getConnectionService(server.getMonitorSysGenServerProtocol());
            if (connectionService == null) {
                return ReturnResult.error("不支持的协议类型: " + server.getMonitorSysGenServerProtocol());
            }

            MonitorSysGenServerMetrics metrics = connectionService.collectMetrics(server);
            return ReturnResult.ok(metrics);
        } catch (Exception e) {
            log.error("收集服务器指标失败: {}", e.getMessage(), e);
            return ReturnResult.error("收集指标失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, List<MonitorSysGenServer>>> getServersByTags() {
        try {
            List<MonitorSysGenServer> servers = this.list();
            Map<String, List<MonitorSysGenServer>> groupedServers = servers.stream()
                    .collect(Collectors.groupingBy(server ->
                            StringUtils.isNotEmpty(server.getMonitorSysGenServerTags()) ?
                                    server.getMonitorSysGenServerTags() : "未分组"));

            return ReturnResult.ok(groupedServers);
        } catch (Exception e) {
            log.error("获取服务器分组失败: {}", e.getMessage(), e);
            return ReturnResult.error("获取服务器分组失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> batchOperation(List<String> ids, String action) {
        if (ids == null || ids.isEmpty()) {
            return ReturnResult.error("服务器ID列表不能为空");
        }

        if (StringUtils.isEmpty(action)) {
            return ReturnResult.error("操作类型不能为空");
        }

        Map<String, Object> result = new HashMap<>();
        List<String> successIds = new ArrayList<>();
        List<String> failedIds = new ArrayList<>();

        for (String id : ids) {
            try {
                boolean success = false;
                switch (action.toLowerCase()) {
                    case "connect":
                        success = connect(id).getData();
                        break;
                    case "disconnect":
                        success = disconnect(id).getData();
                        break;
                    case "enable_monitoring":
                        success = enableMonitoring(id).getData();
                        break;
                    case "disable_monitoring":
                        success = disableMonitoring(id).getData();
                        break;
                    default:
                        failedIds.add(id);
                        continue;
                }

                if (success) {
                    successIds.add(id);
                } else {
                    failedIds.add(id);
                }
            } catch (Exception e) {
                log.error("批量操作失败: serverId={}, action={}, error={}", id, action, e.getMessage());
                failedIds.add(id);
            }
        }

        result.put("action", action);
        result.put("total", ids.size());
        result.put("success", successIds.size());
        result.put("failed", failedIds.size());
        result.put("successIds", successIds);
        result.put("failedIds", failedIds);

        return ReturnResult.ok(result);
    }

    @Override
    public ReturnResult<Map<String, Object>> getServerStatistics() {
        try {
            Map<String, Object> statistics = new HashMap<>();

            // 总服务器数
            long totalServers = this.count();
            statistics.put("totalServers", totalServers);

            // 在线服务器数
            long onlineServers = connectionServices.stream()
                    .mapToLong(service -> this.list().stream()
                            .filter(server -> service.getProtocol().equals(server.getMonitorSysGenServerProtocol()))
                            .mapToLong(server -> service.isConnected(server.getMonitorSysGenServerId()) ? 1 : 0)
                            .sum())
                    .sum();
            statistics.put("onlineServers", onlineServers);

            // 启用监控的服务器数
            long monitoringEnabled = this.count(Wrappers.<MonitorSysGenServer>lambdaQuery()
                    .eq(MonitorSysGenServer::getMonitorSysGenServerMonitorEnabled, 1));
            statistics.put("monitoringEnabled", monitoringEnabled);

            // 按协议分组统计
            Map<String, Long> protocolStats = this.list().stream()
                    .collect(Collectors.groupingBy(
                            MonitorSysGenServer::getMonitorSysGenServerProtocol,
                            Collectors.counting()));
            statistics.put("protocolStats", protocolStats);

            // 按状态分组统计
            Map<String, Long> statusStats = this.list().stream()
                    .collect(Collectors.groupingBy(
                            server -> server.getMonitorSysGenServerStatus() == 1 ? "enabled" : "disabled",
                            Collectors.counting()));
            statistics.put("statusStats", statusStats);

            return ReturnResult.ok(statistics);
        } catch (Exception e) {
            log.error("获取服务器统计信息失败: {}", e.getMessage(), e);
            return ReturnResult.error("获取统计信息失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<String> exportServerConfig(List<String> ids) {
        try {
            List<MonitorSysGenServer> servers;
            if (ids == null || ids.isEmpty()) {
                servers = this.list();
            } else {
                servers = this.listByIds(ids);
            }

            // 清除敏感信息
            servers.forEach(server -> {
                server.setMonitorSysGenServerPassword("***");
                server.setMonitorSysGenServerPrivateKey("***");
            });

            String configData = Json.toJson(servers);
            return ReturnResult.ok(configData);
        } catch (Exception e) {
            log.error("导出服务器配置失败: {}", e.getMessage(), e);
            return ReturnResult.error("导出配置失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> importServerConfig(String configData) {
        try {
            List<MonitorSysGenServer> servers = Json.fromJsonToList(configData, MonitorSysGenServer.class);

            int successCount = 0;
            int failedCount = 0;
            List<String> errors = new ArrayList<>();

            for (MonitorSysGenServer server : servers) {
                try {
                    server.setMonitorSysGenServerId(null); // 清除ID，让数据库自动生成
                    server.setMonitorSysGenServerCreateTime(LocalDateTime.now());
                    server.setMonitorSysGenServerUpdateTime(LocalDateTime.now());

                    this.save(server);
                    successCount++;
                } catch (Exception e) {
                    failedCount++;
                    errors.add("导入服务器 " + server.getMonitorSysGenServerName() + " 失败: " + e.getMessage());
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("total", servers.size());
            result.put("success", successCount);
            result.put("failed", failedCount);
            result.put("errors", errors);

            return ReturnResult.ok(result);
        } catch (Exception e) {
            log.error("导入服务器配置失败: {}", e.getMessage(), e);
            return ReturnResult.error("导入配置失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<MonitorSysGenServer> cloneServer(String sourceId, String targetName) {
        if (StringUtils.isEmpty(sourceId)) {
            return ReturnResult.error("源服务器ID不能为空");
        }

        if (StringUtils.isEmpty(targetName)) {
            return ReturnResult.error("目标服务器名称不能为空");
        }

        try {
            MonitorSysGenServer sourceServer = this.getById(sourceId);
            if (sourceServer == null) {
                return ReturnResult.error("源服务器配置不存在");
            }

            MonitorSysGenServer clonedServer = new MonitorSysGenServer();
            // 复制所有字段
            clonedServer.setMonitorSysGenServerName(targetName);
            clonedServer.setMonitorSysGenServerHost(sourceServer.getMonitorSysGenServerHost());
            clonedServer.setMonitorSysGenServerPort(sourceServer.getMonitorSysGenServerPort());
            clonedServer.setMonitorSysGenServerProtocol(sourceServer.getMonitorSysGenServerProtocol());
            clonedServer.setMonitorSysGenServerUsername(sourceServer.getMonitorSysGenServerUsername());
            clonedServer.setMonitorSysGenServerPassword(sourceServer.getMonitorSysGenServerPassword());
            clonedServer.setMonitorSysGenServerPrivateKey(sourceServer.getMonitorSysGenServerPrivateKey());
            clonedServer.setMonitorSysGenServerMetricsRetentionDays(sourceServer.getMonitorSysGenServerMetricsRetentionDays());
            clonedServer.setMonitorSysGenServerMonitorInterval(sourceServer.getMonitorSysGenServerMonitorInterval());
            clonedServer.setMonitorSysGenServerTimeout(sourceServer.getMonitorSysGenServerTimeout());
            clonedServer.setMonitorSysGenServerDesc(sourceServer.getMonitorSysGenServerDesc() + " (克隆)");
            clonedServer.setMonitorSysGenServerStatus(0); // 默认停用
            clonedServer.setMonitorSysGenServerMonitorEnabled(0); // 默认不启用监控
            clonedServer.setMonitorSysGenServerTags(sourceServer.getMonitorSysGenServerTags());
            clonedServer.setGenId(sourceServer.getGenId());
            clonedServer.setMonitorSysGenServerCreateTime(LocalDateTime.now());
            clonedServer.setMonitorSysGenServerUpdateTime(LocalDateTime.now());

            this.save(clonedServer);

            log.info("服务器配置克隆成功: {} -> {}", sourceServer.getMonitorSysGenServerName(), targetName);
            return ReturnResult.ok(clonedServer);
        } catch (Exception e) {
            log.error("克隆服务器配置失败: {}", e.getMessage(), e);
            return ReturnResult.error("克隆配置失败: " + e.getMessage());
        }
    }

    /**
     * 获取连接服务
     */
    private ServerConnectionService getConnectionService(String protocol) {
        return connectionServices.stream()
                .filter(service -> service.getProtocol().equalsIgnoreCase(protocol))
                .findFirst()
                .orElse(null);
    }

    /**
     * 发送服务器消息
     */
    private void sendServerMessage(MonitorSysGenServer server, String action, boolean success, String error) {
        try {
            ServerWebSocketMessage message = ServerWebSocketMessage.builder()
                    .messageType("server_" + action)
                    .serverId(server.getMonitorSysGenServerId())
                    .serverName(server.getMonitorSysGenServerName())
                    .serverHost(server.getMonitorSysGenServerHost())
                    .serverPort(server.getMonitorSysGenServerPort())
                    .serverProtocol(server.getMonitorSysGenServerProtocol())
                    .errorMessage(error)
                    .data(Map.of("action", action, "success", success))
                    .timestamp(System.currentTimeMillis())
                    .build();

            String topic = "gen/server";
            socketSessionTemplate.send(topic, Json.toJson(message));
        } catch (Exception e) {
            log.error("发送服务器消息失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public ReturnResult<Boolean> updateConnectionStatus(String id, Integer status, String errorMessage) {
        if (StringUtils.isEmpty(id)) {
            return ReturnResult.error("服务器ID不能为空");
        }

        try {
            MonitorSysGenServer server = this.getById(id);
            if (server == null) {
                return ReturnResult.error("服务器配置不存在");
            }

            server.setMonitorSysGenServerConnectionStatus(status);
            server.setMonitorSysGenServerLastConnectTime(LocalDateTime.now());
            server.setMonitorSysGenServerConnectionError(errorMessage);
            server.setMonitorSysGenServerUpdateTime(LocalDateTime.now());

            boolean result = this.updateById(server);

            if (result) {
                // 发送WebSocket消息通知前端
                sendConnectionStatusMessage(server, status, errorMessage);
                log.info("服务器连接状态已更新: {} -> {}", server.getMonitorSysGenServerName(), status);
            }

            return ReturnResult.of(result, result ? "连接状态更新成功" : "连接状态更新失败");
        } catch (Exception e) {
            log.error("更新服务器连接状态失败: {}", e.getMessage(), e);
            return ReturnResult.error("更新连接状态失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> batchTestConnection(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return ReturnResult.error("服务器ID列表不能为空");
        }

        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> successList = new ArrayList<>();
        List<Map<String, Object>> failedList = new ArrayList<>();

        for (String id : ids) {
            try {
                ReturnResult<Boolean> testResult = testConnection(id);
                Map<String, Object> serverResult = new HashMap<>();
                MonitorSysGenServer server = this.getById(id);

                if (server != null) {
                    serverResult.put("serverId", server.getMonitorSysGenServerId());
                    serverResult.put("serverName", server.getMonitorSysGenServerName());
                    serverResult.put("host", server.getMonitorSysGenServerHost());
                    serverResult.put("port", server.getMonitorSysGenServerPort());
                    serverResult.put("protocol", server.getMonitorSysGenServerProtocol());
                    serverResult.put("success", testResult.getData());
                    serverResult.put("message", testResult.getMsg());

                    if (testResult.getData() != null && testResult.getData()) {
                        successList.add(serverResult);
                    } else {
                        failedList.add(serverResult);
                    }
                }
            } catch (Exception e) {
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("serverId", id);
                errorResult.put("success", false);
                errorResult.put("message", "测试连接异常: " + e.getMessage());
                failedList.add(errorResult);
            }
        }

        result.put("total", ids.size());
        result.put("success", successList.size());
        result.put("failed", failedList.size());
        result.put("successList", successList);
        result.put("failedList", failedList);

        return ReturnResult.ok(result);
    }

    @Override
    public ReturnResult<List<Map<String, Object>>> getAllConnectionStatus() {
        try {
            List<MonitorSysGenServer> servers = this.list();
            List<Map<String, Object>> statusList = new ArrayList<>();

            for (MonitorSysGenServer server : servers) {
                Map<String, Object> status = new HashMap<>();
                status.put("serverId", server.getMonitorSysGenServerId());
                status.put("serverName", server.getMonitorSysGenServerName());
                status.put("host", server.getMonitorSysGenServerHost());
                status.put("port", server.getMonitorSysGenServerPort());
                status.put("protocol", server.getMonitorSysGenServerProtocol());
                status.put("connectionStatus", server.getMonitorSysGenServerConnectionStatus());
                status.put("statusDesc", getStatusDesc(server.getMonitorSysGenServerConnectionStatus()));
                status.put("lastConnectTime", server.getMonitorSysGenServerLastConnectTime());
                status.put("connectionError", server.getMonitorSysGenServerConnectionError());
                status.put("monitorEnabled", server.getMonitorSysGenServerMonitorEnabled());
                status.put("tags", server.getMonitorSysGenServerTags());
                statusList.add(status);
            }

            return ReturnResult.ok(statusList);
        } catch (Exception e) {
            log.error("获取所有服务器连接状态失败: {}", e.getMessage(), e);
            return ReturnResult.error("获取连接状态失败: " + e.getMessage());
        }
    }

    @Override
    public void scheduleConnectionCheck() {
        try {
            List<MonitorSysGenServer> servers = this.list(
                Wrappers.lambdaQuery(MonitorSysGenServer.class)
                    .eq(MonitorSysGenServer::getMonitorSysGenServerStatus, 1)
                    .eq(MonitorSysGenServer::getMonitorSysGenServerMonitorEnabled, 1)
            );

            log.info("开始定时检查服务器连接状态，共{}台服务器", servers.size());

            for (MonitorSysGenServer server : servers) {
                try {
                    ReturnResult<Boolean> testResult = testConnection(server.getMonitorSysGenServerId().toString());

                    Integer newStatus = testResult.getData() != null && testResult.getData() ? 1 : 0;

                    // 只有状态发生变化时才更新
                    if (!Objects.equals(server.getMonitorSysGenServerConnectionStatus(), newStatus)) {
                        updateConnectionStatus(server.getMonitorSysGenServerId().toString(),
                            newStatus, testResult.getData() != null && testResult.getData() ? null : testResult.getMsg());
                    }
                } catch (Exception e) {
                    log.error("检查服务器连接状态失败: serverId={}, error={}",
                        server.getMonitorSysGenServerId(), e.getMessage());

                    updateConnectionStatus(server.getMonitorSysGenServerId().toString(),
                        3, e.getMessage());
                }
            }

            log.info("定时检查服务器连接状态完成");
        } catch (Exception e) {
            log.error("定时检查服务器连接状态异常: {}", e.getMessage(), e);
        }
    }



    /**
     * 获取状态描述
     */
    private String getStatusDesc(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 0:
                return "离线";
            case 1:
                return "在线";
            case 2:
                return "连接中";
            case 3:
                return "连接失败";
            default:
                return "未知";
        }
    }

    /**
     * 发送连接状态消息
     */
    private void sendConnectionStatusMessage(MonitorSysGenServer server, Integer status, String errorMessage) {
        try {
            ServerWebSocketMessage message = ServerWebSocketMessage.createConnectionStatusMessage(
                    server.getMonitorSysGenServerId(),
                    server.getMonitorSysGenServerName(),
                    server.getMonitorSysGenServerHost(),
                    server.getMonitorSysGenServerPort(),
                    server.getMonitorSysGenServerProtocol(),
                    status,
                    getStatusDesc(status),
                    errorMessage,
                    null
            );

            String topic = "gen/server";
            socketSessionTemplate.send(topic, Json.toJson(message));
            log.debug("发送连接状态消息: serverId={}, status={}", server.getMonitorSysGenServerId(), status);
        } catch (Exception e) {
            log.error("发送连接状态消息失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public ReturnResult<String> getGuacamoleProxyUrl(String id) {
        try {
            MonitorSysGenServer server = getById(id);
            if (server == null) {
                return ReturnResult.error("服务器不存在");
            }

            // 检查是否配置了Guacamole代理
            if (!"GUACAMOLE".equals(server.getMonitorSysGenServerProxyType())) {
                return ReturnResult.error("服务器未配置Guacamole代理");
            }

            if (server.getMonitorSysGenServerProxyHost() == null || server.getMonitorSysGenServerProxyPort() == null) {
                return ReturnResult.error("代理服务器地址或端口未配置");
            }

            // 构建Guacamole连接URL
            String proxyUrl = String.format("http://%s:%d/guacamole/#/client/%s",
                    server.getMonitorSysGenServerProxyHost(),
                    server.getMonitorSysGenServerProxyPort(),
                    generateGuacamoleConnectionId(server));

            return ReturnResult.ok(proxyUrl);
        } catch (Exception e) {
            log.error("获取Guacamole代理URL失败: serverId={}", id, e);
            return ReturnResult.error("获取代理URL失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> testProxyConnection(String id) {
        try {
            MonitorSysGenServer server = getById(id);
            if (server == null) {
                return ReturnResult.error("服务器不存在");
            }

            if (server.getMonitorSysGenServerProxyHost() == null || server.getMonitorSysGenServerProxyPort() == null) {
                return ReturnResult.error("代理服务器地址或端口未配置");
            }

            // TODO: 实现代理连接测试逻辑
            // 这里可以尝试连接到代理服务器，验证连接是否正常
            return ReturnResult.ok(true);
        } catch (Exception e) {
            log.error("测试代理连接失败: serverId={}", id, e);
            return ReturnResult.error("测试代理连接失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<String>> getSupportedReportMethods(String id) {
        try {
            MonitorSysGenServer server = getById(id);
            if (server == null) {
                return ReturnResult.error("服务器不存在");
            }

            List<String> supportedMethods = new ArrayList<>();
            supportedMethods.add("NONE"); // 不支持上报

            // 根据服务器配置判断支持的上报方式
            if (server.getMonitorSysGenServerReportEnabled() != null && server.getMonitorSysGenServerReportEnabled() == 1) {
                supportedMethods.add("API"); // 接口上报

                // 如果配置了Prometheus服务器，则支持Prometheus上报
                if (server.getMonitorSysGenServerPrometheusHost() != null && server.getMonitorSysGenServerPrometheusPort() != null) {
                    supportedMethods.add("PROMETHEUS");
                }
            }

            return ReturnResult.ok(supportedMethods);
        } catch (Exception e) {
            log.error("获取支持的数据上报方式失败: serverId={}", id, e);
            return ReturnResult.error("获取支持的上报方式失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Object> queryDataByConfig(String id, String queryType, String expression, Map<String, Object> timeRange) {
        try {
            MonitorSysGenServer server = getById(id);
            if (server == null) {
                return ReturnResult.error("服务器不存在");
            }

            // 根据查询类型执行不同的查询
            switch (queryType.toUpperCase()) {
                case "PROMETHEUS":
                    return queryPrometheusData(server, expression, timeRange);
                case "SQL":
                    return querySqlData(server, expression, timeRange);
                default:
                    return ReturnResult.error("不支持的查询类型: " + queryType);
            }
        } catch (Exception e) {
            log.error("根据配置查询数据失败: serverId={}, queryType={}, expression={}", id, queryType, expression, e);
            return ReturnResult.error("查询数据失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> updateReportConfig(String id, Object config) {
        try {
            MonitorSysGenServer server = getById(id);
            if (server == null) {
                return ReturnResult.error("服务器不存在");
            }

            // TODO: 根据config对象更新服务器的上报配置
            // 这里需要解析config对象并更新相应的字段

            boolean success = updateById(server);
            return ReturnResult.ok(success);
        } catch (Exception e) {
            log.error("更新服务器数据上报配置失败: serverId={}", id, e);
            return ReturnResult.error("更新上报配置失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> getReportConfig(String id) {
        try {
            MonitorSysGenServer server = getById(id);
            if (server == null) {
                return ReturnResult.error("服务器不存在");
            }

            Map<String, Object> config = new HashMap<>();
            config.put("reportEnabled", server.getMonitorSysGenServerReportEnabled());
            config.put("dataReportMethod", server.getMonitorSysGenServerDataReportMethod());
            config.put("prometheusHost", server.getMonitorSysGenServerPrometheusHost());
            config.put("prometheusPort", server.getMonitorSysGenServerPrometheusPort());
            config.put("proxyType", server.getMonitorSysGenServerProxyType());
            config.put("proxyHost", server.getMonitorSysGenServerProxyHost());
            config.put("proxyPort", server.getMonitorSysGenServerProxyPort());

            return ReturnResult.ok(config);
        } catch (Exception e) {
            log.error("获取服务器数据上报配置失败: serverId={}", id, e);
            return ReturnResult.error("获取上报配置失败: " + e.getMessage());
        }
    }

    /**
     * 生成Guacamole连接ID
     */
    private String generateGuacamoleConnectionId(MonitorSysGenServer server) {
        // 根据服务器信息生成唯一的连接ID
        return String.format("%s_%s_%d_%s",
                server.getMonitorSysGenServerProtocol().toLowerCase(),
                server.getMonitorSysGenServerHost(),
                server.getMonitorSysGenServerPort(),
                server.getMonitorSysGenServerId());
    }

    /**
     * 查询Prometheus数据
     */
    private ReturnResult<Object> queryPrometheusData(MonitorSysGenServer server, String expression, Map<String, Object> timeRange) {
        // TODO: 实现Prometheus数据查询逻辑
        return ReturnResult.ok("Prometheus查询功能待实现");
    }

    /**
     * 查询SQL数据
     */
    private ReturnResult<Object> querySqlData(MonitorSysGenServer server, String expression, Map<String, Object> timeRange) {
        // TODO: 实现SQL数据查询逻辑
        return ReturnResult.ok("SQL查询功能待实现");
    }
}
