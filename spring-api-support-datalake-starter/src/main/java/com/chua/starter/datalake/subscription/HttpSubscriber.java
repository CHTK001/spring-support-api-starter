package com.chua.starter.datalake.subscription;

import com.chua.common.support.core.annotation.Spi;
import com.chua.datalake.support.envelope.DataEnvelope;
import com.chua.datalake.support.spi.subscription.Subscriber;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * HTTP 订阅器 — 通过 HTTP 长轮询查询订阅数据
 * <p>与 API 服务共享 58081 端口，路由由 DatalakeApiServer 注册。</p>
 *
 * <b>订阅接入标准:</b>
 * <ul>
 *   <li>注册订阅：POST /subscribe/register {"clientId":"...","pipelineId":"...","topics":["t1","t2"]}</li>
 *   <li>拉取数据：GET /subscribe/poll?clientId=xxx&amp;maxBatch=10</li>
 *   <li>输出格式：JSON 序列化的 {@link DataEnvelope} 列表</li>
 * </ul>
 *
 * @author CH
 */
@Spi("http-subscriber")
public class HttpSubscriber implements Subscriber {

    /** 客户端订阅信息 */
    private static class SubscriptionInfo {
        final String pipelineId;
        final Set<String> topics;
        final Map<String, Object> filter;

        SubscriptionInfo(String pipelineId, Set<String> topics, Map<String, Object> filter) {
            this.pipelineId = pipelineId;
            this.topics = topics != null ? topics : new HashSet<>();
            this.filter = filter;
        }

        boolean matches(DataEnvelope envelope) {
            // 检查 pipelineId（为空表示匹配所有管线）
            if (pipelineId != null && !pipelineId.isEmpty()
                    && envelope.getPipelineId() != null
                    && !pipelineId.equals(envelope.getPipelineId())) {
                return false;
            }
            // 检查 topics（为空表示匹配所有主题）
            if (!topics.isEmpty() && envelope.getTopics() != null) {
                for (String topic : envelope.getTopics()) {
                    if (topics.contains(topic)) return true;
                }
                return false;
            }
            return true;
        }
    }

    /** 客户端订阅关系映射: clientId -> 订阅信息 */
    private final Map<String, SubscriptionInfo> clientSubscriptions = new ConcurrentHashMap<>();

    /** 待拉取消息队列: clientId -> 消息列表（每个客户端独立 FIFO） */
    private final Map<String, CopyOnWriteArrayList<DataEnvelope>> pendingMessages = new ConcurrentHashMap<>();

    /** 运行状态标记 */
    private volatile boolean running;

    @Override
    public String protocol() {
        return "HTTP";
    }

    @Override
    public int defaultPort() {
        return 58081;
    }

    @Override
    public void start(int port) {
        this.running = true;
    }

    @Override
    public void stop() {
        this.running = false;
        pendingMessages.clear();
        clientSubscriptions.clear();
    }

    /**
     * 推送数据到指定客户端
     * <p>clientId 为空时按订阅关系匹配所有匹配客户端。</p>
     */
    @Override
    public void push(String clientId, DataEnvelope envelope) {
        if (clientId == null) {
            // 广播给所有订阅匹配的客户端
            for (Map.Entry<String, SubscriptionInfo> entry : clientSubscriptions.entrySet()) {
                if (entry.getValue().matches(envelope)) {
                    enqueue(entry.getKey(), envelope);
                }
            }
        } else {
            enqueue(clientId, envelope);
        }
    }

    /**
     * 向指定客户端的待消费队列添加消息
     */
    private void enqueue(String clientId, DataEnvelope envelope) {
        pendingMessages.computeIfAbsent(clientId, k -> new CopyOnWriteArrayList<>()).add(envelope);
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    /**
     * 注册客户端订阅（带 pipelineId 和过滤条件）
     */
    @Override
    public void subscribe(String clientId, String pipelineId, List<String> topics, Map<String, Object> filter) {
        Set<String> topicSet = topics != null ? new HashSet<>(topics) : new HashSet<>();
        clientSubscriptions.put(clientId, new SubscriptionInfo(pipelineId, topicSet, filter));
    }

    /**
     * 取消客户端订阅
     */
    @Override
    public void unsubscribe(String clientId) {
        clientSubscriptions.remove(clientId);
        pendingMessages.remove(clientId);
    }

    /**
     * 判断是否有客户端匹配此数据信封
     */
    @Override
    public boolean matches(DataEnvelope envelope) {
        for (SubscriptionInfo info : clientSubscriptions.values()) {
            if (info.matches(envelope)) return true;
        }
        return false;
    }

    /**
     * 将 {@link DataEnvelope} 转换为 HTTP 协议输出格式（JSON 字符串）
     */
    @Override
    public String format(DataEnvelope envelope) {
        return toJson(envelope);
    }

    /**
     * 获取所有客户端及其订阅信息（供管理 API 调用）
     *
     * @return 客户端订阅信息映射，key=clientId
     */
    public Map<String, Map<String, Object>> getClientSubscriptions() {
        Map<String, Map<String, Object>> result = new LinkedHashMap<>();
        for (Map.Entry<String, SubscriptionInfo> entry : clientSubscriptions.entrySet()) {
            Map<String, Object> info = new LinkedHashMap<>();
            info.put("pipelineId", entry.getValue().pipelineId);
            info.put("topics", entry.getValue().topics);
            int pending = pendingMessages.getOrDefault(entry.getKey(), new CopyOnWriteArrayList<>()).size();
            info.put("pending", pending);
            result.put(entry.getKey(), info);
        }
        return result;
    }

    /**
     * 轮询拉取待消费消息（覆盖 SPI 接口默认方法）
     */
    @Override
    public List<DataEnvelope> poll(String clientId, int maxBatch) {
        var queue = pendingMessages.get(clientId);
        if (queue == null || queue.isEmpty()) return Collections.emptyList();

        var result = new ArrayList<DataEnvelope>();
        while (!queue.isEmpty() && result.size() < maxBatch) {
            DataEnvelope msg = queue.remove(0);
            result.add(msg);
        }
        return result;
    }
}
