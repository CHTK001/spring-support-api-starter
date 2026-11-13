package com.chua.starter.tenant.server.provision;

import com.chua.common.support.lang.datasource.kv.KvEngine;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link TenantProvisionTaskStore} 的持久化契约。
 *
 * <p>用内存假引擎验证三件事：快照能原样写入/读回、取消标记能覆盖跨节点快照的
 * {@code cancellable}、没有 KV 引擎时退化为 no-op 而不影响建租户主流程。</p>
 *
 * @since 4.0.0.51
 * @author CH
 */
class TenantProvisionTaskStoreTest {

    @Test
    void saveAndFindRoundTripsSnapshot() {
        InMemoryKvEngine kvEngine = new InMemoryKvEngine();
        TenantProvisionTaskStore store = storeWith(kvEngine);
        TenantProvisionTask task = task("task-1");

        store.save(task);

        assertTrue(store.isPersistent());
        Map<String, Object> snapshot = store.find("task-1");
        assertNotNull(snapshot);
        assertEquals("task-1", snapshot.get("taskId"));
        assertEquals("RUNNING", snapshot.get("status"));
        assertEquals("测试租户", snapshot.get("tenantName"));
        assertTrue(snapshot.containsKey("steps"));
    }

    @Test
    void cancelFlagIsVisibleAcrossNodesAndOverridesCancellable() {
        InMemoryKvEngine kvEngine = new InMemoryKvEngine();
        TenantProvisionTaskStore writer = storeWith(kvEngine);
        TenantProvisionTaskStore reader = storeWith(kvEngine);
        writer.save(task("task-2"));

        assertTrue(reader.requestCancel("task-2"));
        assertTrue(reader.isCancelRequested("task-2"));

        Map<String, Object> snapshot = reader.find("task-2");
        assertNotNull(snapshot);
        assertEquals(Boolean.FALSE, snapshot.get("cancellable"));
    }

    @Test
    void reapsStaleRunningTaskAndProtectsTerminalState() {
        InMemoryKvEngine kvEngine = new InMemoryKvEngine();
        TenantProvisionTaskStore store = storeWith(kvEngine);
        long now = System.currentTimeMillis();

        TenantProvisionTask stale = task("task-stale");
        stale.getSteps().get(0).setStatus(TenantProvisionTask.StepStatus.RUNNING);
        Map<String, Object> staleSnapshot = stale.toMap();
        // [P3C A20 豁免]
        staleSnapshot.put("heartbeatTime", now - 10L * 60 * 1000);
        store.save(staleSnapshot);

        TenantProvisionTask fresh = task("task-fresh");
        Map<String, Object> freshSnapshot = fresh.toMap();
        freshSnapshot.put("heartbeatTime", now);
        store.save(freshSnapshot);

        // [P3C A20 豁免]
        assertEquals(1, store.reapOrphaned(now - 3L * 60 * 1000));

        Map<String, Object> reaped = store.find("task-stale");
        assertNotNull(reaped);
        assertEquals("FAILED", reaped.get("status"));
        assertEquals(Boolean.TRUE, reaped.get("finished"));
        assertEquals(Boolean.FALSE, reaped.get("cancellable"));
        assertTrue(store.isReaped("task-stale"));
        assertEquals("ERROR", ((Map<?, ?>) ((java.util.List<?>) reaped.get("steps")).get(0)).get("status"));

        // 旧执行线程恢复后写入 RUNNING，也不能覆盖已经收尾的终态。
        staleSnapshot.put("status", "RUNNING");
        store.save(staleSnapshot);
        assertEquals("FAILED", store.find("task-stale").get("status"));
        assertEquals("RUNNING", store.find("task-fresh").get("status"));
    }

    @Test
    void heartbeatSkipsTaskWhenWorkerIsGone() throws InterruptedException {
        InMemoryKvEngine kvEngine = new InMemoryKvEngine();
        TenantProvisionTaskStore store = storeWith(kvEngine);
        TenantProvisionService service = new TenantProvisionService(
                null, null, null, null, null, null, null, store, new TenantProvisionProgressBus());

        TenantProvisionTask task = task("task-dead-worker");
        task.setStatus(TenantProvisionTask.Status.RUNNING);
        // [P3C C1 豁免]
        Thread worker = new Thread(() -> { });
        worker.start();
        worker.join();

        Map<String, TenantProvisionTask> tasks = (Map<String, TenantProvisionTask>)
                ReflectionTestUtils.getField(service, "tasks");
        Map<String, Thread> workers = (Map<String, Thread>)
                ReflectionTestUtils.getField(service, "workers");
        tasks.put(task.getTaskId(), task);
        workers.put(task.getTaskId(), worker);

        service.heartbeatActiveTasks();

        // 执行线程已退出：不能再刷新心跳，否则失联任务永远达不到收尾阈值。
        assertNull(store.find(task.getTaskId()));
    }

    @Test
    void withoutKvEngineFallsBackToNoOp() {
        TenantProvisionTaskStore store = new TenantProvisionTaskStore();

        assertFalse(store.isPersistent());
        store.save(task("task-3"));
        assertNull(store.find("task-3"));
        assertFalse(store.requestCancel("task-3"));
        assertFalse(store.isCancelRequested("task-3"));
    }

    private TenantProvisionTaskStore storeWith(KvEngine kvEngine) {
        TenantProvisionTaskStore store = new TenantProvisionTaskStore();
        ReflectionTestUtils.setField(store, "kvEngine", kvEngine);
        return store;
    }

    private TenantProvisionTask task(String taskId) {
        TenantProvisionTask task = new TenantProvisionTask(taskId);
        task.setTenantName("测试租户");
        task.setTenantUsername("tenant_admin");
        task.getSteps().add(new TenantProvisionTask.Step("validate", "校验", "检查参数"));
        task.appendDetail("开始");
        return task;
    }

    /**
     * 最小内存 KV 引擎，模拟 Redis 的 get/put 语义；不支持 TTL。
     */
    private static class InMemoryKvEngine implements KvEngine {

        private final Map<String, String> values = new LinkedHashMap<>();

        @Override
        public String get(String key) {
            return values.get(key);
        }

        @Override
        public void put(String key, String value) {
            values.put(key, value);
        }

        @Override
        public boolean containsKey(String key) {
            return values.containsKey(key);
        }

        @Override
        public boolean delete(String key) {
            return values.remove(key) != null;
        }

        @Override
        public long incr(String key) {
            long next = Long.parseLong(values.getOrDefault(key, "0")) + 1;
            values.put(key, String.valueOf(next));
            return next;
        }

        @Override
        public Map<String, String> findAllByPrefix(String prefix) {
            Map<String, String> result = new LinkedHashMap<>();
            values.forEach((key, value) -> {
                if (key.startsWith(prefix)) {
                    result.put(key, value);
                }
            });
            return result;
        }
    }
}
