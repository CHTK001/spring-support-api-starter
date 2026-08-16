package com.chua.starter.signin.service;

import com.chua.common.support.lang.datasource.kv.KvEngine;
import com.chua.starter.redis.support.template.LockTemplate;
import com.chua.starter.signin.dto.SignInDayDTO;
import com.chua.starter.signin.dto.SignInPointDTO;
import com.chua.starter.signin.dto.SignInResultDTO;
import com.chua.starter.signin.dto.SignInStatsDTO;
import com.chua.starter.signin.dto.UserSignInSummaryDTO;
import com.chua.starter.signin.mapper.SignInRecordMapper;
import com.chua.starter.signin.model.SignInRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * SignInService 单元测试
 *
 * <p>覆盖核心签到逻辑：防抖、签到、连续天数、日历、统计、管理员汇总。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@ExtendWith(MockitoExtension.class)
class SignInServiceTest {

    /**
     * 签到记录 Mapper 模拟
     */
    @Mock
    private SignInRecordMapper recordMapper;

    /**
     * 积分服务模拟
     */
    @Mock
    private SignInPointsService pointsService;

    /**
     * 推送服务模拟
     */
    @Mock
    private SignInPushService pushService;

    /**
     * 分布式锁模板模拟
     */
    @Mock
    private LockTemplate lockTemplate;

    /**
     * 事件发布器模拟
     */
    @Mock
    private ApplicationEventPublisher eventPublisher;

    /**
     * 统一 KV 引擎模拟（写入成就上下文数据）
     */
    @Mock
    private KvEngine kvEngine;

    /**
     * 测试目标服务
     */
    private SignInService signInService;

    /**
     * 测试前初始化
     */
    @BeforeEach
    void setUp() {
        signInService = new SignInService(recordMapper, pointsService, pushService, lockTemplate, eventPublisher, kvEngine);
    }

    // ==================== 签到成功 ====================

    /**
     * 测试：签到全流程成功，校验积分、连续天数、事件与推送。
     */
    @Test
    void testSignIn_success() {
        Long userId = 1L;
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        // 防抖锁获取成功
        when(lockTemplate.tryLock(anyString(), any(Duration.class), any(Duration.class), any(Runnable.class)))
                .thenReturn(true);

        // 今日未签到
        when(recordMapper.selectByUserIdAndDate(userId, today)).thenReturn(null);

        // 昨日有签到
        SignInRecord yesterdayRecord = new SignInRecord();
        yesterdayRecord.setStreak(5);
        when(recordMapper.selectByUserIdAndDate(userId, yesterday)).thenReturn(yesterdayRecord);

        // 签到记录插入成功（模拟 MyBatis-Plus 回填 ID）
        doAnswer(invocation -> {
            SignInRecord arg = invocation.getArgument(0);
            arg.setId(100L);
            return 1;
        }).when(recordMapper).insert(any(SignInRecord.class));

        // 积分计算
        List<SignInPointDTO> points = List.of(new SignInPointDTO("DAILY_SIGN", 10, "每日签到基础积分"));
        when(pointsService.calculatePoints(eq(userId), eq(6), eq(100L))).thenReturn(points);
        when(pointsService.sumPoints(points)).thenReturn(10);

        // 累计数据查询
        when(recordMapper.selectTotalPointsByUserId(userId)).thenReturn(50);
        SignInRecord record1 = new SignInRecord();
        record1.setPoints(10);
        record1.setStreak(6);
        SignInRecord record2 = new SignInRecord();
        record2.setPoints(10);
        record2.setStreak(5);
        when(recordMapper.selectByUserIdOrderByDateDesc(userId)).thenReturn(List.of(record1, record2));

        // 执行签到
        SignInResultDTO result = signInService.signIn(userId);

        // 验证结果
        assertTrue(result.isSuccess());
        assertFalse(result.isAlreadySigned());
        assertEquals(10, result.getTodayPoints());
        assertEquals(6, result.getStreak());
        assertEquals(50, result.getTotalPoints());
        assertEquals(2, result.getTotalDays());

        // 验证事件发布
        verify(eventPublisher).publishEvent(any());

        // 验证推送
        verify(pushService).pushSignInDone(eq(userId), any(SignInResultDTO.class));

        // 验证锁释放
        verify(lockTemplate).release(anyString());
    }

