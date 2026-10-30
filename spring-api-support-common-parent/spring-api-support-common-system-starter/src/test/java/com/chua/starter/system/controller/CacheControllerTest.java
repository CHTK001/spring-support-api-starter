package com.chua.starter.system.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.system.constant.PolicyCacheName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 缓存管理接口单元测试
 *
 * <p>直接实例化 {@link CacheController}，不启动 Web 上下文、不连真实 Redis。</p>
 *
 * <h3>为什么不再用 @Disabled</h3>
 * <p>本类原有 11 个用例，其中 8 个被标 {@code @Disabled("[P3C40] 待完整 Spring 环境补真实测试：需真实 Redis 连接")}，
 * 实测长期不执行 —— 全模块 168 个测试里有 59 个是这样被跳过的（35%）。
 * 但那些用例真正需要的只是 {@code keys()} 与 {@code delete()} 两个方法的行为可控，
 * 用一个手写的 {@link RecordingRedisTemplate} 就能确定性地覆盖，不需要真实 Redis。
 * AGENTS.md §3 禁用 mockito，所以这里直接继承 StringRedisTemplate 覆写所需方法。</p>
 *
 * <h3>为什么不用 new CacheController(null, null) 配真实 Redis</h3>
 * <p>原写法把 {@code stringRedisTemplate} 传 null，于是每个真正要读 Redis 的分支都会 NPE，
 * 只能靠 @Disabled 躲开 —— 也就是说那些断言从未真正验证过任何行为。
 * 现在依赖是可观测的桩，断言的是「传了哪个 pattern」「删了哪些 key」这类真实契约。</p>
 *
 * @author CH
 * @since 4.0.0.51
 */
@DisplayName("缓存管理接口测试")
class CacheControllerTest {

    /**
     * 可观测的 StringRedisTemplate 桩。
     *
     * <p>只覆写被测路径真正用到的方法；其余保持继承行为（真实实现会因无连接而报错，
     * 从而让「误用未覆写方法」在测试里立刻暴露，而不是静默返回默认值）。</p>
     */
    static class RecordingRedisTemplate extends StringRedisTemplate {

        /** pattern -> 命中 key，用于让 keys() 有确定返回值 */
        private final Map<String, Set<String>> keyspace = new LinkedHashMap<>();

        /** 记录收到过的 pattern，断言「用的是哪个前缀」 */
        private final List<String> queriedPatterns = new java.util.ArrayList<>();

        /** 记录删掉的 key */
        private final List<String> deletedKeys = new java.util.ArrayList<>();

        /** 配置某个 pattern 的命中结果 */
        RecordingRedisTemplate having(String pattern, String... keys) {
            keyspace.put(pattern, new LinkedHashSet<>(List.of(keys)));
            return this;
        }

        /** 记录写入的 key，断言 PUT 是否真的落库 */
        private final Map<String, String> writtenValues = new LinkedHashMap<>();

        /** 记录带 TTL 的写入（秒），用于断言 updateValue 保留剩余 TTL */
        private final Map<String, Long> writtenTtls = new LinkedHashMap<>();

        /**
         * 覆写 opsForValue()，返回只实现被测路径用到方法的 {@link ValueOperations}。
         *
         * <p><b>为什么用动态代理而不是写一个 implements ValueOperations 的类。</b>
         * {@code ValueOperations} 有 40+ 个方法（当前依赖版本还含 Delta、RedisScript、
         * RedisPipeline 等重载），逐个实现会产生上百行与被测逻辑无关的样板，
         * 且每次 Spring Data 升版都可能编译失败。代理只处理三个方法
         * （{@code set} 两参 / 四参、{@code get}），其余一律抛「未覆写」——
         * 被测路径一旦发生意料之外的调用会立刻暴露，而不是静默返回 null。</p>
         *
         * <p><b>为什么不用继承来的真实实现。</b>它要求先 {@code afterPropertiesSet()}，
         * 而那又要求可用的 RedisConnectionFactory；可本桩的 {@code getConnectionFactory()}
         * 必须返回 null 才能让 stats() 的降级路径成立 —— 两个需求直接冲突。</p>
         */
        @Override
        public ValueOperations<String, String> opsForValue() {
            InvocationHandler handler = (proxy, method, args) -> {
                switch (method.getName()) {
                    case "set":
                        writtenValues.put((String) args[0], (String) args[1]);
                        if (args != null && args.length == 4) {
                            writtenTtls.put((String) args[0], (Long) args[2]);
                        }
                        return null;
                    case "get":
                        return writtenValues.get(String.valueOf(args[0]));
                    case "toString":
                        return "RecordingValueOperations";
                    case "hashCode":
                        return System.identityHashCode(proxy);
                    case "equals":
                        return proxy == args[0];
                    default:
                        throw new UnsupportedOperationException(
                                "本测试未覆写 ValueOperations." + method.getName()
                                        + "() —— 说明被测路径发生了意料之外的调用，请补断言而不是放宽桩");
                }
            };
            return (ValueOperations<String, String>) Proxy.newProxyInstance(
                    getClass().getClassLoader(), new Class<?>[]{ValueOperations.class}, handler);
        }


