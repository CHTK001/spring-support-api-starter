package com.chua.starter.monitor.starter.utils;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServer;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerMetrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 服务器操作工具类
 * 统一处理服务器相关的通用操作，减少重复代码
 *
 * @author CH
 * @since 2024/12/25
 */
@Slf4j
@Component
public class ServerOperationUtils {

    /**
     * 批量执行服务器操作
     *
     * @param servers 服务器列表
     * @param operation 操作函数
     * @param operationName 操作名称（用于日志）
     * @param <T> 返回类型
     * @return 批量操作结果
     */
    public static <T> Map<String, Object> batchExecute(
            List<MonitorSysGenServer> servers,
            Function<MonitorSysGenServer, ReturnResult<T>> operation,
            String operationName) {
        
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> successList = new ArrayList<>();
        List<Map<String, Object>> failedList = new ArrayList<>();
        
        for (MonitorSysGenServer server : servers) {
            try {
                ReturnResult<T> operationResult = operation.apply(server);
                
                Map<String, Object> serverResult = new HashMap<>();
                serverResult.put("serverId", server.getMonitorSysGenServerId());
                serverResult.put("serverName", server.getMonitorSysGenServerName());
                serverResult.put("host", server.getMonitorSysGenServerHost());
                
                if (operationResult.isOk()) {
                    serverResult.put("result", operationResult.getData());
                    successList.add(serverResult);
                    log.debug("服务器 {} {} 成功", server.getMonitorSysGenServerName(), operationName);
                } else {
                    serverResult.put("error", operationResult.getMsg());
                    failedList.add(serverResult);
                    log.warn("服务器 {} {} 失败: {}", server.getMonitorSysGenServerName(), operationName, operationResult.getMsg());
                }
                
            } catch (Exception e) {
                Map<String, Object> serverResult = new HashMap<>();
                serverResult.put("serverId", server.getMonitorSysGenServerId());
                serverResult.put("serverName", server.getMonitorSysGenServerName());
                serverResult.put("host", server.getMonitorSysGenServerHost());
                serverResult.put("error", e.getMessage());
                failedList.add(serverResult);
                log.error("服务器 {} {} 异常", server.getMonitorSysGenServerName(), operationName, e);
            }
        }
        
        result.put("total", servers.size());
        result.put("success", successList.size());
        result.put("failed", failedList.size());
        result.put("successList", successList);
        result.put("failedList", failedList);
        result.put("operationName", operationName);
        
        return result;
    }

    /**
     * 异步批量执行服务器操作
     *
     * @param servers 服务器列表
     * @param operation 操作函数
     * @param operationName 操作名称
     * @param <T> 返回类型
     * @return 异步操作结果
     */
    public static <T> CompletableFuture<Map<String, Object>> batchExecuteAsync(
            List<MonitorSysGenServer> servers,
            Function<MonitorSysGenServer, ReturnResult<T>> operation,
            String operationName) {
        
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Object> result = new ConcurrentHashMap<>();
            List<CompletableFuture<Map<String, Object>>> futures = new ArrayList<>();
            
            for (MonitorSysGenServer server : servers) {
                CompletableFuture<Map<String, Object>> future = CompletableFuture.supplyAsync(() -> {
                    Map<String, Object> serverResult = new HashMap<>();
                    serverResult.put("serverId", server.getMonitorSysGenServerId());
                    serverResult.put("serverName", server.getMonitorSysGenServerName());
                    serverResult.put("host", server.getMonitorSysGenServerHost());
                    
                    try {
                        ReturnResult<T> operationResult = operation.apply(server);
                        if (operationResult.isOk()) {
                            serverResult.put("success", true);
                            serverResult.put("result", operationResult.getData());
                        } else {
                            serverResult.put("success", false);
                            serverResult.put("error", operationResult.getMsg());
                        }
                    } catch (Exception e) {
                        serverResult.put("success", false);
                        serverResult.put("error", e.getMessage());
                        log.error("异步执行服务器 {} {} 失败", server.getMonitorSysGenServerName(), operationName, e);
                    }
                    
                    return serverResult;
                });
                
                futures.add(future);
            }
            
            // 等待所有操作完成
            List<Map<String, Object>> results = futures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());
            
            // 统计结果
            long successCount = results.stream().mapToLong(r -> (Boolean) r.get("success") ? 1 : 0).sum();
            long failedCount = results.size() - successCount;
            
            List<Map<String, Object>> successList = results.stream()
                    .filter(r -> (Boolean) r.get("success"))
                    .collect(Collectors.toList());
            
            List<Map<String, Object>> failedList = results.stream()
                    .filter(r -> !(Boolean) r.get("success"))
                    .collect(Collectors.toList());
            
            result.put("total", servers.size());
            result.put("success", successCount);
            result.put("failed", failedCount);
            result.put("successList", successList);
            result.put("failedList", failedList);
            result.put("operationName", operationName);
            
