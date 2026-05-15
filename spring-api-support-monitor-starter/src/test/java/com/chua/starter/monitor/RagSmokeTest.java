package com.chua.starter.monitor;

import com.chua.common.support.ai.rag.GlobalSettingProvider;
import com.chua.common.support.core.spi.ServiceProvider;
import com.chua.starter.monitor.configuration.RagGlobalSettingProvider;

import java.util.List;

/**
 * RAG 模块快速验证——直接测试核心组件，无需 Spring 容器。
 */
public class RagSmokeTest {

    public static void main(String[] args) {
        System.out.println("=== RAG Smoke Test ===");
        testGlobalSettingProviderSpi();
        System.out.println("=== All tests passed ===");
    }

    static void testGlobalSettingProviderSpi() {
        System.out.println("\n--- GlobalSettingProvider SPI ---");

        // 1. SPI 发现
        ServiceProvider<GlobalSettingProvider> provider = ServiceProvider.of(GlobalSettingProvider.class);
        List<GlobalSettingProvider> providers = provider.collect();
        assertProviders(!providers.isEmpty(), "至少发现一个 GlobalSettingProvider 实现");
        System.out.println("  SPI 发现: " + providers.size() + " 个实现");

        // 2. 验证 RAG provider
        RagGlobalSettingProvider ragProvider = new RagGlobalSettingProvider();
        assertProviders("rag".equals(ragProvider.group()), "group = rag");
        System.out.println("  group: " + ragProvider.group());

        assertProviders("RAG 知识库".equals(ragProvider.groupName()), "groupName = RAG 知识库");
        System.out.println("  groupName: " + ragProvider.groupName());

        // 3. 验证配置项
        List<GlobalSettingProvider.SettingItem> items = ragProvider.getItems();
        assertProviders(items.size() == 8, "应有 8 项配置");
        System.out.println("  配置项数: " + items.size());

        for (GlobalSettingProvider.SettingItem item : items) {
            System.out.println("    " + item.field() + " = " + item.defaultValue()
                    + " (" + item.valueType() + ") [" + item.sort() + "]");

            assertProviders(item.field() != null && !item.field().isBlank(), "字段名不为空");
            assertProviders(item.valueType() != null, "值类型不为空");
            assertProviders(item.sort() > 0, "排序值 > 0");
        }

        // 4. 验证已知字段
        assertProviders(items.stream().anyMatch(i -> "rag_embedding_engine".equals(i.field())), "包含 rag_embedding_engine");
        assertProviders(items.stream().anyMatch(i -> "rag_chunk_size".equals(i.field())), "包含 rag_chunk_size");
        assertProviders(items.stream().anyMatch(i -> "rag_default_top_k".equals(i.field())), "包含 rag_default_top_k");
        assertProviders(items.stream().anyMatch(i -> "rag_similarity_threshold".equals(i.field())), "包含 rag_similarity_threshold");

        System.out.println("  字段完整性验证通过");
    }

    static void assertProviders(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("FAIL: " + message);
        }
        System.out.println("  [OK] " + message);
    }
}