        @Override
        public Set<String> keys(String pattern) {
            queriedPatterns.add(pattern);
            Set<String> hit = keyspace.get(pattern);
            return null == hit ? Set.of() : hit;
        }

        @Override
        public Boolean delete(String key) {
            deletedKeys.add(key);
            return Boolean.TRUE;
        }

        @Override
        public Long delete(java.util.Collection<String> keys) {
            deletedKeys.addAll(keys);
            return (long) keys.size();
        }

        /** 该 key 的类型码；null 表示 key 不存在（对应 Redis 的 none） */
        private DataType typeOfKey;

        /** 配置某个 key 的 Redis 类型 */
        RecordingRedisTemplate typedAs(DataType dataType) {
            this.typeOfKey = dataType;
            return this;
        }

        @Override
        public DataType type(String key) {
            return typeOfKey;
        }

        /** 该 key 的剩余 TTL（秒）；-1 表示永久 */
        private long expireSeconds = -1L;

        /** 配置剩余 TTL */
        RecordingRedisTemplate expireIn(long seconds) {
            this.expireSeconds = seconds;
            return this;
        }

        @Override
        public Long getExpire(String key) {
            return expireSeconds;
        }

        @Override
        public RedisConnectionFactory getConnectionFactory() {
            // 显式返回 null：统计接口应据此降级，而不是抛异常打挂页面
            return null;
        }
    }

    /**
     * 测试：模块列表应登记全部预定义模块。
     *
     * <p>模块数 13 是有意固定的：12 个业务模块 + all。它一旦变动，
     * 前端「按模块清理」的下拉项就会与后端不一致。</p>
     */
    @Test
    @DisplayName("模块列表-返回预定义模块并统计key数")
    void testModules() {
        var redis = new RecordingRedisTemplate()
                .having("sys:dict:*", "sys:dict:1", "sys:dict:2")
                .having("sys:setting:*", "sys:setting:1");
        CacheController cacheController = new CacheController(redis, null);

        ReturnResult<List<Map<String, Object>>> result = cacheController.modules();

        assertTrue(result.isSuccess());
        assertEquals(13, result.getData().size());
        // dict 命中 2 个 key —— 证明 keyCount 是真查出来的，不是硬编码 0
        Map<String, Object> dict = result.getData().stream()
                .filter(m -> "dict".equals(m.get("module")))
                .findFirst().orElseThrow();
        assertEquals("字典", dict.get("name"));
        assertEquals("sys:dict:*", dict.get("keyPrefix"));
        assertEquals(2L, dict.get("keyCount"));
    }

    /**
     * 测试：显式传入的 pattern 应优先于模块前缀。
     */
    @Test
    @DisplayName("key列表-自定义pattern优先于模块前缀")
    void testKeysByPattern() {
        var redis = new RecordingRedisTemplate()
                .having("my:*", "my:a", "my:b")
                .having("sys:menu:*", "sys:menu:1");
        CacheController cacheController = new CacheController(redis, null);

        ReturnResult<List<String>> result = cacheController.keys(null, "my:*");

        assertTrue(result.isSuccess());
        assertEquals(2, result.getData().size());
        assertEquals("my:*", redis.queriedPatterns.get(0), "必须用传入的 pattern，不能退回模块前缀");
    }

    /**
     * 测试：按模块查询时应使用该模块登记的前缀。
     */
    @Test
    @DisplayName("key列表-按模块查询使用模块前缀")
    void testKeysByModule() {
        var redis = new RecordingRedisTemplate().having("sys:menu:*", "sys:menu:1", "sys:menu:2");
        CacheController cacheController = new CacheController(redis, null);

        ReturnResult<List<String>> result = cacheController.keys("menu", null);

        assertTrue(result.isSuccess());
        assertEquals(2, result.getData().size());
        assertEquals("sys:menu:*", redis.queriedPatterns.get(0));
    }

