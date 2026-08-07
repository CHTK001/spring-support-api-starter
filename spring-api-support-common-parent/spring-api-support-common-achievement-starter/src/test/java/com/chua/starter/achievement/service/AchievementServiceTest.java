package com.chua.starter.achievement.service;

import com.chua.common.support.concurrent.circuit.CircuitBreaker;
import com.chua.common.support.lang.datasource.kv.KvEngine;
import com.chua.starter.achievement.dto.UnlockedAchievementDTO;
import com.chua.starter.achievement.mapper.AchievementTierMapper;
import com.chua.starter.achievement.mapper.UserAchievementMapper;
import com.chua.starter.achievement.model.AchievementTier;
import com.chua.starter.achievement.model.UserAchievement;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * AchievementService 单元测试
 *
 * <p>覆盖核心业务逻辑：统计更新、规则判定、解锁、查询等。</p>
 *
 * @author CH
 * @since 2026-08-07
 */
@ExtendWith(MockitoExtension.class)
class AchievementServiceTest {

    @Mock
    private KvEngine kvEngine;

    @Mock
    private AchievementTierMapper tierMapper;

    @Mock
    private UserAchievementMapper userAchievementMapper;

    @Mock
    private AchievementPushService pushService;

    private AchievementService service;

    @BeforeEach
    void setUp() {
        service = new AchievementService(kvEngine, tierMapper, userAchievementMapper, pushService);
    }

    // ==================== onUserAction ====================

    @Test
    void testOnUserAction_increment_positive() {
        Long userId = 1L;
        String statKey = "loginCount";
        long delta = 1L;

        when(kvEngine.incr(anyString())).thenReturn(5L);
        when(kvEngine.findAllByPrefix(eq("achievement:1:")))
                .thenReturn(Map.of("achievement:1:loginCount", "5"));
        when(userAchievementMapper.selectMaxLevelByUserId(userId)).thenReturn(Map.of());
        when(tierMapper.selectList(null)).thenReturn(List.of());

        List<UnlockedAchievementDTO> result = service.onUserAction(userId, statKey, delta);

        assertNotNull(result);
        verify(kvEngine).incr("achievement:1:loginCount");
        verify(kvEngine).findAllByPrefix("achievement:1:");
    }

    @Test
    void testOnUserAction_decrement_negative() {
        Long userId = 1L;
        String statKey = "loginCount";
        long delta = -1L;

        String existingKey = "achievement:1:loginCount";
        when(kvEngine.get(existingKey)).thenReturn("5");
        doNothing().when(kvEngine).put(existingKey, "4");
        when(kvEngine.findAllByPrefix("achievement:1:"))
                .thenReturn(Map.of("achievement:1:loginCount", "4"));
        when(userAchievementMapper.selectMaxLevelByUserId(userId)).thenReturn(Map.of());
        when(tierMapper.selectList(null)).thenReturn(List.of());

        List<UnlockedAchievementDTO> result = service.onUserAction(userId, statKey, delta);

        assertNotNull(result);
        verify(kvEngine).get(existingKey);
        verify(kvEngine).put(existingKey, "4");
    }

    @Test
    void testOnUserAction_decrement_null_value() {
        Long userId = 1L;
        String statKey = "loginCount";
        long delta = -1L;

        String existingKey = "achievement:1:loginCount";
        when(kvEngine.get(existingKey)).thenReturn(null);
        when(kvEngine.findAllByPrefix(eq("achievement:1:")))
                .thenReturn(Map.of());
        when(userAchievementMapper.selectMaxLevelByUserId(userId)).thenReturn(Map.of());
        when(tierMapper.selectList(null)).thenReturn(List.of());

        List<UnlockedAchievementDTO> result = service.onUserAction(userId, statKey, delta);

        assertNotNull(result);
        verify(kvEngine, never()).put(anyString(), anyString());
    }

    // ==================== setStatAndJudge ====================

    @Test
    void testSetStatAndJudge() {
        Long userId = 1L;
        String statKey = "loginCount";
        String value = "10";

        String key = "achievement:1:loginCount";
        doNothing().when(kvEngine).put(key, value);
        when(kvEngine.findAllByPrefix("achievement:1:"))
                .thenReturn(Map.of("achievement:1:loginCount", "10"));
        when(userAchievementMapper.selectMaxLevelByUserId(userId)).thenReturn(Map.of());
        when(tierMapper.selectList(null)).thenReturn(List.of());

        List<UnlockedAchievementDTO> result = service.setStatAndJudge(userId, statKey, value);

        assertNotNull(result);
        verify(kvEngine).put(key, value);
    }

    // ==================== getUnlocked ====================

