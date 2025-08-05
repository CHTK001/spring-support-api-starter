package com.chua.starter.monitor.service.impl;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.FileUtils;
import com.chua.common.support.utils.MapUtils;
import com.chua.starter.monitor.starter.pojo.BatchOperationResultDTO;
import com.chua.starter.monitor.starter.pojo.NodeLoggerConfigDTO;
import com.chua.starter.monitor.starter.pojo.NodeLoggerStatsDTO;
import com.chua.starter.monitor.starter.pojo.OnlineNodeInfo;
import com.chua.starter.monitor.starter.service.NodeLoggerConfigService;
import com.chua.starter.monitor.starter.service.NodeManagementService;
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
    private final NodeManagementService nodeManagementService;

    // 内存缓存，用于临时存储节点信息和应用名称
    private static final Map<String, String> nodeApplicationCache = new ConcurrentHashMap<>();
    private static final Map<String, Set<String>> applicationNodesCache = new ConcurrentHashMap<>();

    @Override
    public ReturnResult<List<NodeLoggerConfigDTO>> getNodeLoggers(final String nodeUrl) {
        try {
            log.info("获取节点日志器配置: nodeUrl={}", nodeUrl);

            // 直接从远程节点获取配置（已经是 DTO 格式）
            final ReturnResult<List<NodeLoggerConfigDTO>> remoteResult = fetchRemoteLoggers(nodeUrl);
            if (!remoteResult.isSuccess()) {
                return ReturnResult.error("获取远程节点配置失败: " + remoteResult.getMsg());
            }

            final List<NodeLoggerConfigDTO> configs = remoteResult.getData();

            // 注册节点到应用缓存（如果有应用名称）
            if (!configs.isEmpty()) {
                final NodeLoggerConfigDTO firstConfig = configs.get(0);
                if (firstConfig.getApplicationName() != null) {
                    registerNodeToApplication(nodeUrl, firstConfig.getApplicationName());
                }
            }

            log.info("获取节点日志器配置成功: nodeUrl={}, 配置数量={}", nodeUrl, configs.size());
            return ReturnResult.success(configs);

        } catch (final Exception e) {
            log.error("获取节点日志器配置失败: nodeUrl={}", nodeUrl, e);
            return ReturnResult.error("获取节点日志器配置失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<NodeLoggerConfigDTO> getLoggerConfig(final String nodeUrl, final String loggerName) {
        try {
            log.info("获取日志器详细配置: nodeUrl={}, loggerName={}", nodeUrl, loggerName);

            // 获取所有日志器配置
            final ReturnResult<List<NodeLoggerConfigDTO>> allLoggersResult = getNodeLoggers(nodeUrl);
            if (!allLoggersResult.isSuccess()) {
                return ReturnResult.error("获取节点配置失败");
            }

            // 查找指定的日志器
            final Optional<NodeLoggerConfigDTO> targetLogger = allLoggersResult.getData().stream()
                    .filter(config -> loggerName.equals(config.getLoggerName())).findFirst();

            if (targetLogger.isPresent()) {
                return ReturnResult.success(targetLogger.get());
            } else {
                return ReturnResult.error("未找到指定的日志器配置");
            }

        } catch (final Exception e) {
            log.error("获取日志器详细配置失败: nodeUrl={}, loggerName={}", nodeUrl, loggerName, e);
            return ReturnResult.error("获取日志器详细配置失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> setLoggerLevel(final String nodeUrl, final String loggerName, final String level) {
        try {
            log.info("设置日志器等级: nodeUrl={}, loggerName={}, level={}", nodeUrl, loggerName, level);

            // 直接向远程节点设置日志等级
            final ReturnResult<Boolean> remoteResult = setRemoteLoggerLevel(nodeUrl, loggerName, level);
            if (!remoteResult.isSuccess()) {
                return remoteResult;
            }

            log.info("日志器等级设置成功: nodeUrl={}, loggerName={}, level={}", nodeUrl, loggerName, level);
            return ReturnResult.success(true);

        } catch (final Exception e) {
            log.error("设置日志器等级失败: nodeUrl={}, loggerName={}, level={}", nodeUrl, loggerName, level, e);
            return ReturnResult.error("设置日志器等级失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<BatchOperationResultDTO> setLoggerLevelForAllNodes(final String applicationName,
            final String loggerName, final String level) {
        try {
            log.info("批量设置日志器等级: applicationName={}, loggerName={}, level={}", applicationName, loggerName, level);

            // 创建批量操作结果DTO
            final BatchOperationResultDTO batchResult = new BatchOperationResultDTO();
            batchResult.setAction("SET_LEVEL");

            // 从内存缓存获取应用的节点列表
            final Set<String> nodeUrls = applicationNodesCache.getOrDefault(applicationName, new HashSet<>());

            final List<String> successIds = new ArrayList<>();
            final List<String> failedIds = new ArrayList<>();

            for (final String nodeUrl : nodeUrls) {
                try {
                    final ReturnResult<Boolean> result = setLoggerLevel(nodeUrl, loggerName, level);
                    if (result.isSuccess()) {
                        successIds.add(nodeUrl);
                    } else {
                        failedIds.add(nodeUrl);
                    }
                } catch (final Exception e) {
                    log.error("设置节点日志器等级失败: nodeUrl={}, loggerName={}, level={}", nodeUrl, loggerName, level, e);
                    failedIds.add(nodeUrl);
                }
            }

            // 设置统计结果
            batchResult.setTotal(nodeUrls.size());
            batchResult.setSuccess(successIds.size());
            batchResult.setFailed(failedIds.size());
            batchResult.setSuccessIds(successIds);
            batchResult.setFailedIds(failedIds);

            return ReturnResult.success(batchResult);

        } catch (final Exception e) {
            log.error("批量设置日志器等级失败: applicationName={}, loggerName={}, level={}", applicationName, loggerName, level,
                    e);
            return ReturnResult.error("批量设置日志器等级失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<String>> getNodesByApplicationName(final String applicationName) {
        try {
            log.info("获取应用的所有节点: applicationName={}", applicationName);

            // 从内存缓存获取应用的节点列表
            final Set<String> nodeUrls = applicationNodesCache.getOrDefault(applicationName, new HashSet<>());
            return ReturnResult.success(new ArrayList<>(nodeUrls));

        } catch (final Exception e) {
            log.error("获取应用的所有节点失败: applicationName={}", applicationName, e);
            return ReturnResult.error("获取应用的所有节点失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<String>> getAllApplicationNames() {
        try {
            log.info("获取所有应用名称");

            // 从内存缓存获取所有应用名称
            final Set<String> applicationNames = applicationNodesCache.keySet();
            return ReturnResult.success(new ArrayList<>(applicationNames));

        } catch (final Exception e) {
            log.error("获取所有应用名称失败", e);
            return ReturnResult.error("获取所有应用名称失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<NodeLoggerStatsDTO> getLoggerConfigStats(final String nodeUrl) {
        try {
            log.info("获取节点日志器配置统计: nodeUrl={}", nodeUrl);

            // 获取节点日志器配置
            final ReturnResult<List<NodeLoggerConfigDTO>> loggersResult = getNodeLoggers(nodeUrl);
            if (!loggersResult.isSuccess()) {
                return ReturnResult.error("获取节点配置失败");
            }

            final List<NodeLoggerConfigDTO> loggers = loggersResult.getData();

            // 创建统计DTO
            final NodeLoggerStatsDTO stats = new NodeLoggerStatsDTO();
            stats.setNodeUrl(nodeUrl);
            stats.setTotalLoggers(loggers.size());
            stats.setConfiguredLoggers((int) loggers.stream().mapToLong(l -> l.hasConfiguredLevel() ? 1 : 0).sum());
            stats.setInheritedLoggers((int) loggers.stream().mapToLong(l -> l.isInherited() ? 1 : 0).sum());
            stats.setNodeStatus(loggers.isEmpty() ? "OFFLINE" : "ONLINE");
            stats.setStatsTime(LocalDateTime.now().toString());

            // 解析节点URL获取IP和端口
            if (!loggers.isEmpty()) {
                final NodeLoggerConfigDTO firstLogger = loggers.get(0);
                stats.setIpAddress(firstLogger.getIpAddress());
                stats.setPort(firstLogger.getPort());
                stats.setApplicationName(firstLogger.getApplicationName());
            }

            // 统计各等级的日志器数量
            final NodeLoggerStatsDTO.LevelStatsDTO levelStats = stats.getLevelStats();
            for (final NodeLoggerConfigDTO logger : loggers) {
                final String level = logger.getConfiguredLevel();
                if (level != null) {
                    switch (level.toUpperCase()) {
                    case "TRACE":
                        levelStats.setTraceCount(levelStats.getTraceCount() + 1);
                        break;
                    case "DEBUG":
                        levelStats.setDebugCount(levelStats.getDebugCount() + 1);
                        break;
                    case "INFO":
                        levelStats.setInfoCount(levelStats.getInfoCount() + 1);
                        break;
                    case "WARN":
                        levelStats.setWarnCount(levelStats.getWarnCount() + 1);
                        break;
                    case "ERROR":
                        levelStats.setErrorCount(levelStats.getErrorCount() + 1);
                        break;
                    case "OFF":
                        levelStats.setOffCount(levelStats.getOffCount() + 1);
                        break;
                    }
                }
            }

            return ReturnResult.success(stats);

        } catch (final Exception e) {
            log.error("获取节点日志器配置统计失败: nodeUrl={}", nodeUrl, e);
            return ReturnResult.error("获取节点日志器配置统计失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> resetLoggerLevel(final String nodeUrl, final String loggerName) {
        try {
            log.info("重置日志器等级: nodeUrl={}, loggerName={}", nodeUrl, loggerName);

            // 向远程节点重置日志等级（设置为空字符串表示继承）
            final ReturnResult<Boolean> remoteResult = setRemoteLoggerLevel(nodeUrl, loggerName, "");
            if (!remoteResult.isSuccess()) {
                return remoteResult;
            }

            log.info("日志器等级重置成功: nodeUrl={}, loggerName={}", nodeUrl, loggerName);
            return ReturnResult.success(true);

        } catch (final Exception e) {
            log.error("重置日志器等级失败: nodeUrl={}, loggerName={}", nodeUrl, loggerName, e);
            return ReturnResult.error("重置日志器等级失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<BatchOperationResultDTO> resetLoggerLevelForAllNodes(final String applicationName,
            final String loggerName) {
        try {
            log.info("批量重置日志器等级: applicationName={}, loggerName={}", applicationName, loggerName);

            // 创建批量操作结果DTO
            final BatchOperationResultDTO batchResult = new BatchOperationResultDTO();
            batchResult.setAction("RESET_LEVEL");

            // 从内存缓存获取应用的节点列表
            final Set<String> nodeUrls = applicationNodesCache.getOrDefault(applicationName, new HashSet<>());

            final List<String> successIds = new ArrayList<>();
            final List<String> failedIds = new ArrayList<>();

            for (final String nodeUrl : nodeUrls) {
                try {
                    final ReturnResult<Boolean> result = resetLoggerLevel(nodeUrl, loggerName);
                    if (result.isSuccess()) {
                        successIds.add(nodeUrl);
                    } else {
                        failedIds.add(nodeUrl);
                    }
                } catch (final Exception e) {
                    log.error("重置节点日志器等级失败: nodeUrl={}, loggerName={}", nodeUrl, loggerName, e);
                    failedIds.add(nodeUrl);
                }
            }

            // 设置统计结果
            batchResult.setTotal(nodeUrls.size());
            batchResult.setSuccess(successIds.size());
            batchResult.setFailed(failedIds.size());
            batchResult.setSuccessIds(successIds);
            batchResult.setFailedIds(failedIds);

            return ReturnResult.success(batchResult);

        } catch (final Exception e) {
            log.error("批量重置日志器等级失败: applicationName={}, loggerName={}", applicationName, loggerName, e);
            return ReturnResult.error("批量重置日志器等级失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> checkNodeStatus(final String nodeUrl) {
        try {
            log.info("检查节点状态: nodeUrl={}", nodeUrl);

            // 通过获取日志器配置来检查节点是否在线
            final ReturnResult<List<NodeLoggerConfigDTO>> result = fetchRemoteLoggers(nodeUrl);
            return ReturnResult.success(result.isSuccess());

        } catch (final Exception e) {
            log.error("检查节点状态失败: nodeUrl={}", nodeUrl, e);
            return ReturnResult.success(false);
        }
    }

    @Override
    public void registerNodeToApplication(final String nodeUrl, final String applicationName) {
        try {
            log.info("注册节点到应用: nodeUrl={}, applicationName={}", nodeUrl, applicationName);

            // 更新节点到应用的映射
            nodeApplicationCache.put(nodeUrl, applicationName);

            // 更新应用到节点列表的映射
            applicationNodesCache.computeIfAbsent(applicationName, k -> new HashSet<>()).add(nodeUrl);

        } catch (final Exception e) {
            log.error("注册节点到应用失败: nodeUrl={}, applicationName={}", nodeUrl, applicationName, e);
        }
    }

    @Override
    public void unregisterNodeFromApplication(final String nodeUrl) {
        try {
            log.info("从应用中移除节点: nodeUrl={}", nodeUrl);

            // 获取节点对应的应用名称
            final String applicationName = nodeApplicationCache.remove(nodeUrl);
            if (applicationName != null) {
                // 从应用的节点列表中移除该节点
                final Set<String> nodes = applicationNodesCache.get(applicationName);
                if (nodes != null) {
                    nodes.remove(nodeUrl);
                    // 如果应用没有节点了，移除应用
                    if (nodes.isEmpty()) {
                        applicationNodesCache.remove(applicationName);
                    }
                }
            }

        } catch (final Exception e) {
            log.error("从应用中移除节点失败: nodeUrl={}", nodeUrl, e);
        }
    }

    @Override
    public ReturnResult<List<NodeLoggerConfigDTO>> fetchRemoteLoggers(final String nodeUrl) {
        try {
            log.info("从远程节点获取日志器配置: nodeUrl={}", nodeUrl);

            final String decodedUrl = decodeNodeUrl(nodeUrl);
            final String[] parts = decodedUrl.split(":");
            if (parts.length != 2) {
                return ReturnResult.error("无效的节点URL格式");
            }

            final String ipAddress = parts[0];
            final Integer port = Integer.parseInt(parts[1]);
            final OnlineNodeInfo onlineNodes = nodeManagementService.getOnlineNode(ipAddress, port);
            if (null == onlineNodes) {
                return ReturnResult.error("节点不存在");
            }

            final Map<String, Object> metadata = onlineNodes.getMetadata();
            final String contextPath = MapUtils.getString(metadata, "contextPath");
            final String endpointsUrl = MapUtils.getString(metadata, "endpointsUrl");
            // 构建日志器配置获取URL
            final String loggersUrl = String.format("http://%s:%d/%s", ipAddress, port,
                    com.chua.common.support.utils.StringUtils.removeStart(
                            FileUtils.normalize(String.format("%s/%s/loggers", contextPath, endpointsUrl)), "/"));

            try {
                final HttpHeaders headers = new HttpHeaders();
                headers.set("Accept", "application/json");
                final HttpEntity<String> entity = new HttpEntity<>(headers);

                final ResponseEntity<Map> response = restTemplate.exchange(loggersUrl, HttpMethod.GET, entity,
                        Map.class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    final Map responseBody = response.getBody();
                    final Map<String, Object> loggers = (Map<String, Object>) responseBody.get("loggers");

                    final List<NodeLoggerConfigDTO> loggerList = new ArrayList<>();
                    if (loggers != null) {
                        for (final Map.Entry<String, Object> entry : loggers.entrySet()) {
                            final String loggerName = entry.getKey();
                            final Map<String, Object> loggerInfo = (Map<String, Object>) entry.getValue();

                            // 创建 NodeLoggerConfigDTO 对象
                            final NodeLoggerConfigDTO loggerConfig = new NodeLoggerConfigDTO();
                            loggerConfig.setNodeUrl(nodeUrl);
                            loggerConfig.setIpAddress(ipAddress);
                            loggerConfig.setPort(port);
                            loggerConfig.setApplicationName(onlineNodes.getApplicationName());
                            loggerConfig.setLoggerName(loggerName);
                            loggerConfig.setConfiguredLevel((String) loggerInfo.get("configuredLevel"));
                            loggerConfig.setEffectiveLevel((String) loggerInfo.get("effectiveLevel"));
                            loggerConfig.setAdditive((Boolean) loggerInfo.get("additive"));
                            loggerConfig.setNodeStatus("ONLINE");
                            loggerConfig.setLastCheckTime(LocalDateTime.now().toString());

                            loggerList.add(loggerConfig);
                        }
                    }

                    log.info("成功获取远程日志器配置: nodeUrl={}, 数量={}", nodeUrl, loggerList.size());
                    return ReturnResult.success(loggerList);
                } else {
                    return ReturnResult.error("获取远程日志器配置失败: HTTP " + response.getStatusCode());
                }

            } catch (final Exception e) {
                log.error("调用远程节点API失败: nodeUrl={}", nodeUrl, e);
                return ReturnResult.error("调用远程节点API失败: " + e.getMessage());
            }

        } catch (final Exception e) {
            log.error("从远程节点获取日志器配置失败: nodeUrl={}", nodeUrl, e);
            return ReturnResult.error("从远程节点获取日志器配置失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> setRemoteLoggerLevel(final String nodeUrl, final String loggerName,
            final String level) {
        try {
            log.info("向远程节点设置日志器等级: nodeUrl={}, loggerName={}, level={}", nodeUrl, loggerName, level);

            final String decodedUrl = decodeNodeUrl(nodeUrl);
            final String[] parts = decodedUrl.split(":");
            if (parts.length != 2) {
                return ReturnResult.error("无效的节点URL格式");
            }

            final String ipAddress = parts[0];
            final Integer port = Integer.parseInt(parts[1]);
            final OnlineNodeInfo onlineNodes = nodeManagementService.getOnlineNode(ipAddress, port);
            if (null == onlineNodes) {
                return ReturnResult.error("节点不存在");
            }

            final Map<String, Object> metadata = onlineNodes.getMetadata();
            final String contextPath = MapUtils.getString(metadata, "contextPath");
            final String endpointsUrl = MapUtils.getString(metadata, "endpointsUrl");
            // 构建设置日志器等级URL
            final String setLevelUrl = String
                    .format("http://%s:%d/%s", ipAddress, port,
                            com.chua.common.support.utils.StringUtils.removeStart(
                                    FileUtils.normalize(
                                            String.format("%s/%s/loggers/%s", contextPath, endpointsUrl, loggerName)),
                                    "/"));

            try {
                final HttpHeaders headers = new HttpHeaders();
                headers.set("Content-Type", "application/json");

                final Map<String, Object> requestBody = new HashMap<>();
                if (StringUtils.hasText(level)) {
                    requestBody.put("configuredLevel", level);
                } else {
                    requestBody.put("configuredLevel", null);
                }

                final HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

                final ResponseEntity<String> response = restTemplate.exchange(setLevelUrl, HttpMethod.POST, entity,
                        String.class);

                if (response.getStatusCode().is2xxSuccessful()) {
                    log.info("成功设置远程日志器等级: nodeUrl={}, loggerName={}, level={}", nodeUrl, loggerName, level);
                    return ReturnResult.success(true);
                } else {
                    return ReturnResult.error("设置远程日志器等级失败: HTTP " + response.getStatusCode());
                }

            } catch (final Exception e) {
                log.error("调用远程节点设置API失败: nodeUrl={}, loggerName={}, level={}", nodeUrl, loggerName, level, e);
                return ReturnResult.error("调用远程节点设置API失败: " + e.getMessage());
            }

        } catch (final Exception e) {
            log.error("向远程节点设置日志器等级失败: nodeUrl={}, loggerName={}, level={}", nodeUrl, loggerName, level, e);
            return ReturnResult.error("向远程节点设置日志器等级失败: " + e.getMessage());
        }
    }

    @Override
    public String decodeNodeUrl(final String encodedNodeUrl) {
        try {
            final byte[] decodedBytes = Base64.getDecoder().decode(encodedNodeUrl);
            return new String(decodedBytes, StandardCharsets.UTF_8);
        } catch (final Exception e) {
            log.error("解码节点URL失败: encodedNodeUrl={}", encodedNodeUrl, e);
            throw new IllegalArgumentException("无效的节点URL编码: " + encodedNodeUrl);
        }
    }

    @Override
    public String encodeNodeUrl(final String ipAddress, final Integer port) {
        try {
            final String nodeUrl = ipAddress + ":" + port;
            return Base64.getEncoder().encodeToString(nodeUrl.getBytes(StandardCharsets.UTF_8));
        } catch (final Exception e) {
            log.error("编码节点URL失败: ipAddress={}, port={}", ipAddress, port, e);
            throw new IllegalArgumentException("无效的IP地址或端口: " + ipAddress + ":" + port);
        }
    }
}
