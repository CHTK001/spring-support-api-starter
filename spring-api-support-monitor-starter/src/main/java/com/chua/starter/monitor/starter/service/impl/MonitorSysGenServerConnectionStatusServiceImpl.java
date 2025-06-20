package com.chua.starter.monitor.starter.service.impl;

import com.chua.common.support.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.socketio.support.session.SocketSessionTemplate;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServer;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerConnectionStatus;
import com.chua.starter.monitor.starter.mapper.MonitorSysGenServerConnectionStatusMapper;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerConnectionStatusService;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerService;
import com.chua.starter.monitor.starter.service.ServerConnectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 服务器连接状态管理服务实现类
 *
 * @author CH
 * @since 2024/12/20
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MonitorSysGenServerConnectionStatusServiceImpl implements MonitorSysGenServerConnectionStatusService {

    private final MonitorSysGenServerConnectionStatusMapper connectionStatusMapper;
    private final MonitorSysGenServerService serverService;
    private final List<ServerConnectionService> connectionServices;
    private final SocketSessionTemplate socketSessionTemplate;
    private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

    @Override
    public ReturnResult<MonitorSysGenServerConnectionStatus> testServerConnection(Integer serverId) {
        try {
            MonitorSysGenServer server = serverService.getById(serverId);
            if (server == null) {
                return ReturnResult.illegal("服务器不存在");
            }

            // 更新状态为连接中
            updateServerConnectionStatus(serverId, MonitorSysGenServerConnectionStatus.ConnectionStatus.CONNECTING.getCode(), null);
            
            // 发送连接中状态
            sendConnectionStatusMessage(serverId, MonitorSysGenServerConnectionStatus.ConnectionStatus.CONNECTING.getCode(), "正在测试连接...");

            MonitorSysGenServerConnectionStatus result = performConnectionTest(server);
            
            // 更新连接状态
            updateServerConnectionStatus(serverId, result.getMonitorSysGenServerConnectionStatus(), result.getMonitorSysGenServerConnectionError());
            
            // 发送连接结果
            sendConnectionStatusMessage(serverId, result.getMonitorSysGenServerConnectionStatus(), 
                    result.getMonitorSysGenServerConnectionError());

            return ReturnResult.success(result);
            
        } catch (Exception e) {
            log.error("测试服务器连接失败: serverId={}", serverId, e);
            updateServerConnectionStatus(serverId, MonitorSysGenServerConnectionStatus.ConnectionStatus.FAILED.getCode(), e.getMessage());
            sendConnectionStatusMessage(serverId, MonitorSysGenServerConnectionStatus.ConnectionStatus.FAILED.getCode(), e.getMessage());
            return ReturnResult.error("测试连接失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<MonitorSysGenServerConnectionStatus>> batchTestServerConnection(List<Integer> serverIds) {
        try {
            List<MonitorSysGenServer> servers = serverService.listByIds(serverIds);
            if (servers.isEmpty()) {
                return ReturnResult.illegal("没有找到有效的服务器");
            }

            // 并行测试连接
            List<CompletableFuture<MonitorSysGenServerConnectionStatus>> futures = servers.stream()
                    .map(server -> CompletableFuture.supplyAsync(() -> {
                        try {
                            // 更新状态为连接中
                            updateServerConnectionStatus(server.getMonitorSysGenServerId(), 
                                    MonitorSysGenServerConnectionStatus.ConnectionStatus.CONNECTING.getCode(), null);
                            
                            sendConnectionStatusMessage(server.getMonitorSysGenServerId(), 
                                    MonitorSysGenServerConnectionStatus.ConnectionStatus.CONNECTING.getCode(), "正在测试连接...");

                            MonitorSysGenServerConnectionStatus result = performConnectionTest(server);
                            
                            // 更新连接状态
                            updateServerConnectionStatus(server.getMonitorSysGenServerId(), 
                                    result.getMonitorSysGenServerConnectionStatus(), result.getMonitorSysGenServerConnectionError());
                            
                            sendConnectionStatusMessage(server.getMonitorSysGenServerId(), 
                                    result.getMonitorSysGenServerConnectionStatus(), result.getMonitorSysGenServerConnectionError());

                            return result;
                        } catch (Exception e) {
                            log.error("测试服务器连接失败: serverId={}", server.getMonitorSysGenServerId(), e);
                            updateServerConnectionStatus(server.getMonitorSysGenServerId(), 
                                    MonitorSysGenServerConnectionStatus.ConnectionStatus.FAILED.getCode(), e.getMessage());
                            sendConnectionStatusMessage(server.getMonitorSysGenServerId(), 
                                    MonitorSysGenServerConnectionStatus.ConnectionStatus.FAILED.getCode(), e.getMessage());
                            
                            MonitorSysGenServerConnectionStatus errorResult = new MonitorSysGenServerConnectionStatus();
                            errorResult.setMonitorSysGenServerId(server.getMonitorSysGenServerId());
                            errorResult.setMonitorSysGenServerConnectionStatus(MonitorSysGenServerConnectionStatus.ConnectionStatus.FAILED.getCode());
                            errorResult.setMonitorSysGenServerConnectionError(e.getMessage());
                            return errorResult;
                        }
                    }, executorService))
                    .toList();

            // 等待所有测试完成
            List<MonitorSysGenServerConnectionStatus> results = futures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());

            return ReturnResult.success(results);
            
        } catch (Exception e) {
            log.error("批量测试服务器连接失败", e);
            return ReturnResult.error("批量测试连接失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<MonitorSysGenServerConnectionStatus>> getAllServerConnectionStatus() {
        try {
            List<MonitorSysGenServerConnectionStatus> statusList = connectionStatusMapper.getAllServerConnectionStatus();
            return ReturnResult.success(statusList);
        } catch (Exception e) {
            log.error("获取所有服务器连接状态失败", e);
            return ReturnResult.error("获取连接状态失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<MonitorSysGenServerConnectionStatus> getServerConnectionStatus(Integer serverId) {
        try {
            MonitorSysGenServerConnectionStatus status = connectionStatusMapper.getServerConnectionStatus(serverId);
            if (status == null) {
                return ReturnResult.illegal("服务器不存在");
            }
            return ReturnResult.success(status);
        } catch (Exception e) {
            log.error("获取服务器连接状态失败: serverId={}", serverId, e);
            return ReturnResult.error("获取连接状态失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<MonitorSysGenServerConnectionStatus>> getServersByConnectionStatus(Integer connectionStatus) {
        try {
            List<MonitorSysGenServerConnectionStatus> statusList = connectionStatusMapper.getServersByConnectionStatus(connectionStatus);
            return ReturnResult.success(statusList);
        } catch (Exception e) {
            log.error("根据连接状态获取服务器列表失败: connectionStatus={}", connectionStatus, e);
            return ReturnResult.error("获取服务器列表失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ReturnResult<Boolean> updateServerConnectionStatus(Integer serverId, Integer connectionStatus, String connectionError) {
        try {
            int updated = connectionStatusMapper.updateServerConnectionStatus(serverId, connectionStatus, LocalDateTime.now(), connectionError);
            return ReturnResult.success(updated > 0);
        } catch (Exception e) {
            log.error("更新服务器连接状态失败: serverId={}, status={}", serverId, connectionStatus, e);
            return ReturnResult.error("更新连接状态失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ReturnResult<Boolean> batchUpdateServerConnectionStatus(List<Integer> serverIds, Integer connectionStatus) {
        try {
            int updated = connectionStatusMapper.batchUpdateServerConnectionStatus(serverIds, connectionStatus, LocalDateTime.now());
            return ReturnResult.success(updated > 0);
        } catch (Exception e) {
            log.error("批量更新服务器连接状态失败: serverIds={}, status={}", serverIds, connectionStatus, e);
            return ReturnResult.error("批量更新连接状态失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> getConnectionStatusStatistics() {
        try {
            List<Map<String, Object>> statistics = connectionStatusMapper.getConnectionStatusStatistics();
            
            Map<String, Object> result = new HashMap<>();
            int total = 0;
            int online = 0;
            int offline = 0;
            int connecting = 0;
            int failed = 0;
            
            for (Map<String, Object> stat : statistics) {
                Integer status = (Integer) stat.get("monitor_sys_gen_server_connection_status");
                Long count = (Long) stat.get("count");
                
                total += count.intValue();
                
                if (status != null) {
                    switch (status) {
                        case 1:
                            online = count.intValue();
                            break;
                        case 0:
                            offline = count.intValue();
                            break;
                        case 2:
                            connecting = count.intValue();
                            break;
                        case 3:
                            failed = count.intValue();
                            break;
                    }
                }
            }
            
            result.put("total", total);
            result.put("online", online);
            result.put("offline", offline);
            result.put("connecting", connecting);
            result.put("failed", failed);
            result.put("onlineRate", total > 0 ? (double) online / total * 100 : 0.0);
            
            return ReturnResult.success(result);
        } catch (Exception e) {
            log.error("获取连接状态统计失败", e);
            return ReturnResult.error("获取统计信息失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<MonitorSysGenServerConnectionStatus>> getLongTimeNoConnectServers(Integer minutes) {
        try {
            List<MonitorSysGenServerConnectionStatus> servers = connectionStatusMapper.getLongTimeNoConnectServers(minutes);
            return ReturnResult.success(servers);
        } catch (Exception e) {
            log.error("获取长时间未连接服务器失败: minutes={}", minutes, e);
            return ReturnResult.error("获取服务器列表失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> checkAllServerConnections() {
        try {
            List<MonitorSysGenServer> servers = serverService.list();
            
            Map<String, Object> result = new HashMap<>();
            result.put("totalServers", servers.size());
            result.put("checkTime", LocalDateTime.now());
            
            // 异步检查所有服务器连接
            CompletableFuture.runAsync(() -> {
                for (MonitorSysGenServer server : servers) {
                    try {
                        performConnectionTest(server);
                    } catch (Exception e) {
                        log.error("检查服务器连接失败: serverId={}", server.getMonitorSysGenServerId(), e);
                    }
                }
            }, executorService);
            
            result.put("message", "连接检查已启动，请稍后查看结果");
            return ReturnResult.success(result);
            
        } catch (Exception e) {
            log.error("检查所有服务器连接失败", e);
            return ReturnResult.error("检查连接失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> getServerConnectionHealthReport() {
        try {
            Map<String, Object> report = new HashMap<>();
            
            // 获取统计信息
            ReturnResult<Map<String, Object>> statisticsResult = getConnectionStatusStatistics();
            if (statisticsResult.isOk()) {
                report.put("statistics", statisticsResult.getData());
            }
            
            // 获取长时间未连接的服务器
            ReturnResult<List<MonitorSysGenServerConnectionStatus>> longTimeNoConnectResult = getLongTimeNoConnectServers(60);
            if (longTimeNoConnectResult.isOk()) {
                report.put("longTimeNoConnectServers", longTimeNoConnectResult.getData());
            }
            
            // 获取连接失败的服务器
            ReturnResult<List<MonitorSysGenServerConnectionStatus>> failedServersResult = 
                    getServersByConnectionStatus(MonitorSysGenServerConnectionStatus.ConnectionStatus.FAILED.getCode());
            if (failedServersResult.isOk()) {
                report.put("failedServers", failedServersResult.getData());
            }
            
            report.put("reportTime", LocalDateTime.now());
            
            return ReturnResult.success(report);
            
        } catch (Exception e) {
            log.error("获取服务器连接健康度报告失败", e);
            return ReturnResult.error("获取健康度报告失败: " + e.getMessage());
        }
    }

    /**
     * 执行连接测试
     */
    private MonitorSysGenServerConnectionStatus performConnectionTest(MonitorSysGenServer server) {
        MonitorSysGenServerConnectionStatus result = new MonitorSysGenServerConnectionStatus();
        result.setMonitorSysGenServerId(server.getMonitorSysGenServerId());
        result.setMonitorSysGenServerName(server.getMonitorSysGenServerName());
        result.setMonitorSysGenServerHost(server.getMonitorSysGenServerHost());
        result.setMonitorSysGenServerPort(server.getMonitorSysGenServerPort());
        result.setMonitorSysGenServerProtocol(server.getMonitorSysGenServerProtocol());
        result.setMonitorSysGenServerTestTime(LocalDateTime.now());

        try {
            ServerConnectionService connectionService = getConnectionService(server.getMonitorSysGenServerProtocol());
            if (connectionService == null) {
                result.setMonitorSysGenServerConnectionStatus(MonitorSysGenServerConnectionStatus.ConnectionStatus.FAILED.getCode());
                result.setMonitorSysGenServerConnectionError("不支持的协议类型: " + server.getMonitorSysGenServerProtocol());
                return result;
            }

            long startTime = System.currentTimeMillis();
            boolean connected = connectionService.testConnection(server);
            long responseTime = System.currentTimeMillis() - startTime;

            result.setMonitorSysGenServerResponseTime(responseTime);
            
            if (connected) {
                result.setMonitorSysGenServerConnectionStatus(MonitorSysGenServerConnectionStatus.ConnectionStatus.ONLINE.getCode());
                result.setMonitorSysGenServerLastConnectTime(LocalDateTime.now());
                result.setMonitorSysGenServerConnectionError(null);
            } else {
                result.setMonitorSysGenServerConnectionStatus(MonitorSysGenServerConnectionStatus.ConnectionStatus.FAILED.getCode());
                result.setMonitorSysGenServerConnectionError("连接测试失败");
            }

        } catch (Exception e) {
            result.setMonitorSysGenServerConnectionStatus(MonitorSysGenServerConnectionStatus.ConnectionStatus.FAILED.getCode());
            result.setMonitorSysGenServerConnectionError(e.getMessage());
        }

        return result;
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
     * 发送连接状态消息
     */
    private void sendConnectionStatusMessage(Integer serverId, Integer status, String message) {
        try {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("serverId", serverId);
            messageData.put("connectionStatus", status);
            messageData.put("message", message);
            messageData.put("timestamp", System.currentTimeMillis());
            messageData.put("type", "connection_status_update");

            String topic = "gen/server/connection/" + serverId;
            socketSessionTemplate.send(topic, Json.toJson(messageData));
        } catch (Exception e) {
            log.error("发送连接状态消息失败: serverId={}", serverId, e);
        }
    }
}
