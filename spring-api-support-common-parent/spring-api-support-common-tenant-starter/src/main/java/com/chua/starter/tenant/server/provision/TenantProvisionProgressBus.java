package com.chua.starter.tenant.server.provision;

import com.chua.common.support.lang.json.Json;
import com.chua.common.support.utils.StringUtils;
import com.chua.socket.support.session.SocketSessionTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 租户开通进度的跨节点分发总线。
 *
 * <p>业务节点只把进度推给本机 Socket 会话；多节点部署时前端可能连在另一台节点上。
 * 这里额外把同一帧发布到 Redis 频道，所有节点订阅后各自投递给本机会话。
 * 发布端在消息里带 {@code source}，订阅端忽略自己发出的消息，避免同一节点重复推送。</p>
 *
 * <p>Redis 不可用时只做本机投递，不影响开通流程；跨节点实时性退化为前端轮询
 * {@code /v2/tenant/provision/status}。监听容器是本组件独占的，使用默认字符串
 * 序列化，避免与公共 Redis 容器的主题序列化配置耦合。</p>
 *
 * @since 4.0.0.51
 * @author CH
 */
@Slf4j
@Component
public class TenantProvisionProgressBus {

    /**
     * 前端 Socket 事件名
     */
    public static final String SOCKET_EVENT = "message";

    /**
     * 前端按模块过滤消息
     */
    public static final String SOCKET_MODULE = "system";

    /**
     * 前端进度主题
     */
    public static final String SOCKET_TOPIC = "tenant:provision:progress";

    /**
     * Redis 跨节点频道
     */
    private static final String REDIS_CHANNEL = "tenant:provision:progress:cluster";

    /**
     * 当前节点标识
     */
    private static final String NODE_ID = UUID.randomUUID().toString();

    /**
     * 本机 Socket 模板；未接入时为空
     */
    @Autowired(required = false)
    private SocketSessionTemplate socketSessionTemplate;

    /**
     * Redis 字符串模板；未启用 Redis 时为空
     */
    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    /**
     * Redis 连接工厂；未启用 Redis 时为空
     */
    @Autowired(required = false)
    private RedisConnectionFactory connectionFactory;

    /**
     * 任务存储；用来确认 Redis 持久化确实已启用，避免 H2 环境误连默认 localhost
     */
    @Autowired(required = false)
    private TenantProvisionTaskStore taskStore;

    /**
     * 本组件独占的 Redis 监听容器
     */
    private RedisMessageListenerContainer container;

    /**
     * 跨节点频道是否已就绪
     */
    private volatile boolean clusterReady;

    /**
     * 启动跨节点监听。
     *
     * <p>只有任务持久化已启用（即 KvEngine/Redis 配置生效）才创建监听容器；
     * H2 等关闭 Redis 的环境不启动后台连接。</p>
     */
    @PostConstruct
    public void start() {
        if (taskStore == null || !taskStore.isPersistent()
                || redisTemplate == null || connectionFactory == null) {
            return;
        }
        try {
            container = new RedisMessageListenerContainer();
            container.setConnectionFactory(connectionFactory);
            MessageListener listener = this::onMessage;
            container.addMessageListener(listener, new ChannelTopic(REDIS_CHANNEL));
            container.afterPropertiesSet();
            container.start();
            clusterReady = true;
            log.info("[租户创建进度] 跨节点进度总线已启动: node={}", NODE_ID);
        } catch (Exception e) {
            clusterReady = false;
            log.warn("[租户创建进度] 跨节点进度总线启动失败，退回本机推送: {}", e.getMessage());
            stopContainer();
        }
    }

    /**
     * 停止跨节点监听。
     */
    @PreDestroy
    public void stop() {
        stopContainer();
    }

    /**
     * 当前节点标识。
     *
     * @return 节点标识
     */
    public String nodeId() {
        return NODE_ID;
    }

    /**
     * 跨节点频道是否可用。
     *
     * @return 已启动返回 true
     */
    public boolean isClusterReady() {
        return clusterReady;
    }

