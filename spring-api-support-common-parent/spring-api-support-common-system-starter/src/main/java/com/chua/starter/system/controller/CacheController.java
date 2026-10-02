package com.chua.starter.system.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.common.support.annotations.SysLog;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.system.constant.PolicyCacheName;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 缓存管理接口
 *
 * <p>提供缓存模块查看、key 查询、单 key/模块前缀/全量缓存清理能力,
 * 供运维管理后台使用。</p>
 *
 * @since 4.0.0.43
 * @author CH
 */
@Tag(name = "缓存管理接口")
@RestController
@RequestMapping("/v2/cache")
@RequiredArgsConstructor
@Slf4j
public class CacheController {

    /**
     * 单次返回 key 的最大数量, 防止 keys 返回过大
    */
    private static final int MAX_KEYS_RETURN = 200;

    /**
     * stringRedisTemplate
    */
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * Spring Cache 管理器（可选注入：未启用缓存时为 empty，统计退化为模块数）
     */
    private final ObjectProvider<CacheManager> cacheManagerProvider;

    /**
     * 多值类型（hash/list/set/zset）的摘要前缀
     */
    private static final String LENGTH_PREFIX = "length: ";

    /**
     * 1 KB
     */
    private static final long KB = 1024L;

    /**
     * 预定义缓存模块: key -> [模块名, key前缀, 描述]
     */
    private static final Map<String, String[]> MODULES = new LinkedHashMap<>();

    static {
        MODULES.put("dict", new String[]{"字典", "sys:dict:*", "数据字典缓存"});
        MODULES.put("setting", new String[]{"设置", "sys:setting:*", "系统设置缓存"});
        MODULES.put("menu", new String[]{"菜单", "sys:menu:*", "菜单权限缓存"});
        MODULES.put("role", new String[]{"角色", "sys:role:*", "角色权限缓存"});
        MODULES.put("dept", new String[]{"部门", "sys:dept:*", "部门树缓存"});
        MODULES.put("user", new String[]{"用户", "sys:user:*", "用户信息缓存"});
        MODULES.put("online", new String[]{"在线用户", "sys:online:*", "在线用户状态缓存"});
        // 策略运行时缓存统一由 Spring Cache 管理，这里按缓存名登记便于运维查看
        MODULES.put(PolicyCacheName.RATE_LIMIT, new String[]{"限流规则", PolicyCacheName.RATE_LIMIT + ":*", "策略限流规则缓存"});
        MODULES.put(PolicyCacheName.IP_BLACKLIST, new String[]{"IP黑名单", PolicyCacheName.IP_BLACKLIST + ":*", "策略IP黑名单缓存"});
        MODULES.put(PolicyCacheName.IP_WHITELIST, new String[]{"IP白名单", PolicyCacheName.IP_WHITELIST + ":*", "策略IP白名单缓存"});
        MODULES.put(PolicyCacheName.ACCOUNT_BLACKLIST, new String[]{"账号黑名单", PolicyCacheName.ACCOUNT_BLACKLIST + ":*", "策略账号黑名单缓存"});
        MODULES.put(PolicyCacheName.ACCOUNT_WHITELIST, new String[]{"账号白名单", PolicyCacheName.ACCOUNT_WHITELIST + ":*", "策略账号白名单缓存"});
        MODULES.put("all", new String[]{"全部", "*", "全部缓存"});
    }

