package com.chua.starter.monitor.starter.service.impl;

import com.chua.starter.monitor.starter.pojo.ClientHealthReport;
import com.chua.starter.monitor.starter.pojo.ClientHealthStatus;
import com.chua.starter.monitor.starter.service.ClientHealthService;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerService;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServer;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerSetting;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.json.Json;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 客户端健康状态服务实现
 * 
 * @author CH
 * @since 2024/12/13
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClientHealthServiceImpl implements ClientHealthService {

    private final RedisTemplate<String, String> redisTemplate;
    private final MonitorSysGenServerService serverService;

    private static final String REDIS_KEY_PREFIX = "client:health:";
    private static final String REDIS_STATISTICS_KEY = "client:health:statistics";

    @Override
    public ReturnResult<Void> reportHealth(ClientHealthReport report) {
        try {
            // 验证上报数据
            if (report == null || report.getClientIp() == null || report.getClientPort() == null) {
                return ReturnResult.error("客户端健康状态上报数据不完整");
            }

            // 创建存储对象
            ClientHealthStatus status = ClientHealthStatus.fromReport(report);
            
            // 存储到Redis，设置TTL为10秒
            String key = status.getRedisKey();
            String value = Json.toJson(status);
            
            redisTemplate.opsForValue().set(key, value, 10, TimeUnit.SECONDS);
            
            // 更新统计信息
            updateStatistics(status);
            
            log.debug("客户端健康状态上报成功: {}", status.getClientAddress());
            return ReturnResult.ok();
            
        } catch (Exception e) {
            log.error("客户端健康状态上报失败", e);
            return ReturnResult.error("客户端健康状态上报失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<ClientHealthStatus> getClientHealth(String clientIp, Integer clientPort) {
        try {
            String key = ClientHealthStatus.getRedisKey(clientIp, clientPort);
            String value = redisTemplate.opsForValue().get(key);
            
            if (value == null) {
                return ReturnResult.error("客户端健康状态不存在或已过期");
            }
            
            ClientHealthStatus status = Json.fromJson(value, ClientHealthStatus.class);
            return ReturnResult.ok(status);
            
        } catch (Exception e) {
            log.error("获取客户端健康状态失败", e);
            return ReturnResult.error("获取客户端健康状态失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> isClientHealthy(String clientIp, Integer clientPort) {
        try {
            ReturnResult<ClientHealthStatus> result = getClientHealth(clientIp, clientPort);
            if (!result.isSuccess()) {
                return ReturnResult.ok(false);
            }
            
            ClientHealthStatus status = result.getData();
            boolean online = status != null && status.isOnlineAndValid();
            return ReturnResult.ok(online);
            
        } catch (Exception e) {
            log.error("检查客户端在线状态失败", e);
            return ReturnResult.ok(false);
        }
    }

    @Override
    public ReturnResult<List<ClientHealthStatus>> getAllHealthyClients() {
        try {
            Set<String> keys = redisTemplate.keys(REDIS_KEY_PREFIX + "*");
            if (keys == null || keys.isEmpty()) {
                return ReturnResult.ok(new ArrayList<>());
            }
            
            List<String> values = redisTemplate.opsForValue().multiGet(keys);
            List<ClientHealthStatus> onlineClients = new ArrayList<>();
            
            for (String value : values) {
                if (value != null) {
                    try {
                        ClientHealthStatus status = Json.fromJson(value, ClientHealthStatus.class);
                        if (status.isOnlineAndValid()) {
                            onlineClients.add(status);
                        }
                    } catch (Exception e) {
                        log.warn("解析客户端健康状态数据失败: {}", value, e);
                    }
                }
            }
            
            return ReturnResult.ok(onlineClients);
            
        } catch (Exception e) {
            log.error("获取在线客户端列表失败", e);
            return ReturnResult.error("获取在线客户端列表失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Object> getHealthStatistics() {
        try {
            String value = redisTemplate.opsForValue().get(REDIS_STATISTICS_KEY);
            Map<String, Object> statistics = new HashMap<>();
            
            if (value != null) {
                statistics = Json.fromJson(value, Map.class);
            }
            
            // 实时统计在线客户端数量
            ReturnResult<List<ClientHealthStatus>> onlineResult = getAllHealthyClients();
            if (onlineResult.isSuccess()) {
                List<ClientHealthStatus> onlineClients = onlineResult.getData();
                statistics.put("onlineCount", onlineClients.size());
                statistics.put("lastUpdateTime", LocalDateTime.now());

                // 统计健康状态分布
                long healthyCount = onlineClients.stream().mapToLong(client -> client.isHealthy() ? 1 : 0).sum();
                statistics.put("healthyCount", healthyCount);
                statistics.put("unhealthyCount", onlineClients.size() - healthyCount);
            }
            
            return ReturnResult.ok(statistics);
            
        } catch (Exception e) {
            log.error("获取健康状态统计信息失败", e);
            return ReturnResult.error("获取健康状态统计信息失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Integer> cleanupExpiredHealthRecords() {
        try {
            Set<String> keys = redisTemplate.keys(REDIS_KEY_PREFIX + "*");
            if (keys == null || keys.isEmpty()) {
                return ReturnResult.ok(0);
            }
            
            int cleanedCount = 0;
            for (String key : keys) {
                String value = redisTemplate.opsForValue().get(key);
                if (value != null) {
                    try {
                        ClientHealthStatus status = Json.fromJson(value, ClientHealthStatus.class);
                        if (status.isExpired()) {
                            redisTemplate.delete(key);
                            cleanedCount++;
                        }
                    } catch (Exception e) {
                        // 数据格式错误，直接删除
                        redisTemplate.delete(key);
                        cleanedCount++;
                    }
                }
            }
            
            log.info("清理过期的客户端健康状态记录: {} 条", cleanedCount);
            return ReturnResult.ok(cleanedCount);
            
        } catch (Exception e) {
            log.error("清理过期记录失败", e);
            return ReturnResult.error("清理过期记录失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Boolean>> batchCheckOnlineStatus(List<String> clientAddresses) {
        try {
            Map<String, Boolean> statusMap = new HashMap<>();
            
            for (String address : clientAddresses) {
                String[] parts = address.split(":");
                if (parts.length == 2) {
                    try {
                        String ip = parts[0];
                        Integer port = Integer.parseInt(parts[1]);
                        ReturnResult<Boolean> result = isClientHealthy(ip, port);
                        statusMap.put(address, result.isSuccess() && result.getData());
                    } catch (NumberFormatException e) {
                        statusMap.put(address, false);
                    }
                } else {
                    statusMap.put(address, false);
                }
            }
            
            return ReturnResult.ok(statusMap);
            
        } catch (Exception e) {
            log.error("批量检查客户端在线状态失败", e);
            return ReturnResult.error("批量检查客户端在线状态失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<ClientHealthStatus> getServerClientHealth(Integer serverId) {
        try {
            // 获取服务器配置
            ReturnResult<MonitorSysGenServer> serverResult = serverService.getById(serverId);
            if (!serverResult.isSuccess()) {
                return ReturnResult.error("服务器不存在");
            }
            
            MonitorSysGenServer server = serverResult.getData();
            
            // 获取服务器设置
            ReturnResult<MonitorSysGenServerSetting> settingResult = serverService.getServerSetting(serverId);
            if (!settingResult.isSuccess()) {
                return ReturnResult.error("服务器配置不存在");
            }
            
            MonitorSysGenServerSetting setting = settingResult.getData();
            
            // 检查是否需要客户端健康状态验证
            if (!setting.requiresClientHealthCheck()) {
                return ReturnResult.error("服务器未启用客户端健康状态验证");
            }
            
            // 获取API配置
            var apiConfig = setting.getFileManagementApiConfigObject();
            String clientHost = apiConfig.useClientAddress() ? null : apiConfig.getApiHost();
            Integer clientPort = apiConfig.useClientPort() ? null : apiConfig.getApiPort();
            
            // 如果配置为使用客户端地址，需要从其他地方获取
            if (clientHost == null || clientPort == null) {
                return ReturnResult.error("无法确定客户端地址，请检查文件管理API配置");
            }
            
            return getClientHealth(clientHost, clientPort);
            
        } catch (Exception e) {
            log.error("获取服务器客户端健康状态失败", e);
            return ReturnResult.error("获取服务器客户端健康状态失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> isServerClientHealthy(Integer serverId) {
        try {
            ReturnResult<ClientHealthStatus> result = getServerClientHealth(serverId);
            if (!result.isSuccess()) {
                return ReturnResult.ok(false);
            }
            
            ClientHealthStatus status = result.getData();
            boolean online = status != null && status.isOnlineAndValid();
            return ReturnResult.ok(online);
            
        } catch (Exception e) {
            log.error("检查服务器客户端在线状态失败", e);
            return ReturnResult.ok(false);
        }
    }

    /**
     * 更新统计信息
     */
    private void updateStatistics(ClientHealthStatus status) {
        try {
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("lastReportTime", LocalDateTime.now());
            statistics.put("lastReportClient", status.getClientAddress());
            
            String value = Json.toJson(statistics);
            redisTemplate.opsForValue().set(REDIS_STATISTICS_KEY, value, 1, TimeUnit.HOURS);
            
        } catch (Exception e) {
            log.warn("更新统计信息失败", e);
        }
    }
}
