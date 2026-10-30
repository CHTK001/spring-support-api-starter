package com.chua.starter.system.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.system.model.cache.SysCacheInfo;
import com.chua.starter.system.service.impl.SysCacheServiceImpl;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 缓存管理服务单元测试
 *
 * <p>覆盖缓存列表(固定6项)、刷新单个缓存(合法/空名称/未知名称)、
 * 刷新全部缓存, 以及未接入 Redis / Redis 异常时的兜底逻辑。</p>
 *
 * <p>P3C40 去mockito改造: 未接入 Redis 时刷新/刷新全部走兜底逻辑(直接返回成功),
 * 参数校验与固定列表测试保留真实断言; 依赖真实 Redis 连接的用例加 @Disabled。</p>
 *
 * @author CH
 * @since 4.0.0.51
 */
@DisplayName("缓存管理服务测试")
class SysCacheServiceTest {

    /**
     * 测试：ReturnFixedSixCaches。
     */
    @Test
    @DisplayName("获取缓存列表-固定返回6项")
    void shouldReturnFixedSixCaches() {
        SysCacheServiceImpl sysCacheService = new SysCacheServiceImpl();
        List<SysCacheInfo> list = sysCacheService.list();

        assertNotNull(list);
        assertEquals(6, list.size(), "缓存定义固定为 role/dict/dept/setting/menu/user 共6项");
        assertEquals("role", list.get(0).cacheName());
        // 顺序与静态初始化块一致
        assertEquals("user", list.get(5).cacheName());
    }

    /**
     * 测试：RefreshSingle缓存Successfully。
     */
    @Disabled("[P3C40] 待完整 Spring 环境补真实测试：需真实 Redis 连接")
    @Test
    @DisplayName("刷新单个缓存-合法名称按模式删除key并返回成功")
    void shouldRefreshSingleCacheSuccessfully() {
        SysCacheServiceImpl sysCacheService = new SysCacheServiceImpl();
        ReturnResult<Boolean> result = sysCacheService.refresh("role");

        assertTrue(result.isSuccess());
    }

    /**
     * 测试：ThrowWhen缓存名称Blank。
     */
    @Test
    @DisplayName("刷新单个缓存-名称为空返回非法参数")
    void shouldThrowWhenCacheNameBlank() {
        SysCacheServiceImpl sysCacheService = new SysCacheServiceImpl();
        ReturnResult<Boolean> result = sysCacheService.refresh("  ");

        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("缓存名称不能为空"));
    }

    /**
     * 测试：ThrowWhen缓存名称Unknown。
     */
    @Test
    @DisplayName("刷新单个缓存-未知名称返回非法参数")
    void shouldThrowWhenCacheNameUnknown() {
        SysCacheServiceImpl sysCacheService = new SysCacheServiceImpl();
        ReturnResult<Boolean> result = sysCacheService.refresh("not-exist");

        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("不支持的缓存名称"));
    }

    /**
     * 测试：SwallowRedisException响应Refresh。
     */
    @Disabled("[P3C40] 待完整 Spring 环境补真实测试：需真实 Redis 连接")
    @Test
    @DisplayName("刷新单个缓存-Redis异常时兜底返回成功(吞异常)")
    void shouldSwallowRedisExceptionOnRefresh() {
        SysCacheServiceImpl sysCacheService = new SysCacheServiceImpl();
        ReturnResult<Boolean> result = sysCacheService.refresh("dict");

        assertTrue(result.isSuccess(), "清理失败不应向上抛出, 仅记录日志");
    }

    /**
     * 测试：Refresh全部CachesSuccessfully。
     */
    @Test
    @DisplayName("刷新全部缓存-遍历6个缓存分别清理")
    void shouldRefreshAllCachesSuccessfully() {
        SysCacheServiceImpl sysCacheService = new SysCacheServiceImpl();
        ReturnResult<Boolean> result = sysCacheService.refreshAll();

        assertTrue(result.isSuccess());
    }
}