    /**
     * 缓存模块列表
     *
     * @return 模块列表, 含 key 数量
     */
    @Operation(summary = "缓存模块列表")
    @GetMapping("/modules")
    @Permission(role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<List<Map<String, Object>>> modules() {
        List<Map<String, Object>> result = new ArrayList<>(MODULES.size());
        for (Map.Entry<String, String[]> entry : MODULES.entrySet()) {
            String[] info = entry.getValue();
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("module", entry.getKey());
            item.put("name", info[0]);
            item.put("keyPrefix", info[1]);
            item.put("description", info[2]);
            item.put("keyCount", countKeys(info[1]));
            result.add(item);
        }
        return ReturnResult.ok(result);
    }

    /**
     * 按模块返回实际 key 列表(最多 200 个)
     *
     * @param module  模块标识
     * @param pattern 自定义 pattern(非空时优先于模块前缀)
     * @return key 列表
     */
    @Operation(summary = "缓存key列表")
    @GetMapping("/keys")
    @Permission(role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<List<String>> keys(@RequestParam(required = false) String module,
                                            @RequestParam(required = false) String pattern) {
        String effectivePattern;
        if (null != pattern && !pattern.isBlank()) {
            effectivePattern = pattern;
        } else {
            String[] info = MODULES.getOrDefault(module, MODULES.get("all"));
            effectivePattern = info[1];
        }
        Set<String> keys = stringRedisTemplate.keys(effectivePattern);
        if (null == keys || keys.isEmpty()) {
            return ReturnResult.ok(new ArrayList<>());
        }
        List<String> list = new ArrayList<>(keys);
        if (list.size() > MAX_KEYS_RETURN) {
            list = list.subList(0, MAX_KEYS_RETURN);
        }
        return ReturnResult.ok(list);
    }

    /**
     * 删除指定 key
     *
     * @param key key
     * @return 删除结果
     */
    @Operation(summary = "删除指定缓存key")
    @DeleteMapping("/key")
    @Permission(value = "sys:cache:delete", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> deleteKey(@RequestParam String key) {
        if (null == key || key.isBlank()) {
            return ReturnResult.illegal("key不能为空");
        }
        stringRedisTemplate.delete(key);
        return ReturnResult.ok(true);
    }

    /**
     * 按模块前缀批量删除
     *
     * @param module 模块标识
     * @return 删除数量
     */
    @Operation(summary = "按模块批量删除缓存")
    @DeleteMapping("/module")
    @Permission(value = "sys:cache:delete", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Long> deleteModule(@RequestParam String module) {
        String[] info = MODULES.get(module);
        if (null == info) {
            return ReturnResult.illegal("缓存模块不存在");
        }
        Set<String> keys = stringRedisTemplate.keys(info[1]);
        if (null == keys || keys.isEmpty()) {
            return ReturnResult.ok(0L);
        }
        Long deleted = stringRedisTemplate.delete(keys);
        return ReturnResult.ok(deleted == null ? 0L : deleted);
    }

    /**
     * 清空所有缓存(需二次确认)
     *
     * @return 删除数量
     */
    @Operation(summary = "清空所有缓存")
    @DeleteMapping("/all")
    @Permission(value = "sys:cache:clear", role = {AuthConstant.SUPER_ADMIN})
    public ReturnResult<Long> clearAll() {
        Set<String> keys = stringRedisTemplate.keys("*");
        if (null == keys || keys.isEmpty()) {
            return ReturnResult.ok(0L);
        }
        Long deleted = stringRedisTemplate.delete(keys);
        log.warn("清空全部缓存, 删除 key 数量: {}", deleted);
        return ReturnResult.ok(deleted == null ? 0L : deleted);
    }

    /**
     * 缓存统计概览
     *
     * <p>供「缓存监控」页顶部的四张统计卡使用（缓存实例数 / 总键数 / 内存占用 / 命中率）。
     * 内存与命中率取自 Redis {@code INFO}，不是估算：{@code used_memory} 给字节数，
     * {@code keyspace_hits} 与 {@code keyspace_misses} 算命中率。
     * 老版本 Redis 或代理不返回这些字段时把 {@code hitRateAvailable} 置 false，
     * 前端据此显示「当前 Redis 不支持命中率统计」而不是显示一个假的 0%。</p>
     *
     * @return 统计概览
     */
    @Operation(summary = "缓存统计概览")
    @GetMapping("/stats")
    @Permission(role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Map<String, Object>> stats() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("cacheCount", countCaches());
        result.put("totalKeys", countKeys("*"));

        Properties info = redisInfo();
        long usedBytes = parseLong(info.getProperty("used_memory"), 0L);
        result.put("usedMemoryBytes", usedBytes);
        result.put("usedMemory", formatBytes(usedBytes));

        long hits = parseLong(info.getProperty("keyspace_hits"), 0L);
        long misses = parseLong(info.getProperty("keyspace_misses"), 0L);
        boolean available = (hits + misses) > 0;
        result.put("hitRateAvailable", available);
        result.put("hitRate", available ? Math.round(hits * 10000.0 / (hits + misses)) / 100.0 : 0.0);
        return ReturnResult.ok(result);
    }

    /**
     * 读取单个缓存值
     *
     * <p>按 Redis 实际类型读取并转成可读文本展示；同时给出 TTL 与序列化长度。
     * {@code editable} 只对 string 类型为 true —— 另外几种类型（hash/list/set/zset）
     * 的「写入」语义不明确，允许多值编辑容易把结构写坏，故只读展示。</p>
     *
     * @param key    key
     * @param module 模块标识（仅用于回显，读取按 key 精确匹配）
     * @return 缓存值详情
     */
    @Operation(summary = "读取单个缓存值")
    @GetMapping("/value")
    @Permission(role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Map<String, Object>> value(@RequestParam String key,
                                                   @RequestParam(required = false) String module) {
        if (null == key || key.isBlank()) {
            return ReturnResult.illegal("key不能为空");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("key", key);
        result.put("module", module);

        String type = dataTypeOf(key);
        if ("none".equals(type)) {
            result.put("type", "none");
            result.put("editable", false);
            return ReturnResult.ok(result);
        }
        result.put("type", type);
        result.put("editable", "string".equals(type));
        result.put("ttl", stringRedisTemplate.getExpire(key));

        String text;
        if ("string".equals(type)) {
            text = stringRedisTemplate.opsForValue().get(key);
        } else if ("hash".equals(type)) {
            Map<Object, Object> m = stringRedisTemplate.opsForHash().entries(key);
            text = LENGTH_PREFIX + m.size() + " entries";
        } else if ("list".equals(type)) {
            text = LENGTH_PREFIX + stringRedisTemplate.opsForList().size(key) + " items";
        } else if ("set".equals(type)) {
            text = LENGTH_PREFIX + stringRedisTemplate.opsForSet().size(key) + " members";
        } else if ("zset".equals(type)) {
            text = LENGTH_PREFIX + stringRedisTemplate.opsForZSet().size(key) + " members";
        } else {
            text = LENGTH_PREFIX + "unsupported type";
        }
        String value = null == text ? "" : text;
        result.put("value", value);
        result.put("plainValue", value);
        result.put("size", value.getBytes(StandardCharsets.UTF_8).length);
        result.put("rootType", "string".equals(type) ? "java.lang.String" : type);
        return ReturnResult.ok(result);
    }

    /**
     * 修改缓存值（仅 string 类型）
     *
     * <p>只允许改 string：直接 SET 会覆盖原值但保留 TTL 语义由 {@code setIfPresent} 之外的
     * 调用方决定，这里显式保留剩余 TTL，避免运维改一个值顺手把过期时间清成永久。</p>
     *
     * @param body 请求体，需含 module / key / value
     * @return 是否成功
     */
    @Operation(summary = "修改缓存值")
    @PutMapping("/value")
    @SysLog(module = "缓存管理")
    @Permission(value = "sys:cache:update", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> updateValue(@RequestBody Map<String, String> body) {
        String key = null == body ? null : body.get("key");
        if (null == key || key.isBlank()) {
            return ReturnResult.illegal("key不能为空");
        }
        String newValue = null == body.get("value") ? "" : body.get("value");
        String type = dataTypeOf(key);
        if (!"none".equals(type) && !"string".equals(type)) {
            return ReturnResult.illegal("仅支持修改 string 类型的缓存值，当前类型: " + type);
        }
        Long ttl = "none".equals(type) ? null : stringRedisTemplate.getExpire(key);
        if (null != ttl && ttl > 0) {
            stringRedisTemplate.opsForValue().set(key, newValue, ttl, TimeUnit.SECONDS);
        } else {
            stringRedisTemplate.opsForValue().set(key, newValue);
        }
        log.info("[Cache] 缓存值已修改: key={}, size={}", key, newValue.getBytes(StandardCharsets.UTF_8).length);
        return ReturnResult.ok(true);
    }

    /**
     * 取 key 的 Redis 类型码
     *
     * <p>{@code StringRedisTemplate.type()} 返回的是 {@link DataType} 枚举而不是字符串，
     * 用 {@code code()} 转成 redis 协议里的类型名（none/string/hash/list/set/zset），
     * 与前端约定的取值一致。</p>
     *
     * @param key key
     * @return 类型码
     */
    private String dataTypeOf(String key) {
        DataType dataType = stringRedisTemplate.type(key);
        return null == dataType ? "none" : dataType.code();
    }

    /**
     * 统计 Spring Cache 管理的缓存实例数
     *
     * <p>取不到 CacheManager（如未启用缓存）时退化为已登记的模块数，
     * 不抛异常 —— 统计接口不该因为环境差异把页面打挂。</p>
     *
     * @return 缓存实例数
     */
    private int countCaches() {
        try {
            CacheManager manager = cacheManagerProvider.getIfAvailable();
            if (null == manager) {
                return MODULES.size() - 1;
            }
            return manager.getCacheNames().size();
        } catch (Exception e) {
            log.warn("统计缓存实例数失败，退化为模块数", e);
            return MODULES.size() - 1;
        }
    }

    /**
     * 读取 Redis INFO
     *
     * @return INFO 键值对；读取失败时返回空对象而不是 null
     */
    private Properties redisInfo() {
        Properties properties = new Properties();
        try {
            RedisConnectionFactory factory = stringRedisTemplate.getConnectionFactory();
            if (null == factory) {
                return properties;
            }
            try (RedisConnection connection = factory.getConnection()) {
                Properties info = connection.serverCommands().info();
                if (null != info) {
                    properties.putAll(info);
                }
            }
        } catch (Exception e) {
            log.warn("读取 Redis INFO 失败", e);
        }
        return properties;
    }

    /**
     * 解析数字，失败回退默认值
     *
     * @param text         文本
     * @param defaultValue 默认值
     * @return 解析结果
     */
    private long parseLong(String text, long defaultValue) {
        if (null == text || text.isBlank()) {
            return defaultValue;
        }
        try {
            return Long.parseLong(text.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 字节数转可读字符串
     *
     * @param bytes 字节数
     * @return 形如 "1.2 MB"
     */
    private String formatBytes(long bytes) {
        if (bytes < KB) {
            return bytes + " B";
        }
        if (bytes < KB * KB) {
            return String.format("%.1f KB", bytes / (double) KB);
        }
        if (bytes < KB * KB * KB) {
            return String.format("%.1f MB", bytes / (double) (KB * KB));
        }
        return String.format("%.2f GB", bytes / (double) (KB * KB * KB));
    }

    /**
     * 统计 pattern 命中的 key 数量
     *
     * @param pattern pattern
     * @return key 数量
     */
    private Long countKeys(String pattern) {
        try {
            Set<String> keys = stringRedisTemplate.keys(pattern);
            return (long) (null == keys ? 0 : keys.size());
        } catch (Exception e) {
            log.warn("统计缓存key数量失败: pattern={}", pattern, e);
            return 0L;
        }
    }
}
