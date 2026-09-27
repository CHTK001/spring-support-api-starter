package com.chua.starter.system.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.system.constant.PolicyCacheName;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
