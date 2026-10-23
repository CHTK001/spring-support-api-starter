package com.chua.starter.face.reid.setting;

import com.chua.starter.face.reid.properties.FaceReidConfig;
import com.chua.starter.face.reid.properties.FaceReidProperties;
import com.chua.starter.face.reid.reid.SimilarityMetric;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 重识别配置服务测试：播种、加载、热更新、重置与无存储降级。
 *
 * @author CH
 * @since 4.0.0.43
 */
class FaceReidSettingServiceTest {

    /**
     * 内存存储，模拟 sys_setting。
     */
    private InMemoryStore store;

    /**
     * 出厂配置。
     */
    private FaceReidProperties properties;

    /**
     * 被测服务。
     */
    private FaceReidSettingService service;

    /**
     * 构造。
     */
    @BeforeEach
    void setUp() {
        store = new InMemoryStore();
        properties = new FaceReidProperties();
        properties.setThreshold(0.75);
        properties.setTopK(7);
        service = new FaceReidSettingService(store, properties);
    }

    /**
     * 首次启动：用出厂配置播种全部配置项。
     */
    @Test
    @DisplayName("首次启动播种出厂配置")
    void shouldSeedDefaults() {
        service.initialize();

        assertTrue(service.available());
        assertEquals(FaceReidSettingService.DESCRIPTORS.size(), store.rows.size(), "应播种全部配置项");
        assertEquals("0.75", store.rows.get(FaceReidSettingService.NAME_THRESHOLD), "应写入 yml 的值");
        assertEquals("7", store.rows.get(FaceReidSettingService.NAME_TOP_K));
        assertEquals("local", store.rows.get(FaceReidSettingService.NAME_ENGINE));
        assertEquals(0.75, service.config().threshold(), 1e-9);
    }

    /**
     * 播种只补缺失项，不覆盖运维已改的值。
     */
    @Test
    @DisplayName("播种不覆盖已有值")
    void shouldNotOverwriteExistingOnSeed() {
        store.rows.put(FaceReidSettingService.NAME_THRESHOLD, "0.93");
        service.initialize();

        assertEquals("0.93", store.rows.get(FaceReidSettingService.NAME_THRESHOLD), "已有值应保留");
        assertEquals(0.93, service.config().threshold(), 1e-9, "生效值应取数据库");
    }

    /**
     * 更新后立即热生效并通知监听者。
     */
    @Test
    @DisplayName("更新配置即时生效")
    void shouldApplyUpdateImmediately() {
        service.initialize();
        List<FaceReidConfig> notified = new ArrayList<>();
        service.addListener(notified::add);

        boolean changed = service.update(Map.of(
                FaceReidSettingService.NAME_THRESHOLD, "0.88",
                FaceReidSettingService.NAME_TOP_K, "25"));

        assertTrue(changed);
        assertEquals(0.88, service.config().threshold(), 1e-9);
        assertEquals(25, service.config().topK());
        assertEquals("0.88", store.rows.get(FaceReidSettingService.NAME_THRESHOLD), "应写回存储");
        assertEquals(1, notified.size(), "应通知一次");
        assertEquals(0.88, notified.get(0).threshold(), 1e-9);
    }

    /**
     * 非法值被忽略，不破坏当前配置。
     */
    @Test
    @DisplayName("非法数值被忽略")
    void shouldIgnoreInvalidValues() {
        service.initialize();
        double before = service.config().threshold();

        service.update(Map.of(FaceReidSettingService.NAME_THRESHOLD, "not-a-number"));

        assertEquals(before, service.config().threshold(), 1e-9, "非法值不应改变生效阈值");
    }

    /**
     * 未知键不算变更。
     */
    @Test
    @DisplayName("未知配置项被忽略")
    void shouldIgnoreUnknownKeys() {
        service.initialize();
        assertFalse(service.update(Map.of("noSuchOption", "1")), "未知键不应产生变更");
    }

