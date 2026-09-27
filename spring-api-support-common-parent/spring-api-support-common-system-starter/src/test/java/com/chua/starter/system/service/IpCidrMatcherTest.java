package com.chua.starter.system.service;

import com.chua.starter.system.util.IpCidrMatcher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("IPv4 CIDR 匹配工具测试")
class IpCidrMatcherTest {

    @Test
    @DisplayName("规范化精确IPv4")
    void canonicalizeIpv4() {
        assertEquals("192.168.1.1", IpCidrMatcher.canonicalize(" 192.168.001.001 "));
    }

    @Test
    @DisplayName("规范化CIDR网络地址")
    void canonicalizeCidr() {
        assertEquals("10.1.2.0/24", IpCidrMatcher.canonicalize("10.1.2.99/24"));
        assertEquals("10.1.2.9/32", IpCidrMatcher.canonicalize("10.1.2.9/32"));
        assertEquals("0.0.0.0/0", IpCidrMatcher.canonicalize("203.0.113.8/0"));
    }

    @Test
    @DisplayName("严格限制CIDR前缀")
    void rejectInvalidPrefix() {
        assertThrows(IllegalArgumentException.class, () -> IpCidrMatcher.canonicalize("10.0.0.0/-1"));
        assertThrows(IllegalArgumentException.class, () -> IpCidrMatcher.canonicalize("10.0.0.0/33"));
        assertThrows(IllegalArgumentException.class, () -> IpCidrMatcher.canonicalize("10.0.0.0/x"));
    }

    @Test
    @DisplayName("拒绝非严格IPv4")
    void rejectInvalidIpv4() {
        assertThrows(IllegalArgumentException.class, () -> IpCidrMatcher.canonicalize("10.0.0.256"));
        assertThrows(IllegalArgumentException.class, () -> IpCidrMatcher.canonicalize("10.0.0"));
        assertThrows(IllegalArgumentException.class, () -> IpCidrMatcher.canonicalize("2001:db8::1"));
    }

    @Test
    @DisplayName("匹配CIDR边界")
    void matchCidr() {
        assertTrue(IpCidrMatcher.matches("10.1.2.0", "10.1.2.0/24"));
        assertTrue(IpCidrMatcher.matches("10.1.2.255", "10.1.2.99/24"));
        assertFalse(IpCidrMatcher.matches("10.1.3.0", "10.1.2.0/24"));
        assertTrue(IpCidrMatcher.matches("203.0.113.8", "0.0.0.0/0"));
    }
}
