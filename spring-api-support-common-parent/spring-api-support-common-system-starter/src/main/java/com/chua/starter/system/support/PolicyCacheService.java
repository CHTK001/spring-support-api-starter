package com.chua.starter.system.support;

import com.chua.starter.common.support.constant.CacheConstant;
import com.chua.starter.system.constant.PolicyCacheName;
import com.chua.starter.system.util.IpCidrMatcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 策略运行时缓存服务。
 *
 * <p>所有策略运行时投影（限流规则、IP/账号黑白名单）统一通过 Spring Cache 读写，
 * 业务代码不再直接依赖 RedisTemplate 或硬编码 key 前缀。默认使用
 * {@code REDIS_CACHE_ALWAYS} CacheManager；当运行环境未启用缓存时回退到进程内
 * ConcurrentMapCache，保证过滤器在测试/降级场景下仍可运行。</p>
 *
 * @since 4.0.0.43
 * @author CH
 */
@Slf4j
@Component
public class PolicyCacheService {

    private final CacheManager cacheManager;

    /**
     * CacheManager 未登记名称时的回退缓存，按名称长期复用。
     *
     * <p>必须是字段而不是每次 new，否则写入即丢失。</p>
     */
    private final Map<String, Cache> fallbackCaches = new ConcurrentHashMap<>();