    /**
     * 重置回到出厂配置。
     */
    @Test
    @DisplayName("重置回出厂配置")
    void shouldResetToDefaults() {
        service.initialize();
        service.update(Map.of(FaceReidSettingService.NAME_THRESHOLD, "0.99"));
        assertEquals(0.99, service.config().threshold(), 1e-9);

        assertTrue(service.reset());
        assertEquals(0.75, service.config().threshold(), 1e-9, "应回到 yml 的值");
    }

    /**
     * 度量切换为欧氏。
     */
    @Test
    @DisplayName("可切换相似度度量")
    void shouldSwitchMetric() {
        service.initialize();
        service.update(Map.of(FaceReidSettingService.NAME_METRIC, "euclidean"));
        assertEquals(SimilarityMetric.EUCLIDEAN, service.config().metric());

        service.update(Map.of(FaceReidSettingService.NAME_METRIC, "cosine"));
        assertEquals(SimilarityMetric.COSINE, service.config().metric());
    }

    /**
     * 无存储时用 yml 配置，更新落在内存覆盖层。
     */
    @Test
    @DisplayName("无存储时降级为内存配置")
    void shouldFallbackWithoutStore() {
        FaceReidSettingService memory = new FaceReidSettingService(new FaceReidSettingStore.Noop(), properties);
        memory.initialize();

        assertFalse(memory.available(), "无存储应报告不可用");
        assertEquals(0.75, memory.config().threshold(), 1e-9, "应使用 yml 值");

        assertTrue(memory.update(Map.of(FaceReidSettingService.NAME_THRESHOLD, "0.5")));
        assertEquals(0.5, memory.config().threshold(), 1e-9, "内存覆盖应生效");
    }

    /**
     * 配置项元数据完整，供管理端渲染表单。
     */
    @Test
    @DisplayName("配置项元数据完整")
    void shouldDescribeAllOptions() {
        assertFalse(FaceReidSettingService.DESCRIPTORS.isEmpty());
        for (FaceReidSettingService.Descriptor descriptor : FaceReidSettingService.DESCRIPTORS) {
            assertNotNull(descriptor.name());
            assertNotNull(descriptor.type());
            assertNotNull(descriptor.remark(), "配置项应有说明: " + descriptor.name());
        }
        assertNotNull(FaceReidSettingService.descriptorOf(FaceReidSettingService.NAME_THRESHOLD));
    }

    /**
     * raw() 返回全部当前值，便于前端回填。
     */
    @Test
    @DisplayName("raw 返回当前全部配置")
    void shouldExposeRawValues() {
        service.initialize();
        Map<String, String> raw = service.raw();
        for (FaceReidSettingService.Descriptor descriptor : FaceReidSettingService.DESCRIPTORS) {
            assertTrue(raw.containsKey(descriptor.name()), "缺少配置项: " + descriptor.name());
        }
    }

    /**
     * 内存存储。
     */
    private static final class InMemoryStore implements FaceReidSettingStore {

        /**
         * 名称 → 值。
         */
        private final Map<String, String> rows = new LinkedHashMap<>();

        @Override
        public boolean available() {
            return true;
        }

        @Override
        public List<Row> list(String group) {
            List<Row> result = new ArrayList<>();
            int sort = 0;
            for (Map.Entry<String, String> entry : rows.entrySet()) {
                result.add(new Row(entry.getKey(), entry.getValue(), sort++));
            }
            return result;
        }

        @Override
        public void upsert(String group, String name, String value, String valueType, String remark, int sort) {
            rows.put(name, value);
        }

        @Override
        public int deleteByPrefix(String group, String namePrefix) {
            int removed = 0;
            for (String name : new ArrayList<>(rows.keySet())) {
                if (name.startsWith(namePrefix)) {
                    rows.remove(name);
                    removed++;
                }
            }
            return removed;
        }
    }
}
