package com.chua.starter.monitor.service.skywalking;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.chua.starter.monitor.entity.MonitorSkywalkingConfig;
import com.chua.starter.monitor.pojo.skywalking.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * SkyWalking GraphQL 服务
 * 用于调用 SkyWalking GraphQL API
 *
 * @author CH
 * @since 2024-12-18
 */
@Slf4j
@Service
public class SkywalkingGraphQLService {

    private static final int TIMEOUT = 30000;

    /**
     * 测试连接
     */
    public boolean testConnection(MonitorSkywalkingConfig config) {
        try {
            String query = "{ listLayers }";
            JSONObject result = executeQuery(config, query);
            return result != null && result.containsKey("data");
        } catch (Exception e) {
            log.error("测试 SkyWalking 连接失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取所有层
     */
    public List<String> listLayers(MonitorSkywalkingConfig config) {
        String query = "{ listLayers }";
        JSONObject result = executeQuery(config, query);
        if (result != null && result.containsKey("data")) {
            JSONArray layers = result.getJSONObject("data").getJSONArray("listLayers");
            return layers.toList(String.class);
        }
        return new ArrayList<>();
    }

    /**
     * 获取服务列表
     */
    public List<SkywalkingServiceInfo> listServices(MonitorSkywalkingConfig config, SkywalkingQueryCondition condition) {
        String layer = condition.getLayer() != null ? condition.getLayer() : "GENERAL";
        String query = String.format("""
            query {
                listServices(duration: {start: "%s", end: "%s", step: %s}, layer: "%s") {
                    id
                    name
                    group
                    shortName
                    layer
                    normal
                }
            }
            """, 
            condition.getStartTime(),
            condition.getEndTime(),
            condition.getStep() != null ? condition.getStep() : "MINUTE",
            layer
        );
        
        JSONObject result = executeQuery(config, query);
        List<SkywalkingServiceInfo> services = new ArrayList<>();
        if (result != null && result.containsKey("data")) {
            JSONArray serviceArray = result.getJSONObject("data").getJSONArray("listServices");
            if (serviceArray != null) {
                for (int i = 0; i < serviceArray.size(); i++) {
                    services.add(JSONUtil.toBean(serviceArray.getJSONObject(i), SkywalkingServiceInfo.class));
                }
            }
        }
        return services;
    }

    /**
     * 搜索服务
     */
    public List<SkywalkingServiceInfo> searchServices(MonitorSkywalkingConfig config, SkywalkingQueryCondition condition) {
        String query = String.format("""
            query {
                searchServices(duration: {start: "%s", end: "%s", step: %s}, keyword: "%s") {
                    id
                    name
                    group
                    shortName
                    layer
                    normal
                }
            }
            """,
            condition.getStartTime(),
            condition.getEndTime(),
            condition.getStep() != null ? condition.getStep() : "MINUTE",
            condition.getKeyword() != null ? condition.getKeyword() : ""
        );

        JSONObject result = executeQuery(config, query);
        List<SkywalkingServiceInfo> services = new ArrayList<>();
        if (result != null && result.containsKey("data")) {
            JSONArray serviceArray = result.getJSONObject("data").getJSONArray("searchServices");
            if (serviceArray != null) {
                for (int i = 0; i < serviceArray.size(); i++) {
                    services.add(JSONUtil.toBean(serviceArray.getJSONObject(i), SkywalkingServiceInfo.class));
                }
            }
        }
        return services;
    }

    /**
     * 获取服务实例列表
     */
    public List<SkywalkingServiceInfo.Instance> listInstances(MonitorSkywalkingConfig config, SkywalkingQueryCondition condition) {
        String query = String.format("""
            query {
                listInstances(duration: {start: "%s", end: "%s", step: %s}, serviceId: "%s") {
                    id
                    name
                    language
                    attributes {
                        name
                        value
                    }
                }
            }
            """,
            condition.getStartTime(),
            condition.getEndTime(),
            condition.getStep() != null ? condition.getStep() : "MINUTE",
            condition.getServiceId()
        );

        JSONObject result = executeQuery(config, query);
        List<SkywalkingServiceInfo.Instance> instances = new ArrayList<>();
        if (result != null && result.containsKey("data")) {
            JSONArray instanceArray = result.getJSONObject("data").getJSONArray("listInstances");
            if (instanceArray != null) {
                for (int i = 0; i < instanceArray.size(); i++) {
                    instances.add(JSONUtil.toBean(instanceArray.getJSONObject(i), SkywalkingServiceInfo.Instance.class));
                }
            }
        }
        return instances;
    }

    /**
     * 获取端点列表
     */
    public List<SkywalkingServiceInfo.Endpoint> findEndpoints(MonitorSkywalkingConfig config, SkywalkingQueryCondition condition) {
        String keyword = condition.getKeyword() != null ? condition.getKeyword() : "";
        int limit = condition.getLimit() != null ? condition.getLimit() : 100;
        
        String query = String.format("""
            query {
                findEndpoint(keyword: "%s", serviceId: "%s", limit: %d) {
                    id
                    name
                }
            }
            """,
            keyword,
            condition.getServiceId(),
            limit
        );

        JSONObject result = executeQuery(config, query);
        List<SkywalkingServiceInfo.Endpoint> endpoints = new ArrayList<>();
        if (result != null && result.containsKey("data")) {
            JSONArray endpointArray = result.getJSONObject("data").getJSONArray("findEndpoint");
            if (endpointArray != null) {
                for (int i = 0; i < endpointArray.size(); i++) {
                    endpoints.add(JSONUtil.toBean(endpointArray.getJSONObject(i), SkywalkingServiceInfo.Endpoint.class));
                }
            }
        }
        return endpoints;
    }

    /**
     * 查询 Trace 列表
     */
    public SkywalkingTrace.Brief queryBasicTraces(MonitorSkywalkingConfig config, SkywalkingQueryCondition condition) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("query { queryBasicTraces(condition: {");
        queryBuilder.append(String.format("queryDuration: {start: \"%s\", end: \"%s\", step: %s}",
                condition.getStartTime(),
                condition.getEndTime(),
                condition.getStep() != null ? condition.getStep() : "MINUTE"));
        
        if (StrUtil.isNotBlank(condition.getTraceId())) {
            queryBuilder.append(String.format(", traceId: \"%s\"", condition.getTraceId()));
        }
        if (StrUtil.isNotBlank(condition.getServiceId())) {
            queryBuilder.append(String.format(", serviceId: \"%s\"", condition.getServiceId()));
        }
        if (StrUtil.isNotBlank(condition.getServiceInstanceId())) {
            queryBuilder.append(String.format(", serviceInstanceId: \"%s\"", condition.getServiceInstanceId()));
        }
        if (StrUtil.isNotBlank(condition.getEndpointId())) {
            queryBuilder.append(String.format(", endpointId: \"%s\"", condition.getEndpointId()));
        }
        if (StrUtil.isNotBlank(condition.getEndpointName())) {
            queryBuilder.append(String.format(", endpointName: \"%s\"", condition.getEndpointName()));
        }
        if (condition.getMinTraceDuration() != null) {
            queryBuilder.append(String.format(", minTraceDuration: %d", condition.getMinTraceDuration()));
        }
        if (condition.getMaxTraceDuration() != null) {
            queryBuilder.append(String.format(", maxTraceDuration: %d", condition.getMaxTraceDuration()));
        }
        
        String traceState = condition.getTraceState() != null ? condition.getTraceState() : "ALL";
        queryBuilder.append(String.format(", traceState: %s", traceState));
        
        String queryOrder = condition.getQueryOrder() != null ? condition.getQueryOrder() : "BY_START_TIME";
        queryBuilder.append(String.format(", queryOrder: %s", queryOrder));
        
        queryBuilder.append(String.format(", paging: {pageNum: %d, pageSize: %d}",
                condition.getPageNum() != null ? condition.getPageNum() : 1,
                condition.getPageSize() != null ? condition.getPageSize() : 20));
        
        queryBuilder.append("}) { traces { segmentId endpointNames duration start isError traceIds } total } }");

        JSONObject result = executeQuery(config, queryBuilder.toString());
        SkywalkingTrace.Brief brief = new SkywalkingTrace.Brief();
        if (result != null && result.containsKey("data")) {
            JSONObject data = result.getJSONObject("data").getJSONObject("queryBasicTraces");
            if (data != null) {
                brief.setTotal(data.getInt("total"));
                JSONArray traceArray = data.getJSONArray("traces");
                List<SkywalkingTrace.BasicTrace> traces = new ArrayList<>();
                if (traceArray != null) {
                    for (int i = 0; i < traceArray.size(); i++) {
                        traces.add(JSONUtil.toBean(traceArray.getJSONObject(i), SkywalkingTrace.BasicTrace.class));
                    }
                }
                brief.setTraces(traces);
            }
        }
        return brief;
    }

    /**
     * 查询 Trace 详情
     */
    public SkywalkingTrace queryTrace(MonitorSkywalkingConfig config, String traceId) {
        String query = String.format("""
            query {
                queryTrace(traceId: "%s") {
                    spans {
                        traceId
                        segmentId
                        spanId
                        parentSpanId
                        refs {
                            traceId
                            parentSegmentId
                            parentSpanId
                            type
                        }
                        serviceCode
                        serviceInstanceName
                        startTime
                        endTime
                        endpointName
                        type
                        peer
                        component
                        isError
                        layer
                        tags {
                            key
                            value
                        }
                        logs {
                            time
                            data {
                                key
                                value
                            }
                        }
                    }
                }
            }
            """, traceId);

        JSONObject result = executeQuery(config, query);
        SkywalkingTrace trace = new SkywalkingTrace();
        trace.setTraceId(traceId);
        if (result != null && result.containsKey("data")) {
            JSONObject data = result.getJSONObject("data").getJSONObject("queryTrace");
            if (data != null) {
                JSONArray spanArray = data.getJSONArray("spans");
                List<SkywalkingTrace.Span> spans = new ArrayList<>();
                if (spanArray != null) {
                    for (int i = 0; i < spanArray.size(); i++) {
                        spans.add(JSONUtil.toBean(spanArray.getJSONObject(i), SkywalkingTrace.Span.class));
                    }
                }
                trace.setSpans(spans);
            }
        }
        return trace;
    }

    /**
     * 获取全局拓扑
     */
    public SkywalkingTopology getGlobalTopology(MonitorSkywalkingConfig config, SkywalkingQueryCondition condition) {
        String layer = condition.getLayer() != null ? ", layer: \"" + condition.getLayer() + "\"" : "";
        String query = String.format("""
            query {
                getGlobalTopology(duration: {start: "%s", end: "%s", step: %s}%s) {
                    nodes {
                        id
                        name
                        type
                        isReal
                        layer
                    }
                    calls {
                        id
                        source
                        target
                        detectPoints
                    }
                }
            }
            """,
            condition.getStartTime(),
            condition.getEndTime(),
            condition.getStep() != null ? condition.getStep() : "MINUTE",
            layer
        );

        JSONObject result = executeQuery(config, query);
        SkywalkingTopology topology = new SkywalkingTopology();
        if (result != null && result.containsKey("data")) {
            JSONObject data = result.getJSONObject("data").getJSONObject("getGlobalTopology");
            if (data != null) {
                // 解析节点
                JSONArray nodeArray = data.getJSONArray("nodes");
                List<SkywalkingTopology.Node> nodes = new ArrayList<>();
                if (nodeArray != null) {
                    for (int i = 0; i < nodeArray.size(); i++) {
                        nodes.add(JSONUtil.toBean(nodeArray.getJSONObject(i), SkywalkingTopology.Node.class));
                    }
                }
                topology.setNodes(nodes);
                
                // 解析调用关系
                JSONArray callArray = data.getJSONArray("calls");
                List<SkywalkingTopology.Call> calls = new ArrayList<>();
                if (callArray != null) {
                    for (int i = 0; i < callArray.size(); i++) {
                        calls.add(JSONUtil.toBean(callArray.getJSONObject(i), SkywalkingTopology.Call.class));
                    }
                }
                topology.setCalls(calls);
            }
        }
        return topology;
    }

    /**
     * 获取服务拓扑
     */
    public SkywalkingTopology getServiceTopology(MonitorSkywalkingConfig config, SkywalkingQueryCondition condition) {
        String query = String.format("""
            query {
                getServiceTopology(serviceId: "%s", duration: {start: "%s", end: "%s", step: %s}) {
                    nodes {
                        id
                        name
                        type
                        isReal
                        layer
                    }
                    calls {
                        id
                        source
                        target
                        detectPoints
                    }
                }
            }
            """,
            condition.getServiceId(),
            condition.getStartTime(),
            condition.getEndTime(),
            condition.getStep() != null ? condition.getStep() : "MINUTE"
        );

        JSONObject result = executeQuery(config, query);
        SkywalkingTopology topology = new SkywalkingTopology();
        if (result != null && result.containsKey("data")) {
            JSONObject data = result.getJSONObject("data").getJSONObject("getServiceTopology");
            if (data != null) {
                JSONArray nodeArray = data.getJSONArray("nodes");
                List<SkywalkingTopology.Node> nodes = new ArrayList<>();
                if (nodeArray != null) {
                    for (int i = 0; i < nodeArray.size(); i++) {
                        nodes.add(JSONUtil.toBean(nodeArray.getJSONObject(i), SkywalkingTopology.Node.class));
                    }
                }
                topology.setNodes(nodes);
                
                JSONArray callArray = data.getJSONArray("calls");
                List<SkywalkingTopology.Call> calls = new ArrayList<>();
                if (callArray != null) {
                    for (int i = 0; i < callArray.size(); i++) {
                        calls.add(JSONUtil.toBean(callArray.getJSONObject(i), SkywalkingTopology.Call.class));
                    }
                }
                topology.setCalls(calls);
            }
        }
        return topology;
    }

    /**
     * 获取告警列表
     */
    public SkywalkingAlarm getAlarms(MonitorSkywalkingConfig config, SkywalkingQueryCondition condition) {
        String keyword = condition.getKeyword() != null ? ", keyword: \"" + condition.getKeyword() + "\"" : "";
        String query = String.format("""
            query {
                getAlarm(duration: {start: "%s", end: "%s", step: %s}, paging: {pageNum: %d, pageSize: %d}%s) {
                    msgs {
                        id
                        message
                        startTime
                        scope
                        scopeId
                        tags {
                            key
                            value
                        }
                    }
                    total
                }
            }
            """,
            condition.getStartTime(),
            condition.getEndTime(),
            condition.getStep() != null ? condition.getStep() : "MINUTE",
            condition.getPageNum() != null ? condition.getPageNum() : 1,
            condition.getPageSize() != null ? condition.getPageSize() : 20,
            keyword
        );

        JSONObject result = executeQuery(config, query);
        SkywalkingAlarm alarm = new SkywalkingAlarm();
        if (result != null && result.containsKey("data")) {
            JSONObject data = result.getJSONObject("data").getJSONObject("getAlarm");
            if (data != null) {
                alarm.setTotal(data.getInt("total"));
                JSONArray msgArray = data.getJSONArray("msgs");
                List<SkywalkingAlarm.AlarmMessage> msgs = new ArrayList<>();
                if (msgArray != null) {
                    for (int i = 0; i < msgArray.size(); i++) {
                        msgs.add(JSONUtil.toBean(msgArray.getJSONObject(i), SkywalkingAlarm.AlarmMessage.class));
                    }
                }
                alarm.setMsgs(msgs);
            }
        }
        return alarm;
    }

    /**
     * 执行 GraphQL 查询
     */
    private JSONObject executeQuery(MonitorSkywalkingConfig config, String query) {
        try {
            String url = config.getGraphqlUrl();
            JSONObject body = new JSONObject();
            body.set("query", query);

            HttpRequest request = HttpRequest.post(url)
                    .header("Content-Type", "application/json")
                    .timeout(TIMEOUT)
                    .body(body.toString());

            // 添加认证信息
            if (StrUtil.isNotBlank(config.getSkywalkingConfigUsername()) 
                    && StrUtil.isNotBlank(config.getSkywalkingConfigPassword())) {
                request.basicAuth(config.getSkywalkingConfigUsername(), config.getSkywalkingConfigPassword());
            }

            HttpResponse response = request.execute();
            if (response.isOk()) {
                return JSONUtil.parseObj(response.body());
            } else {
                log.error("SkyWalking GraphQL 请求失败: status={}, body={}", response.getStatus(), response.body());
                return null;
            }
        } catch (Exception e) {
            log.error("执行 SkyWalking GraphQL 查询失败: {}", e.getMessage(), e);
            return null;
        }
    }
}
