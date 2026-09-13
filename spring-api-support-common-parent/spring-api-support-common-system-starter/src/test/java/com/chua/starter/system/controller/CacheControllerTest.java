package com.chua.starter.system.controller;

import com.chua.common.support.lang.code.ReturnResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 缓存管理接口单元测试
 *
 * <p>直接实例化 CacheController 并 mock StringRedisTemplate,
 * 验证模块列表、key 列表、单 key/模块/全量清理逻辑, 不启动 Web 上下文。</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("缓存管理接口测试")
class CacheControllerTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @InjectMocks
    private CacheController cacheController;

    @Test
    @DisplayName("模块列表-返回预定义模块并统计key数")
    void testModules() {
        when(stringRedisTemplate.keys(anyString())).thenReturn(new HashSet<>(Set.of("k1")));

        ReturnResult<List<Map<String, Object>>> result = cacheController.modules();
        assertTrue(result.isSuccess());
        List<Map<String, Object>> data = result.getData();
        assertEquals(8, data.size(), "应返回8个预定义模块");
        assertEquals("dict", data.get(0).get("module"));
        assertEquals("字典", data.get(0).get("name"));
        assertEquals("sys:dict:*", data.get(0).get("keyPrefix"));
        assertEquals(1L, data.get(0).get("keyCount"));
    }

    @Test
    @DisplayName("模块列表-redis异常时key数兜底为0")
    void testModulesRedisError() {
        when(stringRedisTemplate.keys(anyString())).thenThrow(new RuntimeException("redis down"));
        ReturnResult<List<Map<String, Object>>> result = cacheController.modules();
        assertTrue(result.isSuccess());
        assertEquals(8, result.getData().size());
        assertEquals(0L, result.getData().get(0).get("keyCount"));
    }

    @Test
    @DisplayName("key列表-自定义pattern优先于模块前缀")
    void testKeysByPattern() {
        when(stringRedisTemplate.keys("my:*")).thenReturn(new HashSet<>(Set.of("my:1", "my:2")));
        ReturnResult<List<String>> result = cacheController.keys(null, "my:*");
        assertTrue(result.isSuccess());
        assertEquals(2, result.getData().size());
        verify(stringRedisTemplate).keys("my:*");
    }

    @Test
    @DisplayName("key列表-按模块查询使用模块前缀")
    void testKeysByModule() {
        when(stringRedisTemplate.keys("sys:menu:*")).thenReturn(new HashSet<>());
        ReturnResult<List<String>> result = cacheController.keys("menu", null);
        assertTrue(result.isSuccess());
        assertTrue(result.getData().isEmpty());
    }

    @Test
    @DisplayName("删除key-key为空返回非法参数")
    void testDeleteKeyBlank() {
        ReturnResult<Boolean> result = cacheController.deleteKey("  ");
        assertFalse(result.isSuccess());
        verify(stringRedisTemplate, never()).delete(anyString());
    }

    @Test
    @DisplayName("删除key-合法key调用redis删除")
    void testDeleteKeyOk() {
        ReturnResult<Boolean> result = cacheController.deleteKey("sys:menu:1");
        assertTrue(result.isSuccess());
        verify(stringRedisTemplate).delete("sys:menu:1");
    }

    @Test
    @DisplayName("删除模块-未知模块返回非法参数")
    void testDeleteModuleUnknown() {
        ReturnResult<Long> result = cacheController.deleteModule("not-exist");
        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("模块不存在"));
    }

    @Test
    @DisplayName("删除模块-命中前缀批量删除")
    void testDeleteModuleOk() {
        Set<String> keys = new HashSet<>(Set.of("sys:role:1", "sys:role:2"));
        when(stringRedisTemplate.keys("sys:role:*")).thenReturn(keys);
        when(stringRedisTemplate.delete(keys)).thenReturn(2L);

        ReturnResult<Long> result = cacheController.deleteModule("role");
        assertTrue(result.isSuccess());
        assertEquals(2L, result.getData());
    }

    @Test
    @DisplayName("清空全部-无key时返回0")
    void testClearAllEmpty() {
        when(stringRedisTemplate.keys("*")).thenReturn(new HashSet<>());
        ReturnResult<Long> result = cacheController.clearAll();
        assertTrue(result.isSuccess());
        assertEquals(0L, result.getData());
    }

    @Test
    @DisplayName("清空全部-返回删除数量")
    void testClearAllOk() {
        Set<String> keys = new HashSet<>(Set.of("a", "b", "c"));
        when(stringRedisTemplate.keys("*")).thenReturn(keys);
        when(stringRedisTemplate.delete(keys)).thenReturn(3L);
        ReturnResult<Long> result = cacheController.clearAll();
        assertTrue(result.isSuccess());
        assertEquals(3L, result.getData());
    }
}
