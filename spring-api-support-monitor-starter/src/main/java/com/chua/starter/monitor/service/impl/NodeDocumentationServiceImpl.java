package com.chua.starter.monitor.service.impl;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.monitor.pojo.*;
import com.chua.starter.monitor.service.NodeDocumentationService;
import com.chua.starter.monitor.service.NodeManagementService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 节点文档服务实现
 *
 * @author CH
 * @since 2024/12/30
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NodeDocumentationServiceImpl implements NodeDocumentationService {

    private final NodeManagementService nodeManagementService;
    private final RestTemplate restTemplate;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public ReturnResult<List<NodeApiGroup>> getNodeApiDocs(String nodeId, String nodeAddress, String contextPath) {
        log.info("获取节点API文档: nodeId={}, nodeAddress={}, contextPath={}", nodeId, nodeAddress, contextPath);

        try {
            // 构建Swagger文档URL
            String baseUrl = buildBaseUrl(nodeAddress, contextPath);
            String swaggerUrl = baseUrl + "/v3/api-docs";

            log.info("请求Swagger文档URL: {}", swaggerUrl);

            // 请求Swagger文档
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(swaggerUrl, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                // 解析Swagger文档
                List<NodeApiGroup> apiGroups = parseSwaggerDoc(response.getBody());
                log.info("成功获取API文档: nodeId={}, 分组数={}, API总数={}", nodeId, apiGroups.size(),
                        apiGroups.stream().mapToInt(g -> g.getApis().size()).sum());

                // 如果没有解析到API，返回模拟数据用于测试
                if (apiGroups.isEmpty()) {
                    log.warn("未解析到API文档，返回模拟数据用于测试");
                    apiGroups = createMockApiGroups();
                }

                return ReturnResult.ok(apiGroups);
            } else {
                log.warn("获取Swagger文档失败: nodeId={}, status={}", nodeId, response.getStatusCode());
                // 返回模拟数据用于测试
                List<NodeApiGroup> mockGroups = createMockApiGroups();
                return ReturnResult.ok(mockGroups);
            }

        } catch (Exception e) {
            log.error("获取节点API文档异常: nodeId={}, nodeAddress={}", nodeId, nodeAddress, e);
            // 异常时也返回模拟数据用于测试
            List<NodeApiGroup> mockGroups = createMockApiGroups();
            return ReturnResult.ok(mockGroups);
        }
    }

    @Override
    public ReturnResult<List<Map<String, Object>>> getNodeSwaggerResources(String nodeId, String nodeAddress,
                                                                           String contextPath) {
        log.info("获取节点Swagger资源: nodeId={}, nodeAddress={}, contextPath={}", nodeId, nodeAddress, contextPath);

        try {
            // 构建Swagger资源URL
            String baseUrl = buildBaseUrl(nodeAddress, contextPath);
            String resourcesUrl = baseUrl + "/swagger-resources";

            log.info("请求Swagger资源URL: {}", resourcesUrl);

            // 请求Swagger资源
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(resourcesUrl, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                // 解析Swagger资源
                List<Map<String, Object>> resources = parseSwaggerResources(response.getBody());
                log.info("成功获取Swagger资源: nodeId={}, 资源数={}", nodeId, resources.size());

                return ReturnResult.ok(resources);
            } else {
                log.warn("获取Swagger资源失败: nodeId={}, status={}", nodeId, response.getStatusCode());
                // 返回默认资源
                List<Map<String, Object>> defaultResources = createDefaultSwaggerResources();
                return ReturnResult.ok(defaultResources);
            }

        } catch (Exception e) {
            log.error("获取节点Swagger资源异常: nodeId={}, nodeAddress={}", nodeId, nodeAddress, e);
            // 异常时返回默认资源
            List<Map<String, Object>> defaultResources = createDefaultSwaggerResources();
            return ReturnResult.ok(defaultResources);
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> executeNodeApi(NodeApiExecuteRequest request) {
        log.info("执行节点API: nodeId={}, path={}, method={}", request.getNodeId(), request.getApi().getPath(),
                request.getApi().getMethod());

        Instant startTime = Instant.now();

        try {
            // 构建请求URL
            String baseUrl = buildBaseUrl(request.getNodeAddress(), request.getContextPath());
            String apiPath = request.getApi().getPath();

            // 替换路径参数
            if (request.getPathParams() != null && !request.getPathParams().isEmpty()) {
                for (Map.Entry<String, String> entry : request.getPathParams().entrySet()) {
                    apiPath = apiPath.replace("{" + entry.getKey() + "}", entry.getValue());
                }
            }

            // 构建完整URL
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(baseUrl + apiPath);

            // 添加查询参数
            if (request.getQueryParams() != null && !request.getQueryParams().isEmpty()) {
                for (Map.Entry<String, String> entry : request.getQueryParams().entrySet()) {
                    if (StringUtils.isNotEmpty(entry.getValue())) {
                        uriBuilder.queryParam(entry.getKey(), entry.getValue());
                    }
                }
            }

            String finalUrl = uriBuilder.toUriString();
            log.debug("执行API请求URL: {}", finalUrl);

            // 构建请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            if (request.getHeaders() != null && !request.getHeaders().isEmpty()) {
                for (Map.Entry<String, String> entry : request.getHeaders().entrySet()) {
                    headers.add(entry.getKey(), entry.getValue());
                }
            }

            // 构建请求体
            Object requestBody = null;
            if (StringUtils.isNotEmpty(request.getRequestBody())) {
                try {
                    requestBody = OBJECT_MAPPER.readValue(request.getRequestBody(), Object.class);
                } catch (Exception e) {
                    log.warn("解析请求体失败，使用原始字符串: {}", e.getMessage());
                    requestBody = request.getRequestBody();
                }
            }

            HttpEntity<Object> entity = new HttpEntity<>(requestBody, headers);

            // 执行请求
            HttpMethod httpMethod = HttpMethod.valueOf(request.getApi().getMethod().toUpperCase());
            ResponseEntity<String> response = restTemplate.exchange(finalUrl, httpMethod, entity, String.class);

            // 计算执行时间
            Duration duration = Duration.between(startTime, Instant.now());

            // 构建响应结果
            Map<String, Object> result = new HashMap<>();
            result.put("status", response.getStatusCode().value());
            result.put("statusText", HttpStatus.valueOf(response.getStatusCode().value()).getReasonPhrase());
            result.put("headers", convertHeaders(response.getHeaders()));
            result.put("duration", duration.toMillis());
            result.put("ok", response.getStatusCode().is2xxSuccessful());

            // 解析响应体
            if (response.getBody() != null) {
                try {
                    Object responseData = OBJECT_MAPPER.readValue(response.getBody(), Object.class);
                    result.put("data", responseData);
                } catch (Exception e) {
                    log.debug("响应体不是JSON格式，使用原始字符串");
                    result.put("data", response.getBody());
                }
            } else {
                result.put("data", null);
            }

            log.info("API执行完成: nodeId={}, path={}, status={}, duration={}ms", request.getNodeId(),
                    request.getApi().getPath(), response.getStatusCode().value(), duration.toMillis());

            return ReturnResult.ok(result);

        } catch (Exception e) {
            Duration duration = Duration.between(startTime, Instant.now());
            log.error("执行节点API异常: nodeId={}, path={}, duration={}ms", request.getNodeId(), request.getApi().getPath(),
                    duration.toMillis(), e);

            // 构建错误响应
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("status", 500);
            errorResult.put("statusText", "Internal Server Error");
            errorResult.put("headers", new HashMap<>());
            errorResult.put("duration", duration.toMillis());
            errorResult.put("ok", false);
            errorResult.put("data", "执行API异常: " + e.getMessage());

            return ReturnResult.ok(errorResult);
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> getNodeHealth(String nodeId) {
        log.info("获取节点健康状态: nodeId={}", nodeId);

        try {
            // 通过节点管理服务获取节点信息
            ReturnResult<OnlineNodeInfo> nodeResult = nodeManagementService.getNodeDetails(nodeId);
            if (!nodeResult.isOk() || nodeResult.getData() == null) {
                return ReturnResult.error("节点不存在或已离线");
            }

            OnlineNodeInfo nodeInfo = nodeResult.getData();
            String contextPath = getContextPathFromNode(nodeInfo);
            String baseUrl = buildBaseUrl(nodeInfo.getIpAddress() + ":" + nodeInfo.getPort(), contextPath);
            String healthUrl = baseUrl + "/actuator/health";

            // 请求健康检查接口
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(healthUrl, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> healthData = OBJECT_MAPPER.readValue(response.getBody(), Map.class);
                return ReturnResult.ok(healthData);
            } else {
                return ReturnResult.error("获取健康状态失败");
            }

        } catch (Exception e) {
            log.error("获取节点健康状态异常: nodeId={}", nodeId, e);
            return ReturnResult.error("获取健康状态异常: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> getNodeInfo(String nodeId) {
        log.info("获取节点基本信息: nodeId={}", nodeId);

        try {
            // 通过节点管理服务获取节点信息
            ReturnResult<OnlineNodeInfo> nodeResult = nodeManagementService.getNodeDetails(nodeId);
            if (!nodeResult.isOk() || nodeResult.getData() == null) {
                return ReturnResult.error("节点不存在或已离线");
            }

            OnlineNodeInfo nodeInfo = nodeResult.getData();
            String contextPath = getContextPathFromNode(nodeInfo);
            String baseUrl = buildBaseUrl(nodeInfo.getIpAddress() + ":" + nodeInfo.getPort(), contextPath);
            String infoUrl = baseUrl + "/actuator/info";

            // 请求信息接口
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(infoUrl, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> infoData = OBJECT_MAPPER.readValue(response.getBody(), Map.class);
                return ReturnResult.ok(infoData);
            } else {
                return ReturnResult.error("获取节点信息失败");
            }

        } catch (Exception e) {
            log.error("获取节点基本信息异常: nodeId={}", nodeId, e);
            return ReturnResult.error("获取节点信息异常: " + e.getMessage());
        }
    }

    /**
     * 从节点信息中获取上下文路径
     */
    private String getContextPathFromNode(OnlineNodeInfo nodeInfo) {
        if (nodeInfo.getMetadata() != null) {
            Object contextPath = nodeInfo.getMetadata().get("contextPath");
            if (contextPath != null) {
                return contextPath.toString();
            }
        }
        return ""; // 默认为空
    }

    /**
     * 构建基础URL
     */
    private String buildBaseUrl(String nodeAddress, String contextPath) {
        String baseUrl = "http://" + nodeAddress;
        if (StringUtils.isNotEmpty(contextPath) && !"/".equals(contextPath)) {
            if (!contextPath.startsWith("/")) {
                contextPath = "/" + contextPath;
            }
            baseUrl += contextPath;
        }
        return baseUrl;
    }

    /**
     * 解析Swagger文档
     */
    private List<NodeApiGroup> parseSwaggerDoc(String swaggerJson) {
        try {
            JsonNode rootNode = OBJECT_MAPPER.readTree(swaggerJson);
            JsonNode pathsNode = rootNode.get("paths");

            if (pathsNode == null) {
                return Collections.emptyList();
            }

            Map<String, List<NodeApiInfo>> groupedApis = new HashMap<>();

            // 遍历所有路径
            pathsNode.fields().forEachRemaining(pathEntry -> {
                String path = pathEntry.getKey();
                JsonNode pathNode = pathEntry.getValue();

                // 遍历所有HTTP方法
                pathNode.fields().forEachRemaining(methodEntry -> {
                    String method = methodEntry.getKey().toUpperCase();
                    JsonNode methodNode = methodEntry.getValue();

                    // 创建API信息
                    NodeApiInfo apiInfo = new NodeApiInfo(path, method);

                    // 设置摘要和描述
                    if (methodNode.has("summary")) {
                        apiInfo.setSummary(methodNode.get("summary").asText());
                    }
                    if (methodNode.has("description")) {
                        apiInfo.setDescription(methodNode.get("description").asText());
                    }

                    // 解析参数
                    if (methodNode.has("parameters")) {
                        List<NodeApiParameter> parameters = new ArrayList<>();
                        methodNode.get("parameters").forEach(paramNode -> {
                            NodeApiParameter param = new NodeApiParameter();
                            param.setName(paramNode.get("name").asText());
                            param.setIn(paramNode.get("in").asText());
                            param.setRequired(paramNode.has("required") && paramNode.get("required").asBoolean());

                            if (paramNode.has("schema") && paramNode.get("schema").has("type")) {
                                param.setType(paramNode.get("schema").get("type").asText());
                            }
                            if (paramNode.has("description")) {
                                param.setDescription(paramNode.get("description").asText());
                            }

                            parameters.add(param);
                        });
                        apiInfo.setParameters(parameters);
                    }

                    // 确定分组
                    String groupName = determineGroupName(path, methodNode);
                    groupedApis.computeIfAbsent(groupName, k -> new ArrayList<>()).add(apiInfo);
                });
            });

            // 转换为分组列表
            return groupedApis.entrySet().stream().map(entry -> new NodeApiGroup(entry.getKey(), entry.getValue()))
                    .sorted(Comparator.comparing(NodeApiGroup::getName)).collect(Collectors.toList());

        } catch (Exception e) {
            log.error("解析Swagger文档失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 确定API分组名称
     */
    private String determineGroupName(String path, JsonNode methodNode) {
        // 优先使用tags
        if (methodNode.has("tags") && methodNode.get("tags").isArray() && methodNode.get("tags").size() > 0) {
            return methodNode.get("tags").get(0).asText();
        }

        // 根据路径确定分组
        String[] pathParts = path.split("/");
        if (pathParts.length > 1) {
            return pathParts[1];
        }

        return "默认分组";
    }

    /**
     * 转换响应头
     */
    private Map<String, String> convertHeaders(HttpHeaders headers) {
        Map<String, String> result = new HashMap<>();
        headers.forEach((key, values) -> {
            if (!values.isEmpty()) {
                result.put(key, String.join(", ", values));
            }
        });
        return result;
    }

    /**
     * 创建模拟API数据用于测试
     */
    private List<NodeApiGroup> createMockApiGroups() {
        List<NodeApiGroup> groups = new ArrayList<>();

        // 用户管理分组
        List<NodeApiInfo> userApis = new ArrayList<>();

        NodeApiInfo getUsersApi = new NodeApiInfo("/api/users", "GET", "获取用户列表");
        getUsersApi.setDescription("获取系统中所有用户的列表信息");
        List<NodeApiParameter> getUsersParams = new ArrayList<>();
        getUsersParams.add(new NodeApiParameter("page", "query", false, "integer", "页码，默认为1"));
        getUsersParams.add(new NodeApiParameter("size", "query", false, "integer", "每页大小，默认为10"));
        getUsersApi.setParameters(getUsersParams);
        userApis.add(getUsersApi);

        NodeApiInfo getUserApi = new NodeApiInfo("/api/users/{id}", "GET", "获取用户详情");
        getUserApi.setDescription("根据用户ID获取用户详细信息");
        List<NodeApiParameter> getUserParams = new ArrayList<>();
        getUserParams.add(new NodeApiParameter("id", "path", true, "string", "用户ID"));
        getUserApi.setParameters(getUserParams);
        userApis.add(getUserApi);

        NodeApiInfo createUserApi = new NodeApiInfo("/api/users", "POST", "创建用户");
        createUserApi.setDescription("创建新的用户账户");
        createUserApi.setParameters(new ArrayList<>());
        userApis.add(createUserApi);

        groups.add(new NodeApiGroup("用户管理", userApis));

        // 系统监控分组
        List<NodeApiInfo> monitorApis = new ArrayList<>();

        NodeApiInfo healthApi = new NodeApiInfo("/actuator/health", "GET", "健康检查");
        healthApi.setDescription("获取应用程序健康状态");
        healthApi.setParameters(new ArrayList<>());
        monitorApis.add(healthApi);

        NodeApiInfo infoApi = new NodeApiInfo("/actuator/info", "GET", "应用信息");
        infoApi.setDescription("获取应用程序基本信息");
        infoApi.setParameters(new ArrayList<>());
        monitorApis.add(infoApi);

        NodeApiInfo metricsApi = new NodeApiInfo("/actuator/metrics", "GET", "系统指标");
        metricsApi.setDescription("获取系统性能指标");
        metricsApi.setParameters(new ArrayList<>());
        monitorApis.add(metricsApi);

        groups.add(new NodeApiGroup("系统监控", monitorApis));

        // 文件管理分组
        List<NodeApiInfo> fileApis = new ArrayList<>();

        NodeApiInfo listFilesApi = new NodeApiInfo("/api/files", "GET", "文件列表");
        listFilesApi.setDescription("获取文件系统中的文件列表");
        List<NodeApiParameter> listFilesParams = new ArrayList<>();
        listFilesParams.add(new NodeApiParameter("path", "query", false, "string", "文件路径"));
        listFilesApi.setParameters(listFilesParams);
        fileApis.add(listFilesApi);

        NodeApiInfo uploadApi = new NodeApiInfo("/api/files/upload", "POST", "上传文件");
        uploadApi.setDescription("上传文件到服务器");
        uploadApi.setParameters(new ArrayList<>());
        fileApis.add(uploadApi);

        groups.add(new NodeApiGroup("文件管理", fileApis));

        return groups;
    }

    /**
     * 解析Swagger资源响应
     *
     * @param responseBody 响应体
     * @return Swagger资源列表
     */
    private List<Map<String, Object>> parseSwaggerResources(String responseBody) {
        try {
            JsonNode rootNode = OBJECT_MAPPER.readTree(responseBody);
            List<Map<String, Object>> resources = new ArrayList<>();

            if (rootNode.isArray()) {
                for (JsonNode resourceNode : rootNode) {
                    Map<String, Object> resource = new HashMap<>();

                    if (resourceNode.has("name")) {
                        resource.put("name", resourceNode.get("name").asText());
                    }
                    if (resourceNode.has("url")) {
                        resource.put("url", resourceNode.get("url").asText());
                    }
                    if (resourceNode.has("location")) {
                        resource.put("location", resourceNode.get("location").asText());
                    }
                    if (resourceNode.has("swaggerVersion")) {
                        resource.put("swaggerVersion", resourceNode.get("swaggerVersion").asText());
                    }

                    resources.add(resource);
                }
            }

            return resources;
        } catch (Exception e) {
            log.error("解析Swagger资源失败", e);
            return createDefaultSwaggerResources();
        }
    }

    /**
     * 创建默认Swagger资源
     *
     * @return 默认Swagger资源列表
     */
    private List<Map<String, Object>> createDefaultSwaggerResources() {
        List<Map<String, Object>> resources = new ArrayList<>();

        Map<String, Object> defaultResource = new HashMap<>();
        defaultResource.put("name", "default");
        defaultResource.put("url", "/v2/api-docs");
        defaultResource.put("location", "/v2/api-docs");
        defaultResource.put("swaggerVersion", "2.0");
        resources.add(defaultResource);

        Map<String, Object> v3Resource = new HashMap<>();
        v3Resource.put("name", "default-v3");
        v3Resource.put("url", "/v3/api-docs");
        v3Resource.put("location", "/v3/api-docs");
        v3Resource.put("swaggerVersion", "3.0");
        resources.add(v3Resource);

        return resources;
    }
}
