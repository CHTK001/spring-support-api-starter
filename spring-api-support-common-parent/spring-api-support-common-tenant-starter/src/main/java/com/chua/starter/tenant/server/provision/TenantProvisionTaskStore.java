package com.chua.starter.tenant.server.provision;

import com.chua.common.support.lang.datasource.kv.KvEngine;
import com.chua.common.support.lang.json.Json;
import com.chua.common.support.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
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
     * 已被判定为执行节点失联的任务标记前缀
     */
    private static final String REAPED_PREFIX = "tenant:provision:reaped:";

    /**
     * 运行中状态字面量
     */
    private static final String STATUS_RUNNING = "RUNNING";

    /**
     * 失败状态字面量
     */
    private static final String STATUS_FAILED = "FAILED";

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
        // 与 markReaped 共用一把锁：同一 JVM 内“检查已收尾 → 写入”必须原子，
        // 否则旧线程可能在 reaper 写入 FAILED 之后又把 RUNNING 覆盖回去。
        synchronized (this) {
            if (isReaped(taskId) && !STATUS_FAILED.equals(stringValue(snapshot.get("status")))) {
                return;
            }
            try {
                put(TASK_PREFIX + taskId, Json.toJSONString(snapshot));
            } catch (Exception e) {
                log.warn("[租户创建进度] 任务快照持久化失败: taskId={}, {}", taskId, e.getMessage());
            }
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
     * 标记任务已被判定为失联并收尾。
     *
     * @param taskId 任务标识
     */
    public void markReaped(String taskId) {
        if (kvEngine == null || StringUtils.isBlank(taskId)) {
            return;
        }
        try {
            synchronized (this) {
                put(REAPED_PREFIX + taskId, "1");
            }
        } catch (Exception e) {
            log.warn("[租户创建进度] 失联标记写入失败: taskId={}, {}", taskId, e.getMessage());
        }
    }

    /**
     * 任务是否已被判定为执行节点失联。
     *
     * @param taskId 任务标识
     * @return 已收尾返回 true
     */
    public boolean isReaped(String taskId) {
        if (kvEngine == null || StringUtils.isBlank(taskId)) {
            return false;
        }
        try {
            return StringUtils.isNotBlank(kvEngine.get(REAPED_PREFIX + taskId));
        } catch (Exception e) {
            log.warn("[租户创建进度] 失联标记读取失败: taskId={}, {}", taskId, e.getMessage());
            return false;
        }
    }

    /**
     * 收尾心跳超时的 RUNNING 任务。
     *
     * <p>由任意节点定期执行：扫描任务快照，把超过阈值未刷新 {@code heartbeatTime}
     * 的 RUNNING 任务改成 FAILED，并把当前步骤标成失败。写入“已收尾”标记，
     * 防止原执行节点恢复后覆盖终态。</p>
     *
     * @param staleBeforeMillis 心跳早于该时间点视为失联
     * @return 本次收尾的任务数
     */
    public int reapOrphaned(long staleBeforeMillis) {
        if (kvEngine == null) {
            return 0;
        }
        Map<String, String> snapshots;
        try {
            snapshots = kvEngine.findAllByPrefix(TASK_PREFIX);
        } catch (Exception e) {
            log.warn("[租户创建进度] 扫描失联任务失败: {}", e.getMessage());
            return 0;
        }
        int count = 0;
        for (String json : snapshots.values()) {
            try {
                Map<String, Object> snapshot = Json.fromJson(json);
                if (snapshot == null || !STATUS_RUNNING.equals(stringValue(snapshot.get("status")))) {
                    continue;
                }
                long heartbeat = longValue(snapshot.get("heartbeatTime"));
                if (heartbeat <= 0 || heartbeat >= staleBeforeMillis) {
                    continue;
                }
                String taskId = stringValue(snapshot.get("taskId"));
                if (StringUtils.isBlank(taskId)) {
                    continue;
                }
                markReaped(taskId);
                markOrphaned(snapshot);
                save(snapshot);
                count++;
                log.warn("[租户创建进度] 任务心跳超时，已标记失败: taskId={}, heartbeat={}",
                        taskId, heartbeat);
            } catch (Exception e) {
                log.warn("[租户创建进度] 收尾失联任务失败: {}", e.getMessage());
            }
        }
        return count;
    }

    /**
     * 把快照改成失联失败终态。
     *
     * @param snapshot 任务快照
     */
    private void markOrphaned(Map<String, Object> snapshot) {
        long now = System.currentTimeMillis();
        String reason = "执行节点心跳超时，任务已终止；已创建的库/账号/记录需人工核对";
        snapshot.put("status", STATUS_FAILED);
        snapshot.put("finished", true);
        snapshot.put("cancellable", false);
        snapshot.put("finishTime", now);
        snapshot.put("currentTitle", "执行中断");
        snapshot.put("currentDescription", reason);
        snapshot.put("message", reason);
        Object stepsValue = snapshot.get("steps");
        if (stepsValue instanceof List<?> steps) {
            for (Object stepValue : steps) {
                if (stepValue instanceof Map<?, ?> rawStep) {
                    for (Map.Entry<?, ?> entry : rawStep.entrySet()) {
                        if ("status".equals(String.valueOf(entry.getKey()))
                                && STATUS_RUNNING.equals(stringValue(entry.getValue()))) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> step = (Map<String, Object>) rawStep;
                            step.put("status", "ERROR");
                            step.put("statusLabel", "失败");
                            step.put("detail", reason);
                            step.put("endTime", now);
                        }
                    }
                }
            }
        }
        Object detailsValue = snapshot.get("details");
        if (detailsValue instanceof List<?> details) {
            @SuppressWarnings("unchecked")
            List<Object> mutableDetails = (List<Object>) details;
            mutableDetails.add(reason);
            while (mutableDetails.size() > 200) {
                mutableDetails.remove(0);
            }
        }
    }

    /**
     * 安全读取长整型。
     *
     * @param value 原始值
     * @return 长整型；无法解析返回 0
     */
    private long longValue(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return value == null ? 0L : Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException e) {
            return 0L;
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
