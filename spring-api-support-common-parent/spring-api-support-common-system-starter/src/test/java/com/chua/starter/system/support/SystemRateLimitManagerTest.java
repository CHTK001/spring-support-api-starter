// [P3C A20 豁免] A20/A30/A31: 含日志/界面/SQL 字符串字面量等非业务魔法值场景，豁免。
package com.chua.starter.system.support;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SystemRateLimitManagerTest {

    @Test
    void shouldDisableWhenEnabledButQpsIsNotPositive() {
        SystemRateLimitManager manager = new SystemRateLimitManager();

        manager.update(true, 0, 37, "tenant");

        assertFalse(manager.isEnabled());
        assertTrue(manager.tryAcquire());
        assertTrue(manager.tryAcquire());
        Map<String, Object> status = manager.getStatus();
        assertEquals(true, status.get("enabled"));
        assertEquals(0, status.get("qps"));
        assertEquals(37, status.get("burstSize"));
        assertEquals("tenant", status.get("dimension"));
    }

    @Test
    void shouldKeepStatusWhenDisabledWithPositiveQps() {
        SystemRateLimitManager manager = new SystemRateLimitManager();

        manager.update(false, 1, 9, "global");

        assertFalse(manager.isEnabled());
        assertTrue(manager.tryAcquire());
        assertTrue(manager.tryAcquire());
        assertEquals(9, manager.getStatus().get("burstSize"));
    }

    @Test
    void shouldLimitWithPositiveQps() {
        SystemRateLimitManager manager = new SystemRateLimitManager();
        manager.update(true, 1, 1, "global");

        assertTrue(manager.isEnabled());
        assertTrue(manager.tryAcquire());
        assertFalse(manager.tryAcquire());
    }
}