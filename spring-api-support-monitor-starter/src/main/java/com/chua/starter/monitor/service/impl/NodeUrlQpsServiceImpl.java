package com.chua.starter.monitor.service.impl;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.FileUtils;
import com.chua.common.support.utils.MapUtils;
import com.chua.starter.monitor.pojo.node.NodeUrlMappingDTO;
import com.chua.starter.monitor.pojo.node.NodeUrlQpsDTO;
import com.chua.starter.monitor.pojo.node.OnlineNodeInfo;
import com.chua.starter.monitor.service.node.NodeManagementService;
import com.chua.starter.monitor.service.node.NodeUrlQpsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 节点 URL QPS 统计服务实现类
 * <p>
 * 接收客户端上报的 URL QPS 数据，提供查询接口
 * </p>
 *
 * @author CH
 * @version 1.0.0
 * @since 2025/01/17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NodeUrlQpsServiceImpl implements NodeUrlQpsService {

    private final RestTemplate restTemplate;
    private final NodeManagementService nodeManagementService;

    /**
     * QPS 数据缓存
     * key: nodeUrl (ip:port)
     * value: URL QPS 数据列表
     */
    private final Map<String, List<NodeUrlQpsDTO>> qpsDataCache = new ConcurrentHashMap<>();

    /**
     * 最后更新时间缓存
     */
    private final Map<String, Long> lastUpdateTimeCache = new ConcurrentHashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public void handleQpsReport(Map<String, Object> data) {
        try {
            String appName = MapUtils.getString(data, "appName");
            String host = MapUtils.getString(data, "host");
            Long timestamp = MapUtils.getLong(data, "timestamp", System.currentTimeMillis());
            List<Map<String, Object>> stats = MapUtils.getList(data, "stats");

            if (stats == null || stats.isEmpty()) {
                return;
            }

            // 构建节点 key
            String nodeKey = host;
            log.debug("[UrlQpsService] 收到 QPS 上报: appName={}, host={}, statsCount={}", 
                    appName, host, stats.size());

            // 转换并缓存数据
            List<NodeUrlQpsDTO> qpsList = new ArrayList<>();
            for (Map<String, Object> stat : stats) {
                NodeUrlQpsDTO dto = new NodeUrlQpsDTO();
                dto.setUrl(MapUtils.getString(stat, "url"));
                dto.setMethod(MapUtils.getString(stat, "method"));
                dto.setCount(MapUtils.getLong(stat, "count", 0L));
                dto.setSuccessCount(MapUtils.getLong(stat, "successCount", 0L));
                dto.setFailCount(MapUtils.getLong(stat, "failCount", 0L));
                dto.setAvgDuration(MapUtils.getLong(stat, "avgDuration", 0L));
                dto.setMinDuration(MapUtils.getLong(stat, "minDuration", 0L));
                dto.setMaxDuration(MapUtils.getLong(stat, "maxDuration", 0L));
                dto.setQps(MapUtils.getDouble(stat, "qps", 0.0));
                dto.setHistoryTotalCount(MapUtils.getLong(stat, "historyTotalCount", 0L));
                dto.setHistorySuccessCount(MapUtils.getLong(stat, "historySuccessCount", 0L));
                dto.setHistoryFailCount(MapUtils.getLong(stat, "historyFailCount", 0L));

                // 状态码统计
                Map<String, Object> statusCodesMap = MapUtils.getMap(stat, "statusCodes");
                if (statusCodesMap != null) {
                    Map<String, Long> statusCodes = new HashMap<>();
                    for (Map.Entry<String, Object> entry : statusCodesMap.entrySet()) {
                        statusCodes.put(entry.getKey(), 
                                entry.getValue() instanceof Number ? ((Number) entry.getValue()).longValue() : 0L);
                    }
                    dto.setStatusCodes(statusCodes);
                }

                qpsList.add(dto);
            }

            // 更新缓存
            qpsDataCache.put(nodeKey, qpsList);
            lastUpdateTimeCache.put(nodeKey, timestamp);

        } catch (Exception e) {
            log.error("[UrlQpsService] 处理 QPS 上报失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public ReturnResult<List<NodeUrlQpsDTO>> getNodeUrlQps(String nodeUrl) {
        try {
            log.info("获取节点 URL QPS: nodeUrl={}", nodeUrl);

            String decodedUrl = decodeNodeUrl(nodeUrl);
            List<NodeUrlQpsDTO> qpsList = qpsDataCache.get(decodedUrl);

            if (qpsList == null) {
                return ReturnResult.success(Collections.emptyList());
            }

            // 按历史总请求数降序排序
            qpsList.sort((a, b) -> Long.compare(
                    b.getHistoryTotalCount() != null ? b.getHistoryTotalCount() : 0,
                    a.getHistoryTotalCount() != null ? a.getHistoryTotalCount() : 0));

            log.info("获取节点 URL QPS 成功: nodeUrl={}, 数量={}", nodeUrl, qpsList.size());
            return ReturnResult.success(qpsList);

        } catch (Exception e) {
            log.error("获取节点 URL QPS 失败: nodeUrl={}", nodeUrl, e);
            return ReturnResult.error("获取 URL QPS 失败: " + e.getMessage());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public ReturnResult<List<NodeUrlMappingDTO>> getNodeUrlMappings(String nodeUrl) {
        try {
            log.info("获取节点 URL 映射: nodeUrl={}", nodeUrl);

            String decodedUrl = decodeNodeUrl(nodeUrl);
            String[] parts = decodedUrl.split(":");
            if (parts.length != 2) {
                return ReturnResult.error("无效的节点URL格式");
            }

            String ipAddress = parts[0];
            int port = Integer.parseInt(parts[1]);

            // 获取在线节点信息
            OnlineNodeInfo nodeInfo = nodeManagementService.getOnlineNode(ipAddress, port);
            if (nodeInfo == null) {
                return ReturnResult.error("节点不在线");
            }

            String contextPath = nodeInfo.getContextPath();
            String actuatorPath = nodeInfo.getActuatorPath();

            // 构建 mappings 端点 URL
            String mappingsUrl = String.format("http://%s:%d/%s", ipAddress, port,
                    com.chua.common.support.utils.StringUtils.removeStart(
                            FileUtils.normalize(String.format("%s/%s/mappings", contextPath, actuatorPath)), "/"));

            // 调用 actuator mappings 端点
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(mappingsUrl, HttpMethod.GET, entity, Map.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return ReturnResult.error("获取 URL 映射失败: HTTP " + response.getStatusCode());
            }

            // 解析 mappings 数据
            List<NodeUrlMappingDTO> mappings = parseMappingsResponse(response.getBody());

            log.info("获取节点 URL 映射成功: nodeUrl={}, 数量={}", nodeUrl, mappings.size());
            return ReturnResult.success(mappings);

        } catch (Exception e) {
            log.error("获取节点 URL 映射失败: nodeUrl={}", nodeUrl, e);
            return ReturnResult.error("获取 URL 映射失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> getNodeQpsSummary(String nodeUrl) {
        try {
            log.info("获取节点 QPS 汇总: nodeUrl={}", nodeUrl);

            String decodedUrl = decodeNodeUrl(nodeUrl);
            List<NodeUrlQpsDTO> qpsList = qpsDataCache.get(decodedUrl);

            Map<String, Object> summary = new HashMap<>();
            summary.put("nodeUrl", nodeUrl);
            summary.put("lastUpdateTime", lastUpdateTimeCache.get(decodedUrl));

            if (qpsList == null || qpsList.isEmpty()) {
                summary.put("totalUrls", 0);
                summary.put("totalRequests", 0L);
                summary.put("totalSuccess", 0L);
                summary.put("totalFail", 0L);
                summary.put("avgQps", 0.0);
                return ReturnResult.success(summary);
            }

            long totalRequests = 0;
            long totalSuccess = 0;
            long totalFail = 0;
            double totalQps = 0;

            for (NodeUrlQpsDTO dto : qpsList) {
                totalRequests += dto.getHistoryTotalCount() != null ? dto.getHistoryTotalCount() : 0;
                totalSuccess += dto.getHistorySuccessCount() != null ? dto.getHistorySuccessCount() : 0;
                totalFail += dto.getHistoryFailCount() != null ? dto.getHistoryFailCount() : 0;
                totalQps += dto.getQps() != null ? dto.getQps() : 0;
            }

            summary.put("totalUrls", qpsList.size());
            summary.put("totalRequests", totalRequests);
            summary.put("totalSuccess", totalSuccess);
            summary.put("totalFail", totalFail);
            summary.put("avgQps", totalQps);
            summary.put("successRate", totalRequests > 0 ? 
                    String.format("%.2f%%", totalSuccess * 100.0 / totalRequests) : "0%");

            log.info("获取节点 QPS 汇总成功: nodeUrl={}", nodeUrl);
            return ReturnResult.success(summary);

        } catch (Exception e) {
            log.error("获取节点 QPS 汇总失败: nodeUrl={}", nodeUrl, e);
            return ReturnResult.error("获取 QPS 汇总失败: " + e.getMessage());
        }
    }

    @Override
    public void clearNodeQpsData(String nodeUrl) {
        try {
            String decodedUrl = decodeNodeUrl(nodeUrl);
            qpsDataCache.remove(decodedUrl);
            lastUpdateTimeCache.remove(decodedUrl);
            log.info("清除节点 QPS 数据: nodeUrl={}", nodeUrl);
        } catch (Exception e) {
            log.error("清除节点 QPS 数据失败: nodeUrl={}", nodeUrl, e);
        }
    }

    /**
     * 解析 actuator mappings 响应
     *
     * @param responseBody 响应体
     * @return URL 映射列表
     */
    @SuppressWarnings("unchecked")
    private List<NodeUrlMappingDTO> parseMappingsResponse(Map<String, Object> responseBody) {
        List<NodeUrlMappingDTO> mappings = new ArrayList<>();

        try {
            Map<String, Object> contexts = MapUtils.getMap(responseBody, "contexts");
            if (contexts == null) {
                return mappings;
            }

            for (Map.Entry<String, Object> contextEntry : contexts.entrySet()) {
                Map<String, Object> context = (Map<String, Object>) contextEntry.getValue();
                Map<String, Object> mappingsData = MapUtils.getMap(context, "mappings");
                if (mappingsData == null) {
                    continue;
                }

                // 处理 dispatcherServlets
                Map<String, Object> dispatcherServlets = MapUtils.getMap(mappingsData, "dispatcherServlets");
                if (dispatcherServlets != null) {
                    for (Map.Entry<String, Object> servletEntry : dispatcherServlets.entrySet()) {
                        List<Map<String, Object>> handlerMappings = (List<Map<String, Object>>) servletEntry.getValue();
                        if (handlerMappings != null) {
                            for (Map<String, Object> handlerMapping : handlerMappings) {
                                NodeUrlMappingDTO dto = parseHandlerMapping(handlerMapping);
                                if (dto != null && dto.getPattern() != null) {
                                    mappings.add(dto);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("解析 mappings 响应失败: {}", e.getMessage());
        }

        return mappings;
    }

    /**
     * 解析单个 handler mapping
     *
     * @param handlerMapping handler mapping 数据
     * @return URL 映射 DTO
     */
    @SuppressWarnings("unchecked")
    private NodeUrlMappingDTO parseHandlerMapping(Map<String, Object> handlerMapping) {
        try {
            NodeUrlMappingDTO dto = new NodeUrlMappingDTO();

            // 获取 predicate 信息
            String predicate = MapUtils.getString(handlerMapping, "predicate");
            if (predicate != null) {
                // 解析 predicate 字符串，例如：{GET [/api/users]}
                if (predicate.contains("[") && predicate.contains("]")) {
                    int start = predicate.indexOf("[");
                    int end = predicate.indexOf("]");
                    if (start < end) {
                        dto.setPattern(predicate.substring(start + 1, end));
                    }
                }

                // 解析 HTTP 方法
                if (predicate.contains("{") && predicate.contains(" ")) {
                    String methodPart = predicate.substring(1, predicate.indexOf(" "));
                    if (!methodPart.isEmpty()) {
                        dto.setMethods(Arrays.asList(methodPart.split("\\|")));
                    }
                }
            }

            // 获取 handler 信息
            String handler = MapUtils.getString(handlerMapping, "handler");
            if (handler != null) {
                dto.setHandler(handler);

                // 提取 Controller 类名
                if (handler.contains("#")) {
                    String controllerPart = handler.substring(0, handler.indexOf("#"));
                    dto.setController(controllerPart);
                }
            }

            // 获取 details 信息
            Map<String, Object> details = MapUtils.getMap(handlerMapping, "details");
            if (details != null) {
                Map<String, Object> requestMappingConditions = MapUtils.getMap(details, "requestMappingConditions");
                if (requestMappingConditions != null) {
                    List<String> produces = MapUtils.getList(requestMappingConditions, "produces");
                    List<String> consumes = MapUtils.getList(requestMappingConditions, "consumes");
                    dto.setProduces(produces);
                    dto.setConsumes(consumes);

                    // 从 patterns 获取更准确的 URL
                    List<String> patterns = MapUtils.getList(requestMappingConditions, "patterns");
                    if (patterns != null && !patterns.isEmpty()) {
                        dto.setPattern(patterns.get(0));
                    }

                    // 从 methods 获取更准确的 HTTP 方法
                    List<String> methods = MapUtils.getList(requestMappingConditions, "methods");
                    if (methods != null && !methods.isEmpty()) {
                        dto.setMethods(methods);
                    }
                }
            }

            return dto;
        } catch (Exception e) {
            log.warn("解析 handler mapping 失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 解码节点 URL
     *
     * @param encodedNodeUrl Base64 编码的节点 URL
     * @return 解码后的节点 URL
     */
    private String decodeNodeUrl(String encodedNodeUrl) {
        byte[] decodedBytes = Base64.getDecoder().decode(encodedNodeUrl);
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }
}
