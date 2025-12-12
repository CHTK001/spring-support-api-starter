package com.chua.starter.monitor.service.agent.impl;

import com.chua.common.support.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.MapUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.redis.support.client.RedisSearch;
import com.chua.redis.support.search.SearchIndex;
import com.chua.redis.support.search.SearchQuery;
import com.chua.redis.support.search.SearchResultItem;
import com.chua.redis.support.search.SearchSchema;
import com.chua.starter.monitor.pojo.agent.AgentFileHandleDTO;
import com.chua.starter.monitor.pojo.agent.AgentServiceInstanceDTO;
import com.chua.starter.monitor.pojo.agent.AgentTraceDTO;
import com.chua.starter.monitor.service.agent.AgentDataStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisPooled;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Agent 数据存储服务实现类
 * <p>
 * 使用 RediSearch 存储链路追踪数据，使用 Redis Hash 存储服务实例和文件句柄数据
 * </p>
 *
 * @author CH
 * @version 1.0.0
 * @since 2025/01/17
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnBean(JedisPooled.class)
public class AgentDataStorageServiceImpl implements AgentDataStorageService {

    private static final String TRACE_INDEX_NAME = "idx:agent:trace";
    private static final String TRACE_PREFIX = "agent:trace:";
    private static final String SERVICE_KEY = "agent:service:topology";
    private static final String FILE_HANDLE_PREFIX = "agent:fd:";
    
    // 数据过期时间（7天）
    private static final long TRACE_EXPIRE_SECONDS = 7 * 24 * 60 * 60;
    private static final long FILE_HANDLE_EXPIRE_SECONDS = 24 * 60 * 60;

    private final JedisPooled jedisPooled;
    private RedisSearch redisSearch;

    @PostConstruct
    public void init() {
        this.redisSearch = new RedisSearch(jedisPooled);
        initTraceIndex();
        log.info("[AgentDataStorage] Agent 数据存储服务初始化完成");
    }

    /**
     * 初始化链路追踪索引
     */
    private void initTraceIndex() {
        try {
            if (!redisSearch.indexExists(TRACE_INDEX_NAME)) {
                SearchSchema schema = new SearchSchema();
                schema.addTextField("linkId", 1.0);
                schema.addTextField("spanId", 1.0);
                schema.addTextField("method", 1.0);
                schema.addTextField("typeName", 1.0);
                schema.addTextField("category", 1.0);
                schema.addTextField("protocol", 1.0);
                schema.addTextField("address", 1.0);
                schema.addTextField("statusCode", 1.0);
                schema.addTextField("error", 1.0);
                schema.addTextField("applicationName", 1.0);
                schema.addTextField("sourceIp", 1.0);
                schema.addNumericField("enterTime");
                schema.addNumericField("costTime");

                SearchIndex index = new SearchIndex();
                index.setName(TRACE_INDEX_NAME);
                index.setPrefix(TRACE_PREFIX);
                index.setSchema(schema);
                index.setLanguage("chinese");

                redisSearch.createIndex(index);
                log.info("[AgentDataStorage] 链路追踪索引创建成功: {}", TRACE_INDEX_NAME);
            }
        } catch (Exception e) {
            log.warn("[AgentDataStorage] 链路追踪索引初始化失败: {}", e.getMessage());
        }
    }

    // ==================== 链路追踪 ====================

