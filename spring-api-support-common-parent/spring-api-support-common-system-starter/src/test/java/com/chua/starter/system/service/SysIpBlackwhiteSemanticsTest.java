package com.chua.starter.system.service;

import com.chua.starter.system.service.impl.SysIpBlackwhiteServiceImpl;
import com.chua.starter.system.support.IpPolicyCacheProjector;
import com.chua.starter.system.support.PolicyCacheService;
import com.chua.starter.system.support.PolicyConfigConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * IP 黑白名单准入语义测试
 *
 * <p>用真实的投影器 + 内存回退缓存跑通 checkIp 的判定分支，锁定三条规则：</p>
 * <ol>
 *   <li>命中黑名单 → 拒绝（白名单不能覆盖）</li>
 *   <li>命中白名单 → 放行</li>
 *   <li><b>存在白名单时，未列入任何名单的 IP 依然放行</b> —— 白名单是放行豁免，不是准入名单</li>
 * </ol>
 *
 * <p>第 3 条是回归点：早期实现写成 {@code !hasWhitelist()}，
 * 配一条白名单就会把其余所有 IP 全拒，与 {@code PolicyRateLimitFilter}
 * 的白名单语义和策略页文案都相矛盾。</p>
 *
 * @since 4.0.0.50
 * @author CH
 */
@DisplayName("IP名单准入语义测试")
class SysIpBlackwhiteSemanticsTest {

    private PolicyCacheService cache;

    private SysIpBlackwhiteServiceImpl service;

    /**
     * CacheManager 恒返回 null，强制走进程内回退缓存（回退实例按名称复用）
     */
    private static final class NoopCacheManager implements CacheManager {
        @Override
        public Cache getCache(String name) {
            return null;
        }

        @Override
        public Collection<String> getCacheNames() {
            return Collections.emptyList();
        }
    }

    @BeforeEach
    void setUp() {
        cache = new PolicyCacheService(new NoopCacheManager());
        service = new SysIpBlackwhiteServiceImpl(null, new IpPolicyCacheProjector(cache));
    }

    @Test
    @DisplayName("命中黑名单则拒绝")
    void testBlacklistRejects() {
        cache.projectIp(PolicyConfigConverter.IP_TYPE_BLACK, "203.0.113.88");
        assertFalse(service.checkIp("203.0.113.88"));
    }

    @Test
    @DisplayName("黑名单CIDR命中网段内IP则拒绝")
    void testBlacklistCidrRejects() {
        cache.projectIp(PolicyConfigConverter.IP_TYPE_BLACK, "198.51.100.0/24");
        assertFalse(service.checkIp("198.51.100.77"));
        assertTrue(service.checkIp("8.8.8.8"), "网段外的IP不受影响");
    }

    @Test
    @DisplayName("白名单IP放行")
    void testWhitelistAllows() {
        cache.projectIp(PolicyConfigConverter.IP_TYPE_WHITE, "192.0.2.5");
        assertTrue(service.checkIp("192.0.2.5"));
    }

    @Test
    @DisplayName("存在白名单时未列入名单的IP仍放行（白名单非准入名单）")
    void testUnlistedAllowedEvenWithWhitelist() {
        cache.projectIp(PolicyConfigConverter.IP_TYPE_WHITE, "192.0.2.5");
        assertTrue(cache.hasIpWhitelist(), "前置条件：白名单确实存在");
        assertTrue(service.checkIp("8.8.8.8"),
                "白名单只是放行豁免，不应把其余IP全部拒绝");
    }

    @Test
    @DisplayName("黑白同时命中时黑名单优先拒绝")
    void testBlacklistWinsOverWhitelist() {
        cache.projectIp(PolicyConfigConverter.IP_TYPE_BLACK, "203.0.113.88");
        cache.projectIp(PolicyConfigConverter.IP_TYPE_WHITE, "203.0.113.88");
        assertFalse(service.checkIp("203.0.113.88"), "黑名单优先，白名单不能覆盖");
    }

    @Test
    @DisplayName("无任何名单时全部放行")
    void testNoListsAllowsAll() {
        assertTrue(service.checkIp("8.8.8.8"));
        assertTrue(service.checkIp("203.0.113.88"));
    }

    @Test
    @DisplayName("空IP与多IP头取首个后放行")
    void testBlankAndMultiHeader() {
        cache.projectIp(PolicyConfigConverter.IP_TYPE_BLACK, "203.0.113.88");
        assertTrue(service.checkIp(""));
        assertTrue(service.checkIp(null));
        assertFalse(service.checkIp("203.0.113.88, 10.0.0.1"), "取首个IP判定");
    }

    @Test
    @DisplayName("清理黑名单后恢复放行")
    void testAfterClearBlacklist() {
        cache.projectIp(PolicyConfigConverter.IP_TYPE_BLACK, "203.0.113.88");
        assertFalse(service.checkIp("203.0.113.88"));
        cache.clearIp(PolicyConfigConverter.IP_TYPE_BLACK, "203.0.113.88");
        assertTrue(service.checkIp("203.0.113.88"));
    }
}
