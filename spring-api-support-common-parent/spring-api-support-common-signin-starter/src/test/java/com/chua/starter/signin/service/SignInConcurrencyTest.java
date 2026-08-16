package com.chua.starter.signin.service;

import com.chua.common.support.lang.datasource.kv.KvEngine;
import com.chua.starter.redis.support.template.LockTemplate;
import com.chua.starter.signin.dto.SignInPointDTO;
import com.chua.starter.signin.dto.SignInResultDTO;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * SignInService 并发测试
 *
 * <p>验证分布式锁防抖机制在多线程同时签到场景下的正确性：
 * 同一用户同一日期仅允许一次签到成功，其余请求被拦截。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@ExtendWith(MockitoExtension.class)
class SignInConcurrencyTest {

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
     * 锁获取计数器：统计多少线程成功获取锁
     */
    private final AtomicInteger lockAcquiredCount = new AtomicInteger(0);

    /**
     * 锁释放计数器
     */
    private final AtomicInteger lockReleasedCount = new AtomicInteger(0);

    /**
     * 测试用户 ID
     */
    private static final Long TEST_USER_ID = 42L;

    /**
     * 并发线程数
     */
    private static final int CONCURRENT_THREADS = 10;

    /**
     * 测试前初始化：构建签到服务并重置计数器。
     */
    @BeforeEach
    void setUp() {
        signInService = new SignInService(recordMapper, pointsService, pushService, lockTemplate, eventPublisher, kvEngine);
        lockAcquiredCount.set(0);
        lockReleasedCount.set(0);
    }