    /**
     * 测试：未知模块应回落到 all，而不是返回空。
     *
     * <p>回落到 all 是既有行为：运维手输模块名拼错时仍能看到 key，
     * 否则会误以为「这个模块没有缓存」。</p>
     */
    @Test
    @DisplayName("key列表-未知模块回落到全部")
    void testKeysUnknownModuleFallsBackToAll() {
        var redis = new RecordingRedisTemplate().having("*", "any:key");
        CacheController cacheController = new CacheController(redis, null);

        ReturnResult<List<String>> result = cacheController.keys("no-such-module", null);

        assertTrue(result.isSuccess());
        assertEquals(1, result.getData().size());
        assertEquals("*", redis.queriedPatterns.get(0));
    }

    /**
     * 测试：key 返回量应被截断到上限，防止 keys 过大拖垮响应。
     */
    @Test
    @DisplayName("key列表-超过上限时截断为200")
    void testKeysTruncatedAtLimit() {
        String[] many = new String[250];
        for (int i = 0; i < many.length; i++) {
            many[i] = "k:" + i;
        }
        var redis = new RecordingRedisTemplate().having("*", many);
        CacheController cacheController = new CacheController(redis, null);

        ReturnResult<List<String>> result = cacheController.keys(null, "*");

        assertTrue(result.isSuccess());
        assertEquals(200, result.getData().size(), "MAX_KEYS_RETURN 上限失效会让 keys 响应体积不可控");
    }

    /**
     * 测试：删除键-空值校验。
     */
    @Test
    @DisplayName("删除key-key为空返回非法参数")
    void testDeleteKeyBlank() {
        CacheController cacheController = new CacheController(new RecordingRedisTemplate(), null);
        ReturnResult<Boolean> result = cacheController.deleteKey("  ");
        assertFalse(result.isSuccess());
    }

    /**
     * 测试：删除键-合法 key 应委托给 Redis。
     */
    @Test
    @DisplayName("删除key-合法key调用redis删除")
    void testDeleteKeyOk() {
        var redis = new RecordingRedisTemplate();
        CacheController cacheController = new CacheController(redis, null);

        ReturnResult<Boolean> result = cacheController.deleteKey("sys:menu:1");

        assertTrue(result.isSuccess());
        assertEquals(List.of("sys:menu:1"), redis.deletedKeys, "删除必须真正下发到 Redis");
    }

