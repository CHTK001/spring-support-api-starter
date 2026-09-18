package com.chua.starter.system.service.impl;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.system.model.cache.SysCacheInfo;
import com.chua.starter.system.service.SysCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
* 缓存管理服务实现
*
* <p>基于 StringRedisTemplate 按 Key 前缀清除 Redis 中各类业务缓存。</p>
*
* @since 4.0.0.50
* @author CH
 */
@Slf4j
@Service
public class SysCacheServiceImpl implements SysCacheService {

    /**
    * Redis操作模板(未接入Redis时为空, 不影响其他主流程)
     */
    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;

    /**
    * 缓存定义: cacheName -> (cacheKey描述, Redis Key匹配模式)
     */
    private static final Map<String, SysCacheInfo> CACHE_MAP = new LinkedHashMap<>();

    static {
        CACHE_MAP.put("role", new SysCacheInfo("role", "cache:role", "角色缓存"));
        CACHE_MAP.put("dict", new SysCacheInfo("dict", "sys:dict:", "字典缓存"));
        CACHE_MAP.put("dept", new SysCacheInfo("dept", "sys:dept:list:", "部门缓存"));
        CACHE_MAP.put("setting", new SysCacheInfo("setting", "sys:setting:", "设置缓存"));
        CACHE_MAP.put("menu", new SysCacheInfo("menu", "sys:menu:list:all", "菜单缓存"));
        CACHE_MAP.put("user", new SysCacheInfo("user", "cache:user", "用户缓存"));
    }

    @Override
    /** 列表 */
    public List<SysCacheInfo> list() {
        return new ArrayList<>(CACHE_MAP.values());
    }

    @Override
    /** 刷新 */
    public ReturnResult<Boolean> refresh(String cacheName) {
        if (StringUtils.isBlank(cacheName)) {
            return ReturnResult.illegal("缓存名称不能为空");
        }
        SysCacheInfo info = CACHE_MAP.get(cacheName);
        if (null == info) {
            return ReturnResult.illegal("不支持的缓存名称: " + cacheName);
        }
        int cleared = deleteByPattern(toPattern(info.getCacheKey()));
        log.info("刷新缓存: cacheName={}, 删除Key数={}", cacheName, cleared);
        return ReturnResult.ok(true);
    }

    @Override
    /** 刷新全部 */
    public ReturnResult<Boolean> refreshAll() {
        for (SysCacheInfo info : CACHE_MAP.values()) {
            int cleared = deleteByPattern(toPattern(info.getCacheKey()));
            log.info("刷新全部缓存: cacheName={}, 删除Key数={}", info.getCacheName(), cleared);
        }
        return ReturnResult.ok(true);
    }

    /**
    * 将缓存Key描述转换为Redis匹配模式(前后加通配符)
    * @param cacheKey 缓存键，不允许为 null
    * @return 结果字符串
     */
    private String toPattern(String cacheKey) {
        return "*" + cacheKey + "*";
    }

    /**
    * 按模式删除Redis Key
     *
    * @param pattern Key匹配模式
    * @return 删除的Key数量
     */
    private int deleteByPattern(String pattern) {
        if (null == stringRedisTemplate) {
            log.warn("未接入Redis, 跳过缓存清理: pattern={}", pattern);
            return 0;
        }
        try {
            Set<String> keys = stringRedisTemplate.keys(pattern);
            if (null == keys || keys.isEmpty()) {
                return 0;
            }
            stringRedisTemplate.delete(keys);
            return keys.size();
        } catch (Exception e) {
            log.error("清理缓存失败: pattern={}", pattern, e);
            return 0;
        }
    }
}
