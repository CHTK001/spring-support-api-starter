package com.chua.starter.monitor.service.impl;

import com.chua.common.support.text.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.socket.support.session.SocketSessionTemplate;
import com.chua.starter.monitor.entity.MonitorSysGenServer;
import com.chua.starter.monitor.entity.MonitorSysGenServerConnectionStatus;
import com.chua.starter.sync.support.message.ServerWebSocketMessage;
import com.chua.starter.monitor.mapper.MonitorSysGenServerConnectionStatusMapper;
import com.chua.starter.monitor.pojo.MonitorSysGenServerConnectionCheckResult;
import com.chua.starter.monitor.pojo.MonitorSysGenServerConnectionHealthReport;
import com.chua.starter.monitor.pojo.MonitorSysGenServerConnectionStatistics;
import com.chua.starter.monitor.service.server.MonitorSysGenServerConnectionStatusService;
import com.chua.starter.monitor.service.server.MonitorSysGenServerService;
import com.chua.starter.monitor.service.server.ServerConnectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
    @Lazy
    @Autowired
    private MonitorSysGenServerService serverService;
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
    public ReturnResult<MonitorSysGenServerConnectionStatistics> getConnectionStatusStatistics() {
        try {
            List<Map<String, Object>> statistics = connectionStatusMapper.getConnectionStatusStatistics();

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

            MonitorSysGenServerConnectionStatistics result = MonitorSysGenServerConnectionStatistics.builder()
                    .total(total)
                    .online(online)
                    .offline(offline)
                    .connecting(connecting)
                    .failed(failed)
                    .onlineRate(total > 0 ? (double) online / total * 100 : 0.0)
                    .build();

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
    public ReturnResult<MonitorSysGenServerConnectionCheckResult> checkAllServerConnections() {
        try {
            List<MonitorSysGenServer> servers = serverService.list();

            MonitorSysGenServerConnectionCheckResult result = MonitorSysGenServerConnectionCheckResult.builder()
                    .totalServers(servers.size())
                    .checkTime(LocalDateTime.now())
                    .message("连接检查已启动，请稍后查看结果")
                    .build();

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

            return ReturnResult.success(result);

        } catch (Exception e) {
            log.error("检查所有服务器连接失败", e);
            return ReturnResult.error("检查连接失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<MonitorSysGenServerConnectionHealthReport> getServerConnectionHealthReport() {
        try {
            // 获取统计信息
            ReturnResult<MonitorSysGenServerConnectionStatistics> statisticsResult = getConnectionStatusStatistics();
            MonitorSysGenServerConnectionStatistics statistics = null;
            if (statisticsResult.isOk()) {
                statistics = statisticsResult.getData();
            }

            // 获取长时间未连接的服务器
            ReturnResult<List<MonitorSysGenServerConnectionStatus>> longTimeNoConnectResult = getLongTimeNoConnectServers(60);
            List<MonitorSysGenServerConnectionStatus> longTimeNoConnectServers = null;
            if (longTimeNoConnectResult.isOk()) {
                longTimeNoConnectServers = longTimeNoConnectResult.getData();
            }

            // 获取连接失败的服务器
            ReturnResult<List<MonitorSysGenServerConnectionStatus>> failedServersResult =
                    getServersByConnectionStatus(MonitorSysGenServerConnectionStatus.ConnectionStatus.FAILED.getCode());
            List<MonitorSysGenServerConnectionStatus> failedServers = null;
            if (failedServersResult.isOk()) {
                failedServers = failedServersResult.getData();
            }

            MonitorSysGenServerConnectionHealthReport report = MonitorSysGenServerConnectionHealthReport.builder()
                    .statistics(statistics)
                    .longTimeNoConnectServers(longTimeNoConnectServers)
                    .failedServers(failedServers)
                    .reportTime(LocalDateTime.now())
                    .build();

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
            ServerWebSocketMessage wsMessage = ServerWebSocketMessage.builder()
                    .messageType("connection_status_update")
                    .serverId(serverId)
                    .connectionStatus(status)
                    .statusDesc(message)
                    .timestamp(System.currentTimeMillis())
                    .build();

            socketSessionTemplate.send("gen/server", Json.toJson(wsMessage));
        } catch (Exception e) {
            log.error("发送连接状态消息失败: serverId={}", serverId, e);
        }
    }

    @Override
    @Transactional
    public ReturnResult<Boolean> resetServerConnectionStatus(Integer serverId) {
        try {
            // 重置连接状态为离线
            int updated = connectionStatusMapper.resetServerConnectionStatus(serverId, LocalDateTime.now());
            if (updated > 0) {
                sendConnectionStatusMessage(serverId, MonitorSysGenServerConnectionStatus.ConnectionStatus.OFFLINE.getCode(), "连接状态已重置");
            }
            return ReturnResult.success(updated > 0);
        } catch (Exception e) {
            log.error("重置服务器连接状态失败: serverId={}", serverId, e);
            return ReturnResult.error("重置连接状态失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ReturnResult<Boolean> batchResetServerConnectionStatus(List<Integer> serverIds) {
        try {
            int updated = connectionStatusMapper.batchResetServerConnectionStatus(serverIds, LocalDateTime.now());
            for (Integer serverId : serverIds) {
                sendConnectionStatusMessage(serverId, MonitorSysGenServerConnectionStatus.ConnectionStatus.OFFLINE.getCode(), "连接状态已重置");
            }
            return ReturnResult.success(updated > 0);
        } catch (Exception e) {
            log.error("批量重置服务器连接状态失败: serverIds={}", serverIds, e);
            return ReturnResult.error("批量重置连接状态失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<Map<String, Object>>> getServerConnectionTrend(Integer serverId, Integer hours) {
        try {
            List<Map<String, Object>> trendData = connectionStatusMapper.getServerConnectionTrend(serverId, hours);
            return ReturnResult.success(trendData);
        } catch (Exception e) {
            log.error("获取服务器连接趋势数据失败: serverId={}, hours={}", serverId, hours, e);
            return ReturnResult.error("获取趋势数据失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> getRealTimeConnectionStatus() {
        try {
            // 获取所有服务器的实时连接状态
            List<MonitorSysGenServerConnectionStatus> statusList = connectionStatusMapper.getAllServerConnectionStatus();
            
            // 获取统计信息
            ReturnResult<MonitorSysGenServerConnectionStatistics> statisticsResult = getConnectionStatusStatistics();
            
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("servers", statusList);
            result.put("statistics", statisticsResult.isOk() ? statisticsResult.getData() : null);
            result.put("timestamp", System.currentTimeMillis());
            
            return ReturnResult.success(result);
        } catch (Exception e) {
            log.error("获取实时连接状态数据失败", e);
            return ReturnResult.error("获取实时状态失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ReturnResult<Boolean> setConnectionMonitorConfig(Integer serverId, Map<String, Object> config) {
        try {
            MonitorSysGenServer server = serverService.getById(serverId);
            if (server == null) {
                return ReturnResult.illegal("服务器不存在");
            }
            
            // 将配置存储到服务器设置中
            server.setMonitorSysGenServerConnectionMonitorConfig(Json.toJson(config));
            serverService.updateById(server);
            
            return ReturnResult.success(true);
        } catch (Exception e) {
            log.error("设置连接监控配置失败: serverId={}", serverId, e);
            return ReturnResult.error("设置监控配置失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> getConnectionMonitorConfig(Integer serverId) {
        try {
            MonitorSysGenServer server = serverService.getById(serverId);
            if (server == null) {
                return ReturnResult.illegal("服务器不存在");
            }
            
            String configJson = server.getMonitorSysGenServerConnectionMonitorConfig();
            Map<String, Object> config = new java.util.HashMap<>();
            if (configJson != null && !configJson.isEmpty()) {
                config = Json.fromJson(configJson, Map.class);
            }
            
            // 添加默认配置
            config.putIfAbsent("checkInterval", 60); // 检查间隔（秒）
            config.putIfAbsent("timeout", 30); // 超时时间（秒）
            config.putIfAbsent("retryCount", 3); // 重试次数
            config.putIfAbsent("enabled", server.getMonitorSysGenServerConnectionMonitorEnabled());
            
            return ReturnResult.success(config);
        } catch (Exception e) {
            log.error("获取连接监控配置失败: serverId={}", serverId, e);
            return ReturnResult.error("获取监控配置失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ReturnResult<Boolean> toggleConnectionMonitor(Integer serverId, Boolean enabled) {
        try {
            MonitorSysGenServer server = serverService.getById(serverId);
            if (server == null) {
                return ReturnResult.illegal("服务器不存在");
            }
            
            server.setMonitorSysGenServerConnectionMonitorEnabled(enabled ? 1 : 0);
            serverService.updateById(server);
            
            return ReturnResult.success(true);
        } catch (Exception e) {
            log.error("切换连接监控状态失败: serverId={}, enabled={}", serverId, enabled, e);
            return ReturnResult.error("切换监控状态失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<Map<String, Object>>> getConnectionFailureReasons(Integer days) {
        try {
            List<Map<String, Object>> reasons = connectionStatusMapper.getConnectionFailureReasons(days);
            return ReturnResult.success(reasons);
        } catch (Exception e) {
            log.error("获取连接失败原因统计失败: days={}", days, e);
            return ReturnResult.error("获取失败原因统计失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<String> exportConnectionStatusReport(Map<String, Object> params) {
        try {
            // TODO: 实现导出功能
            // 获取所有服务器连接状态
            List<MonitorSysGenServerConnectionStatus> statusList = connectionStatusMapper.getAllServerConnectionStatus();
            
            // 生成报告文件
            String fileName = "connection_status_report_" + System.currentTimeMillis() + ".json";
            String filePath = "/tmp/" + fileName;
            
            // 写入文件
            java.nio.file.Files.writeString(java.nio.file.Path.of(filePath), Json.toJson(statusList));
            
            return ReturnResult.success(filePath);
        } catch (Exception e) {
            log.error("导出连接状态报告失败", e);
            return ReturnResult.error("导出报告失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> getServerConnectionStatusHistory(Map<String, Object> params) {
        try {
            Integer serverId = (Integer) params.get("serverId");
            Integer page = (Integer) params.getOrDefault("page", 1);
            Integer pageSize = (Integer) params.getOrDefault("pageSize", 20);
            String startTime = (String) params.get("startTime");
            String endTime = (String) params.get("endTime");
            
            // 计算偏移量
            int offset = (page - 1) * pageSize;
            
            List<Map<String, Object>> records = connectionStatusMapper.getServerConnectionStatusHistory(
                    serverId, startTime, endTime, offset, pageSize);
            Long total = connectionStatusMapper.getServerConnectionStatusHistoryCount(serverId, startTime, endTime);
            
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("records", records);
            result.put("total", total);
            result.put("page", page);
            result.put("pageSize", pageSize);
            
            return ReturnResult.success(result);
        } catch (Exception e) {
            log.error("获取连接状态历史记录失败", e);
            return ReturnResult.error("获取历史记录失败: " + e.getMessage());
        }
    }
}