    /**
     * 发布一帧进度：先推本机，再发 Redis 给其它节点。
     *
     * @param snapshot     任务快照
     * @param targetUserId 发起人用户标识，用于定向推送
     */
    public void publish(Map<String, Object> snapshot, String targetUserId) {
        deliver(snapshot, targetUserId);
        publishToCluster(snapshot, targetUserId);
    }

    /**
     * 把进度帧投递到本机 Socket 会话。
     *
     * @param snapshot     任务快照
     * @param targetUserId 发起人用户标识
     */
    private void deliver(Map<String, Object> snapshot, String targetUserId) {
        if (socketSessionTemplate == null || snapshot == null) {
            return;
        }
        try {
            Map<String, Object> payload = new LinkedHashMap<>(snapshot);
            payload.put("module", SOCKET_MODULE);
            payload.put("event", SOCKET_TOPIC);
            payload.put("timestamp", System.currentTimeMillis());
            String message = Json.toJSONString(payload);
            if (socketSessionTemplate.supportsUserTargeting()) {
                // 已启用鉴权但拿不到发起人：宁可不推，也不能广播给其他管理员。
                if (StringUtils.isBlank(targetUserId)) {
                    log.debug("[租户创建进度] 缺少发起人，跳过本机定向推送: taskId={}",
                            snapshot.get("taskId"));
                    return;
                }
                socketSessionTemplate.sendToUser(targetUserId, SOCKET_EVENT, message);
            } else {
                socketSessionTemplate.broadcast(SOCKET_EVENT, message);
            }
        } catch (Exception e) {
            log.warn("[租户创建进度] 本机套接字推送失败: taskId={}, {}",
                    snapshot.get("taskId"), e.getMessage());
        }
    }

    /**
     * 发布到 Redis，供其它节点投递。
     *
     * @param snapshot     任务快照
     * @param targetUserId 发起人用户标识
     */
    private void publishToCluster(Map<String, Object> snapshot, String targetUserId) {
        if (!clusterReady || snapshot == null) {
            return;
        }
        try {
            Map<String, Object> envelope = new LinkedHashMap<>(4);
            envelope.put("source", NODE_ID);
            envelope.put("targetUserId", targetUserId);
            envelope.put("snapshot", snapshot);
            redisTemplate.convertAndSend(REDIS_CHANNEL, Json.toJSONString(envelope));
        } catch (Exception e) {
            // 本机已经推送成功，跨节点失败只影响其它节点；前端仍有轮询兜底。
            log.warn("[租户创建进度] 跨节点发布失败: taskId={}, {}",
                    snapshot.get("taskId"), e.getMessage());
        }
    }

    /**
     * 接收其它节点发布的进度帧。
     *
     * @param message Redis 消息
     * @param pattern 订阅模式
     */
    @SuppressWarnings("unused")
    private void onMessage(Message message, byte[] pattern) {
        try {
            String body = new String(message.getBody(), StandardCharsets.UTF_8);
            Map<String, Object> envelope = Json.fromJson(body);
            if (envelope == null || NODE_ID.equals(stringValue(envelope.get("source")))) {
                return;
            }
            Object value = envelope.get("snapshot");
            if (!(value instanceof Map<?, ?> raw)) {
                return;
            }
            Map<String, Object> snapshot = new LinkedHashMap<>();
            raw.forEach((key, item) -> snapshot.put(String.valueOf(key), item));
            deliver(snapshot, stringValue(envelope.get("targetUserId")));
        } catch (Exception e) {
            log.warn("[租户创建进度] 跨节点进度消息解析失败: {}", e.getMessage());
        }
    }

    /**
     * 停止并释放监听容器。
     */
    private void stopContainer() {
        clusterReady = false;
        if (container != null) {
            try {
                container.stop();
                container.destroy();
            } catch (Exception e) {
                log.debug("[租户创建进度] 跨节点总线停止异常: {}", e.getMessage());
            } finally {
                container = null;
            }
        }
    }

    /**
     * 安全转字符串。
     *
     * @param value 原始值
     * @return 字符串或 null
     */
    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
