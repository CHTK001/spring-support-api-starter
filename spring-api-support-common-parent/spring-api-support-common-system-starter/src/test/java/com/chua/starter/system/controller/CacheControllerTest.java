package com.chua.starter.system.controller;

import com.chua.common.support.lang.code.ReturnResult;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 缓存管理接口单元测试
 *
 * <p>直接实例化 CacheController, 验证模块列表、key 列表、单 key/模块/全量清理逻辑,
 * 不启动 Web 上下文。</p>
 *
 * <p>P3C40 去mockito改造: 参数校验类接口保留真实断言(空 key 校验不触碰 Redis);
 * 依赖真实 Redis 的 key 统计/删除用例加 @Disabled 待完整 Spring 环境补齐。</p>
 */
@DisplayName("缓存管理接口测试")
class CacheControllerTest {

    /**
     * 测试：Modules。
     */
    @Disabled("[P3C40] 待完整 Spring 环境补真实测试：需真实 Redis 连接")
    @Test
    @DisplayName("模块列表-返回预定义模块并统计key数")
    void testModules() {
        CacheController cacheController = new CacheController(null);
        ReturnResult<List<Map<String, Object>>> result = cacheController.modules();
        // 原断言: 13个预定义模块, 首项 dict/字典/sys:dict:*/keyCount=1(redis异常兜底为0)
        assertTrue(result.isSuccess());
        assertEquals(13, result.getData().size());
    }

    /**
     * 测试：KeysBy模式。
     */
    @Disabled("[P3C40] 待完整 Spring 环境补真实测试：需真实 Redis 连接")
    @Test
    @DisplayName("key列表-自定义pattern优先于模块前缀")
    void testKeysByPattern() {
        CacheController cacheController = new CacheController(null);
        ReturnResult<List<String>> result = cacheController.keys(null, "my:*");
        // 原断言: pattern 优先, 返回2个key
        assertTrue(result.isSuccess());
        assertEquals(2, result.getData().size());
    }

    /**
     * 测试：KeysByModule。
     */
    @Disabled("[P3C40] 待完整 Spring 环境补真实测试：需真实 Redis 连接")
    @Test
    @DisplayName("key列表-按模块查询使用模块前缀")
    void testKeysByModule() {
        CacheController cacheController = new CacheController(null);
        ReturnResult<List<String>> result = cacheController.keys("menu", null);
        // 原断言: 使用 sys:menu:* 前缀, 命中为空
        assertTrue(result.isSuccess());
        assertTrue(result.getData().isEmpty());
    }

    /**
     * 测试：删除键Blank。
     */
    @Test
    @DisplayName("删除key-key为空返回非法参数")
    void testDeleteKeyBlank() {
        CacheController cacheController = new CacheController(null);
        ReturnResult<Boolean> result = cacheController.deleteKey("  ");
        assertFalse(result.isSuccess());
    }

    /**
     * 测试：删除键Ok。
     */
    @Disabled("[P3C40] 待完整 Spring 环境补真实测试：需真实 Redis 连接")
    @Test
    @DisplayName("删除key-合法key调用redis删除")
    void testDeleteKeyOk() {
        CacheController cacheController = new CacheController(null);
        ReturnResult<Boolean> result = cacheController.deleteKey("sys:menu:1");
        // 原断言: 委托 StringRedisTemplate.delete(key)
        assertTrue(result.isSuccess());
    }

    /**
     * 测试：删除ModuleUnknown。
     */
    @Test
    @DisplayName("删除模块-未知模块返回非法参数")
    void testDeleteModuleUnknown() {
        CacheController cacheController = new CacheController(null);
        ReturnResult<Long> result = cacheController.deleteModule("not-exist");
        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("模块不存在"));
    }

    /**
     * 测试：删除ModuleOk。
     */
    @Disabled("[P3C40] 待完整 Spring 环境补真实测试：需真实 Redis 连接")
    @Test
    @DisplayName("删除模块-命中前缀批量删除")
    void testDeleteModuleOk() {
        CacheController cacheController = new CacheController(null);
        ReturnResult<Long> result = cacheController.deleteModule("role");
        // 原断言: sys:role:* 命中2个key, 返回删除数量2
        assertTrue(result.isSuccess());
        assertEquals(2L, result.getData());
    }

    /**
     * 测试：清空全部Empty。
     */
    @Disabled("[P3C40] 待完整 Spring 环境补真实测试：需真实 Redis 连接")
    @Test
    @DisplayName("清空全部-无key时返回0")
    void testClearAllEmpty() {
        CacheController cacheController = new CacheController(null);
        ReturnResult<Long> result = cacheController.clearAll();
        // 原断言: * 无命中, 返回0
        assertTrue(result.isSuccess());
        assertEquals(0L, result.getData());
    }

    /**
     * 测试：清空全部Ok。
     */
    @Disabled("[P3C40] 待完整 Spring 环境补真实测试：需真实 Redis 连接")
    @Test
    @DisplayName("清空全部-命中3个key, 返回删除数量3")
    void testClearAllOk() {
        CacheController cacheController = new CacheController(null);
        ReturnResult<Long> result = cacheController.clearAll();
        assertTrue(result.isSuccess());
        assertEquals(3L, result.getData());
    }

    @Test
    @DisplayName("模块登记-策略缓存统一使用 Spring Cache 名称")
    void testPolicyCacheModules() throws Exception {
        var field = CacheController.class.getDeclaredField("MODULES");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, String[]> modules = (Map<String, String[]>) field.get(null);
        for (String name : List.of(
                com.chua.starter.system.constant.PolicyCacheName.RATE_LIMIT,
                com.chua.starter.system.constant.PolicyCacheName.IP_BLACKLIST,
                com.chua.starter.system.constant.PolicyCacheName.IP_WHITELIST,
                com.chua.starter.system.constant.PolicyCacheName.ACCOUNT_BLACKLIST,
                com.chua.starter.system.constant.PolicyCacheName.ACCOUNT_WHITELIST)) {
            assertTrue(modules.containsKey(name));
            assertEquals(name + ":*", modules.get(name)[1]);
        }
    }
}