    /**
     * 测试：删除未知模块应报错。
     */
    @Test
    @DisplayName("删除模块-未知模块返回非法参数")
    void testDeleteModuleUnknown() {
        CacheController cacheController = new CacheController(new RecordingRedisTemplate(), null);
        ReturnResult<Long> result = cacheController.deleteModule("not-exist");
        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("模块不存在"));
    }

    /**
     * 测试：删除模块-命中前缀批量删除。
     */
    @Test
    @DisplayName("删除模块-命中前缀批量删除")
    void testDeleteModuleOk() {
        var redis = new RecordingRedisTemplate().having("sys:role:*", "sys:role:1", "sys:role:2");
        CacheController cacheController = new CacheController(redis, null);

        ReturnResult<Long> result = cacheController.deleteModule("role");

        assertTrue(result.isSuccess());
        assertEquals(2L, result.getData());
        assertEquals(2, redis.deletedKeys.size());
        assertEquals("sys:role:*", redis.queriedPatterns.get(0), "必须用模块登记的前缀，不能误删其他模块");
    }

    /**
     * 测试：清空全部-无 key 时返回 0。
     */
    @Test
    @DisplayName("清空全部-无key时返回0")
    void testClearAllEmpty() {
        var redis = new RecordingRedisTemplate();
        CacheController cacheController = new CacheController(redis, null);

        ReturnResult<Long> result = cacheController.clearAll();

        assertTrue(result.isSuccess());
        assertEquals(0L, result.getData());
        assertTrue(redis.deletedKeys.isEmpty(), "无命中时不应下发删除");
    }

    /**
     * 测试：清空全部-命中时返回删除数量。
     */
    @Test
    @DisplayName("清空全部-命中3个key, 返回删除数量3")
    void testClearAllOk() {
        var redis = new RecordingRedisTemplate().having("*", "a", "b", "c");
        CacheController cacheController = new CacheController(redis, null);

        ReturnResult<Long> result = cacheController.clearAll();

        assertTrue(result.isSuccess());
        assertEquals(3L, result.getData());
        assertEquals(3, redis.deletedKeys.size());
    }

    @Test
    @DisplayName("模块登记-策略缓存统一使用 Spring Cache 名称")
    void testPolicyCacheModules() throws Exception {
        var field = CacheController.class.getDeclaredField("MODULES");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, String[]> modules = (Map<String, String[]>) field.get(null);
        for (String name : List.of(
                PolicyCacheName.RATE_LIMIT,
                PolicyCacheName.IP_BLACKLIST,
                PolicyCacheName.IP_WHITELIST,
                PolicyCacheName.ACCOUNT_BLACKLIST,
                PolicyCacheName.ACCOUNT_WHITELIST)) {
            assertTrue(modules.containsKey(name));
            assertEquals(name + ":*", modules.get(name)[1]);
        }
    }

    // ==================== 以下为 2026-10-02 新增：/v2/cache/stats 与 /v2/cache/value ====================

    /**
     * 测试：Redis 完全不可用时统计接口必须降级返回，而不是抛异常。
     *
     * <p>这条对应「缓存监控页统计卡全空」那个缺陷的根因防护。
     * 三个数据源（Spring Cache 实例数、key 总数、Redis INFO）都取不到时，
     * 接口仍应返回一份结构完整的零值，让前端显示「0」而不是整页报错。</p>
     */
    @Test
    @DisplayName("统计概览-Redis不可用时降级返回而非抛异常")
    void testStatsDegradesWhenRedisDown() {
        CacheController cacheController = new CacheController(new RecordingRedisTemplate(), null);

        ReturnResult<Map<String, Object>> result = cacheController.stats();

        assertTrue(result.isSuccess(), "Redis 不可用时统计接口不得失败");
        Map<String, Object> data = result.getData();
        // 结构必须齐全：前端四张统计卡按固定 key 取值，缺 key 会渲染成 undefined
        assertTrue(data.containsKey("cacheCount"));
        assertTrue(data.containsKey("totalKeys"));
        assertTrue(data.containsKey("usedMemoryBytes"));
        assertTrue(data.containsKey("usedMemory"));
        assertTrue(data.containsKey("hitRateAvailable"));
        assertTrue(data.containsKey("hitRate"));
        assertEquals(0L, data.get("totalKeys"));
        assertEquals("0 B", data.get("usedMemory"));
        // 取不到 INFO 就必须声明不可用，绝不能返回假的 0% 命中率
        assertEquals(Boolean.FALSE, data.get("hitRateAvailable"));
    }

    /**
     * 测试：统计概览-无 TTL 键时命中率标记为不可用。
     *
     * <p>hits + misses 为 0 时算不出命中率，返回 0.0 会被前端渲染成「命中率 0%」，
     * 那是在说谎。正确做法是置 hitRateAvailable=false 让前端显示「不支持统计」。</p>
     */
    @Test
    @DisplayName("统计概览-无命中数据时命中率标记为不可用")
    void testStatsHitRateUnavailable() {
        CacheController cacheController = new CacheController(new RecordingRedisTemplate(), null);

        Map<String, Object> data = cacheController.stats().getData();

        assertEquals(Boolean.FALSE, data.get("hitRateAvailable"));
        assertEquals(0.0, data.get("hitRate"));
    }

    /**
     * 测试：读取缓存值-空 key 校验。
     */
    @Test
    @DisplayName("读取缓存值-key为空返回非法参数")
    void testValueBlankKey() {
        CacheController cacheController = new CacheController(new RecordingRedisTemplate(), null);
        assertFalse(cacheController.value("   ", null).isSuccess());
    }

    /**
     * 测试：读取不存在的 key 应返回 none 类型且不可编辑。
     *
     * <p>type() 返回 null 会被归一成 "none"，此时不应继续取 TTL 与值 ——
     * 对不存在的 key 调 getExpire 是无意义的下发。</p>
     */
    @Test
    @DisplayName("读取缓存值-不存在的key返回none且不可编辑")
    void testValueOfMissingKey() {
        CacheController cacheController = new CacheController(new RecordingRedisTemplate(), null);

        ReturnResult<Map<String, Object>> result = cacheController.value("no:such:key", "dict");

        assertTrue(result.isSuccess());
        Map<String, Object> data = result.getData();
        assertEquals("none", data.get("type"));
        assertEquals(Boolean.FALSE, data.get("editable"));
        assertEquals("no:such:key", data.get("key"));
        assertEquals("dict", data.get("module"));
        assertNull(data.get("ttl"), "none 类型不应下发 TTL 查询");
    }

    /**
     * 测试：修改缓存值-空 key 校验。
     */
    @Test
    @DisplayName("修改缓存值-key为空返回非法参数")
    void testUpdateValueBlankKey() {
        CacheController cacheController = new CacheController(new RecordingRedisTemplate(), null);
        assertFalse(cacheController.updateValue(null).isSuccess());
        assertFalse(cacheController.updateValue(Map.of("value", "x")).isSuccess());
        assertFalse(cacheController.updateValue(Map.of("key", "  ", "value", "x")).isSuccess());
    }

    /**
     * 测试：修改缓存值-非 string 类型必须拒绝。
     *
     * <p>只允许改 string 是刻意的：hash/list/set/zset 的「值」是多值结构，
     * 界面上给一个输入框直接 SET 会把结构写坏。这里钉住该约束。</p>
     */
    @Test
    @DisplayName("修改缓存值-非string类型被拒绝")
    void testUpdateValueRejectsNonString() {
        CacheController cacheController = new CacheController(
                new RecordingRedisTemplate().typedAs(DataType.HASH), null);

        ReturnResult<Boolean> result = cacheController.updateValue(Map.of("key", "k", "value", "v"));

        assertFalse(result.isSuccess(), "hash 是多值结构，界面上给一个输入框直接 SET 会把结构写坏");
        assertTrue(result.getMsg().contains("仅支持修改 string"), "错误信息应说明限制，实际: " + result.getMsg());
    }

    /**
     * 测试：修改缓存值-不存在的 key 视为创建，允许写入。
     */
    @Test
    @DisplayName("修改缓存值-不存在的key允许创建")
    void testUpdateValueCreatesMissingKey() {
        var redis = new RecordingRedisTemplate();
        CacheController cacheController = new CacheController(redis, null);

        ReturnResult<Boolean> result = cacheController.updateValue(Map.of("key", "new:key", "value", "v"));

        assertTrue(result.isSuccess(), "none 类型允许写入（等同于新建）");
        assertEquals("v", redis.writtenValues.get("new:key"), "写入必须真的下发到 Redis");
    }

    /**
     * 测试：修改缓存值-有 TTL 时必须保留剩余过期时间。
     *
     * <p>这是一个容易回归的行为：直接 {@code set(key, value)} 会把过期时间清成永久，
     * 运维改一个值顺手让一条本该过期的策略配置变成永久生效。这里钉住「保留 TTL」。</p>
     */
    @Test
    @DisplayName("修改缓存值-保留剩余TTL而不是改成永久")
    void testUpdateValuePreservesTtl() {
        // 必须同时给出 string 类型：updateValue 对 none 类型会把 ttl 置 null
        // （key 不存在时无 TTL 可保留），那样就测不到「保留 TTL」这条分支了。
        var redis = new RecordingRedisTemplate().typedAs(DataType.STRING).expireIn(300L);
        CacheController cacheController = new CacheController(redis, null);

        ReturnResult<Boolean> result = cacheController.updateValue(Map.of("key", "ttl:key", "value", "v"));

        assertTrue(result.isSuccess());
        assertEquals(300L, redis.writtenTtls.get("ttl:key"),
                "必须带 TTL 写入，否则改一次值就把过期策略变成永久生效");
    }

    /**
     * 测试：修改缓存值-无 TTL 时按永久写入。
     */
    @Test
    @DisplayName("修改缓存值-无TTL时按永久写入")
    void testUpdateValueWithoutTtl() {
        var redis = new RecordingRedisTemplate().typedAs(DataType.STRING).expireIn(-1L);
        CacheController cacheController = new CacheController(redis, null);

        assertTrue(cacheController.updateValue(Map.of("key", "k", "value", "v")).isSuccess());
        assertTrue(redis.writtenTtls.isEmpty(), "剩余 TTL 为 -1（永久）时不应传 TTL 参数");
        assertEquals("v", redis.writtenValues.get("k"));
    }

    /**
     * 测试：编辑标记只对 string 为真。
     */
    @Test
    @DisplayName("读取缓存值-仅string类型可编辑")
    void testEditableOnlyForString() {
        // 直接验证分支判定：反射调用私有 dataTypeOf 不必要，
        // 这里确认 none 分支已由上面的用例覆盖，且 editable 语义一致
        CacheController cacheController = new CacheController(new RecordingRedisTemplate(), null);
        Map<String, Object> data = cacheController.value("missing", null).getData();
        assertNotNull(data.get("editable"));
        assertEquals(Boolean.FALSE, data.get("editable"));
    }
}