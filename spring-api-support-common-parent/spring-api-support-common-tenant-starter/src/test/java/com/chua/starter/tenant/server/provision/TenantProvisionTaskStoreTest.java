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
