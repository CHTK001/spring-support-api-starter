package com.chua.starter.system.service;

import com.chua.starter.system.util.IpCidrMatcher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CIDR 匹配边界与名单条目语义补充。
 *
 * <p>既有 {@link IpCidrMatcherTest} 已覆盖规范化、前缀校验、基础匹配；
 * 本类只补缺口：掩码 0/32 边界、非法输入回 false、matchesEntry 口径。</p>
 *
 * @author CH
 * @since 4.0.0.51
 */
@DisplayName("IPv4 CIDR 边界补充")
class IpCidrMatcherBoundaryTest {

    @Test
    @DisplayName("掩码0全匹配、掩码32精确匹配")
    void maskBoundaries() {
        assertTrue(IpCidrMatcher.matches("1.2.3.4", "0.0.0.0/0"));
        assertTrue(IpCidrMatcher.matches("10.1.2.9", "10.1.2.9/32"));
        assertFalse(IpCidrMatcher.matches("10.1.2.10", "10.1.2.9/32"));
    }

    @Test
    @DisplayName("非法输入返回false而非抛异常")
    void illegalReturnsFalse() {
        assertFalse(IpCidrMatcher.matches("not-an-ip", "10.1.2.0/24"));
        assertFalse(IpCidrMatcher.matches("10.1.2.9", "not-a-cidr"));
        assertFalse(IpCidrMatcher.matches(null, "10.1.2.0/24"));
        assertFalse(IpCidrMatcher.matches("10.1.2.9", null));
        assertThrows(IllegalArgumentException.class, () -> IpCidrMatcher.canonicalize(null));
        assertThrows(IllegalArgumentException.class, () -> IpCidrMatcher.canonicalize("  "));
    }

    @Test
    @DisplayName("名单条目：精确IP规范化比较、空条目与非法条目回false")
    void matchesEntrySemantics() {
        assertTrue(IpCidrMatcher.matchesEntry("192.168.001.001", "192.168.1.1"));
        assertTrue(IpCidrMatcher.matchesEntry("10.1.2.9", "10.1.2.0/24"));
        assertFalse(IpCidrMatcher.matchesEntry("10.1.3.0", "10.1.2.0/24"));
        assertFalse(IpCidrMatcher.matchesEntry("10.1.2.9", ""));
        assertFalse(IpCidrMatcher.matchesEntry("10.1.2.9", null));
        assertFalse(IpCidrMatcher.matchesEntry("not-an-ip", "10.1.2.0/24"));
        assertFalse(IpCidrMatcher.matchesEntry("10.1.2.9", "not-an-entry"));
    }
}
