package com.chua.starter.monitor;

import com.chua.starter.monitor.configuration.RagGlobalSettingProvider;

/**
 * RAG 模块快速验证——直接测试 RagGlobalSettingProvider。
 */
public class RagSmokeTest {

    public static void main(String[] args) {
        System.out.println("=== RAG Smoke Test ===");

        // 直接测试 RagGlobalSettingProvider，不依赖 SPI ServiceProvider
        RagGlobalSettingProvider provider = new RagGlobalSettingProvider();

        // 1. group
        String group = provider.group();
        assertEqual("rag", group, "group");
        System.out.println("[OK] group = " + group);

        // 2. groupName
        String groupName = provider.groupName();
        assertEqual("RAG 知识库", groupName, "groupName");
        System.out.println("[OK] groupName = " + groupName);

        // 3. 配置项
        var items = provider.getItems();
        assertEqual(8, items.size(), "配置项数量");
        System.out.println("[OK] 配置项数 = " + items.size());

        // 4. 验证每个配置项
        for (var item : items) {
            assertNotEmpty(item.field(), "field");
            assertNotEmpty(item.valueType(), "valueType");
            assertTrue(item.sort() > 0, item.field() + " 排序 > 0");
            System.out.println("  " + item.field() + " = " + item.defaultValue()
                    + " (" + item.valueType() + ") [" + item.sort() + "]");
        }

        // 5. 验证特定字段
        assertTrue(items.stream().anyMatch(i -> "rag_embedding_engine".equals(i.field())), "rag_embedding_engine");
        assertTrue(items.stream().anyMatch(i -> "rag_vector_storage".equals(i.field())), "rag_vector_storage");
        assertTrue(items.stream().anyMatch(i -> "rag_chunk_size".equals(i.field())), "rag_chunk_size");
        assertTrue(items.stream().anyMatch(i -> "rag_chunk_overlap".equals(i.field())), "rag_chunk_overlap");
        assertTrue(items.stream().anyMatch(i -> "rag_default_top_k".equals(i.field())), "rag_default_top_k");
        assertTrue(items.stream().anyMatch(i -> "rag_similarity_threshold".equals(i.field())), "rag_similarity_threshold");
        assertTrue(items.stream().anyMatch(i -> "rag_llm_provider".equals(i.field())), "rag_llm_provider");
        assertTrue(items.stream().anyMatch(i -> "rag_llm_model".equals(i.field())), "rag_llm_model");
        System.out.println("[OK] 字段完整性验证通过");

        System.out.println("\n=== All tests passed ===");
    }

    static void assertEqual(Object expected, Object actual, String msg) {
        if (!java.util.Objects.equals(expected, actual)) {
            throw new AssertionError("FAIL: " + msg + " expected=" + expected + " actual=" + actual);
        }
    }

    static void assertNotEmpty(String value, String msg) {
        if (value == null || value.isBlank()) {
            throw new AssertionError("FAIL: " + msg + " is empty");
        }
    }

    static void assertTrue(boolean condition, String msg) {
        if (!condition) {
            throw new AssertionError("FAIL: " + msg);
        }
    }
}
