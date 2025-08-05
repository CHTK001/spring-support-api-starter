package com.chua.starter.monitor.service.impl;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.pojo.NodeLoggerConfigDTO;
import com.chua.starter.monitor.service.NodeLoggerConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 节点日志器配置服务实现类
 *
 * 纯粹通过 Spring Boot Actuator 端点实现节点日志器配置的远程管理 不涉及数据库操作，所有数据都是实时从节点获取
 *
 * @author CH
 * @since 2025/01/17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NodeLoggerConfigServiceImpl implements NodeLoggerConfigService {

    private final RestTemplate restTemplate;

    // 内存缓存，用于临时存储节点信息和应用名称
    private final Map<String, String> nodeApplicationCache = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> applicationNodesCache = new ConcurrentHashMap<>();

    @Override
    public ReturnResult<List<NodeLoggerConfigDTO>> getNodeLoggers(String nodeUrl) {
        try {
            log.info("获取节点日志器配置: nodeUrl={}", nodeUrl);

            // 直接从远程节点获取配置
            ReturnResult<List<Map<String, Object>>> remoteResult = fetchRemoteLoggers(nodeUrl);
            if (!remoteResult.isSuccess()) {
                return ReturnResult.error("获取远程节点配置失败");
            }

            // 解析节点URL获取IP和端口
            String decodedUrl = decodeNodeUrl(nodeUrl);
            String[] parts = decodedUrl.split(":");
            if (parts.length != 2) {
                return ReturnResult.error("无效的节点URL格式");
            }

            String ipAddress = parts[0];
            Integer port = Integer.parseInt(parts[1]);

            // 转换为 DTO 列表
            List<NodeLoggerConfigDTO> configs = new ArrayList<>();
            List<Map<String, Object>> loggers = remoteResult.getData();

            if (loggers != null) {
                for (Map<String, Object> loggerData : loggers) {
                    String loggerName = (String) loggerData.get("name");
                    String configuredLevel = (String) loggerData.get("configuredLevel");
                    String effectiveLevel = (String) loggerData.get("effectiveLevel");
                    Boolean additive = (Boolean) loggerData.get("additive");

                    NodeLoggerConfigDTO config = new NodeLoggerConfigDTO(nodeUrl, ipAddress, port, "unknown",
                            loggerName, configuredLevel, effectiveLevel, additive);
                    config.setNodeStatus("ONLINE");
                    config.setLastCheckTime(LocalDateTime.now().toString());

                    configs.add(config);
                }
            }

            log.info("获取节点日志器配置成功: nodeUrl={}, 配置数量={}", nodeUrl, configs.size());
            return ReturnResult.success(configs);

        } catch (Exception e) {
            log.error("获取节点日志器配置失败: nodeUrl={}", nodeUrl, e);
            return ReturnResult.error("获取节点日志器配置失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<NodeLoggerConfigDTO> getLoggerConfig(String nodeUrl, String loggerName) {
        try {
            log.info("获取日志器详细配置: nodeUrl={}, loggerName={}", nodeUrl, loggerName);

            // 获取所有日志器配置
            ReturnResult<List<NodeLoggerConfigDTO>> allLoggersResult = getNodeLoggers(nodeUrl);
            if (!allLoggersResult.isSuccess()) {
                return ReturnResult.error("获取节点配置失败");
            }

            // 查找指定的日志器
            Optional<NodeLoggerConfigDTO> targetLogger = allLoggersResult.getData().stream()
                    .filter(config -> loggerName.equals(config.getLoggerName())).findFirst();

            if (targetLogger.isPresent()) {
                return ReturnResult.success(targetLogger.get());
            } else {
                return ReturnResult.error("未找到指定的日志器配置");
            }

        } catch (Exception e) {
            log.error("获取日志器详细配置失败: nodeUrl={}, loggerName={}", nodeUrl, loggerName, e);
            return ReturnResult.error("获取日志器详细配置失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> setLoggerLevel(String nodeUrl, String loggerName, String level) {
        try {
            log.info("设置日志器等级: nodeUrl={}, loggerName={}, level={}", nodeUrl, loggerName, level);

            // 直接向远程节点设置日志等级
            ReturnResult<Boolean> remoteResult = setRemoteLoggerLevel(nodeUrl, loggerName, level);
            if (!remoteResult.isSuccess()) {
                return remoteResult;
            }

            log.info("日志器等级设置成功: nodeUrl={}, loggerName={}, level={}", nodeUrl, loggerName, level);
            return ReturnResult.success(true);

        } catch (Exception e) {
            log.error("设置日志器等级失败: nodeUrl={}, loggerName={}, level={}", nodeUrl, loggerName, level, e);
            return ReturnResult.error("设置日志器等级失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Boolean>> setLoggerLevelForAllNodes(String applicationName, String loggerName,
            String level) {
        try {
            log.info("批量设置日志器等级: applicationName={}, loggerName={}, level={}", applicationName, loggerName, level);

            // 从缓存中获取应用的所有节点
            Set<String> nodeUrls = applicationNodesCache.getOrDefault(applicationName, new HashSet<>());
            Map<String, Boolean> results = new HashMap<>();

            for (String nodeUrl : nodeUrls) {
                try {
                    ReturnResult<Boolean> result = setLoggerLevel(nodeUrl, loggerName, level);
                    results.put(nodeUrl, result.isSuccess());
                } catch (Exception e) {
                    log.error("设置节点日志器等级失败: nodeUrl={}, loggerName={}, level={}", nodeUrl, loggerName, level, e);
                    results.put(nodeUrl, false);
                }
            }

            return ReturnResult.success(results);

        } catch (Exception e) {
            log.error("批量设置日志器等级失败: applicationName={}, loggerName={}, level={}", applicationName, loggerName, level,
                    e);
            return ReturnResult.error("批量设置日志器等级失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<String>> getNodesByApplicationName(String applicationName) {
        try {
            log.info("获取应用的所有节点: applicationName={}", applicationName);

            Set<String> nodeUrls = applicationNodesCache.getOrDefault(applicationName, new HashSet<>());
            return ReturnResult.success(new ArrayList<>(nodeUrls));

        } catch (Exception e) {
            log.error("获取应用的所有节点失败: applicationName={}", applicationName, e);
            return ReturnResult.error("获取应用的所有节点失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<NodeLoggerConfigDTO>> refreshNodeLoggers(String nodeUrl) {
        try {
            log.info("刷新节点日志器配置: nodeUrl={}", nodeUrl);

            // 直接重新获取节点配置
            return getNodeLoggers(nodeUrl);

        } catch (Exception e) {
            log.error("刷新节点日志器配置失败: nodeUrl={}", nodeUrl, e);
            return ReturnResult.error("刷新节点日志器配置失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<String>> getAllApplicationNames() {
        try {
            log.info("获取所有应用名称");

            Set<String> applicationNames = applicationNodesCache.keySet();
            return ReturnResult.success(new ArrayList<>(applicationNames));

        } catch (Exception e) {
            log.error("获取所有应用名称失败", e);
            return ReturnResult.error("获取所有应用名称失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> getLoggerConfigStats(String nodeUrl) {
        try {
            log.info("获取节点日志器配置统计: nodeUrl={}", nodeUrl);

            // 获取节点所有日志器配置
            ReturnResult<List<NodeLoggerConfigDTO>> loggersResult = getNodeLoggers(nodeUrl);
            if (!loggersResult.isSuccess()) {
                return ReturnResult.error("获取节点配置失败");
            }

            List<NodeLoggerConfigDTO> loggers = loggersResult.getData();
            Map<String, Object> stats = new HashMap<>();

            stats.put("totalLoggers", loggers.size());
            stats.put("configuredLoggers",
                    loggers.stream().mapToLong(config -> config.hasConfiguredLevel() ? 1 : 0).sum());
            stats.put("inheritedLoggers", loggers.stream().mapToLong(config -> config.isInherited() ? 1 : 0).sum());
            stats.put("lastCheckTime", LocalDateTime.now().toString());

            return ReturnResult.success(stats);

        } catch (Exception e) {
            log.error("获取节点日志器配置统计失败: nodeUrl={}", nodeUrl, e);
            return ReturnResult.error("获取节点日志器配置统计失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> resetLoggerLevel(String nodeUrl, String loggerName) {
        try {
            log.info("重置日志器等级: nodeUrl={}, loggerName={}", nodeUrl, loggerName);

            // 向远程节点重置日志等级（设置为空字符串表示继承）
            ReturnResult<Boolean> remoteResult = setRemoteLoggerLevel(nodeUrl, loggerName, "");
            if (!remoteResult.isSuccess()) {
                return remoteResult;
            }

            log.info("日志器等级重置成功: nodeUrl={}, loggerName={}", nodeUrl, loggerName);
            return ReturnResult.success(true);

        } catch (Exception e) {
            log.error("重置日志器等级失败: nodeUrl={}, loggerName={}", nodeUrl, loggerName, e);
            return ReturnResult.error("重置日志器等级失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> checkNodeStatus(String nodeUrl) {
        try {
            log.info("检查节点状态: nodeUrl={}", nodeUrl);

            String decodedUrl = decodeNodeUrl(nodeUrl);
            String[] parts = decodedUrl.split(":");
            if (parts.length != 2) {
                return ReturnResult.error("无效的节点URL格式");
            }

            String ipAddress = parts[0];
            Integer port = Integer.parseInt(parts[1]);

            // 构建健康检查URL
            String healthUrl = String.format("http://%s:%d/actuator/health", ipAddress, port);

            try {
                ResponseEntity<String> response = restTemplate.getForEntity(healthUrl, String.class);
                boolean isOnline = response.getStatusCode().is2xxSuccessful();

                log.info("节点状态检查完成: nodeUrl={}, isOnline={}", nodeUrl, isOnline);
                return ReturnResult.success(isOnline);

            } catch (Exception e) {
                log.warn("节点不可访问: nodeUrl={}, error={}", nodeUrl, e.getMessage());
                return ReturnResult.success(false);
            }

        } catch (Exception e) {
            log.error("检查节点状态失败: nodeUrl={}", nodeUrl, e);
            return ReturnResult.error("检查节点状态失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<Map<String, Object>>> fetchRemoteLoggers(String nodeUrl) {
        try {
            log.info("从远程节点获取日志器配置: nodeUrl={}", nodeUrl);

            String decodedUrl = decodeNodeUrl(nodeUrl);
            String[] parts = decodedUrl.split(":");
            if (parts.length != 2) {
                return ReturnResult.error("无效的节点URL格式");
            }

            String ipAddress = parts[0];
            Integer port = Integer.parseInt(parts[1]);

            // 构建日志器配置获取URL
            String loggersUrl = String.format("http://%s:%d/actuator/loggers", ipAddress, port);

            try {
                HttpHeaders headers = new HttpHeaders();
                headers.set("Accept", "application/json");
                HttpEntity<String> entity = new HttpEntity<>(headers);

                ResponseEntity<Map> response = restTemplate.exchange(loggersUrl, HttpMethod.GET, entity, Map.class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    Map<String, Object> responseBody = response.getBody();
                    Map<String, Object> loggers = (Map<String, Object>) responseBody.get("loggers");

                    List<Map<String, Object>> loggerList = new ArrayList<>();
                    if (loggers != null) {
                        for (Map.Entry<String, Object> entry : loggers.entrySet()) {
                            String loggerName = entry.getKey();
                            Map<String, Object> loggerInfo = (Map<String, Object>) entry.getValue();

                            Map<String, Object> loggerData = new HashMap<>();
                            loggerData.put("name", loggerName);
                            loggerData.put("configuredLevel", loggerInfo.get("configuredLevel"));
                            loggerData.put("effectiveLevel", loggerInfo.get("effectiveLevel"));
                            loggerData.put("additive", loggerInfo.get("additive"));

                            loggerList.add(loggerData);
                        }
                    }

                    log.info("成功获取远程日志器配置: nodeUrl={}, 数量={}", nodeUrl, loggerList.size());
                    return ReturnResult.success(loggerList);
                } else {
                    return ReturnResult.error("获取远程日志器配置失败: HTTP " + response.getStatusCode());
                }

            } catch (Exception e) {
                log.error("调用远程节点API失败: nodeUrl={}", nodeUrl, e);
                return ReturnResult.error("调用远程节点API失败: " + e.getMessage());
            }

        } catch (Exception e) {
            log.error("从远程节点获取日志器配置失败: nodeUrl={}", nodeUrl, e);
            return ReturnResult.error("从远程节点获取日志器配置失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> setRemoteLoggerLevel(String nodeUrl, String loggerName, String level) {
        try {
            log.info("向远程节点设置日志器等级: nodeUrl={}, loggerName={}, level={}", nodeUrl, loggerName, level);

            String decodedUrl = decodeNodeUrl(nodeUrl);
            String[] parts = decodedUrl.split(":");
            if (parts.length != 2) {
                return ReturnResult.error("无效的节点URL格式");
            }

            String ipAddress = parts[0];
            Integer port = Integer.parseInt(parts[1]);

            // 构建设置日志器等级URL
            String setLevelUrl = String.format("http://%s:%d/actuator/loggers/%s", ipAddress, port,
                    loggerName.replace(".", "%2E"));

            try {
                HttpHeaders headers = new HttpHeaders();
                headers.set("Content-Type", "application/json");

                Map<String, Object> requestBody = new HashMap<>();
                if (StringUtils.hasText(level)) {
                    requestBody.put("configuredLevel", level);
                } else {
                    requestBody.put("configuredLevel", null);
                }

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

                ResponseEntity<String> response = restTemplate.exchange(setLevelUrl, HttpMethod.POST, entity,
                        String.class);

                if (response.getStatusCode().is2xxSuccessful()) {
                    log.info("成功设置远程日志器等级: nodeUrl={}, loggerName={}, level={}", nodeUrl, loggerName, level);
                    return ReturnResult.success(true);
                } else {
                    return ReturnResult.error("设置远程日志器等级失败: HTTP " + response.getStatusCode());
                }

            } catch (Exception e) {
                log.error("调用远程节点设置API失败: nodeUrl={}, loggerName={}, level={}", nodeUrl, loggerName, level, e);
                return ReturnResult.error("调用远程节点设置API失败: " + e.getMessage());
            }

        } catch (Exception e) {
            log.error("向远程节点设置日志器等级失败: nodeUrl={}, loggerName={}, level={}", nodeUrl, loggerName, level, e);
            return ReturnResult.error("向远程节点设置日志器等级失败: " + e.getMessage());
        }
    }

    @Override
    public String decodeNodeUrl(String encodedNodeUrl) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(encodedNodeUrl);
            return new String(decodedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("解码节点URL失败: encodedNodeUrl={}", encodedNodeUrl, e);
            throw new IllegalArgumentException("无效的节点URL编码: " + encodedNodeUrl);
        }
    }

    @Override
    public String encodeNodeUrl(String ipAddress, Integer port) {
        try {
            String nodeUrl = ipAddress + ":" + port;
            return Base64.getEncoder().encodeToString(nodeUrl.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("编码节点URL失败: ipAddress={}, port={}", ipAddress, port, e);
            throw new IllegalArgumentException("无效的IP地址或端口: " + ipAddress + ":" + port);
        }
    }

    /**
     * 注册节点到应用缓存
     *
     * @param nodeUrl         节点URL
     * @param applicationName 应用名称
     */
    public void registerNodeToApplication(String nodeUrl, String applicationName) {
        nodeApplicationCache.put(nodeUrl, applicationName);
        applicationNodesCache.computeIfAbsent(applicationName, k -> new HashSet<>()).add(nodeUrl);
        log.debug("注册节点到应用缓存: nodeUrl={}, applicationName={}", nodeUrl, applicationName);
    }

    /**
     * 从应用缓存中移除节点
     *
     * @param nodeUrl 节点URL
     */
    public void unregisterNodeFromApplication(String nodeUrl) {
        String applicationName = nodeApplicationCache.remove(nodeUrl);
        if (applicationName != null) {
            Set<String> nodes = applicationNodesCache.get(applicationName);
            if (nodes != null) {
                nodes.remove(nodeUrl);
                if (nodes.isEmpty()) {
                    applicationNodesCache.remove(applicationName);
                }
            }
            log.debug("从应用缓存中移除节点: nodeUrl={}, applicationName={}", nodeUrl, applicationName);
        }
    }
}
