package com.chua.starter.benchmark.concurrency;

import com.chua.starter.benchmark.AbstractBenchmarkBase;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 高并发请求无状态端点，验证 Rust 服务器能正确处理所有请求无数据竞争。
 */
class RequestConcurrencyTest extends AbstractBenchmarkBase {

    @Test
    void testConcurrentHealthRequests() throws Exception {
        int requestCount = 200;
        CountDownLatch latch = new CountDownLatch(requestCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor();

        for (int i = 0; i < requestCount; i++) {
            pool.submit(() -> {
                try {
                    HttpResponse<String> resp = get(rustUrl("/api/health"));
                    if (resp.statusCode() == 200) {
                        successCount.incrementAndGet();
                    } else {
                        errorCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        pool.shutdown();

        log.info("Health check: {} success, {} errors out of {} requests",
                successCount.get(), errorCount.get(), requestCount);

        assertEquals(requestCount, successCount.get(),
                "All concurrent requests should succeed");
        assertEquals(0, errorCount.get(),
                "No errors should occur");
    }

    @Test
    void testConcurrentEchoWithParams() throws Exception {
        int requestCount = 100;
        CountDownLatch latch = new CountDownLatch(requestCount);
        AtomicInteger successCount = new AtomicInteger(0);
        ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor();

        for (int i = 0; i < requestCount; i++) {
            String msg = "msg-" + i;
            pool.submit(() -> {
                try {
                    HttpResponse<String> resp = get(rustUrl("/api/echo?msg=" + msg));
                    if (resp.statusCode() == 200 && resp.body().contains(msg)) {
                        successCount.incrementAndGet();
                    }
                } catch (Exception ignored) {
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        pool.shutdown();

        assertTrue(successCount.get() > requestCount * 0.95,
                "At least 95% of echo requests should succeed with correct response");
    }
}