    @Test
    void testGetUnlocked() {
        Long userId = 1L;

        AchievementTier tier1 = createTier("login", 1, "首次登录");
        AchievementTier tier2 = createTier("login", 2, "登录达人");
        when(userAchievementMapper.selectMaxLevelByUserId(userId))
                .thenReturn(Map.of("login", 2));
        when(tierMapper.selectList(null))
                .thenReturn(List.of(tier1, tier2));
        when(userAchievementMapper.selectByUserIdAndType(userId, "login"))
                .thenReturn(createRecord(userId, "login", 2, LocalDateTime.now()));

        List<UnlockedAchievementDTO> result = service.getUnlocked(userId);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(dto -> "login".equals(dto.getType())));
        assertTrue(result.stream().anyMatch(dto -> "首次登录".equals(dto.getName())));
        assertTrue(result.stream().anyMatch(dto -> "登录达人".equals(dto.getName())));
    }

    @Test
    void testGetUnlocked_no_unlocked() {
        Long userId = 1L;
        when(userAchievementMapper.selectMaxLevelByUserId(userId)).thenReturn(Map.of());

        List<UnlockedAchievementDTO> result = service.getUnlocked(userId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== getUncompleted ====================

    @Test
    void testGetUncompleted_some_unlocked() {
        Long userId = 1L;
        String type = "login";

        AchievementTier tier1 = createTier("login", 1, "首次登录");
        AchievementTier tier2 = createTier("login", 2, "登录达人");
        AchievementTier tier3 = createTier("login", 3, "登录高手");

        when(userAchievementMapper.selectByUserIdAndType(userId, type))
                .thenReturn(createRecord(userId, type, 2, LocalDateTime.now()));
        when(tierMapper.selectByTypeOrderBySortAndLevel(type))
                .thenReturn(List.of(tier1, tier2, tier3));

        List<AchievementTier> result = service.getUncompleted(userId, type);

        assertEquals(1, result.size());
        assertEquals("登录高手", result.get(0).getName());
    }

    @Test
    void testGetUncompleted_none_unlocked() {
        Long userId = 1L;
        String type = "login";

        AchievementTier tier1 = createTier("login", 1, "首次登录");
        when(userAchievementMapper.selectByUserIdAndType(userId, type)).thenReturn(null);
        when(tierMapper.selectByTypeOrderBySortAndLevel(type))
                .thenReturn(List.of(tier1));

        List<AchievementTier> result = service.getUncompleted(userId, type);

        assertEquals(1, result.size());
        assertEquals("首次登录", result.get(0).getName());
    }

    // ==================== getTierListByType ====================

    @Test
    void testGetTierListByType() {
        String type = "login";
        AchievementTier tier1 = createTier("login", 1, "首次登录");
        AchievementTier tier2 = createTier("login", 2, "登录达人");

        when(tierMapper.selectByTypeOrderBySortAndLevel(type))
                .thenReturn(List.of(tier1, tier2));

        List<AchievementTier> result = service.getTierListByType(type);

        assertEquals(2, result.size());
        assertEquals("login", result.get(0).getType());
    }

    // ==================== judgeAll (CircuitBreaker 判定) ====================

    @Test
    void testJudgeAll_unlock_achievement() {
        Long userId = 1L;

        AchievementTier tier = createTier("login", 1, "首次登录");
        tier.setRule("loginCount >= 1");

        when(userAchievementMapper.selectMaxLevelByUserId(userId))
                .thenReturn(Map.of());
        when(tierMapper.selectList(null))
                .thenReturn(List.of(tier));
        // 返回 null 使测试走 insert 路径（新增用户）
        when(userAchievementMapper.selectByUserIdAndType(eq(userId), eq("login")))
                .thenReturn(null);
        when(userAchievementMapper.insert(any(UserAchievement.class))).thenReturn(1);
        doNothing().when(pushService).pushAchievements(anyLong(), anyList());

        Map<String, String> context = Map.of("loginCount", "5");

        List<UnlockedAchievementDTO> result = service.judgeAll(userId, context);

        assertEquals(1, result.size());
        assertEquals("首次登录", result.get(0).getName());
        assertEquals(1, result.get(0).getLevel());
        verify(pushService).pushAchievements(eq(userId), anyList());
    }

    @Test
    void testJudgeAll_no_new_unlock() {
        Long userId = 1L;

        AchievementTier tier = createTier("login", 2, "登录达人");
        tier.setRule("loginCount >= 10");

        when(userAchievementMapper.selectMaxLevelByUserId(userId))
                .thenReturn(Map.of());
        when(tierMapper.selectList(null)).thenReturn(List.of(tier));

        Map<String, String> context = Map.of("loginCount", "5");

        List<UnlockedAchievementDTO> result = service.judgeAll(userId, context);

        assertEquals(0, result.size());
        verify(pushService, never()).pushAchievements(anyLong(), anyList());
    }

    @Test
    void testJudgeAll_skip_already_unlocked() {
        Long userId = 1L;

        AchievementTier tier = createTier("login", 1, "首次登录");
        tier.setRule("loginCount >= 1");

        when(userAchievementMapper.selectMaxLevelByUserId(userId))
                .thenReturn(Map.of("login", 1));
        when(tierMapper.selectList(null)).thenReturn(List.of(tier));

        Map<String, String> context = Map.of("loginCount", "10");

        List<UnlockedAchievementDTO> result = service.judgeAll(userId, context);

        assertEquals(0, result.size());
    }

    @Test
    void testJudgeAll_empty_rule() {
        Long userId = 1L;

        AchievementTier tier = createTier("login", 1, "空规则");
        tier.setRule("");

        when(userAchievementMapper.selectMaxLevelByUserId(userId))
                .thenReturn(Map.of());
        when(tierMapper.selectList(null)).thenReturn(List.of(tier));

        Map<String, String> context = Map.of("loginCount", "5");

        List<UnlockedAchievementDTO> result = service.judgeAll(userId, context);

        assertEquals(0, result.size());
    }

    // ==================== convertToBreakerContext (via judgeAll) ====================

    @Test
    void testConvertToBreakerContext_numeric_values() {
        Long userId = 1L;

        AchievementTier tier = createTier("login", 1, "首次登录");
        tier.setRule("loginCount >= 10 AND signInCount >= 30");

        when(userAchievementMapper.selectMaxLevelByUserId(userId)).thenReturn(Map.of());
        when(tierMapper.selectList(null)).thenReturn(List.of(tier));
        when(userAchievementMapper.selectByUserIdAndType(eq(userId), eq("login")))
                .thenReturn(null);
        when(userAchievementMapper.insert(any(UserAchievement.class))).thenReturn(1);
        doNothing().when(pushService).pushAchievements(anyLong(), anyList());

        Map<String, String> context = Map.of(
                "achievement:1:loginCount", "10",
                "achievement:1:signInCount", "30"
        );

        List<UnlockedAchievementDTO> result = service.judgeAll(userId, context);

        assertEquals(1, result.size());
    }

    @Test
    void testConvertToBreakerContext_missing_stat_returns_false() {
        Long userId = 1L;

        AchievementTier tier = createTier("login", 1, "首次登录");
        tier.setRule("loginCount >= 5 AND missingStat >= 10");

        when(userAchievementMapper.selectMaxLevelByUserId(userId)).thenReturn(Map.of());
        when(tierMapper.selectList(null)).thenReturn(List.of(tier));

        Map<String, String> context = Map.of("loginCount", "5");

        List<UnlockedAchievementDTO> result = service.judgeAll(userId, context);

        assertEquals(0, result.size());
    }

    // ==================== saveUnlocked via setStatAndJudge ====================

    @Test
    void testSaveUnlocked_new_record() {
        Long userId = 1L;

        AchievementTier tier = createTier("login", 1, "首次登录");
        tier.setRule("loginCount >= 5");

        doNothing().when(kvEngine).put("achievement:1:loginCount", "10");
        when(kvEngine.findAllByPrefix("achievement:1:"))
                .thenReturn(Map.of("achievement:1:loginCount", "10"));
        when(userAchievementMapper.selectMaxLevelByUserId(userId)).thenReturn(Map.of());
        when(tierMapper.selectList(null)).thenReturn(List.of(tier));
        when(userAchievementMapper.selectByUserIdAndType(eq(userId), eq("login")))
                .thenReturn(null);
        when(userAchievementMapper.insert(any(UserAchievement.class))).thenReturn(1);
        doNothing().when(pushService).pushAchievements(eq(userId), anyList());

        List<UnlockedAchievementDTO> result = service.setStatAndJudge(userId, "loginCount", "10");

        assertEquals(1, result.size());
        verify(userAchievementMapper).insert(isA(UserAchievement.class));
    }

    // ==================== reportStats（刷新接口） ====================

    @Test
    void testReportStats_unlock() {
        Long userId = 1L;

        AchievementTier tier = createTier("menu", 1, "菜单达人");
        tier.setRule("menuVisitedCount >= 50");

        doNothing().when(kvEngine).put(eq("achievement:1:menuVisitedCount"), eq("50"));
        when(kvEngine.findAllByPrefix("achievement:1:"))
                .thenReturn(Map.of("achievement:1:menuVisitedCount", "50"));
        when(userAchievementMapper.selectMaxLevelByUserId(userId)).thenReturn(Map.of());
        when(tierMapper.selectList(null)).thenReturn(List.of(tier));
        when(userAchievementMapper.selectByUserIdAndType(eq(userId), eq("menu")))
                .thenReturn(null);
        when(userAchievementMapper.insert(any(UserAchievement.class))).thenReturn(1);
        doNothing().when(pushService).pushAchievements(eq(userId), anyList());

        List<UnlockedAchievementDTO> result = service.reportStats(userId, Map.of("menuVisitedCount", 50));

        assertEquals(1, result.size());
        assertEquals("菜单达人", result.get(0).getName());
        verify(kvEngine).put("achievement:1:menuVisitedCount", "50");
    }

    @Test
    void testReportStats_no_stat_returns_empty() {
        Long userId = 1L;

        List<UnlockedAchievementDTO> result = service.reportStats(userId, Map.of());

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(kvEngine, never()).put(anyString(), anyString());
    }

    @Test
    void testReportStats_multiple_stats_judged() {
        Long userId = 1L;

        AchievementTier tier = createTier("menu", 1, "菜单达人");
        tier.setRule("menuVisitedCount >= 50 AND messageSentCount >= 10");

        doNothing().when(kvEngine).put(eq("achievement:1:menuVisitedCount"), eq("50"));
        doNothing().when(kvEngine).put(eq("achievement:1:messageSentCount"), eq("10"));
        when(kvEngine.findAllByPrefix("achievement:1:"))
                .thenReturn(Map.of(
                        "achievement:1:menuVisitedCount", "50",
                        "achievement:1:messageSentCount", "10"
                ));
        when(userAchievementMapper.selectMaxLevelByUserId(userId)).thenReturn(Map.of());
        when(tierMapper.selectList(null)).thenReturn(List.of(tier));
        when(userAchievementMapper.selectByUserIdAndType(eq(userId), eq("menu")))
                .thenReturn(null);
        when(userAchievementMapper.insert(any(UserAchievement.class))).thenReturn(1);
        doNothing().when(pushService).pushAchievements(eq(userId), anyList());

        List<UnlockedAchievementDTO> result = service.reportStats(userId, Map.of(
                "menuVisitedCount", 50,
                "messageSentCount", 10
        ));

        assertEquals(1, result.size());
        verify(kvEngine).put("achievement:1:menuVisitedCount", "50");
        verify(kvEngine).put("achievement:1:messageSentCount", "10");
    }

    // ==================== CircuitBreaker expression compatibility ====================

    @Test
    void testCircuitBreaker_ge_expression() {
        Map<String, Object> ctx = Map.of("loginCount", 5);
        assertTrue(CircuitBreaker.evaluate("loginCount >= 5", ctx));
        assertTrue(CircuitBreaker.evaluate("loginCount >= 3", ctx));
        assertFalse(CircuitBreaker.evaluate("loginCount >= 10", ctx));
    }

    @Test
    void testCircuitBreaker_and_expression() {
        Map<String, Object> ctx = Map.of("loginCount", 10, "signInCount", 5);
        assertTrue(CircuitBreaker.evaluate("loginCount >= 5 AND signInCount >= 3", ctx));
        assertFalse(CircuitBreaker.evaluate("loginCount >= 5 AND signInCount >= 10", ctx));
    }

    @Test
    void testCircuitBreaker_or_expression() {
        Map<String, Object> ctx = Map.of("loginCount", 10);
        assertTrue(CircuitBreaker.evaluate("loginCount >= 5 OR signInCount >= 100", ctx));
        assertFalse(CircuitBreaker.evaluate("loginCount >= 100 OR signInCount >= 100", ctx));
    }

    @Test
    void testCircuitBreaker_missing_column_returns_false() {
        Map<String, Object> ctx = Map.of("loginCount", 5);
        assertFalse(CircuitBreaker.evaluate("missingStat >= 5", ctx));
    }

    // ==================== helpers ====================

    private AchievementTier createTier(String type, int level, String name) {
        AchievementTier tier = new AchievementTier();
        tier.setId(Long.valueOf(level));
        tier.setType(type);
        tier.setLevel(level);
        tier.setName(name);
        tier.setBadge("\uD83C\uDFC5");
        tier.setDescription("test");
        tier.setRule("testRule");
        tier.setSort(level);
        return tier;
    }

    private UserAchievement createRecord(Long userId, String type, int maxLevel, LocalDateTime unlockedAt) {
        UserAchievement record = new UserAchievement();
        record.setUserId(userId);
        record.setType(type);
        record.setMaxLevel(maxLevel);
        record.setUnlockedAt(unlockedAt);
        return record;
    }
}
