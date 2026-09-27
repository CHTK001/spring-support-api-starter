package com.chua.starter.system.support;

import com.chua.starter.system.constant.PolicyCacheName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 策略运行时缓存单元测试
 *
 * <p>重点覆盖「CacheManager 未登记该缓存名」时的回退分支：回退实例必须按名称复用，
 * 否则写入即丢弃，会表现为保存成功但策略不生效。</p>
 *
 * @since 4.0.0.50
 * @author CH
 */
@DisplayName("策略运行时缓存测试")
class PolicyCacheServiceTest {

    /**
     * 恒返回 null 的 CacheManager：模拟动态缓存管理器未登记策略缓存名的情形
     */
    private static final class EmptyCacheManager implements CacheManager {
        @Override
        public Cache getCache(String name) {
            return null;
        }

        @Override
        public java.util.Collection<String> getCacheNames() {
            return java.util.Collections.emptyList();
        }
    }

    @Test
    @DisplayName("回退分支-写入后能读回（曾因每次new缓存导致写丢）")
    void testFallbackCacheReuse() {
        PolicyCacheService service = new PolicyCacheService(new EmptyCacheManager());
        service.putRateLimit("interface", "/v2/x", 42);
        assertEquals("42", service.getRateLimit("interface", "/v2/x"));
    }

    @Test
    @DisplayName("回退分支-清理后读不到")
    void testFallbackCacheEvict() {
        PolicyCacheService service = new PolicyCacheService(new EmptyCacheManager());
        service.putRateLimit("interface", "/v2/y", 7);
        service.evictRateLimit("interface", "/v2/y");
        assertEquals(null, service.getRateLimit("interface", "/v2/y"));
    }

    @Test
    @DisplayName("回退分支-不同缓存名互不干扰")
    void testFallbackCacheIsolatedByName() {
        PolicyCacheService service = new PolicyCacheService(new EmptyCacheManager());
        service.projectIp(1, "10.0.0.1");
        assertTrue(service.hasIpWhitelist() == false, "只写黑名单时不应有白名单");
    }

    @Test
    @DisplayName("白名单写入后 hasIpWhitelist 应为 true（曾恒为 false）")
    void testHasIpWhitelistAfterWrite() {
        PolicyCacheService service = new PolicyCacheService(new EmptyCacheManager());
        assertFalse(service.hasIpWhitelist());
        service.projectIp(0, "192.0.2.5");
        assertTrue(service.hasIpWhitelist(), "写入白名单后必须能查到白名单存在");
        assertTrue(service.matchesIp(0, "192.0.2.5"), "白名单IP应命中");
    }

    @Test
    @DisplayName("黑名单与白名单分别命中")
    void testBlackAndWhiteSeparated() {
        PolicyCacheService service = new PolicyCacheService(new EmptyCacheManager());
        service.projectIp(1, "203.0.113.88");
        service.projectIp(0, "192.0.2.5");
        assertTrue(service.matchesIp(1, "203.0.113.88"));
        assertFalse(service.matchesIp(1, "192.0.2.5"), "白名单IP不应命中黑名单");
        assertTrue(service.matchesIp(0, "192.0.2.5"));
    }

    @Test
    @DisplayName("CIDR 黑名单命中网段内IP")
    void testCidrMatch() {
        PolicyCacheService service = new PolicyCacheService(new EmptyCacheManager());
        service.projectIp(1, "198.51.100.0/24");
        assertTrue(service.matchesIp(1, "198.51.100.77"));
        assertFalse(service.matchesIp(1, "8.8.8.8"));
    }

    @Test
    @DisplayName("清理黑名单后不再命中")
    void testClearIp() {
        PolicyCacheService service = new PolicyCacheService(new EmptyCacheManager());
        service.projectIp(1, "203.0.113.88");
        service.clearIp(1, "203.0.113.88");
        assertFalse(service.matchesIp(1, "203.0.113.88"));
    }

    @Test
    @DisplayName("已登记缓存名时走 CacheManager")
    void testUsesCacheManagerWhenPresent() {
        ConcurrentMapCacheManager manager = new ConcurrentMapCacheManager(
                PolicyCacheName.RATE_LIMIT, PolicyCacheName.IP_BLACKLIST, PolicyCacheName.IP_WHITELIST);
        PolicyCacheService service = new PolicyCacheService(manager);
        service.projectIp(1, "203.0.113.99");
        assertNotNull(manager.getCache(PolicyCacheName.IP_BLACKLIST));
        assertTrue(service.matchesIp(1, "203.0.113.99"));
    }

    @Test
    @DisplayName("账号名单写入与清理")
    void testAccountCache() {
        PolicyCacheService service = new PolicyCacheService(new EmptyCacheManager());
        service.putAccount(1, "banned01");
        assertTrue(service.matchesAccount(1, "banned01"));
        assertFalse(service.matchesAccount(1, "ops"));
        service.evictAccount(1, "banned01");
        assertFalse(service.matchesAccount(1, "banned01"));
    }
}