    // ==================== 防抖：锁获取失败 ====================

    /**
     * 测试：防抖锁获取失败时返回重复签到提示。
     */
    @Test
    void testSignIn_debounce_lock_failed() {
        Long userId = 1L;

        // 防抖锁获取失败
        when(lockTemplate.tryLock(anyString(), any(Duration.class), any(Duration.class), any(Runnable.class)))
                .thenReturn(false);

        SignInResultDTO result = signInService.signIn(userId);

        assertFalse(result.isSuccess());
        assertTrue(result.isAlreadySigned());
        assertEquals("请勿重复签到", result.getMsg());

        // 验证未发布事件
        verify(eventPublisher, never()).publishEvent(any());
    }

    // ==================== 今日已签到 ====================

    /**
     * 测试：今日已签到过时返回已签到结果。
     */
    @Test
    void testSignIn_already_signed() {
        Long userId = 1L;
        LocalDate today = LocalDate.now();

        // 防抖锁获取成功
        when(lockTemplate.tryLock(anyString(), any(Duration.class), any(Duration.class), any(Runnable.class)))
                .thenReturn(true);

        // 今日已签到
        SignInRecord existing = new SignInRecord();
        existing.setPoints(10);
        existing.setStreak(3);
        when(recordMapper.selectByUserIdAndDate(userId, today)).thenReturn(existing);
        when(recordMapper.selectTotalPointsByUserId(userId)).thenReturn(30);
        when(recordMapper.selectByUserIdOrderByDateDesc(userId)).thenReturn(List.of(existing));

        SignInResultDTO result = signInService.signIn(userId);

        assertFalse(result.isSuccess());
        assertTrue(result.isAlreadySigned());
        assertEquals("今日已签到", result.getMsg());

        verify(lockTemplate).release(anyString());
    }

    // ==================== 首次签到（无昨日记录） ====================

    /**
     * 测试：首次签到（无昨日记录）连续天数从 1 开始。
     */
    @Test
    void testSignIn_first_time() {
        Long userId = 2L;
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        when(lockTemplate.tryLock(anyString(), any(Duration.class), any(Duration.class), any(Runnable.class)))
                .thenReturn(true);
        when(recordMapper.selectByUserIdAndDate(userId, today)).thenReturn(null);
        when(recordMapper.selectByUserIdAndDate(userId, yesterday)).thenReturn(null);
        doAnswer(invocation -> {
            SignInRecord arg = invocation.getArgument(0);
            arg.setId(200L);
            return 1;
        }).when(recordMapper).insert(any(SignInRecord.class));

        List<SignInPointDTO> points = List.of(new SignInPointDTO("DAILY_SIGN", 10, "每日签到基础积分"));
        when(pointsService.calculatePoints(eq(userId), eq(1), eq(200L))).thenReturn(points);
        when(pointsService.sumPoints(points)).thenReturn(10);

        when(recordMapper.selectTotalPointsByUserId(userId)).thenReturn(10);
        SignInRecord record = new SignInRecord();
        record.setPoints(10);
        record.setStreak(1);
        when(recordMapper.selectByUserIdOrderByDateDesc(userId)).thenReturn(List.of(record));

        SignInResultDTO result = signInService.signIn(userId);

        assertTrue(result.isSuccess());
        assertEquals(1, result.getStreak());
        assertEquals(10, result.getTodayPoints());
        assertEquals(1, result.getTotalDays());

        verify(lockTemplate).release(anyString());
    }

    // ==================== isSignedToday ====================

    /**
     * 测试：今日已签到返回 true。
     */
    @Test
    void testIsSignedToday_true() {
        when(recordMapper.selectByUserIdAndDate(eq(1L), any(LocalDate.class))).thenReturn(new SignInRecord());
        assertTrue(signInService.isSignedToday(1L));
    }

    /**
     * 测试：今日未签到返回 false。
     */
    @Test
    void testIsSignedToday_false() {
        when(recordMapper.selectByUserIdAndDate(eq(1L), any(LocalDate.class))).thenReturn(null);
        assertFalse(signInService.isSignedToday(1L));
    }