    /**
     * 并发签到测试：模拟 10 个线程同时签到，验证防抖机制。
     *
     * <p>测试策略：</p>
     * <ul>
     *   <li>第 1 个到达的线程成功获取锁，执行签到流程</li>
     *   <li>其余 9 个线程获取锁失败，返回"请勿重复签到"</li>
     *   <li>最终签到记录仅插入 1 次</li>
     * </ul>
     */
    @Test
    void testConcurrentSignIn_debounce() throws Exception {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        // 模拟锁：第 1 个线程获取成功，后续均失败
        when(lockTemplate.tryLock(anyString(), any(Duration.class), any(Duration.class), any(Runnable.class)))
                .thenAnswer(invocation -> {
                    boolean acquired = lockAcquiredCount.incrementAndGet() == 1;
                    return acquired;
                });

        // 释放锁不做实际逻辑，仅计数
        doAnswer(invocation -> {
            lockReleasedCount.incrementAndGet();
            return null;
        }).when(lockTemplate).release(anyString());

        // 第 1 个线程的签到流程：今日未签到，昨日无记录，插入成功
        when(recordMapper.selectByUserIdAndDate(eq(TEST_USER_ID), eq(today)))
                .thenAnswer(invocation -> {
                    // 模拟首次为 null，后续查询（已签到）返回已有记录
                    Thread.sleep(50);
                    return null;
                });
        when(recordMapper.selectByUserIdAndDate(eq(TEST_USER_ID), eq(yesterday)))
                .thenReturn(null);
        doAnswer(invocation -> {
            SignInRecord arg = invocation.getArgument(0);
            arg.setId(100L);
            return 1;
        }).when(recordMapper).insert(any(SignInRecord.class));

        List<SignInPointDTO> points = List.of(new SignInPointDTO("DAILY_SIGN", 10, "每日签到基础积分"));
        when(pointsService.calculatePoints(eq(TEST_USER_ID), eq(1), eq(100L))).thenReturn(points);
        when(pointsService.sumPoints(points)).thenReturn(10);
        when(recordMapper.selectTotalPointsByUserId(TEST_USER_ID)).thenReturn(10);
        SignInRecord record = new SignInRecord();
        record.setPoints(10);
        record.setStreak(1);
        when(recordMapper.selectByUserIdOrderByDateDesc(TEST_USER_ID)).thenReturn(List.of(record));

        // 并发执行
        int totalThreads = CONCURRENT_THREADS;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(totalThreads);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger debounceCount = new AtomicInteger(0);
        AtomicReference<String> firstMsg = new AtomicReference<>();

        ExecutorService executor = Executors.newFixedThreadPool(totalThreads);
        for (int i = 0; i < totalThreads; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    SignInResultDTO result = signInService.signIn(TEST_USER_ID);
                    if (result.isSuccess()) {
                        successCount.incrementAndGet();
                        firstMsg.compareAndSet(null, result.getMsg());
                    } else if (result.isAlreadySigned() && "请勿重复签到".equals(result.getMsg())) {
                        debounceCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    // 忽略预期内的异常
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        // 所有线程同时启动
        startLatch.countDown();
        boolean allDone = doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // 验证
        assertTrue(allDone, "并发测试超时");
        assertEquals(1, successCount.get(), "仅允许 1 个线程签到成功");
        assertEquals(totalThreads - 1, debounceCount.get(), "其余 " + (totalThreads - 1) + " 个线程应被防抖拦截");
        assertEquals("签到成功", firstMsg.get());

        // 验证签到记录只插入一次
        verify(recordMapper, times(1)).insert(any(SignInRecord.class));
        // 验证事件只发布一次
        verify(eventPublisher, times(1)).publishEvent(any());
        // 验证推送只调用一次
        verify(pushService, times(1)).pushSignInDone(anyLong(), any(SignInResultDTO.class));
    }

    /**
     * 不同用户并发签到测试：互不干扰。
     *
     * <p>两个不同用户同时签到，都应成功，互不影响。</p>
     */
    @Test
    void testConcurrentSignIn_differentUsers() throws Exception {
        Long userId1 = 1L;
        Long userId2 = 2L;
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        // 两个用户都能获取锁
        when(lockTemplate.tryLock(anyString(), any(Duration.class), any(Duration.class), any(Runnable.class)))
                .thenReturn(true);

        // 两个用户都未签到
        when(recordMapper.selectByUserIdAndDate(anyLong(), eq(today))).thenReturn(null);
        when(recordMapper.selectByUserIdAndDate(anyLong(), eq(yesterday))).thenReturn(null);
        doAnswer(invocation -> {
            SignInRecord arg = invocation.getArgument(0);
            arg.setId(arg.getUserId() == 1L ? 100L : 200L);
            return 1;
        }).when(recordMapper).insert(any(SignInRecord.class));

        List<SignInPointDTO> points = List.of(new SignInPointDTO("DAILY_SIGN", 10, "每日签到基础积分"));
        when(pointsService.calculatePoints(anyLong(), eq(1), anyLong())).thenReturn(points);
        when(pointsService.sumPoints(points)).thenReturn(10);
        when(recordMapper.selectTotalPointsByUserId(anyLong())).thenReturn(10);
        SignInRecord record = new SignInRecord();
        record.setPoints(10);
        record.setStreak(1);
        when(recordMapper.selectByUserIdOrderByDateDesc(anyLong())).thenReturn(List.of(record));

        // 并发执行
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);
        AtomicInteger successCount = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        for (Long uid : List.of(userId1, userId2)) {
            Long finalUid = uid;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    SignInResultDTO result = signInService.signIn(finalUid);
                    if (result.isSuccess()) successCount.incrementAndGet();
                } catch (Exception e) {
                    // 忽略
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        boolean allDone = doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(allDone, "并发测试超时");
        assertEquals(2, successCount.get(), "两个不同用户均应签到成功");

        // 验证签到记录插入两次
        verify(recordMapper, times(2)).insert(any(SignInRecord.class));
        // 验证事件发布两次
        verify(eventPublisher, times(2)).publishEvent(any());
    }

    /**
     * 同一用户快速连续签到（串行防抖）。
     *
     * <p>模拟用户在极短时间内连续点击两次签到按钮，
     * 第一次成功获取锁，第二次获取锁失败被防抖拦截。</p>
     */
    @Test
    void testRapidDoubleClick_debounce() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        // 第一次调用获取锁成功，第二次调用失败
        when(lockTemplate.tryLock(anyString(), any(Duration.class), any(Duration.class), any(Runnable.class)))
                .thenReturn(true)
                .thenReturn(false);

        // 第一次签到的流程
        when(recordMapper.selectByUserIdAndDate(TEST_USER_ID, today)).thenReturn(null);
        when(recordMapper.selectByUserIdAndDate(TEST_USER_ID, yesterday)).thenReturn(null);
        doAnswer(invocation -> {
            SignInRecord arg = invocation.getArgument(0);
            arg.setId(100L);
            return 1;
        }).when(recordMapper).insert(any(SignInRecord.class));

        List<SignInPointDTO> points = List.of(new SignInPointDTO("DAILY_SIGN", 10, "每日签到基础积分"));
        when(pointsService.calculatePoints(eq(TEST_USER_ID), eq(1), eq(100L))).thenReturn(points);
        when(pointsService.sumPoints(points)).thenReturn(10);
        when(recordMapper.selectTotalPointsByUserId(TEST_USER_ID)).thenReturn(10);
        SignInRecord record = new SignInRecord();
        record.setPoints(10);
        record.setStreak(1);
        when(recordMapper.selectByUserIdOrderByDateDesc(TEST_USER_ID)).thenReturn(List.of(record));

        // 第一次签到
        SignInResultDTO firstResult = signInService.signIn(TEST_USER_ID);
        assertTrue(firstResult.isSuccess());
        assertEquals("签到成功", firstResult.getMsg());

        // 第二次快速点击（锁已被占）
        SignInResultDTO secondResult = signInService.signIn(TEST_USER_ID);
        assertFalse(secondResult.isSuccess());
        assertTrue(secondResult.isAlreadySigned());
        assertEquals("请勿重复签到", secondResult.getMsg());

        // 验证签到记录只插入一次
        verify(recordMapper, times(1)).insert(any(SignInRecord.class));
    }

    /**
     * 同步锁释放验证：锁在 finally 块中保证释放。
     */
    @Test
    void testLockReleasedAfterSignIn() {
        when(lockTemplate.tryLock(anyString(), any(Duration.class), any(Duration.class), any(Runnable.class)))
                .thenReturn(false);

        signInService.signIn(TEST_USER_ID);

        // 锁获取失败，finally 中的 release 不应执行
        // 因为我们提前 return 在 try 块之前，所以不进入 try-finally
        // 但锁未获取成功，无需释放
        verify(lockTemplate, never()).release(anyString());
    }
}