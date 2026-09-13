package com.chua.starter.system.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.system.model.cache.SysCacheInfo;
import com.chua.starter.system.service.impl.SysCacheServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 缓存管理服务单元测试
 *
 * <p>覆盖缓存列表(固定6项)、刷新单个缓存(合法/空名称/未知名称)、
 * 刷新全部缓存, 以及未接入 Redis / Redis 异常时的兜底逻辑。</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("缓存管理服务测试")
class SysCacheServiceTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @InjectMocks
    private SysCacheServiceImpl sysCacheService;

    @Test
    @DisplayName("获取缓存列表-固定返回6项")
    void shouldReturnFixedSixCaches() {
        List<SysCacheInfo> list = sysCacheService.list();

        assertNotNull(list);
        assertEquals(6, list.size(), "缓存定义固定为 role/dict/dept/setting/menu/user 共6项");
        assertEquals("role", list.get(0).getCacheName());
        // 顺序与静态初始化块一致
        assertEquals("user", list.get(5).getCacheName());
    }

    @Test
    @DisplayName("刷新单个缓存-合法名称按模式删除key并返回成功")
    void shouldRefreshSingleCacheSuccessfully() {
        Set<String> keys = new HashSet<>(Set.of("cache:role:1", "cache:role:2"));
        when(stringRedisTemplate.keys("*cache:role*")).thenReturn(keys);
        when(stringRedisTemplate.delete(keys)).thenReturn(2L);

        ReturnResult<Boolean> result = sysCacheService.refresh("role");

        assertTrue(result.isSuccess());
        verify(stringRedisTemplate).keys("*cache:role*");
        verify(stringRedisTemplate).delete(keys);
    }

    @Test
    @DisplayName("刷新单个缓存-名称为空返回非法参数")
    void shouldThrowWhenCacheNameBlank() {
        ReturnResult<Boolean> result = sysCacheService.refresh("  ");

        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("缓存名称不能为空"));
        verify(stringRedisTemplate, never()).keys(anyString());
    }

    @Test
    @DisplayName("刷新单个缓存-未知名称返回非法参数")
    void shouldThrowWhenCacheNameUnknown() {
        ReturnResult<Boolean> result = sysCacheService.refresh("not-exist");

        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("不支持的缓存名称"));
        verify(stringRedisTemplate, never()).keys(anyString());
    }

    @Test
    @DisplayName("刷新单个缓存-Redis异常时兜底返回成功(吞异常)")
    void shouldSwallowRedisExceptionOnRefresh() {
        when(stringRedisTemplate.keys(anyString())).thenThrow(new RuntimeException("redis down"));

        ReturnResult<Boolean> result = sysCacheService.refresh("dict");

        assertTrue(result.isSuccess(), "清理失败不应向上抛出, 仅记录日志");
    }

    @Test
    @DisplayName("刷新全部缓存-遍历6个缓存分别清理")
    void shouldRefreshAllCachesSuccessfully() {
        when(stringRedisTemplate.keys(anyString())).thenReturn(new HashSet<>());

        ReturnResult<Boolean> result = sysCacheService.refreshAll();

        assertTrue(result.isSuccess());
        // 6 个缓存各调用一次 keys
        verify(stringRedisTemplate, times(6)).keys(anyString());
    }
}
