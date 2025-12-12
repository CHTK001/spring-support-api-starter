package com.chua.starter.monitor.service.impl;

import com.chua.common.support.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.MapUtils;
import com.chua.socket.support.session.SocketSessionTemplate;
import com.chua.starter.monitor.service.node.NodeLogService;
import com.chua.sync.support.SyncServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 节点日志服务实现
 * <p>
 * 通过 SyncServer 订阅节点日志，通过 WebSocket 推送到前端
 * </p>
 *
 * @author CH
 * @version 1.0.0
 * @since 2025/01/17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NodeLogServiceImpl implements NodeLogService {

    private final SyncServer syncServer;
    private final SocketSessionTemplate socketSessionTemplate;

    /**
     * WebSocket Topic - 节点日志推送
     */
    private static final String WS_TOPIC_NODE_LOG = "monitor/node/log";

    /**
     * SyncServer Topic
     */
    private static final String TOPIC_LOG_SUBSCRIBE = "monitor/log/subscribe";
    private static final String TOPIC_LOG_UNSUBSCRIBE = "monitor/log/unsubscribe";

    /**
     * 请求超时时间（秒）
     */
    private static final int REQUEST_TIMEOUT = 10;

    /**
     * 已订阅的节点（nodeKey -> level）
     */
    private final ConcurrentHashMap<String, String> subscribedNodes = new ConcurrentHashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public ReturnResult<Map<String, Object>> subscribeLogForNode(String ipAddress, Integer port, String level) {
        String nodeKey = buildNodeKey(ipAddress, port);
        log.info("[NodeLog] 订阅日志: {}, level={}", nodeKey, level);

        try {
            Map<String, Object> request = new LinkedHashMap<>();
            request.put("level", level);

            CompletableFuture<Object> future = syncServer.sendRequest(nodeKey, TOPIC_LOG_SUBSCRIBE, request);
            Object response = future.get(REQUEST_TIMEOUT, TimeUnit.SECONDS);

            if (response instanceof Map) {
                Map<String, Object> responseMap = (Map<String, Object>) response;
                int code = MapUtils.getIntValue(responseMap, "code", 500);
                String message = MapUtils.getString(responseMap, "message", "未知错误");

                if (code == 200) {
                    // 记录订阅
                    subscribedNodes.put(nodeKey, level);
                    return ReturnResult.ok(responseMap);
                } else {
                    return ReturnResult.error(message);
                }
            }

            return ReturnResult.error("响应格式错误");
        } catch (java.util.concurrent.TimeoutException e) {
            log.error("[NodeLog] 订阅超时: {}", nodeKey);
            return ReturnResult.error("订阅超时，节点可能离线");
        } catch (Exception e) {
            log.error("[NodeLog] 订阅失败: {}", nodeKey, e);
            return ReturnResult.error("订阅失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> unsubscribeLogForNode(String ipAddress, Integer port) {
        String nodeKey = buildNodeKey(ipAddress, port);
        log.info("[NodeLog] 取消订阅: {}", nodeKey);

        try {
            Map<String, Object> request = new LinkedHashMap<>();

            CompletableFuture<Object> future = syncServer.sendRequest(nodeKey, TOPIC_LOG_UNSUBSCRIBE, request);
            future.get(REQUEST_TIMEOUT, TimeUnit.SECONDS);

            // 移除订阅记录
            subscribedNodes.remove(nodeKey);

            return ReturnResult.ok(true);
        } catch (Exception e) {
            log.error("[NodeLog] 取消订阅失败: {}", nodeKey, e);
            // 即使失败也移除本地记录
            subscribedNodes.remove(nodeKey);
            return ReturnResult.ok(true);
        }
    }

    @Override
    public void handleLogPush(Map<String, Object> logData) {
        try {
            String applicationName = MapUtils.getString(logData, "applicationName");
            Integer port = MapUtils.getInteger(logData, "port", 0);

            // 构建推送消息
            Map<String, Object> wsMessage = new LinkedHashMap<>();
            wsMessage.put("type", "NODE_LOG");
            wsMessage.put("applicationName", applicationName);
            wsMessage.put("port", port);
            wsMessage.put("timestamp", MapUtils.getLong(logData, "timestamp"));
            wsMessage.put("time", MapUtils.getString(logData, "time"));
            wsMessage.put("level", MapUtils.getString(logData, "level"));
            wsMessage.put("loggerName", MapUtils.getString(logData, "loggerName"));
            wsMessage.put("threadName", MapUtils.getString(logData, "threadName"));
            wsMessage.put("message", MapUtils.getString(logData, "message"));

            // 异常信息
            if (logData.containsKey("exception")) {
                wsMessage.put("exception", MapUtils.getString(logData, "exception"));
                wsMessage.put("exceptionClass", MapUtils.getString(logData, "exceptionClass"));
            }

            // 通过 WebSocket 推送到前端
            socketSessionTemplate.send(WS_TOPIC_NODE_LOG, Json.toJson(wsMessage));

            log.debug("[NodeLog] 日志推送: {}:{} - {}", applicationName, port, 
                    MapUtils.getString(logData, "message"));
        } catch (Exception e) {
            log.error("[NodeLog] 日志推送失败", e);
        }
    }

    /**
     * 构建节点 Key
     */
    private String buildNodeKey(String ipAddress, Integer port) {
        return ipAddress + ":" + port;
    }

    /**
     * 检查节点是否已订阅
     */
    public boolean isSubscribed(String ipAddress, Integer port) {
        return subscribedNodes.containsKey(buildNodeKey(ipAddress, port));
    }

    /**
     * 获取已订阅节点数量
     */
    public int getSubscribedCount() {
        return subscribedNodes.size();
    }
}