    // ==================== getCalendar ====================

    /**
     * 测试：按月份查询签到日历。
     */
    @Test
    void testGetCalendar() {
        Long userId = 1L;
        String yearMonth = "2026-08";
        LocalDate date1 = LocalDate.of(2026, 8, 1);
        LocalDate date2 = LocalDate.of(2026, 8, 3);

        when(recordMapper.selectDatesByUserIdAndMonth(userId, yearMonth)).thenReturn(List.of(date1, date2));
        when(recordMapper.selectByUserIdAndDate(userId, date1)).thenReturn(createRecord(date1, 10));
        when(recordMapper.selectByUserIdAndDate(userId, date2)).thenReturn(createRecord(date2, 15));

        List<SignInDayDTO> result = signInService.getCalendar(userId, yearMonth);

        assertEquals(2, result.size());
        assertTrue(result.get(0).isSigned());
        assertTrue(result.get(1).isSigned());
    }

    // ==================== getStats ====================

    /**
     * 测试：今日已签到时统计当前连续及历史最高连续天数。
     */
    @Test
    void testGetStats_with_today_signed() {
        Long userId = 1L;
        LocalDate today = LocalDate.now();

        SignInRecord todayRecord = new SignInRecord();
        todayRecord.setStreak(5);
        todayRecord.setPoints(10);

        when(recordMapper.selectByUserIdOrderByDateDesc(userId)).thenReturn(List.of(todayRecord));
        when(recordMapper.selectMaxStreakByUserId(userId)).thenReturn(10);
        when(recordMapper.selectTotalPointsByUserId(userId)).thenReturn(100);
        when(recordMapper.selectByUserIdAndDate(userId, today)).thenReturn(todayRecord);

        SignInStatsDTO stats = signInService.getStats(userId);

        assertEquals(1, stats.getTotalDays());
        assertEquals(5, stats.getCurrentStreak());
        assertEquals(10, stats.getMaxStreak());
        assertEquals(100, stats.getTotalPoints());
        assertTrue(stats.isSignedToday());
    }

    /**
     * 测试：无任何签到记录时统计值均为 0。
     */
    @Test
    void testGetStats_no_records() {
        Long userId = 1L;

        when(recordMapper.selectByUserIdOrderByDateDesc(userId)).thenReturn(List.of());
        when(recordMapper.selectMaxStreakByUserId(userId)).thenReturn(0);
        when(recordMapper.selectTotalPointsByUserId(userId)).thenReturn(0);
        when(recordMapper.selectByUserIdAndDate(eq(userId), any(LocalDate.class))).thenReturn(null);

        SignInStatsDTO stats = signInService.getStats(userId);

        assertEquals(0, stats.getTotalDays());
        assertEquals(0, stats.getCurrentStreak());
        assertEquals(0, stats.getMaxStreak());
        assertEquals(0, stats.getTotalPoints());
        assertFalse(stats.isSignedToday());
    }

    // ==================== getUserSummaryPage ====================

    /**
     * 测试：分页查询用户签到汇总及总数。
     */
    @Test
    void testGetUserSummaryPage() {
        UserSignInSummaryDTO summary = new UserSignInSummaryDTO();
        summary.setUserId(1L);
        summary.setSignInDays(10);
        summary.setTotalPoints(100);

        when(recordMapper.selectUserSummaryPage(0L, 10)).thenReturn(List.of(summary));
        when(recordMapper.countUserSummary()).thenReturn(5L);

        List<UserSignInSummaryDTO> result = signInService.getUserSummaryPage(1, 10);
        long total = signInService.countUserSummary();

        assertEquals(1, result.size());
        assertEquals(100, result.get(0).getTotalPoints());
        assertEquals(5L, total);
    }

    // ==================== 辅助方法 ====================

    /**
     * 创建签到记录用于测试。
     *
     * @param date   签到日期
     * @param points 积分
     * @return 签到记录
     */
    private SignInRecord createRecord(LocalDate date, int points) {
        SignInRecord record = new SignInRecord();
        record.setSignDate(date);
        record.setPoints(points);
        record.setStreak(1);
        return record;
    }
}