package com.chua.starter.monitor.service.impl;

import com.chua.common.support.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.FileUtils;
import com.chua.common.support.utils.MapUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.monitor.pojo.node.NodeControlRequest;
import com.chua.starter.monitor.pojo.node.NodeMetricsDTO;
import com.chua.starter.monitor.pojo.node.OnlineNodeInfo;
import com.chua.starter.monitor.service.node.NodeControlService;
import com.chua.starter.monitor.service.node.NodeManagementService;
import com.chua.sync.support.server.SyncServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 节点控制服务实现类
 * <p>
 * 实现节点的日志管理、配置管理、重启管理等功能
 * </p>
 *
 * @author CH
 * @version 1.0.0
 * @since 2025/12/08
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NodeControlServiceImpl implements NodeControlService {

    /**
     * Redis缓存键前缀
     */
    private static final String METRICS_KEY_PREFIX = "monitor:metrics:";

    /**
     * Redis缓存过期时间（分钟）
     */
    private static final int METRICS_EXPIRE_MINUTES = 30;

    /**
     * 同步主题常量
     */
    private static final String TOPIC_LOG_LEVEL = "monitor/control/log-level";
    private static final String TOPIC_RESTART = "monitor/control/node-restart";
    private static final String TOPIC_SHUTDOWN = "monitor/control/node-shutdown";
    private static final String TOPIC_CONTROL = "monitor/control/command";

    private final RestTemplate restTemplate;
    private final NodeManagementService nodeManagementService;

    @Autowired(required = false)
    private SyncServer syncServer;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    // ==================== 日志管理 ====================

    @Override
    public ReturnResult<List<String>> getLogFilesForNode(String ipAddress, Integer port) {
        try {
            log.info("获取节点日志文件列表: ipAddress={}, port={}", ipAddress, port);

            // 获取节点信息
            OnlineNodeInfo nodeInfo = nodeManagementService.getOnlineNode(ipAddress, port);
            if (nodeInfo == null) {
                return ReturnResult.error("节点不在线");
            }

            // 构建actuator日志文件接口URL
            String logFileUrl = buildActuatorUrl(nodeInfo, "logfile");

            try {
                HttpHeaders headers = new HttpHeaders();
                headers.set("Accept", "application/json");
                HttpEntity<String> entity = new HttpEntity<>(headers);

                ResponseEntity<Map> response = restTemplate.exchange(logFileUrl, HttpMethod.GET, entity, Map.class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    Map<String, Object> body = response.getBody();
                    List<String> logFiles = new ArrayList<>();
                    // 解析日志文件列表
                    if (body.containsKey("logFiles")) {
                        logFiles.addAll((List<String>) body.get("logFiles"));
                    } else {
                        // 默认返回主日志文件
                        logFiles.add("application.log");
                    }
                    return ReturnResult.success(logFiles);
                }
            } catch (Exception e) {
                log.debug("Actuator日志文件接口不可用，返回默认列表: {}", e.getMessage());
            }

            // 返回默认日志文件列表
            return ReturnResult.success(List.of("application.log"));

        } catch (Exception e) {
            log.error("获取节点日志文件列表失败: ipAddress={}, port={}", ipAddress, port, e);
            return ReturnResult.error("获取节点日志文件列表失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<String> getLogContentForNode(String ipAddress, Integer port, String logFile, Integer lines) {
        try {
            log.info("获取节点日志内容: ipAddress={}, port={}, logFile={}, lines={}", ipAddress, port, logFile, lines);

            // 获取节点信息
            OnlineNodeInfo nodeInfo = nodeManagementService.getOnlineNode(ipAddress, port);
            if (nodeInfo == null) {
                return ReturnResult.error("节点不在线");
            }

            // 构建actuator日志接口URL
            String logUrl = buildActuatorUrl(nodeInfo, "logfile");

            try {
                HttpHeaders headers = new HttpHeaders();
                headers.set("Accept", "text/plain");
                // 添加Range头来获取最后N行
                if (lines != null && lines > 0) {
                    headers.set("Range", "lines=-" + lines);
                }
                HttpEntity<String> entity = new HttpEntity<>(headers);

                ResponseEntity<String> response = restTemplate.exchange(logUrl, HttpMethod.GET, entity, String.class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    return ReturnResult.success(response.getBody());
                } else {
                    return ReturnResult.error("获取日志内容失败: HTTP " + response.getStatusCode());
                }
            } catch (Exception e) {
                log.error("调用Actuator日志接口失败: {}", e.getMessage());
                return ReturnResult.error("获取日志内容失败: " + e.getMessage());
            }

        } catch (Exception e) {
            log.error("获取节点日志内容失败: ipAddress={}, port={}", ipAddress, port, e);
            return ReturnResult.error("获取节点日志内容失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> setLogLevelForNode(NodeControlRequest request) {
        try {
            log.info("设置节点日志级别: request={}", request);

            // 校验参数
            if (StringUtils.isBlank(request.getLoggerName())) {
                return ReturnResult.error("日志器名称不能为空");
            }
            if (StringUtils.isBlank(request.getLogLevel())) {
                return ReturnResult.error("日志级别不能为空");
            }

            // 查找客户端ID
            String clientId = findClientId(request.getIpAddress(), request.getPort());
            if (clientId == null) {
                return ReturnResult.error("节点不在线或未注册");
            }

            // 通过SyncServer发送日志级别设置命令
            Map<String, Object> command = new HashMap<>();
            command.put("action", "SET_LOG_LEVEL");
            command.put("loggerName", request.getLoggerName());
            command.put("level", request.getLogLevel());
            command.put("timestamp", System.currentTimeMillis());

            syncServer.send(clientId, TOPIC_LOG_LEVEL, command);

            log.info("日志级别设置命令已发送: clientId={}, command={}", clientId, command);
            return ReturnResult.success(true);

        } catch (Exception e) {
            log.error("设置节点日志级别失败: request={}", request, e);
            return ReturnResult.error("设置节点日志级别失败: " + e.getMessage());
        }
    }

    // ==================== 配置管理 ====================

    @Override
    public ReturnResult<Map<String, Object>> getEnvironmentForNode(String ipAddress, Integer port) {
        try {
            log.info("获取节点环境配置: ipAddress={}, port={}", ipAddress, port);

            // 获取节点信息
            OnlineNodeInfo nodeInfo = nodeManagementService.getOnlineNode(ipAddress, port);
            if (nodeInfo == null) {
                return ReturnResult.error("节点不在线");
            }

            // 构建actuator环境接口URL
            String envUrl = buildActuatorUrl(nodeInfo, "env");

            try {
                HttpHeaders headers = new HttpHeaders();
                headers.set("Accept", "application/json");
                HttpEntity<String> entity = new HttpEntity<>(headers);

                ResponseEntity<Map> response = restTemplate.exchange(envUrl, HttpMethod.GET, entity, Map.class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    return ReturnResult.success(response.getBody());
                } else {
                    return ReturnResult.error("获取环境配置失败: HTTP " + response.getStatusCode());
                }
            } catch (Exception e) {
                log.error("调用Actuator环境接口失败: {}", e.getMessage());
                return ReturnResult.error("获取环境配置失败: " + e.getMessage());
            }

        } catch (Exception e) {
            log.error("获取节点环境配置失败: ipAddress={}, port={}", ipAddress, port, e);
            return ReturnResult.error("获取节点环境配置失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> getConfigPropsForNode(String ipAddress, Integer port, String prefix) {
        try {
            log.info("获取节点配置属性: ipAddress={}, port={}, prefix={}", ipAddress, port, prefix);

            // 获取节点信息
            OnlineNodeInfo nodeInfo = nodeManagementService.getOnlineNode(ipAddress, port);
            if (nodeInfo == null) {
                return ReturnResult.error("节点不在线");
            }

            // 构建actuator配置属性接口URL
            String configUrl = buildActuatorUrl(nodeInfo, "configprops");
            if (StringUtils.isNotBlank(prefix)) {
                configUrl += "/" + prefix;
            }

            try {
                HttpHeaders headers = new HttpHeaders();
                headers.set("Accept", "application/json");
                HttpEntity<String> entity = new HttpEntity<>(headers);

                ResponseEntity<Map> response = restTemplate.exchange(configUrl, HttpMethod.GET, entity, Map.class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    return ReturnResult.success(response.getBody());
                } else {
                    return ReturnResult.error("获取配置属性失败: HTTP " + response.getStatusCode());
                }
            } catch (Exception e) {
                log.error("调用Actuator配置属性接口失败: {}", e.getMessage());
                return ReturnResult.error("获取配置属性失败: " + e.getMessage());
            }

        } catch (Exception e) {
            log.error("获取节点配置属性失败: ipAddress={}, port={}", ipAddress, port, e);
            return ReturnResult.error("获取节点配置属性失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> getSystemInfoForNode(String ipAddress, Integer port) {
        try {
            log.info("获取节点系统信息: ipAddress={}, port={}", ipAddress, port);

            // 获取节点信息
            OnlineNodeInfo nodeInfo = nodeManagementService.getOnlineNode(ipAddress, port);
            if (nodeInfo == null) {
                return ReturnResult.error("节点不在线");
            }

            Map<String, Object> systemInfo = new HashMap<>();

            // 获取健康信息
            try {
                String healthUrl = buildActuatorUrl(nodeInfo, "health");
                HttpHeaders headers = new HttpHeaders();
                headers.set("Accept", "application/json");
                HttpEntity<String> entity = new HttpEntity<>(headers);

                ResponseEntity<Map> response = restTemplate.exchange(healthUrl, HttpMethod.GET, entity, Map.class);
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    systemInfo.put("health", response.getBody());
                }
            } catch (Exception e) {
                log.debug("获取健康信息失败: {}", e.getMessage());
            }

            // 获取应用信息
            try {
                String infoUrl = buildActuatorUrl(nodeInfo, "info");
                HttpHeaders headers = new HttpHeaders();
                headers.set("Accept", "application/json");
                HttpEntity<String> entity = new HttpEntity<>(headers);

                ResponseEntity<Map> response = restTemplate.exchange(infoUrl, HttpMethod.GET, entity, Map.class);
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    systemInfo.put("info", response.getBody());
                }
            } catch (Exception e) {
                log.debug("获取应用信息失败: {}", e.getMessage());
            }

            // 获取指标信息
            try {
                String metricsUrl = buildActuatorUrl(nodeInfo, "metrics");
                HttpHeaders headers = new HttpHeaders();
                headers.set("Accept", "application/json");
                HttpEntity<String> entity = new HttpEntity<>(headers);

                ResponseEntity<Map> response = restTemplate.exchange(metricsUrl, HttpMethod.GET, entity, Map.class);
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    systemInfo.put("metrics", response.getBody());
                }
            } catch (Exception e) {
                log.debug("获取指标信息失败: {}", e.getMessage());
            }

            return ReturnResult.success(systemInfo);

        } catch (Exception e) {
            log.error("获取节点系统信息失败: ipAddress={}, port={}", ipAddress, port, e);
            return ReturnResult.error("获取节点系统信息失败: " + e.getMessage());
        }
    }

    // ==================== 监控数据管理 ====================

    @Override
    public ReturnResult<NodeMetricsDTO> getNodeMetricsForNode(String ipAddress, Integer port) {
        try {
            log.debug("获取节点监控指标: ipAddress={}, port={}", ipAddress, port);

            if (redisTemplate == null) {
                return ReturnResult.error("Redis不可用");
            }

            String key = METRICS_KEY_PREFIX + ipAddress + ":" + port;
            Object data = redisTemplate.opsForValue().get(key);

            if (data != null) {
                if (data instanceof NodeMetricsDTO) {
                    return ReturnResult.success((NodeMetricsDTO) data);
                } else if (data instanceof Map) {
                    // 兼容Map类型的数据
                    NodeMetricsDTO metrics = Json.fromJson(Json.toJson(data), NodeMetricsDTO.class);
                    return ReturnResult.success(metrics);
                }
            }

            return ReturnResult.error("未找到节点监控数据");

        } catch (Exception e) {
            log.error("获取节点监控指标失败: ipAddress={}, port={}", ipAddress, port, e);
            return ReturnResult.error("获取节点监控指标失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<NodeMetricsDTO>> getNodeMetricsHistoryForNode(String ipAddress, Integer port, Integer minutes) {
        try {
            log.debug("获取节点监控历史数据: ipAddress={}, port={}, minutes={}", ipAddress, port, minutes);

            if (redisTemplate == null) {
                return ReturnResult.error("Redis不可用");
            }

            String keyPattern = METRICS_KEY_PREFIX + "history:" + ipAddress + ":" + port + ":*";
            long now = System.currentTimeMillis();
            long startTime = now - (minutes != null ? minutes : 30) * 60 * 1000L;

            // 获取历史数据
            Set<String> keys = redisTemplate.keys(keyPattern);
            if (keys == null || keys.isEmpty()) {
                return ReturnResult.success(new ArrayList<>());
            }

            List<NodeMetricsDTO> historyList = new ArrayList<>();
            for (String key : keys) {
                try {
                    // 从key中提取时间戳
                    String[] parts = key.split(":");
                    long timestamp = Long.parseLong(parts[parts.length - 1]);

                    if (timestamp >= startTime) {
                        Object data = redisTemplate.opsForValue().get(key);
                        if (data instanceof NodeMetricsDTO) {
                            historyList.add((NodeMetricsDTO) data);
                        } else if (data instanceof Map) {
                            NodeMetricsDTO metrics = Json.fromJson(Json.toJson(data), NodeMetricsDTO.class);
                            historyList.add(metrics);
                        }
                    }
                } catch (Exception e) {
                    log.debug("解析历史数据失败: key={}", key);
                }
            }

            // 按时间戳排序
            historyList.sort(Comparator.comparing(NodeMetricsDTO::getTimestamp));

            return ReturnResult.success(historyList);

        } catch (Exception e) {
            log.error("获取节点监控历史数据失败: ipAddress={}, port={}", ipAddress, port, e);
            return ReturnResult.error("获取节点监控历史数据失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> saveNodeMetricsForNode(NodeMetricsDTO metrics) {
        try {
            log.debug("保存节点监控指标: nodeId={}", metrics.getNodeId());

            if (redisTemplate == null) {
                return ReturnResult.error("Redis不可用");
            }

            // 保存最新数据
            String latestKey = METRICS_KEY_PREFIX + metrics.getIpAddress() + ":" + metrics.getPort();
            redisTemplate.opsForValue().set(latestKey, metrics, Duration.ofMinutes(METRICS_EXPIRE_MINUTES));

            // 保存历史数据
            String historyKey = METRICS_KEY_PREFIX + "history:" + metrics.getIpAddress() + ":" + metrics.getPort()
                    + ":" + metrics.getTimestamp();
            redisTemplate.opsForValue().set(historyKey, metrics, Duration.ofMinutes(METRICS_EXPIRE_MINUTES));

            return ReturnResult.success(true);

        } catch (Exception e) {
            log.error("保存节点监控指标失败: metrics={}", metrics, e);
            return ReturnResult.error("保存节点监控指标失败: " + e.getMessage());
        }
    }

    // ==================== 重启管理 ====================

    @Override
    public ReturnResult<Boolean> restartNodeForNode(String ipAddress, Integer port) {
        try {
            log.info("重启节点: ipAddress={}, port={}", ipAddress, port);

            // 查找客户端ID
            String clientId = findClientId(ipAddress, port);
            if (clientId == null) {
                return ReturnResult.error("节点不在线或未注册");
            }

            // 通过SyncServer发送重启命令
            Map<String, Object> command = new HashMap<>();
            command.put("action", "RESTART");
            command.put("timestamp", System.currentTimeMillis());

            syncServer.send(clientId, TOPIC_RESTART, command);

            log.info("重启命令已发送: clientId={}", clientId);
            return ReturnResult.success(true);

        } catch (Exception e) {
            log.error("重启节点失败: ipAddress={}, port={}", ipAddress, port, e);
            return ReturnResult.error("重启节点失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> shutdownNodeForNode(String ipAddress, Integer port) {
        try {
            log.info("关闭节点: ipAddress={}, port={}", ipAddress, port);

            // 查找客户端ID
            String clientId = findClientId(ipAddress, port);
            if (clientId == null) {
                return ReturnResult.error("节点不在线或未注册");
            }

            // 通过SyncServer发送关闭命令
            Map<String, Object> command = new HashMap<>();
            command.put("action", "SHUTDOWN");
            command.put("timestamp", System.currentTimeMillis());

            syncServer.send(clientId, TOPIC_SHUTDOWN, command);

            log.info("关闭命令已发送: clientId={}", clientId);
            return ReturnResult.success(true);

        } catch (Exception e) {
            log.error("关闭节点失败: ipAddress={}, port={}", ipAddress, port, e);
            return ReturnResult.error("关闭节点失败: " + e.getMessage());
        }
    }

    // ==================== 通用方法 ====================

    @Override
    public ReturnResult<Object> sendControlCommandForNode(NodeControlRequest request) {
        try {
            log.info("发送控制命令: request={}", request);

            // 查找客户端ID
            String clientId = findClientId(request.getIpAddress(), request.getPort());
            if (clientId == null) {
                return ReturnResult.error("节点不在线或未注册");
            }

            // 构建命令
            Map<String, Object> command = new HashMap<>();
            command.put("action", request.getAction());
            command.put("timestamp", System.currentTimeMillis());
            if (request.getParams() != null) {
                command.putAll(request.getParams());
            }

            // 发送命令
            syncServer.send(clientId, TOPIC_CONTROL, command);

            log.info("控制命令已发送: clientId={}, action={}", clientId, request.getAction());
            return ReturnResult.success(true);

        } catch (Exception e) {
            log.error("发送控制命令失败: request={}", request, e);
            return ReturnResult.error("发送控制命令失败: " + e.getMessage());
        }
    }

    @Override
    public boolean isNodeOnlineForNode(String ipAddress, Integer port) {
        try {
            OnlineNodeInfo nodeInfo = nodeManagementService.getOnlineNode(ipAddress, port);
            return nodeInfo != null;
        } catch (Exception e) {
            log.debug("检查节点在线状态失败: ipAddress={}, port={}", ipAddress, port);
            return false;
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 构建Actuator接口URL
     *
     * @param nodeInfo 节点信息
     * @param endpoint 端点名称
     * @return URL
     */
    private String buildActuatorUrl(OnlineNodeInfo nodeInfo, String endpoint) {
        String contextPath = nodeInfo.getContextPath();
        String endpointsUrl =nodeInfo.getActuatorPath();
        return String.format("http://%s:%d/%s",
                nodeInfo.getIpAddress(),
                nodeInfo.getPort(),
                StringUtils.removeStart(
                        FileUtils.normalize(String.format("%s/%s/%s", contextPath, endpointsUrl, endpoint)),
                        "/"));
    }

    /**
     * 查找客户端ID
     *
     * @param ipAddress IP地址
     * @param port      端口
     * @return 客户端ID，未找到返回null
     */
    private String findClientId(String ipAddress, Integer port) {
        if (syncServer == null) {
            log.warn("SyncServer未初始化");
            return null;
        }

        return syncServer.findClientId(ipAddress, port, null);
    }
}