    @Override
    public ReturnResult<Boolean> storeTraceForAgent(String sessionId, Map<String, Object> traceData) {
        try {
            String linkId = MapUtils.getString(traceData, "linkId");
            String spanId = MapUtils.getString(traceData, "id");
            
            if (StringUtils.isEmpty(linkId)) {
                linkId = UUID.randomUUID().toString().replace("-", "");
            }
            if (StringUtils.isEmpty(spanId)) {
                spanId = UUID.randomUUID().toString().replace("-", "");
            }

            // 构建文档ID
            String docId = TRACE_PREFIX + linkId + ":" + spanId;

            // 构建存储数据
            Map<String, String> document = new HashMap<>();
            document.put("linkId", linkId);
            document.put("spanId", spanId);
            document.put("parentSpanId", MapUtils.getString(traceData, "pid", ""));
            document.put("threadName", MapUtils.getString(traceData, "threadName", ""));
            document.put("typeName", MapUtils.getString(traceData, "typeName", ""));
            document.put("method", MapUtils.getString(traceData, "method", ""));
            document.put("category", MapUtils.getString(traceData, "category", ""));
            document.put("protocol", MapUtils.getString(traceData, "protocol", ""));
            document.put("address", MapUtils.getString(traceData, "address", ""));
            document.put("database", MapUtils.getString(traceData, "database", ""));
            document.put("statusCode", MapUtils.getString(traceData, "code", ""));
            document.put("description", MapUtils.getString(traceData, "description", ""));
            document.put("error", MapUtils.getString(traceData, "error", ""));
            document.put("enterTime", String.valueOf(MapUtils.getLong(traceData, "enterTime", 0L)));
            document.put("exitTime", String.valueOf(MapUtils.getLong(traceData, "exitTime", 0L)));
            document.put("costTime", String.valueOf(MapUtils.getLong(traceData, "costTime", 0L)));
            document.put("sourceIp", MapUtils.getString(traceData, "sourceIp", ""));
            document.put("sourcePort", String.valueOf(MapUtils.getInteger(traceData, "sourcePort", 0)));
            document.put("applicationName", MapUtils.getString(traceData, "applicationName", ""));
            document.put("sessionId", sessionId);
            document.put("timestamp", String.valueOf(System.currentTimeMillis()));

            // 存储请求头和参数（JSON格式）
            Object headers = traceData.get("headers");
            if (headers != null) {
                document.put("headers", Json.toJson(headers));
            }
            Object params = traceData.get("param");
            if (params != null) {
                document.put("params", Json.toJson(params));
            }

            // 存储子Span（JSON格式）
            Object children = traceData.get("children");
            if (children != null) {
                document.put("children", Json.toJson(children));
            }

            // 存储服务实例（JSON格式）
            Object instances = traceData.get("instances");
            if (instances != null) {
                document.put("instances", Json.toJson(instances));
                // 同时更新服务拓扑
                updateServiceTopology(instances);
            }

            // 设置过期时间
            document.put("expire", String.valueOf(TRACE_EXPIRE_SECONDS));

            // 存储到 Redis
            redisSearch.addDocument("chinese", docId, document);

            log.debug("[AgentDataStorage] 链路追踪存储成功: linkId={}, spanId={}", linkId, spanId);
            return ReturnResult.success(true);
        } catch (Exception e) {
            log.error("[AgentDataStorage] 链路追踪存储失败: {}", e.getMessage(), e);
            return ReturnResult.error("链路追踪存储失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<AgentTraceDTO>> searchTracesForAgent(String keyword, Long startTime, Long endTime,
                                                                   int offset, int limit) {
        try {
            StringBuilder queryBuilder = new StringBuilder();

            // 关键词搜索
            if (StringUtils.isNotEmpty(keyword)) {
                queryBuilder.append(keyword);
            } else {
                queryBuilder.append("*");
            }

            // 时间范围过滤
            if (startTime != null && endTime != null) {
                queryBuilder.append(" @enterTime:[").append(startTime).append(" ").append(endTime).append("]");
            } else if (startTime != null) {
                queryBuilder.append(" @enterTime:[").append(startTime).append(" +inf]");
            } else if (endTime != null) {
                queryBuilder.append(" @enterTime:[-inf ").append(endTime).append("]");
            }

            SearchQuery query = new SearchQuery();
            query.setIndex(TRACE_INDEX_NAME);
            query.setKeyword(queryBuilder.toString());
            query.setSort("enterTime");
            query.setSortDesc(true);

            SearchResultItem result = redisSearch.advancedSearch(query, offset, limit);

            List<AgentTraceDTO> traces = result.getData().stream()
                    .map(this::convertToTraceDTO)
                    .collect(Collectors.toList());

            log.debug("[AgentDataStorage] 链路追踪查询成功: keyword={}, count={}", keyword, traces.size());
            return ReturnResult.success(traces);
        } catch (Exception e) {
            log.error("[AgentDataStorage] 链路追踪查询失败: {}", e.getMessage(), e);
            return ReturnResult.error("链路追踪查询失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<AgentTraceDTO> getTraceByLinkIdForAgent(String linkId) {
        try {
            SearchQuery query = new SearchQuery();
            query.setIndex(TRACE_INDEX_NAME);
            query.setKeyword("@linkId:" + linkId);
            query.setSort("enterTime");
            query.setSortDesc(false);

            SearchResultItem result = redisSearch.advancedSearch(query, 0, 100);

            if (result.getData().isEmpty()) {
                return ReturnResult.error("未找到链路: " + linkId);
            }

            // 构建链路树
            List<AgentTraceDTO> spans = result.getData().stream()
                    .map(this::convertToTraceDTO)
                    .collect(Collectors.toList());

            AgentTraceDTO rootTrace = buildTraceTree(spans);
            return ReturnResult.success(rootTrace);
        } catch (Exception e) {
            log.error("[AgentDataStorage] 获取链路失败: linkId={}, error={}", linkId, e.getMessage(), e);
            return ReturnResult.error("获取链路失败: " + e.getMessage());
        }
    }

    // ==================== 服务实例（关系图谱）====================

    @Override
    public ReturnResult<Boolean> storeServiceInstanceForAgent(String sessionId, Map<String, Object> instanceData) {
        try {
            String sourceName = MapUtils.getString(instanceData, "sourceName", "");
            String sourceHost = MapUtils.getString(instanceData, "sourceHost", "");
            int sourcePort = MapUtils.getInteger(instanceData, "sourcePort", 0);
            String targetHost = MapUtils.getString(instanceData, "targetHost", "");
            int targetPort = MapUtils.getInteger(instanceData, "targetPort", 0);

            // 构建关系键
            String relationKey = sourceHost + ":" + sourcePort + "->" + targetHost + ":" + targetPort;

            // 更新服务拓扑
            Map<String, String> serviceData = new HashMap<>();
            serviceData.put("sourceName", sourceName);
            serviceData.put("sourceHost", sourceHost);
            serviceData.put("sourcePort", String.valueOf(sourcePort));
            serviceData.put("targetHost", targetHost);
            serviceData.put("targetPort", String.valueOf(targetPort));
            serviceData.put("lastCallTime", String.valueOf(System.currentTimeMillis()));

            // 增加调用次数
            jedisPooled.hincrBy(SERVICE_KEY + ":" + relationKey, "callCount", 1);
            jedisPooled.hset(SERVICE_KEY + ":" + relationKey, serviceData);

            // 添加到关系集合
            jedisPooled.sadd(SERVICE_KEY + ":relations", relationKey);

            log.debug("[AgentDataStorage] 服务实例存储成功: {}", relationKey);
            return ReturnResult.success(true);
        } catch (Exception e) {
            log.error("[AgentDataStorage] 服务实例存储失败: {}", e.getMessage(), e);
            return ReturnResult.error("服务实例存储失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> getServiceTopologyForAgent() {
        try {
            Set<String> relations = jedisPooled.smembers(SERVICE_KEY + ":relations");

            // 构建节点和边
            Set<Map<String, Object>> nodes = new HashSet<>();
            List<Map<String, Object>> edges = new ArrayList<>();

            for (String relation : relations) {
                Map<String, String> data = jedisPooled.hgetAll(SERVICE_KEY + ":" + relation);
                if (data.isEmpty()) {
                    continue;
                }

                String sourceHost = MapUtils.getString(data, "sourceHost", "");
                int sourcePort = MapUtils.getInteger(data, "sourcePort", 0);
                String targetHost = MapUtils.getString(data, "targetHost", "");
                int targetPort = MapUtils.getInteger(data, "targetPort", 0);
                String sourceName = MapUtils.getString(data, "sourceName", "");

                // 添加源节点
                Map<String, Object> sourceNode = new HashMap<>();
                sourceNode.put("id", sourceHost + ":" + sourcePort);
                sourceNode.put("label", sourceName.isEmpty() ? sourceHost : sourceName);
                sourceNode.put("host", sourceHost);
                sourceNode.put("port", sourcePort);
                nodes.add(sourceNode);

                // 添加目标节点
                Map<String, Object> targetNode = new HashMap<>();
                targetNode.put("id", targetHost + ":" + targetPort);
                targetNode.put("label", targetHost + ":" + targetPort);
                targetNode.put("host", targetHost);
                targetNode.put("port", targetPort);
                nodes.add(targetNode);

                // 添加边
                Map<String, Object> edge = new HashMap<>();
                edge.put("source", sourceHost + ":" + sourcePort);
                edge.put("target", targetHost + ":" + targetPort);
                edge.put("callCount", MapUtils.getInteger(data, "callCount", 0));
                edge.put("lastCallTime", MapUtils.getLong(data, "lastCallTime", 0L));
                edges.add(edge);
            }

            Map<String, Object> topology = new HashMap<>();
            topology.put("nodes", new ArrayList<>(nodes));
            topology.put("edges", edges);

            return ReturnResult.success(topology);
        } catch (Exception e) {
            log.error("[AgentDataStorage] 获取服务拓扑失败: {}", e.getMessage(), e);
            return ReturnResult.error("获取服务拓扑失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<AgentServiceInstanceDTO>> getServiceRelationsForAgent(String serviceName) {
        try {
            Set<String> relations = jedisPooled.smembers(SERVICE_KEY + ":relations");
            List<AgentServiceInstanceDTO> result = new ArrayList<>();

            for (String relation : relations) {
                if (!relation.contains(serviceName)) {
                    continue;
                }

                Map<String, String> data = jedisPooled.hgetAll(SERVICE_KEY + ":" + relation);
                if (data.isEmpty()) {
                    continue;
                }

                AgentServiceInstanceDTO dto = AgentServiceInstanceDTO.builder()
                        .name(MapUtils.getString(data, "sourceName", ""))
                        .sourceHost(MapUtils.getString(data, "sourceHost", ""))
                        .sourcePort(MapUtils.getInteger(data, "sourcePort", 0))
                        .targetHost(MapUtils.getString(data, "targetHost", ""))
                        .targetPort(MapUtils.getInteger(data, "targetPort", 0))
                        .callCount(MapUtils.getInteger(data, "callCount", 0))
                        .lastCallTime(MapUtils.getLong(data, "lastCallTime", 0L))
                        .build();
                result.add(dto);
            }

            return ReturnResult.success(result);
        } catch (Exception e) {
            log.error("[AgentDataStorage] 获取服务关系失败: {}", e.getMessage(), e);
            return ReturnResult.error("获取服务关系失败: " + e.getMessage());
        }
    }

    // ==================== 文件句柄 ====================

    @Override
    public ReturnResult<Boolean> storeFileHandleForAgent(String sessionId, Map<String, Object> handleData) {
        try {
            String ipAddress = MapUtils.getString(handleData, "ipAddress", "");
            int port = MapUtils.getInteger(handleData, "port", 0);
            int fd = MapUtils.getInteger(handleData, "fd", 0);

            String key = FILE_HANDLE_PREFIX + ipAddress + ":" + port;
            String field = String.valueOf(fd);

            Map<String, String> fdData = new HashMap<>();
            fdData.put("fd", String.valueOf(fd));
            fdData.put("filePath", MapUtils.getString(handleData, "filePath", ""));
            fdData.put("handleType", MapUtils.getString(handleData, "handleType", ""));
            fdData.put("mode", MapUtils.getString(handleData, "mode", ""));
            fdData.put("pid", String.valueOf(MapUtils.getLong(handleData, "pid", 0L)));
            fdData.put("applicationName", MapUtils.getString(handleData, "applicationName", ""));
            fdData.put("ipAddress", ipAddress);
            fdData.put("port", String.valueOf(port));
            fdData.put("openTime", String.valueOf(MapUtils.getLong(handleData, "openTime", System.currentTimeMillis())));
            fdData.put("lastAccessTime", String.valueOf(System.currentTimeMillis()));
            fdData.put("readBytes", String.valueOf(MapUtils.getLong(handleData, "readBytes", 0L)));
            fdData.put("writeBytes", String.valueOf(MapUtils.getLong(handleData, "writeBytes", 0L)));

            jedisPooled.hset(key, field, Json.toJson(fdData));
            jedisPooled.expire(key, FILE_HANDLE_EXPIRE_SECONDS);

            log.debug("[AgentDataStorage] 文件句柄存储成功: {}:{}, fd={}", ipAddress, port, fd);
            return ReturnResult.success(true);
        } catch (Exception e) {
            log.error("[AgentDataStorage] 文件句柄存储失败: {}", e.getMessage(), e);
            return ReturnResult.error("文件句柄存储失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<AgentFileHandleDTO>> getFileHandlesForAgent(String ipAddress, Integer port) {
        try {
            String key = FILE_HANDLE_PREFIX + ipAddress + ":" + port;
            Map<String, String> allHandles = jedisPooled.hgetAll(key);

            List<AgentFileHandleDTO> result = allHandles.values().stream()
                    .map(json -> {
                        Map<String, Object> data = Json.fromJson(json, Map.class);
                        return AgentFileHandleDTO.builder()
                                .fd(MapUtils.getInteger(data, "fd", 0))
                                .filePath(MapUtils.getString(data, "filePath", ""))
                                .handleType(MapUtils.getString(data, "handleType", ""))
                                .mode(MapUtils.getString(data, "mode", ""))
                                .pid(MapUtils.getLong(data, "pid", 0L))
                                .applicationName(MapUtils.getString(data, "applicationName", ""))
                                .ipAddress(MapUtils.getString(data, "ipAddress", ""))
                                .port(MapUtils.getInteger(data, "port", 0))
                                .openTime(MapUtils.getLong(data, "openTime", 0L))
                                .lastAccessTime(MapUtils.getLong(data, "lastAccessTime", 0L))
                                .readBytes(MapUtils.getLong(data, "readBytes", 0L))
                                .writeBytes(MapUtils.getLong(data, "writeBytes", 0L))
                                .build();
                    })
                    .collect(Collectors.toList());

            return ReturnResult.success(result);
        } catch (Exception e) {
            log.error("[AgentDataStorage] 获取文件句柄失败: {}", e.getMessage(), e);
            return ReturnResult.error("获取文件句柄失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> getFileHandleStatsForAgent() {
        try {
            Set<String> keys = jedisPooled.keys(FILE_HANDLE_PREFIX + "*");
            Map<String, Object> stats = new HashMap<>();
            List<Map<String, Object>> nodeStats = new ArrayList<>();

            int totalHandles = 0;
            for (String key : keys) {
                Map<String, String> handles = jedisPooled.hgetAll(key);
                String nodeKey = key.replace(FILE_HANDLE_PREFIX, "");

                Map<String, Object> nodeStat = new HashMap<>();
                nodeStat.put("node", nodeKey);
                nodeStat.put("handleCount", handles.size());
                nodeStats.add(nodeStat);

                totalHandles += handles.size();
            }

            stats.put("totalNodes", keys.size());
            stats.put("totalHandles", totalHandles);
            stats.put("nodes", nodeStats);

            return ReturnResult.success(stats);
        } catch (Exception e) {
            log.error("[AgentDataStorage] 获取文件句柄统计失败: {}", e.getMessage(), e);
            return ReturnResult.error("获取文件句柄统计失败: " + e.getMessage());
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 转换为 TraceDTO
     */
    @SuppressWarnings("unchecked")
    private AgentTraceDTO convertToTraceDTO(Map<String, Object> data) {
        AgentTraceDTO dto = AgentTraceDTO.builder()
                .linkId(MapUtils.getString(data, "linkId", ""))
                .spanId(MapUtils.getString(data, "spanId", ""))
                .parentSpanId(MapUtils.getString(data, "parentSpanId", ""))
                .threadName(MapUtils.getString(data, "threadName", ""))
                .typeName(MapUtils.getString(data, "typeName", ""))
                .method(MapUtils.getString(data, "method", ""))
                .category(MapUtils.getString(data, "category", ""))
                .protocol(MapUtils.getString(data, "protocol", ""))
                .address(MapUtils.getString(data, "address", ""))
                .database(MapUtils.getString(data, "database", ""))
                .statusCode(MapUtils.getString(data, "statusCode", ""))
                .description(MapUtils.getString(data, "description", ""))
                .error(MapUtils.getString(data, "error", ""))
                .enterTime(MapUtils.getLong(data, "enterTime", 0L))
                .exitTime(MapUtils.getLong(data, "exitTime", 0L))
                .costTime(MapUtils.getLong(data, "costTime", 0L))
                .sourceIp(MapUtils.getString(data, "sourceIp", ""))
                .sourcePort(MapUtils.getInteger(data, "sourcePort", 0))
                .applicationName(MapUtils.getString(data, "applicationName", ""))
                .build();

        // 解析请求头
        String headersJson = MapUtils.getString(data, "headers", "");
        if (StringUtils.isNotEmpty(headersJson)) {
            dto.setHeaders(Json.fromJson(headersJson, List.class));
        }

        // 解析参数
        String paramsJson = MapUtils.getString(data, "params", "");
        if (StringUtils.isNotEmpty(paramsJson)) {
            dto.setParams(Json.fromJson(paramsJson, List.class));
        }

        return dto;
    }

    /**
     * 构建链路树
     */
    private AgentTraceDTO buildTraceTree(List<AgentTraceDTO> spans) {
        if (spans.isEmpty()) {
            return null;
        }

        // 按 parentSpanId 分组
        Map<String, List<AgentTraceDTO>> childrenMap = spans.stream()
                .filter(s -> StringUtils.isNotEmpty(s.getParentSpanId()))
                .collect(Collectors.groupingBy(AgentTraceDTO::getParentSpanId));

        // 找到根节点（没有父节点的）
        AgentTraceDTO root = spans.stream()
                .filter(s -> StringUtils.isEmpty(s.getParentSpanId()))
                .findFirst()
                .orElse(spans.get(0));

        // 递归构建树
        buildChildren(root, childrenMap);

        return root;
    }

    /**
     * 递归构建子节点
     */
    private void buildChildren(AgentTraceDTO parent, Map<String, List<AgentTraceDTO>> childrenMap) {
        List<AgentTraceDTO> children = childrenMap.get(parent.getSpanId());
        if (children != null && !children.isEmpty()) {
            parent.setChildren(children);
            for (AgentTraceDTO child : children) {
                buildChildren(child, childrenMap);
            }
        }
    }

    /**
     * 更新服务拓扑
     */
    @SuppressWarnings("unchecked")
    private void updateServiceTopology(Object instances) {
        if (instances instanceof List) {
            List<Map<String, Object>> instanceList = (List<Map<String, Object>>) instances;
            for (Map<String, Object> instance : instanceList) {
                storeServiceInstanceForAgent("system", instance);
            }
        }
    }
}
