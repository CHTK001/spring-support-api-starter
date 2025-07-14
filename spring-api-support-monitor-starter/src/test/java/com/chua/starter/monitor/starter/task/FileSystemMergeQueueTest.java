package com.chua.starter.monitor.starter.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 文件系统合并队列测试
 *
 * @author CH
 * @since 2024/12/30
 */
@SpringBootTest
class FileSystemMergeQueueTest {

    private FileSystemMergeQueue mergeQueue;

    @BeforeEach
    void setUp() {
        mergeQueue = new FileSystemMergeQueue();
    }

    @Test
    void testEnqueueAndDequeue() {
        // 测试入队和出队
        assertTrue(mergeQueue.isEmpty());
        assertEquals(0, mergeQueue.size());

        // 添加文件ID
        assertTrue(mergeQueue.enqueue(1));
        assertTrue(mergeQueue.enqueue(2));
        assertTrue(mergeQueue.enqueue(3));

        assertEquals(3, mergeQueue.size());
        assertFalse(mergeQueue.isEmpty());

        // 出队测试
        assertEquals(Integer.valueOf(1), mergeQueue.dequeue());
        assertEquals(Integer.valueOf(2), mergeQueue.dequeue());
        assertEquals(Integer.valueOf(3), mergeQueue.dequeue());

        assertTrue(mergeQueue.isEmpty());
        assertNull(mergeQueue.dequeue());
    }

    @Test
    void testDuplicateEnqueue() {
        // 测试重复入队
        assertTrue(mergeQueue.enqueue(1));
        assertFalse(mergeQueue.enqueue(1)); // 重复添加应该返回false

        assertEquals(1, mergeQueue.size());
    }

    @Test
    void testBatchDequeue() {
        // 添加多个文件ID
        for (int i = 1; i <= 10; i++) {
            mergeQueue.enqueue(i);
        }

        // 批量出队
        Integer[] batch1 = mergeQueue.dequeueBatch(5);
        assertEquals(5, batch1.length);
        assertEquals(5, mergeQueue.size());

        Integer[] batch2 = mergeQueue.dequeueBatch(10); // 请求10个，但只有5个
        assertEquals(5, batch2.length);
        assertTrue(mergeQueue.isEmpty());

        // 空队列批量出队
        Integer[] emptyBatch = mergeQueue.dequeueBatch(5);
        assertEquals(0, emptyBatch.length);
    }

    @Test
    void testNullFileId() {
        // 测试空文件ID
        assertFalse(mergeQueue.enqueue(null));
        assertTrue(mergeQueue.isEmpty());
    }

    @Test
    void testClear() {
        // 添加一些数据
        mergeQueue.enqueue(1);
        mergeQueue.enqueue(2);
        mergeQueue.enqueue(3);

        assertEquals(3, mergeQueue.size());

        // 清空队列
        mergeQueue.clear();
        assertTrue(mergeQueue.isEmpty());
        assertEquals(0, mergeQueue.size());
    }

    @Test
    void testStatistics() {
        // 测试统计信息
        String initialStats = mergeQueue.getStatistics();
        assertNotNull(initialStats);

        // 添加和处理一些数据
        mergeQueue.enqueue(1);
        mergeQueue.enqueue(2);
        mergeQueue.dequeue();

        String stats = mergeQueue.getStatistics();
        assertNotNull(stats);
        assertTrue(stats.contains("总入队: 2"));
        assertTrue(stats.contains("总处理: 1"));

        // 重置统计
        mergeQueue.resetStatistics();
        String resetStats = mergeQueue.getStatistics();
        assertTrue(resetStats.contains("总入队: 0"));
        assertTrue(resetStats.contains("总处理: 0"));
    }

    @Test
    void testHealthStatus() {
        // 测试健康状态
        String healthStatus = mergeQueue.getHealthStatus();
        assertNotNull(healthStatus);
        assertTrue(healthStatus.contains("状态: IDLE"));

        // 添加一些数据
        for (int i = 1; i <= 100; i++) {
            mergeQueue.enqueue(i);
        }

        healthStatus = mergeQueue.getHealthStatus();
        assertNotNull(healthStatus);
        // 应该显示NORMAL或BUSY状态
        assertTrue(healthStatus.contains("状态: NORMAL") || healthStatus.contains("状态: BUSY"));
    }

    @Test
    void testConcurrentAccess() throws InterruptedException {
        // 测试并发访问
        final int threadCount = 10;
        final int itemsPerThread = 100;
        Thread[] threads = new Thread[threadCount];

        // 创建多个线程同时入队
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < itemsPerThread; j++) {
                    mergeQueue.enqueue(threadId * itemsPerThread + j);
                }
            });
        }

        // 启动所有线程
        for (Thread thread : threads) {
            thread.start();
        }

        // 等待所有线程完成
        for (Thread thread : threads) {
            thread.join();
        }

        // 验证结果
        assertEquals(threadCount * itemsPerThread, mergeQueue.size());

        // 测试并发出队
        Thread[] dequeueThreads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            dequeueThreads[i] = new Thread(() -> {
                for (int j = 0; j < itemsPerThread; j++) {
                    mergeQueue.dequeue();
                }
            });
        }

        // 启动出队线程
        for (Thread thread : dequeueThreads) {
            thread.start();
        }

        // 等待所有出队线程完成
        for (Thread thread : dequeueThreads) {
            thread.join();
        }

        // 验证队列为空
        assertTrue(mergeQueue.isEmpty());
    }

    @Test
    void testPerformance() {
        // 性能测试
        final int itemCount = 10000;
        
        // 测试入队性能
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < itemCount; i++) {
            mergeQueue.enqueue(i);
        }
        long enqueueTime = System.currentTimeMillis() - startTime;
        
        System.out.println("入队 " + itemCount + " 个项目耗时: " + enqueueTime + "ms");
        assertEquals(itemCount, mergeQueue.size());
        
        // 测试出队性能
        startTime = System.currentTimeMillis();
        while (!mergeQueue.isEmpty()) {
            mergeQueue.dequeue();
        }
        long dequeueTime = System.currentTimeMillis() - startTime;
        
        System.out.println("出队 " + itemCount + " 个项目耗时: " + dequeueTime + "ms");
        assertTrue(mergeQueue.isEmpty());
        
        // 性能应该在合理范围内（这里设置为每个操作不超过1ms）
        assertTrue(enqueueTime < itemCount);
        assertTrue(dequeueTime < itemCount);
    }
}
