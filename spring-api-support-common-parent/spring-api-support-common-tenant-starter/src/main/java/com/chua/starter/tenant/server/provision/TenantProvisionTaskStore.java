package com.chua.starter.tenant.server.provision;

import com.chua.common.support.lang.datasource.kv.KvEngine;
import com.chua.common.support.lang.json.Json;
import com.chua.common.support.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;

/**
 * 租户创建任务快照的持久化存储。
 *
 * <p>任务本身由后台虚拟线程推进，不能只放在当前 JVM 的 Map 里：多节点部署时
 * 状态查询与取消可能落到另一台机器，进程重启也会把内存快照清空。这里把
 * {@link TenantProvisionTask#toMap()} 的 JSON 写入统一 KV 引擎（生产环境为 Redis），
 * 并给取消请求单独放一个带 TTL 的标记，供执行节点在步骤边界读取。</p>
 *
 * <p>没有 Redis（例如本地 H2 验证环境）时，KV 引擎为空，本类自动退化为无操作；
 * 调用方仍使用进程内任务表，行为与改造前一致，不会因为缺少缓存让创建失败。</p>
 *
 * <p>快照与取消标记都设置 TTL，避免异常退出后留下永远无人清理的任务键。
 * 写入/读取异常只记 WARN，不阻断建库主流程。</p>
 *
 * @since 4.0.0.51
 * @author CH
 */
@Slf4j
@Component
public class TenantProvisionTaskStore {

    /**
     * 任务快照键前缀
     */
    private static final String TASK_PREFIX = "tenant:provision:task:";

    /**
     * 取消请求键前缀
     */
    private static final String CANCEL_PREFIX = "tenant:provision:cancel:";

    /**
     * 与内存任务的保留时长保持一致：30 分钟
     */
    private static final Duration TASK_TTL = Duration.ofMinutes(30);

    /**
     * 统一 KV 引擎；未装 Redis 时为空
     */
    @Autowired(required = false)
    private KvEngine kvEngine;

    /**
     * 是否接入持久化后端。
     *
     * @return 存在 KV 引擎返回 true
     */
    public boolean isPersistent() {
        return kvEngine != null;
    }

    /**
     * 保存任务快照。
     *
     * @param task 任务
     */
    public void save(TenantProvisionTask task) {
        if (task == null) {
            return;
        }
        save(task.toMap());
    }

    /**
     * 保存已经生成的任务快照，避免重复调用 {@link TenantProvisionTask#toMap()}。
     *
     * @param snapshot 任务快照
     */
    public void save(Map<String, Object> snapshot) {
        if (snapshot == null || kvEngine == null) {
            return;
        }
        String taskId = stringValue(snapshot.get("taskId"));
        if (StringUtils.isBlank(taskId)) {
            return;
        }
        try {
            put(TASK_PREFIX + taskId, Json.toJSONString(snapshot));
        } catch (Exception e) {
            log.warn("[租户创建进度] 任务快照持久化失败: taskId={}, {}", taskId, e.getMessage());
        }
    }

    /**
     * 查询任务快照。
     *
     * @param taskId 任务标识
     * @return 快照；不存在或未接入持久化返回 null
     */
    public Map<String, Object> find(String taskId) {
        if (kvEngine == null || StringUtils.isBlank(taskId)) {
            return null;
        }
        try {
            String json = kvEngine.get(TASK_PREFIX + taskId);
            if (StringUtils.isBlank(json)) {
                return null;
            }
            Map<String, Object> snapshot = Json.fromJson(json);
            if (snapshot != null && isCancelRequested(taskId)) {
                // 取消请求可能落在另一个节点；执行节点尚未推进到步骤边界时，
                // 持久化快照里仍是 cancellable=true，这里按共享标记修正。
                snapshot.put("cancellable", false);
            }
            return snapshot;
        } catch (Exception e) {
            log.warn("[租户创建进度] 任务快照读取失败: taskId={}, {}", taskId, e.getMessage());
            return null;
        }
    }

    /**
     * 写入取消请求标记。
     *
     * @param taskId 任务标识
     * @return 持久化后端可用且写入成功返回 true
     */
    public boolean requestCancel(String taskId) {
        if (kvEngine == null || StringUtils.isBlank(taskId)) {
            return false;
        }
        try {
            put(CANCEL_PREFIX + taskId, "1");
            return true;
        } catch (Exception e) {
            log.warn("[租户创建进度] 取消标记写入失败: taskId={}, {}", taskId, e.getMessage());
            return false;
        }
    }

    /**
     * 是否收到其它节点写入的取消请求。
     *
     * @param taskId 任务标识
     * @return 已请求取消返回 true
     */
    public boolean isCancelRequested(String taskId) {
        if (kvEngine == null || StringUtils.isBlank(taskId)) {
            return false;
        }
        try {
            return StringUtils.isNotBlank(kvEngine.get(CANCEL_PREFIX + taskId));
        } catch (Exception e) {
            log.warn("[租户创建进度] 取消标记读取失败: taskId={}, {}", taskId, e.getMessage());
            return false;
        }
    }

    /**
     * 带 TTL 写入；后端不支持 TTL 时退化为普通写入。
     *
     * @param key 键
     * @param value 值
     */
    private void put(String key, String value) {
        if (kvEngine.supportsTtl()) {
            kvEngine.put(key, value, TASK_TTL);
        } else {
            kvEngine.put(key, value);
        }
    }

    /**
     * 把快照字段安全转为字符串。
     *
     * @param value 字段值
     * @return 字符串；空值返回 null
     */
    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
