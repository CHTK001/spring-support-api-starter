package com.chua.starter.monitor.starter.task.manager;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 虚拟线程任务管理器测试
 *
 * @author CH
 * @since 2024/12/28
 */
@ExtendWith(MockitoExtension.class)
class VirtualThreadTaskManagerTest {

    @Test
    void testVirtualThreadPerformance() throws InterruptedException {
        // 测试虚拟线程的性能
        int taskCount = 1000;
        CountDownLatch latch = new CountDownLatch(taskCount);
        AtomicInteger completedTasks = new AtomicInteger(0);

        ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

        long startTime = System.currentTimeMillis();

        // 提交大量任务
        for (int i = 0; i < taskCount; i++) {
            final int taskId = i;
            virtualThreadExecutor.submit(() -> {
                try {
                    // 模拟任务执行
                    Thread.sleep(10); // 10ms
                    completedTasks.incrementAndGet();
                    System.out.println("虚拟线程任务 " + taskId + " 完成，线程: " + Thread.currentThread());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }

        // 等待所有任务完成
        assertTrue(latch.await(30, TimeUnit.SECONDS), "任务应该在30秒内完成");

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("完成 " + taskCount + " 个虚拟线程任务耗时: " + duration + "ms");
        System.out.println("完成的任务数: " + completedTasks.get());

        assertEquals(taskCount, completedTasks.get(), "所有任务都应该完成");

        virtualThreadExecutor.shutdown();
        assertTrue(virtualThreadExecutor.awaitTermination(5, TimeUnit.SECONDS));
    }

    @Test
    void testVirtualThreadVsPlatformThread() throws InterruptedException {
        int taskCount = 100;

        // 测试虚拟线程
        long virtualThreadTime = measureExecutionTime(taskCount, true);

        // 测试平台线程
        long platformThreadTime = measureExecutionTime(taskCount, false);

        System.out.println("虚拟线程执行时间: " + virtualThreadTime + "ms");
        System.out.println("平台线程执行时间: " + platformThreadTime + "ms");

        // 虚拟线程通常在高并发场景下表现更好
        assertTrue(virtualThreadTime > 0, "虚拟线程执行时间应该大于0");
        assertTrue(platformThreadTime > 0, "平台线程执行时间应该大于0");
    }

    private long measureExecutionTime(int taskCount, boolean useVirtualThread) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(taskCount);
        AtomicInteger completedTasks = new AtomicInteger(0);

        ExecutorService executor = useVirtualThread 
            ? Executors.newVirtualThreadPerTaskExecutor()
            : Executors.newCachedThreadPool();

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < taskCount; i++) {
            executor.submit(() -> {
                try {
                    // 模拟I/O密集型任务
                    Thread.sleep(50);
                    completedTasks.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        long endTime = System.currentTimeMillis();

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        return endTime - startTime;
    }

    @Test
    void testVirtualThreadCharacteristics() {
        ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

        virtualThreadExecutor.submit(() -> {
            Thread currentThread = Thread.currentThread();
            System.out.println("当前线程: " + currentThread);
            System.out.println("是否为虚拟线程: " + currentThread.isVirtual());
            System.out.println("线程名称: " + currentThread.getName());

            // 验证是虚拟线程
            assertTrue(currentThread.isVirtual(), "应该是虚拟线程");
        });

        virtualThreadExecutor.shutdown();
    }

    @Test
    void testConcurrentVirtualThreadTasks() throws InterruptedException {
        // 测试大量并发虚拟线程任务
        int taskCount = 10000;
        CountDownLatch latch = new CountDownLatch(taskCount);
        AtomicInteger counter = new AtomicInteger(0);

        ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < taskCount; i++) {
            virtualThreadExecutor.submit(() -> {
                try {
                    // 模拟轻量级任务
                    counter.incrementAndGet();
                    Thread.sleep(1); // 1ms
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }

        // 等待所有任务完成
        assertTrue(latch.await(60, TimeUnit.SECONDS), "大量任务应该在60秒内完成");

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("完成 " + taskCount + " 个并发虚拟线程任务耗时: " + duration + "ms");
        System.out.println("平均每个任务耗时: " + (double) duration / taskCount + "ms");

        assertEquals(taskCount, counter.get(), "所有任务都应该完成");

        virtualThreadExecutor.shutdown();
        assertTrue(virtualThreadExecutor.awaitTermination(10, TimeUnit.SECONDS));
    }

    @Test
    void testVirtualThreadMemoryUsage() throws InterruptedException {
        // 测试虚拟线程的内存使用情况
        Runtime runtime = Runtime.getRuntime();
        
        // 记录初始内存使用
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        
        int taskCount = 5000;
        CountDownLatch latch = new CountDownLatch(taskCount);
        
        ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();
        
        for (int i = 0; i < taskCount; i++) {
            virtualThreadExecutor.submit(() -> {
                try {
                    // 模拟长时间运行的任务
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // 记录峰值内存使用
        long peakMemory = runtime.totalMemory() - runtime.freeMemory();
        
        latch.await(30, TimeUnit.SECONDS);
        
        // 记录结束时内存使用
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        
        System.out.println("初始内存使用: " + (initialMemory / 1024 / 1024) + " MB");
        System.out.println("峰值内存使用: " + (peakMemory / 1024 / 1024) + " MB");
        System.out.println("结束时内存使用: " + (finalMemory / 1024 / 1024) + " MB");
        System.out.println("内存增长: " + ((peakMemory - initialMemory) / 1024 / 1024) + " MB");
        
        virtualThreadExecutor.shutdown();
        assertTrue(virtualThreadExecutor.awaitTermination(10, TimeUnit.SECONDS));
        
        // 虚拟线程应该比平台线程使用更少的内存
        assertTrue(peakMemory > initialMemory, "应该有内存使用增长");
    }
}
