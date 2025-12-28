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
import java.util.Collections;
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

    // ==================== 自定义查询方法 ====================

    /**
     * 执行自定义 GraphQL 查询
     *
     * @param config    SkyWalking 配置
     * @param query     GraphQL 查询语句
     * @param variables 查询变量（可为 null）
     * @return 查询结果 JSON
     */
    public Object executeCustomQuery(MonitorSkywalkingConfig config, String query, java.util.Map<String, Object> variables) {
        try {
            String fullQuery = query;
            
            // 如果有变量，需要构建带变量的查询
            if (variables != null && !variables.isEmpty()) {
                return executeQueryWithVariables(config, query, variables);
            }
            
            JSONObject result = executeQuery(config, fullQuery);
            if (result != null) {
                if (result.containsKey("errors")) {
                    JSONArray errors = result.getJSONArray("errors");
                    if (errors != null && !errors.isEmpty()) {
                        StringBuilder errorMsg = new StringBuilder("查询失败: ");
                        for (int i = 0; i < errors.size(); i++) {
                            JSONObject error = errors.getJSONObject(i);
                            errorMsg.append(error.getStr("message"));
                            if (i < errors.size() - 1) {
                                errorMsg.append("; ");
                            }
                        }
                        throw new RuntimeException(errorMsg.toString());
                    }
                }
                return result.get("data");
            }
            return null;
        } catch (Exception e) {
            log.error("执行自定义 GraphQL 查询失败: {}", e.getMessage(), e);
            throw new RuntimeException("执行自定义查询失败: " + e.getMessage(), e);
        }
    }

    /**
     * 执行带变量的 GraphQL 查询
     */
    private Object executeQueryWithVariables(MonitorSkywalkingConfig config, String query, java.util.Map<String, Object> variables) {
        try {
            String url = config.getGraphqlUrl();
            JSONObject body = new JSONObject();
            body.set("query", query);
            body.set("variables", variables);

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
                JSONObject result = JSONUtil.parseObj(response.body());
                if (result.containsKey("errors")) {
                    JSONArray errors = result.getJSONArray("errors");
                    if (errors != null && !errors.isEmpty()) {
                        StringBuilder errorMsg = new StringBuilder("查询失败: ");
                        for (int i = 0; i < errors.size(); i++) {
                            JSONObject error = errors.getJSONObject(i);
                            errorMsg.append(error.getStr("message"));
                            if (i < errors.size() - 1) {
                                errorMsg.append("; ");
                            }
                        }
                        throw new RuntimeException(errorMsg.toString());
                    }
                }
                return result.get("data");
            } else {
                log.error("SkyWalking GraphQL 请求失败: status={}, body={}", response.getStatus(), response.body());
                throw new RuntimeException("GraphQL 请求失败: HTTP " + response.getStatus());
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("执行 SkyWalking GraphQL 查询失败: {}", e.getMessage(), e);
            throw new RuntimeException("查询失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取 SkyWalking GraphQL Schema
     * 用于帮助用户了解可用的查询和类型
     */
    public Object getGraphQLSchema(MonitorSkywalkingConfig config) {
        String introspectionQuery = """
            query IntrospectionQuery {
                __schema {
                    queryType { name }
                    mutationType { name }
                    types {
                        kind
                        name
                        description
                        fields(includeDeprecated: true) {
                            name
                            description
                            args {
                                name
                                description
                                type {
                                    kind
                                    name
                                    ofType { kind name }
                                }
                                defaultValue
                            }
                            type {
                                kind
                                name
                                ofType { kind name ofType { kind name } }
                            }
                        }
                        inputFields {
                            name
                            description
                            type {
                                kind
                                name
                                ofType { kind name }
                            }
                        }
                        enumValues(includeDeprecated: true) {
                            name
                            description
                        }
                    }
                }
            }
            """;
        return executeCustomQuery(config, introspectionQuery, null);
    }

    /**
     * 获取可用的查询列表
     */
    public Object getAvailableQueries(MonitorSkywalkingConfig config) {
        String query = """
            query {
                __schema {
                    queryType {
                        fields {
                            name
                            description
                            args {
                                name
                                description
                                type {
                                    kind
                                    name
                                    ofType { kind name }
                                }
                            }
                        }
                    }
                }
            }
            """;
        return executeCustomQuery(config, query, null);
    }

    /**
     * 获取可用的指标名称列表
     */
    public List<String> getAvailableMetrics(MonitorSkywalkingConfig config, String regex) {
        String queryRegex = StrUtil.isNotBlank(regex) ? regex : ".*";
        String query = String.format("""
            query {
                typeOfMetrics(regex: "%s")
            }
            """, queryRegex);
        
        JSONObject result = executeQuery(config, query);
        List<String> metrics = new ArrayList<>();
        if (result != null && result.containsKey("data")) {
            JSONArray metricsArray = result.getJSONObject("data").getJSONArray("typeOfMetrics");
            if (metricsArray != null) {
                for (int i = 0; i < metricsArray.size(); i++) {
                    metrics.add(metricsArray.getStr(i));
                }
            }
        }
        return metrics;
    }

    // ==================== 指标查询方法 ====================

    /**
     * 获取服务指标概览
     */
    public SkywalkingMetrics.ServiceOverview getServiceMetricsOverview(
            MonitorSkywalkingConfig config, SkywalkingQueryCondition condition) {
        String serviceId = condition.getServiceId();
        String startTime = condition.getStartTime();
        String endTime = condition.getEndTime();
        String step = condition.getStep() != null ? condition.getStep() : "MINUTE";

        // 查询多个指标
        String query = String.format("""
            query {
                cpm: readMetricsValue(condition: {name: "service_cpm", entity: {scope: Service, serviceName: "%s", normal: true}}, duration: {start: "%s", end: "%s", step: %s})
                sla: readMetricsValue(condition: {name: "service_sla", entity: {scope: Service, serviceName: "%s", normal: true}}, duration: {start: "%s", end: "%s", step: %s})
                respTime: readMetricsValue(condition: {name: "service_resp_time", entity: {scope: Service, serviceName: "%s", normal: true}}, duration: {start: "%s", end: "%s", step: %s})
                apdex: readMetricsValue(condition: {name: "service_apdex", entity: {scope: Service, serviceName: "%s", normal: true}}, duration: {start: "%s", end: "%s", step: %s})
            }
            """,
            serviceId, startTime, endTime, step,
            serviceId, startTime, endTime, step,
            serviceId, startTime, endTime, step,
            serviceId, startTime, endTime, step
        );

        JSONObject result = executeQuery(config, query);
        SkywalkingMetrics.ServiceOverview overview = new SkywalkingMetrics.ServiceOverview();
        if (result != null && result.containsKey("data")) {
            JSONObject data = result.getJSONObject("data");
            overview.setCpm(data.getLong("cpm"));
            overview.setSla(data.getLong("sla"));
            overview.setRespTime(data.getLong("respTime"));
            overview.setApdex(data.getLong("apdex"));
        }
        return overview;
    }

    /**
     * 获取服务指标趋势
     */
    public SkywalkingMetrics.ServiceTrend getServiceMetricsTrend(
            MonitorSkywalkingConfig config, SkywalkingQueryCondition condition) {
        String serviceId = condition.getServiceId();
        String startTime = condition.getStartTime();
        String endTime = condition.getEndTime();
        String step = condition.getStep() != null ? condition.getStep() : "MINUTE";

        String query = String.format("""
            query {
                cpm: readMetricsValues(condition: {name: "service_cpm", entity: {scope: Service, serviceName: "%s", normal: true}}, duration: {start: "%s", end: "%s", step: %s}) {
                    label
                    values { values { id value } }
                }
                respTime: readMetricsValues(condition: {name: "service_resp_time", entity: {scope: Service, serviceName: "%s", normal: true}}, duration: {start: "%s", end: "%s", step: %s}) {
                    label
                    values { values { id value } }
                }
                sla: readMetricsValues(condition: {name: "service_sla", entity: {scope: Service, serviceName: "%s", normal: true}}, duration: {start: "%s", end: "%s", step: %s}) {
                    label
                    values { values { id value } }
                }
                apdex: readMetricsValues(condition: {name: "service_apdex", entity: {scope: Service, serviceName: "%s", normal: true}}, duration: {start: "%s", end: "%s", step: %s}) {
                    label
                    values { values { id value } }
                }
            }
            """,
            serviceId, startTime, endTime, step,
            serviceId, startTime, endTime, step,
            serviceId, startTime, endTime, step,
            serviceId, startTime, endTime, step
        );

        JSONObject result = executeQuery(config, query);
        SkywalkingMetrics.ServiceTrend trend = new SkywalkingMetrics.ServiceTrend();
        if (result != null && result.containsKey("data")) {
            JSONObject data = result.getJSONObject("data");
            trend.setCpm(extractMetricValues(data, "cpm"));
            trend.setRespTime(extractMetricValues(data, "respTime"));
            trend.setSla(extractMetricValues(data, "sla"));
            trend.setApdex(extractMetricValues(data, "apdex"));
            trend.setTimestamps(extractMetricTimestamps(data, "cpm"));
        }
        return trend;
    }

    /**
     * 获取全局指标概览
     */
    public SkywalkingMetrics.GlobalOverview getGlobalMetricsOverview(
            MonitorSkywalkingConfig config, SkywalkingQueryCondition condition) {
        // 先获取服务列表计算统计
        List<SkywalkingServiceInfo> services = listServices(config, condition);
        
        SkywalkingMetrics.GlobalOverview overview = new SkywalkingMetrics.GlobalOverview();
        overview.setTotalServices(services.size());
        overview.setTotalEndpoints(0);
        overview.setTotalCpm(0L);
        overview.setAvgRespTime(0L);
        overview.setAvgSla(10000L);

        if (!services.isEmpty()) {
            long totalCpm = 0;
            long totalRespTime = 0;
            long totalSla = 0;
            int count = 0;

            for (SkywalkingServiceInfo service : services) {
                try {
                    SkywalkingQueryCondition serviceCondition = new SkywalkingQueryCondition();
                    serviceCondition.setServiceId(service.getId());
                    serviceCondition.setStartTime(condition.getStartTime());
                    serviceCondition.setEndTime(condition.getEndTime());
                    serviceCondition.setStep(condition.getStep());

                    SkywalkingMetrics.ServiceOverview serviceOverview = getServiceMetricsOverview(config, serviceCondition);
                    if (serviceOverview.getCpm() != null) {
                        totalCpm += serviceOverview.getCpm();
                    }
                    if (serviceOverview.getRespTime() != null) {
                        totalRespTime += serviceOverview.getRespTime();
                        count++;
                    }
                    if (serviceOverview.getSla() != null) {
                        totalSla += serviceOverview.getSla();
                    }
                } catch (Exception e) {
                    log.debug("获取服务 {} 指标失败", service.getName(), e);
                }
            }

            overview.setTotalCpm(totalCpm);
            if (count > 0) {
                overview.setAvgRespTime(totalRespTime / count);
                overview.setAvgSla(totalSla / count);
            }
        }

        return overview;
    }

    /**
     * 获取全局指标趋势（通过聚合所有服务的趋势）
     */
    public SkywalkingMetrics.ServiceTrend getGlobalMetricsTrend(
            MonitorSkywalkingConfig config, SkywalkingQueryCondition condition) {
        // 获取所有服务列表
        List<SkywalkingServiceInfo> services = listServices(config, condition);
        SkywalkingMetrics.ServiceTrend globalTrend = new SkywalkingMetrics.ServiceTrend();
        
        if (services.isEmpty()) {
            globalTrend.setCpm(Collections.emptyList());
            globalTrend.setRespTime(Collections.emptyList());
            globalTrend.setSla(Collections.emptyList());
            globalTrend.setTimestamps(Collections.emptyList());
            return globalTrend;
        }

        // 获取第一个服务的趋势数据作为基准（获取时间戳）
        List<List<Long>> allCpm = new ArrayList<>();
        List<List<Long>> allRespTime = new ArrayList<>();
        List<List<Long>> allSla = new ArrayList<>();
        List<String> timestamps = null;
        int maxSize = 0;

        for (SkywalkingServiceInfo service : services) {
            try {
                SkywalkingQueryCondition serviceCondition = new SkywalkingQueryCondition();
                serviceCondition.setServiceId(service.getId());
                serviceCondition.setStartTime(condition.getStartTime());
                serviceCondition.setEndTime(condition.getEndTime());
                serviceCondition.setStep(condition.getStep());

                SkywalkingMetrics.ServiceTrend serviceTrend = getServiceMetricsTrend(config, serviceCondition);
                if (serviceTrend.getCpm() != null && !serviceTrend.getCpm().isEmpty()) {
                    allCpm.add(serviceTrend.getCpm());
                    maxSize = Math.max(maxSize, serviceTrend.getCpm().size());
                }
                if (serviceTrend.getRespTime() != null && !serviceTrend.getRespTime().isEmpty()) {
                    allRespTime.add(serviceTrend.getRespTime());
                }
                if (serviceTrend.getSla() != null && !serviceTrend.getSla().isEmpty()) {
                    allSla.add(serviceTrend.getSla());
                }
                if (timestamps == null && serviceTrend.getTimestamps() != null && !serviceTrend.getTimestamps().isEmpty()) {
                    timestamps = serviceTrend.getTimestamps();
                }
            } catch (Exception e) {
                log.debug("获取服务 {} 趋势失败", service.getName(), e);
            }
        }

        // 聚合数据
        List<Long> aggregatedCpm = new ArrayList<>();
        List<Long> aggregatedRespTime = new ArrayList<>();
        List<Long> aggregatedSla = new ArrayList<>();

        for (int i = 0; i < maxSize; i++) {
            // CPM 累加
            long cpmSum = 0;
            for (List<Long> cpmList : allCpm) {
                if (i < cpmList.size()) {
                    cpmSum += cpmList.get(i) != null ? cpmList.get(i) : 0;
                }
            }
            aggregatedCpm.add(cpmSum);

            // 响应时间平均
            long respTimeSum = 0;
            int respTimeCount = 0;
            for (List<Long> respTimeList : allRespTime) {
                if (i < respTimeList.size() && respTimeList.get(i) != null) {
                    respTimeSum += respTimeList.get(i);
                    respTimeCount++;
                }
            }
            aggregatedRespTime.add(respTimeCount > 0 ? respTimeSum / respTimeCount : 0);

            // SLA 平均
            long slaSum = 0;
            int slaCount = 0;
            for (List<Long> slaList : allSla) {
                if (i < slaList.size() && slaList.get(i) != null) {
                    slaSum += slaList.get(i);
                    slaCount++;
                }
            }
            aggregatedSla.add(slaCount > 0 ? slaSum / slaCount : 10000);
        }

        globalTrend.setCpm(aggregatedCpm);
        globalTrend.setRespTime(aggregatedRespTime);
        globalTrend.setSla(aggregatedSla);
        globalTrend.setTimestamps(timestamps != null ? timestamps : Collections.emptyList());

        return globalTrend;
    }

    /**
     * 获取慢端点排行
     */
    public List<SkywalkingMetrics.RankItem> getSlowEndpoints(
            MonitorSkywalkingConfig config, SkywalkingQueryCondition condition) {
        String startTime = condition.getStartTime();
        String endTime = condition.getEndTime();
        String step = condition.getStep() != null ? condition.getStep() : "MINUTE";
        int limit = condition.getLimit() != null ? condition.getLimit() : 10;

        // 如果指定了 serviceId，查询该服务下的慢端点
        String serviceFilter = StrUtil.isNotBlank(condition.getServiceId()) 
            ? String.format(", parentService: \"%s\"", condition.getServiceId()) 
            : "";

        String query = String.format("""
            query {
                sortMetrics(condition: {
                    name: "endpoint_avg",
                    topN: %d,
                    order: DES%s
                }, duration: {start: "%s", end: "%s", step: %s}) {
                    id
                    name
                    value
                }
            }
            """,
            limit, serviceFilter, startTime, endTime, step
        );

        JSONObject result = executeQuery(config, query);
        List<SkywalkingMetrics.RankItem> items = new ArrayList<>();
        if (result != null && result.containsKey("data")) {
            JSONArray sortArray = result.getJSONObject("data").getJSONArray("sortMetrics");
            if (sortArray != null) {
                for (int i = 0; i < sortArray.size(); i++) {
                    JSONObject item = sortArray.getJSONObject(i);
                    SkywalkingMetrics.RankItem rankItem = new SkywalkingMetrics.RankItem();
                    rankItem.setId(item.getStr("id"));
                    rankItem.setName(item.getStr("name"));
                    rankItem.setValue(item.getLong("value"));
                    items.add(rankItem);
                }
            }
        }
        return items;
    }

    /**
     * 获取错误率高的服务排行
     */
    public List<SkywalkingMetrics.RankItem> getErrorRateServices(
            MonitorSkywalkingConfig config, SkywalkingQueryCondition condition) {
        String startTime = condition.getStartTime();
        String endTime = condition.getEndTime();
        String step = condition.getStep() != null ? condition.getStep() : "MINUTE";
        int limit = condition.getLimit() != null ? condition.getLimit() : 10;

        // 查询服务成功率从低到高排序
        String query = String.format("""
            query {
                sortMetrics(condition: {
                    name: "service_sla",
                    topN: %d,
                    order: ASC
                }, duration: {start: "%s", end: "%s", step: %s}) {
                    id
                    name
                    value
                }
            }
            """,
            limit, startTime, endTime, step
        );

        JSONObject result = executeQuery(config, query);
        List<SkywalkingMetrics.RankItem> items = new ArrayList<>();
        if (result != null && result.containsKey("data")) {
            JSONArray sortArray = result.getJSONObject("data").getJSONArray("sortMetrics");
            if (sortArray != null) {
                for (int i = 0; i < sortArray.size(); i++) {
                    JSONObject item = sortArray.getJSONObject(i);
                    SkywalkingMetrics.RankItem rankItem = new SkywalkingMetrics.RankItem();
                    rankItem.setId(item.getStr("id"));
                    rankItem.setName(item.getStr("name"));
                    // SLA 转为错误率 (10000 - SLA)
                    Long sla = item.getLong("value");
                    rankItem.setValue(sla != null ? 10000 - sla : 0);
                    items.add(rankItem);
                }
            }
        }
        return items;
    }

    /**
     * 提取指标值列表
     */
    private List<Long> extractMetricValues(JSONObject data, String metricName) {
        List<Long> values = new ArrayList<>();
        try {
            JSONObject metric = data.getJSONObject(metricName);
            if (metric != null) {
                JSONObject valuesObj = metric.getJSONObject("values");
                if (valuesObj != null) {
                    JSONArray valuesArray = valuesObj.getJSONArray("values");
                    if (valuesArray != null) {
                        for (int i = 0; i < valuesArray.size(); i++) {
                            JSONObject v = valuesArray.getJSONObject(i);
                            values.add(v.getLong("value"));
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("提取指标值失败: {}", metricName, e);
        }
        return values;
    }

    /**
     * 提取指标时间戳列表
     */
    private List<String> extractMetricTimestamps(JSONObject data, String metricName) {
        List<String> timestamps = new ArrayList<>();
        try {
            JSONObject metric = data.getJSONObject(metricName);
            if (metric != null) {
                JSONObject valuesObj = metric.getJSONObject("values");
                if (valuesObj != null) {
                    JSONArray valuesArray = valuesObj.getJSONArray("values");
                    if (valuesArray != null) {
                        for (int i = 0; i < valuesArray.size(); i++) {
                            JSONObject v = valuesArray.getJSONObject(i);
                            String rawId = v.getStr("id");
                            timestamps.add(formatTimestampId(rawId));
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("提取时间戳失败: {}", metricName, e);
        }
        return timestamps;
    }

    /**
     * 格式化 SkyWalking 时间戳 ID 为可读时间
     * SkyWalking 时间戳格式：
     * - MINUTE: yyyyMMddHHmm (202412281009)
     * - HOUR: yyyyMMddHH (2024122810)
     * - DAY: yyyyMMdd (20241228)
     */
    private String formatTimestampId(String id) {
        if (StrUtil.isBlank(id)) {
            return id;
        }
        try {
            int len = id.length();
            if (len == 12) {
                // MINUTE: yyyyMMddHHmm -> HH:mm
                return id.substring(8, 10) + ":" + id.substring(10, 12);
            } else if (len == 10) {
                // HOUR: yyyyMMddHH -> MM-dd HH:00
                return id.substring(4, 6) + "-" + id.substring(6, 8) + " " + id.substring(8, 10) + ":00";
            } else if (len == 8) {
                // DAY: yyyyMMdd -> MM-dd
                return id.substring(4, 6) + "-" + id.substring(6, 8);
            }
        } catch (Exception e) {
            log.debug("格式化时间戳失败: {}", id, e);
        }
        return id;
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