    public PolicyCacheService(@Qualifier(CacheConstant.REDIS_CACHE_ALWAYS) CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /* ==================== 限流规则 ==================== */

    /**
     * 读取规则 QPS。
     *
     * @param dimension 维度
     * @param target    目标
     * @return QPS 文本，不存在返回 null
     */
    public String getRateLimit(String dimension, String target) {
        return read(PolicyCacheName.RATE_LIMIT, rateKey(dimension, target));
    }

    /**
     * 写入规则 QPS。
     *
     * @param dimension 维度
     * @param target    目标
     * @param qps       QPS
     */
    public void putRateLimit(String dimension, String target, Integer qps) {
        write(PolicyCacheName.RATE_LIMIT, rateKey(dimension, target), String.valueOf(qps));
    }

    /**
     * 清理规则缓存。
     *
     * @param dimension 维度
     * @param target    目标
     */
    public void evictRateLimit(String dimension, String target) {
        evict(PolicyCacheName.RATE_LIMIT, rateKey(dimension, target));
    }

    /* ==================== IP 名单 ==================== */

    /**
     * 判断 IP 是否命中指定类型名单（精确 IP + CIDR）。
     *
     * @param type     1 黑名单 0 白名单
     * @param clientIp 客户端 IP
     * @return 是否命中
     */
    public boolean matchesIp(int type, String clientIp) {
        return null != matchIp(type, clientIp);
    }

    /**
     * 找出实际命中的 IP 名单条目。
     *
     * <p>{@link #matchesIp} 只回答「有没有命中」，但命中统计要归因到<b>具体哪一行</b>
     * （10.0.0.0/8 命中和 1.2.3.4 命中是两条不同的名单），所以需要把命中的条目返回出来。
     * 匹配顺序与 {@code matchesIp} 完全一致（精确 IP 优先，再按 CIDR 顺序），
     * 后者已改为委托本方法，两者不会分叉。</p>
     *
     * @param type     1 黑名单 0 白名单
     * @param clientIp 客户端 IP
     * @return 命中的条目原文（精确 IP 或 CIDR）；未命中返回 null
     */
    public String matchIp(int type, String clientIp) {
        String cacheName = ipCache(type);
        try {
            if (null != read(cacheName, clientIp)) {
                return clientIp;
            }
            for (String pattern : patternIndex(cacheName)) {
                if (IpCidrMatcher.matchesEntry(clientIp, pattern)) {
                    return pattern;
                }
            }
            return null;
        } catch (Exception e) {
            log.warn("[PolicyCache] 匹配 IP 策略失败: type={}, ip={}", type, clientIp, e);
            return null;
        }
    }

    /**
     * 投影 IP 策略。
     *
     * @param type    1 黑名单 0 白名单
     * @param pattern 精确 IP 或 CIDR
     */
    public void projectIp(int type, String pattern) {
        String cacheName = ipCache(type);
        String canonical = IpCidrMatcher.canonicalize(pattern);
        Set<String> patterns = new LinkedHashSet<>(patternIndex(cacheName));
        patterns.add(canonical);
        write(cacheName, PolicyCacheName.IP_PATTERN_INDEX_KEY, String.join(",", patterns));
        if (canonical.indexOf('/') < 0) {
            write(cacheName, canonical, PolicyCacheName.ENABLED_FLAG);
        }
    }

    /**
     * 清理 IP 策略缓存。
     *
     * @param type    1 黑名单 0 白名单
     * @param pattern 精确 IP 或 CIDR
     */
    public void clearIp(int type, String pattern) {
        String cacheName = ipCache(type);
        try {
            String canonical = IpCidrMatcher.canonicalize(pattern);
            Set<String> patterns = new LinkedHashSet<>(patternIndex(cacheName));
            patterns.remove(canonical);
            if (patterns.isEmpty()) {
                evict(cacheName, PolicyCacheName.IP_PATTERN_INDEX_KEY);
            } else {
                write(cacheName, PolicyCacheName.IP_PATTERN_INDEX_KEY, String.join(",", patterns));
            }
            if (canonical.indexOf('/') < 0) {
                evict(cacheName, canonical);
            }
        } catch (Exception e) {
            log.warn("[PolicyCache] 清理 IP 策略缓存失败: type={}, pattern={}", type, pattern, e);
        }
    }

    /**
     * 是否存在任何 IP 白名单策略。
     *
     * @return 是否存在
     */
    public boolean hasIpWhitelist() {
        return !patternIndex(PolicyCacheName.IP_WHITELIST).isEmpty();
    }

    /**
     * 清空 IP 名单缓存。
     */
    public void clearAllIp() {
        clear(PolicyCacheName.IP_BLACKLIST);
        clear(PolicyCacheName.IP_WHITELIST);
    }

    /* ==================== 账号名单 ==================== */

    /**
     * 清空账号名单缓存。
     */
    public void clearAllAccount() {
        clear(PolicyCacheName.ACCOUNT_BLACKLIST);
        clear(PolicyCacheName.ACCOUNT_WHITELIST);
    }

    /**
     * 判断账号是否命中指定类型名单。
     *
     * @param type    1 黑名单 2 白名单
     * @param account 账号
     * @return 是否命中
     */
    public boolean matchesAccount(int type, String account) {
        return null != matchAccount(type, account);
    }

    /**
     * 找出实际命中的账号名单条目。
     *
     * <p>账号名单是精确匹配（无 CIDR 这类通配），所以命中条目就是账号本身；
     * 返回账号而不是 boolean 是为了让调用方与 {@link #matchIp} 用同一种写法，
     * 命中统计的归因逻辑不必区分两种名单。</p>
     *
     * @param type    1 黑名单 2 白名单
     * @param account 账号
     * @return 命中的账号；未命中返回 null
     */
    public String matchAccount(int type, String account) {
        if (null == account || account.isBlank()) {
            return null;
        }
        try {
            return null != read(accountCache(type), account) ? account : null;
        } catch (Exception e) {
            log.warn("[PolicyCache] 匹配账号策略失败: type={}, account={}", type, account, e);
            return null;
        }
    }

    /**
     * 投影账号策略。
     *
     * @param type    1 黑名单 2 白名单
     * @param account 账号
     */
    public void putAccount(int type, String account) {
        write(accountCache(type), account, PolicyCacheName.ENABLED_FLAG);
    }

    /**
     * 清理账号策略缓存。
     *
     * @param type    1 黑名单 2 白名单
     * @param account 账号
     */
    public void evictAccount(int type, String account) {
        evict(accountCache(type), account);
    }

    /**
     * 清空全部策略缓存。
     */
    public void clearAll() {
        clear(PolicyCacheName.RATE_LIMIT);
        clearAllIp();
        clear(PolicyCacheName.ACCOUNT_BLACKLIST);
        clear(PolicyCacheName.ACCOUNT_WHITELIST);
        clear(PolicyCacheName.API_ROUTE);
    }

    /* ==================== 接口停用 / 转发 ==================== */

    /**
     * 读取指定路径的接口路由规则。
     *
     * @param path 去掉 context-path 的请求路径
     * @return 规则 JSON；无规则返回 null
     */
    public String getApiRoute(String path) {
        if (null == path || path.isEmpty()) {
            return null;
        }
        return read(PolicyCacheName.API_ROUTE, path);
    }

    /**
     * 投影接口路由规则。
     *
     * @param path    去掉 context-path 的请求路径
     * @param ruleJson 规则 JSON
     */
    public void putApiRoute(String path, String ruleJson) {
        if (null == path || path.isEmpty()) {
            return;
        }
        write(PolicyCacheName.API_ROUTE, path, ruleJson);
    }

    /**
     * 清理指定路径的接口路由规则。
     *
     * @param path 去掉 context-path 的请求路径
     */
    public void evictApiRoute(String path) {
        if (null == path || path.isEmpty()) {
            return;
        }
        evict(PolicyCacheName.API_ROUTE, path);
    }

    /**
     * 清空接口路由规则缓存。
     */
    public void clearAllApiRoute() {
        clear(PolicyCacheName.API_ROUTE);
    }

    /* ==================== 消息头拦截 ==================== */

    /**
     * 读取某消息头的拦截规则 JSON。
     *
     * @param headerName 消息头名（内部统一小写）
     * @return 规则 JSON；无规则时返回 null
     */
    public String getHeaderRule(String headerName) {
        if (null == headerName || headerName.isEmpty()) {
            return null;
        }
        return read(PolicyCacheName.HEADER, headerName.toLowerCase(Locale.ROOT));
    }

    /**
     * 投影某消息头的拦截规则。
     *
     * @param headerName 消息头名
     * @param ruleJson   规则 JSON
     */
    public void projectHeaderRule(String headerName, String ruleJson) {
        if (null == headerName || headerName.isEmpty() || null == ruleJson) {
            return;
        }
        write(PolicyCacheName.HEADER, headerName.toLowerCase(Locale.ROOT), ruleJson);
        Set<String> names = new LinkedHashSet<>(headerNameIndex());
        names.add(headerName.toLowerCase(Locale.ROOT));
        write(PolicyCacheName.HEADER, PolicyCacheName.HEADER_NAME_INDEX_KEY, String.join(",", names));
    }

    /**
     * 清理某消息头的拦截规则，并同步索引。
     *
     * @param headerName 消息头名
     */
    public void evictHeaderRule(String headerName) {
        if (null == headerName || headerName.isEmpty()) {
            return;
        }
        String lower = headerName.toLowerCase(Locale.ROOT);
        evict(PolicyCacheName.HEADER, lower);
        Set<String> names = new LinkedHashSet<>(headerNameIndex());
        if (names.remove(lower)) {
            write(PolicyCacheName.HEADER, PolicyCacheName.HEADER_NAME_INDEX_KEY, String.join(",", names));
        }
    }

    /**
     * 已启用规则覆盖的消息头名。
     *
     * @return 小写消息头名集合
     */
    public Set<String> headerNameIndex() {
        String raw = read(PolicyCacheName.HEADER, PolicyCacheName.HEADER_NAME_INDEX_KEY);
        if (null == raw || raw.isBlank()) {
            return Set.of();
        }
        return new LinkedHashSet<>(Arrays.asList(raw.split(",")));
    }

    /**
     * 清空全部消息头拦截规则。
     */
    public void clearAllHeader() {
        clear(PolicyCacheName.HEADER);
    }

    /* ==================== 内部工具 ==================== */

    private String read(String name, Object key) {
        Cache cache = cache(name);
        if (null == cache) {
            return null;
        }
        Cache.ValueWrapper wrapper = cache.get(key);
        return null == wrapper ? null : String.valueOf(wrapper.get());
    }

    private void write(String name, Object key, Object value) {
        Cache cache = cache(name);
        if (null != cache) {
            cache.put(key, value);
        }
    }

    private void evict(String name, Object key) {
        Cache cache = cache(name);
        if (null != cache) {
            cache.evict(key);
        }
    }

    private void clear(String name) {
        Cache cache = cache(name);
        if (null != cache) {
            cache.clear();
        }
    }

    /**
     * 取指定名称的缓存。
     *
     * <p>CacheManager 未登记该名称时（动态缓存管理器未开启、或名称未预热），
     * 回退到进程内 {@link ConcurrentMapCache}。<b>回退实例必须按名称复用</b>：
     * 每次调用都 {@code new} 一个的话，写进去的条目会在方法返回后即被丢弃，
     * 表现为「保存成功但策略不生效、hasIpWhitelist 恒为 false」这类难查的问题。</p>
     *
     * @param name 缓存名
     * @return 缓存实例
     */
    private Cache cache(String name) {
        Cache cache = cacheManager.getCache(name);
        if (null != cache) {
            return cache;
        }
        return fallbackCaches.computeIfAbsent(name, ConcurrentMapCache::new);    }

    private Set<String> patternIndex(String cacheName) {
        String raw = read(cacheName, PolicyCacheName.IP_PATTERN_INDEX_KEY);
        if (null == raw || raw.isBlank()) {
            return new LinkedHashSet<>();
        }
        return new LinkedHashSet<>(Arrays.asList(raw.split(",")));
    }

    private String rateKey(String dimension, String target) {
        return dimension + ":" + target;
    }

    private String ipCache(int type) {
        return type == 1 ? PolicyCacheName.IP_BLACKLIST : PolicyCacheName.IP_WHITELIST;
    }

    private String accountCache(int type) {
        return type == 1 ? PolicyCacheName.ACCOUNT_BLACKLIST : PolicyCacheName.ACCOUNT_WHITELIST;
    }
}
