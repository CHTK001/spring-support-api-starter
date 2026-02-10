package com.chua.starter.monitor.service.impl;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.core.utils.FileUtils;
import com.chua.common.support.core.utils.MapUtils;
import com.chua.starter.monitor.pojo.node.NodeMyBatisMapperDTO;
import com.chua.starter.monitor.pojo.node.OnlineNodeInfo;
import com.chua.starter.monitor.service.node.NodeManagementService;
import com.chua.starter.monitor.service.node.NodeMyBatisConfigService;
import com.chua.starter.sync.support.server.SyncServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 节点 MyBatis 配置服务实现类
 * <p>
 * 通过 actuator 获取 MyBatis 信息，通过 SyncServer 下发刷新命令
 * </p>
 *
 * @author CH
 * @version 1.0.0
 * @since 2025/01/17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NodeMyBatisConfigServiceImpl implements NodeMyBatisConfigService {

    private final SyncServer syncServer;
    private final RestTemplate restTemplate;
    private final NodeManagementService nodeManagementService;

    /**
     * MyBatis 配置主题
     */
    private static final String MYBATIS_CONFIG_TOPIC = "monitor/control/mybatis";

    @Override
    @SuppressWarnings("unchecked")
    public ReturnResult<List<NodeMyBatisMapperDTO>> getNodeMappers(String nodeUrl, String keyword) {
        try {
            log.info("获取节点 Mapper 列表: nodeUrl={}, keyword={}", nodeUrl, keyword);

            // 解码节点 URL
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

            // 通过 actuator 获取 MyBatis 信息
            String mybatisUrl = buildActuatorUrl(nodeInfo, "mybatis");
            Map<String, Object> response = callActuator(mybatisUrl);
            if (response == null) {
                return ReturnResult.error("获取 MyBatis 信息失败");
            }

            Boolean enabled = MapUtils.getBoolean(response, "enabled", false);
            if (!enabled) {
                return ReturnResult.error("MyBatis 未启用");
            }

            // 解析 Mapper 列表
            List<Map<String, Object>> mappersList = MapUtils.getList(response, "mappers");
            List<NodeMyBatisMapperDTO> mappers = new ArrayList<>();
            if (mappersList != null) {
                for (Map<String, Object> item : mappersList) {
                    String name = MapUtils.getString(item, "name");
                    // 关键词过滤
                    if (keyword != null && !keyword.isEmpty() 
                            && !name.toLowerCase().contains(keyword.toLowerCase())) {
                        continue;
                    }

                    NodeMyBatisMapperDTO dto = new NodeMyBatisMapperDTO();
                    dto.setName(name);
                    dto.setSimpleName(MapUtils.getString(item, "simpleName"));
                    dto.setMethodCount(MapUtils.getLong(item, "methodCount", 0L));
                    mappers.add(dto);
                }
            }

            log.info("获取节点 Mapper 列表成功: nodeUrl={}, 数量={}", nodeUrl, mappers.size());
            return ReturnResult.success(mappers);

        } catch (Exception e) {
            log.error("获取节点 Mapper 列表失败: nodeUrl={}", nodeUrl, e);
            return ReturnResult.error("获取 Mapper 列表失败: " + e.getMessage());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public ReturnResult<NodeMyBatisMapperDTO> getMapperDetail(String nodeUrl, String mapperName) {
        try {
            log.info("获取 Mapper 详情: nodeUrl={}, mapperName={}", nodeUrl, mapperName);

            // 解码节点 URL
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

            // 通过 actuator 获取 Mapper 详情
            String encodedMapperName = URLEncoder.encode(mapperName, StandardCharsets.UTF_8);
            String mybatisUrl = buildActuatorUrl(nodeInfo, "mybatis/" + encodedMapperName);
            Map<String, Object> response = callActuator(mybatisUrl);
            if (response == null) {
                return ReturnResult.error("获取 Mapper 详情失败");
            }

            if (response.containsKey("error")) {
                return ReturnResult.error(MapUtils.getString(response, "error"));
            }

            // 解析响应数据
            NodeMyBatisMapperDTO dto = new NodeMyBatisMapperDTO();
            dto.setName(MapUtils.getString(response, "mapperName"));
            dto.setSimpleName(MapUtils.getString(response, "simpleName"));
            dto.setMethodCount(MapUtils.getLong(response, "methodCount", 0L));

            List<Map<String, Object>> statementsList = MapUtils.getList(response, "statements");
            if (statementsList != null) {
                List<NodeMyBatisMapperDTO.MappedStatementInfo> statements = new ArrayList<>();
                for (Map<String, Object> item : statementsList) {
                    NodeMyBatisMapperDTO.MappedStatementInfo info = new NodeMyBatisMapperDTO.MappedStatementInfo();
                    info.setId(MapUtils.getString(item, "id"));
                    info.setMethodName(MapUtils.getString(item, "methodName"));
                    info.setSqlCommandType(MapUtils.getString(item, "sqlCommandType"));
                    info.setResultType(MapUtils.getString(item, "resultType"));
                    info.setResource(MapUtils.getString(item, "resource"));
                    statements.add(info);
                }
                dto.setStatements(statements);
            }

            log.info("获取 Mapper 详情成功: nodeUrl={}, mapperName={}", nodeUrl, mapperName);
            return ReturnResult.success(dto);

        } catch (Exception e) {
            log.error("获取 Mapper 详情失败: nodeUrl={}, mapperName={}", nodeUrl, mapperName, e);
            return ReturnResult.error("获取 Mapper 详情失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> refreshXml(String nodeUrl, String resourcePath) {
        try {
            log.info("刷新 XML: nodeUrl={}, resourcePath={}", nodeUrl, resourcePath);

            // 解码节点 URL
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

            // 通过 SyncServer 下发刷新命令
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("action", "refreshXml");
            requestData.put("resourcePath", resourcePath);

            Map<String, Object> response = sendRequest(nodeInfo.getIpAddress(), nodeInfo.getPort(), requestData);
            if (response == null) {
                return ReturnResult.error("请求节点超时");
            }

            int code = MapUtils.getInteger(response, "code", 500);
            if (code != 200) {
                return ReturnResult.error(MapUtils.getString(response, "message"));
            }

            log.info("刷新 XML 成功: nodeUrl={}, resourcePath={}", nodeUrl, resourcePath);
            return ReturnResult.success(true);

        } catch (Exception e) {
            log.error("刷新 XML 失败: nodeUrl={}, resourcePath={}", nodeUrl, resourcePath, e);
            return ReturnResult.error("刷新 XML 失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> refreshAllXml(String nodeUrl) {
        try {
            log.info("刷新所有 XML: nodeUrl={}", nodeUrl);

            // 解码节点 URL
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

            // 通过 SyncServer 下发刷新命令
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("action", "refreshAllXml");

            Map<String, Object> response = sendRequest(nodeInfo.getIpAddress(), nodeInfo.getPort(), requestData);
            if (response == null) {
                return ReturnResult.error("请求节点超时");
            }

            int code = MapUtils.getInteger(response, "code", 500);
            if (code != 200) {
                return ReturnResult.error(MapUtils.getString(response, "message"));
            }

            Map<String, Object> result = new HashMap<>();
            result.put("refreshedCount", MapUtils.getInteger(response, "refreshedCount", 0));
            result.put("totalResources", MapUtils.getInteger(response, "totalResources", 0));

            log.info("刷新所有 XML 成功: nodeUrl={}, refreshedCount={}", 
                    nodeUrl, result.get("refreshedCount"));
            return ReturnResult.success(result);

        } catch (Exception e) {
            log.error("刷新所有 XML 失败: nodeUrl={}", nodeUrl, e);
            return ReturnResult.error("刷新所有 XML 失败: " + e.getMessage());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public ReturnResult<Map<String, Object>> getStatistics(String nodeUrl) {
        try {
            log.info("获取 MyBatis 统计信息: nodeUrl={}", nodeUrl);

            // 解码节点 URL
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

            // 通过 actuator 获取统计信息
            String mybatisUrl = buildActuatorUrl(nodeInfo, "mybatis");
            Map<String, Object> response = callActuator(mybatisUrl);
            if (response == null) {
                return ReturnResult.error("获取统计信息失败");
            }

            // 提取统计信息
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("enabled", MapUtils.getBoolean(response, "enabled", false));
            statistics.put("mapperCount", MapUtils.getInteger(response, "mapperCount", 0));
            statistics.put("mappedStatementCount", MapUtils.getInteger(response, "mappedStatementCount", 0));
            statistics.put("resultMapCount", MapUtils.getInteger(response, "resultMapCount", 0));
            statistics.put("sqlFragmentCount", MapUtils.getInteger(response, "sqlFragmentCount", 0));
            statistics.put("cacheEnabled", MapUtils.getBoolean(response, "cacheEnabled", false));
            statistics.put("lazyLoadingEnabled", MapUtils.getBoolean(response, "lazyLoadingEnabled", false));
            statistics.put("sqlTypeCount", MapUtils.getMap(response, "sqlTypeCount"));

            log.info("获取 MyBatis 统计信息成功: nodeUrl={}", nodeUrl);
            return ReturnResult.success(statistics);

        } catch (Exception e) {
            log.error("获取 MyBatis 统计信息失败: nodeUrl={}", nodeUrl, e);
            return ReturnResult.error("获取统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 构建 actuator URL
     *
     * @param nodeInfo 节点信息
     * @param endpoint 端点名称
     * @return actuator URL
     */
    private String buildActuatorUrl(OnlineNodeInfo nodeInfo, String endpoint) {
        String contextPath = nodeInfo.getContextPath();
        String actuatorPath = nodeInfo.getActuatorPath();
        return String.format("http://%s:%d/%s", 
                nodeInfo.getIpAddress(), 
                nodeInfo.getPort(),
                com.chua.common.support.core.utils.StringUtils.removeStart(
                        FileUtils.normalize(String.format("%s/%s/%s", contextPath, actuatorPath, endpoint)), "/"));
    }

    /**
     * 调用 actuator 端点
     *
     * @param url actuator URL
     * @return 响应数据
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> callActuator(String url) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            return null;
        } catch (Exception e) {
            log.error("调用 actuator 失败: url={}, error={}", url, e.getMessage());
            return null;
        }
    }

    /**
     * 发送请求到节点
     * <p>
     * 使用 SyncServer 的 publish 方法发送消息到指定节点
     * 注意：此方法只发送消息，无法获取节点的响应结果
     * </p>
     *
     * @param ipAddress   节点IP地址
     * @param port        节点端口
     * @param requestData 请求数据
     * @return 响应数据（仅表示消息是否发送成功）
     * @author CH
     * @since 1.0.0
     */
    private Map<String, Object> sendRequest(String ipAddress, Integer port, Map<String, Object> requestData) {
        try {
            boolean sent = syncServer.publish(ipAddress, port, MYBATIS_CONFIG_TOPIC, requestData);
            if (sent) {
                // 消息发送成功，返回成功响应
                Map<String, Object> result = new HashMap<>();
                result.put("code", 200);
                result.put("message", "指令已发送");
                return result;
            } else {
                log.warn("发送请求到节点失败，节点可能离线: {}:{}", ipAddress, port);
                return null;
            }
        } catch (Exception e) {
            log.error("发送请求到节点失败: {}:{}", ipAddress, port, e);
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