            return result;
        });
    }

    /**
     * 验证服务器配置
     *
     * @param server 服务器配置
     * @return 验证结果
     */
    public static ReturnResult<Map<String, Object>> validateServerConfig(MonitorSysGenServer server) {
        Map<String, Object> validation = new HashMap<>();
        List<String> errors = new ArrayList<>();
        
        // 基本字段验证
        if (server.getMonitorSysGenServerName() == null || server.getMonitorSysGenServerName().trim().isEmpty()) {
            errors.add("服务器名称不能为空");
        }
        
        if (server.getMonitorSysGenServerHost() == null || server.getMonitorSysGenServerHost().trim().isEmpty()) {
            errors.add("服务器地址不能为空");
        }
        
        if (server.getMonitorSysGenServerPort() == null || server.getMonitorSysGenServerPort() <= 0) {
            errors.add("服务器端口必须大于0");
        }
        
        if (server.getMonitorSysGenServerProtocol() == null || server.getMonitorSysGenServerProtocol().trim().isEmpty()) {
            errors.add("协议类型不能为空");
        }
        
        // 协议特定验证
        String protocol = server.getMonitorSysGenServerProtocol();
        if ("SSH".equalsIgnoreCase(protocol)) {
            if (server.getMonitorSysGenServerUsername() == null || server.getMonitorSysGenServerUsername().trim().isEmpty()) {
                errors.add("SSH协议需要用户名");
            }
        }
        
        validation.put("valid", errors.isEmpty());
        validation.put("errors", errors);
        validation.put("errorCount", errors.size());
        
        if (errors.isEmpty()) {
            return ReturnResult.success(validation);
        } else {
            return ReturnResult.error(validation);
        }
    }

    /**
     * 构建连接状态信息
     *
     * @param server 服务器配置
     * @param isConnected 是否连接
     * @param errorMessage 错误信息
     * @param additionalInfo 额外信息
     * @return 连接状态信息
     */
    public static Map<String, Object> buildConnectionStatus(
            MonitorSysGenServer server, 
            boolean isConnected, 
            String errorMessage,
            Map<String, Object> additionalInfo) {
        
        Map<String, Object> status = new HashMap<>();
        status.put("serverId", server.getMonitorSysGenServerId());
        status.put("serverName", server.getMonitorSysGenServerName());
        status.put("host", server.getMonitorSysGenServerHost());
        status.put("port", server.getMonitorSysGenServerPort());
        status.put("protocol", server.getMonitorSysGenServerProtocol());
        status.put("connected", isConnected);
        status.put("status", isConnected ? "CONNECTED" : "DISCONNECTED");
        status.put("lastCheckTime", System.currentTimeMillis());
        
        if (errorMessage != null) {
            status.put("errorMessage", errorMessage);
        }
        
        if (additionalInfo != null) {
            status.putAll(additionalInfo);
        }
        
        return status;
    }

    /**
     * 合并服务器指标数据
     *
     * @param metrics 指标数据
     * @param additionalData 额外数据
     * @return 合并后的数据
     */
    public static Map<String, Object> mergeMetricsData(MonitorSysGenServerMetrics metrics, Map<String, Object> additionalData) {
        Map<String, Object> result = new HashMap<>();
        
        if (metrics != null) {
            result.put("serverId", metrics.getMonitorSysGenServerId());
            result.put("collectTime", metrics.getMonitorSysGenServerMetricsCollectTime());
            result.put("cpuUsage", metrics.getMonitorSysGenServerMetricsCpuUsage());
            result.put("memoryUsage", metrics.getMonitorSysGenServerMetricsMemoryUsage());
            result.put("diskUsage", metrics.getMonitorSysGenServerMetricsDiskUsage());
            result.put("networkIn", metrics.getMonitorSysGenServerMetricsNetworkIn());
            result.put("networkOut", metrics.getMonitorSysGenServerMetricsNetworkOut());
            result.put("uptime", metrics.getMonitorSysGenServerMetricsUptime());
        }
        
        if (additionalData != null) {
            result.putAll(additionalData);
        }
        
        return result;
    }

    /**
     * 检查服务器是否支持指定操作
     *
     * @param server 服务器配置
     * @param operation 操作类型
     * @return 是否支持
     */
    public static boolean isOperationSupported(MonitorSysGenServer server, String operation) {
        if (server == null || operation == null) {
            return false;
        }
        
        String protocol = server.getMonitorSysGenServerProtocol();
        if (protocol == null) {
            return false;
        }
        
        switch (operation.toUpperCase()) {
            case "SSH":
                return "SSH".equalsIgnoreCase(protocol);
            case "RDP":
                return "RDP".equalsIgnoreCase(protocol);
            case "TELNET":
                return "TELNET".equalsIgnoreCase(protocol);
            case "LOCAL":
                return server.getMonitorSysGenServerIsLocal() != null && server.getMonitorSysGenServerIsLocal() == 1;
            case "PROMETHEUS":
                return "PROMETHEUS".equalsIgnoreCase(server.getMonitorSysGenServerDataReportMethod());
            default:
                return false;
        }
    }
}
