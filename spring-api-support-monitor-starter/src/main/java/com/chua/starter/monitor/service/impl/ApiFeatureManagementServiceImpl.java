package com.chua.starter.monitor.service.impl;

import com.chua.common.support.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.pojo.app.ApiFeatureVO;
import com.chua.starter.monitor.pojo.OnlineNodeInfo;
import com.chua.starter.monitor.service.app.ApiFeatureManagementService;
import com.chua.starter.monitor.service.node.NodeManagementService;
import com.chua.sync.support.server.SyncServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * API 功能开关管理服务实现
 * <p>
 * 通过 REST 调用或 SyncServer 获取和管理客户端节点的功能开关。
 * </p>
 *
 * @author CH
 * @since 2024/12/08
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiFeatureManagementServiceImpl implements ApiFeatureManagementService {

    private static final String FEATURE_API_PATH = "/api/features";
    /**
     * SyncServer 主题（与客户端 MonitorTopics.API_FEATURE_CONTROL 保持一致）
     */
    private static final String TOPIC_FEATURE_CONTROL = "monitor/control/api-feature";

    private final RestTemplate restTemplate;
    private final NodeManagementService nodeManagementService;

    @Autowired(required = false)
    private SyncServer syncServer;

    @Override
    @SuppressWarnings("unchecked")
    public ReturnResult<List<ApiFeatureVO>> getNodeFeatures(String ipAddress, Integer port) {
        try {
            log.debug("获取节点功能开关列表: ipAddress={}, port={}", ipAddress, port);

            OnlineNodeInfo nodeInfo = nodeManagementService.getOnlineNode(ipAddress, port);
            if (nodeInfo == null) {
                return ReturnResult.error("节点不在线");
            }

            String url = buildNodeUrl(nodeInfo, FEATURE_API_PATH);
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                ReturnResult<List<Map<String, Object>>> result = Json.fromJson(
                        response.getBody(),
                        ReturnResult.class
                );

                if (result.isSuccess() && result.getData() != null) {
                    List<ApiFeatureVO> features = new ArrayList<>();
                    for (Map<String, Object> item : result.getData()) {
                        features.add(ApiFeatureVO.fromMap(item));
                    }
                    return ReturnResult.ok(features);
                }
                return ReturnResult.error(result.getMsg());
            }

            return ReturnResult.error("获取功能开关列表失败");
        } catch (Exception e) {
            log.error("获取节点功能开关列表失败: ipAddress={}, port={}", ipAddress, port, e);
            return ReturnResult.error("获取功能开关列表失败: " + e.getMessage());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public ReturnResult<Map<String, Object>> getNodeFeatureStats(String ipAddress, Integer port) {
        try {
            log.debug("获取节点功能开关统计: ipAddress={}, port={}", ipAddress, port);

            OnlineNodeInfo nodeInfo = nodeManagementService.getOnlineNode(ipAddress, port);
            if (nodeInfo == null) {
                return ReturnResult.error("节点不在线");
            }

            String url = buildNodeUrl(nodeInfo, FEATURE_API_PATH + "/stats");
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                ReturnResult<Map<String, Object>> result = Json.fromJson(
                        response.getBody(),
                        ReturnResult.class
                );

                if (result.isSuccess()) {
                    return ReturnResult.ok(result.getData());
                }
                return ReturnResult.error(result.getMsg());
            }

            return ReturnResult.error("获取功能开关统计失败");
        } catch (Exception e) {
            log.error("获取节点功能开关统计失败: ipAddress={}, port={}", ipAddress, port, e);
            return ReturnResult.error("获取功能开关统计失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> setNodeFeatureEnabled(String ipAddress, Integer port, String featureId, boolean enabled) {
        try {
            log.info("设置节点功能开关状态: ipAddress={}, port={}, featureId={}, enabled={}",
                    ipAddress, port, featureId, enabled);

            OnlineNodeInfo nodeInfo = nodeManagementService.getOnlineNode(ipAddress, port);
            if (nodeInfo == null) {
                return ReturnResult.error("节点不在线");
            }

            // 优先使用 SyncServer
            if (syncServer != null) {
                Map<String, Object> message = new HashMap<>();
                message.put("action", "setEnabled");
                message.put("featureId", featureId);
                message.put("enabled", enabled);

                boolean success = syncServer.publish(ipAddress, port, TOPIC_FEATURE_CONTROL, message);
                if (success) {
                    log.info("通过 SyncServer 发送功能开关控制命令: {}:{}", ipAddress, port);
                    return ReturnResult.ok(true);
                }
                // SyncServer 未找到节点，降级使用 REST
            }

            // 降级使用 REST 调用
            String url = buildNodeUrl(nodeInfo, FEATURE_API_PATH + "/" + featureId + "?enabled=" + enabled);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
            return response.getStatusCode().is2xxSuccessful()
                    ? ReturnResult.ok(true)
                    : ReturnResult.error("设置功能开关状态失败");
        } catch (Exception e) {
            log.error("设置节点功能开关状态失败: ipAddress={}, port={}, featureId={}",
                    ipAddress, port, featureId, e);
            return ReturnResult.error("设置功能开关状态失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> setNodeFeatureEnabledBatch(String ipAddress, Integer port, Map<String, Boolean> states) {
        try {
            log.info("批量设置节点功能开关状态: ipAddress={}, port={}, count={}", ipAddress, port, states.size());

            OnlineNodeInfo nodeInfo = nodeManagementService.getOnlineNode(ipAddress, port);
            if (nodeInfo == null) {
                return ReturnResult.error("节点不在线");
            }

            // 优先使用 SyncServer
            if (syncServer != null) {
                Map<String, Object> message = new HashMap<>();
                message.put("action", "setBatch");
                message.put("states", states);

                boolean success = syncServer.publish(ipAddress, port, TOPIC_FEATURE_CONTROL, message);
                if (success) {
                    log.info("通过 SyncServer 发送批量功能开关控制命令: {}:{}", ipAddress, port);
                    return ReturnResult.ok(true);
                }
                // SyncServer 未找到节点，降级使用 REST
            }

            // 降级使用 REST 调用
            String url = buildNodeUrl(nodeInfo, FEATURE_API_PATH + "/batch");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(Json.toJson(states), headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
            return response.getStatusCode().is2xxSuccessful()
                    ? ReturnResult.ok(true)
                    : ReturnResult.error("批量设置功能开关状态失败");
        } catch (Exception e) {
            log.error("批量设置节点功能开关状态失败: ipAddress={}, port={}", ipAddress, port, e);
            return ReturnResult.error("批量设置功能开关状态失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> resetNodeAllFeatures(String ipAddress, Integer port) {
        try {
            log.info("重置节点所有功能开关: ipAddress={}, port={}", ipAddress, port);

            OnlineNodeInfo nodeInfo = nodeManagementService.getOnlineNode(ipAddress, port);
            if (nodeInfo == null) {
                return ReturnResult.error("节点不在线");
            }

            // 优先使用 SyncServer
            if (syncServer != null) {
                Map<String, Object> message = new HashMap<>();
                message.put("action", "resetAll");

                boolean success = syncServer.publish(ipAddress, port, TOPIC_FEATURE_CONTROL, message);
                if (success) {
                    log.info("通过 SyncServer 发送重置功能开关命令: {}:{}", ipAddress, port);
                    return ReturnResult.ok(true);
                }
                // SyncServer 未找到节点，降级使用 REST
            }

            // 降级使用 REST 调用
            String url = buildNodeUrl(nodeInfo, FEATURE_API_PATH + "/reset-all");
            ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);
            return response.getStatusCode().is2xxSuccessful()
                    ? ReturnResult.ok(true)
                    : ReturnResult.error("重置功能开关失败");
        } catch (Exception e) {
            log.error("重置节点所有功能开关失败: ipAddress={}, port={}", ipAddress, port, e);
            return ReturnResult.error("重置功能开关失败: " + e.getMessage());
        }
    }

    /**
     * 构建节点 URL
     */
    private String buildNodeUrl(OnlineNodeInfo nodeInfo, String path) {
        String protocol = "http";
        String contextPath = nodeInfo.getContextPath() != null ? nodeInfo.getContextPath() : "";
        return String.format("%s://%s:%d%s%s", protocol, nodeInfo.getIpAddress(), nodeInfo.getPort(), contextPath, path);
    }

}
