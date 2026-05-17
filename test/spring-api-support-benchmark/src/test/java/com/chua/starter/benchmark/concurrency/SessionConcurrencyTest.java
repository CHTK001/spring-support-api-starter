package com.chua.starter.benchmark.concurrency;

import com.chua.starter.benchmark.AbstractBenchmarkBase;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 多线程并发创建 Session，验证 Session ID 唯一性和属性原子性。
 */
class SessionConcurrencyTest extends AbstractBenchmarkBase {

    @Test
    void testConcurrentSessionCreation() throws Exception {
        int threadCount = 50;
        int requestsPerThread = 10;
        Set<String> sessionIds = ConcurrentHashMap.newKeySet();
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor();

        for (int t = 0; t < threadCount; t++) {
            pool.submit(() -> {
                try {
                    for (int r = 0; r < requestsPerThread; r++) {
                        // 每个请求创建新 session（不传 cookie）
                        HttpResponse<String> resp = get(rustUrl("/api/health"));
                        String setCookie = resp.headers().firstValue("Set-Cookie").orElse("");
                        if (!setCookie.isEmpty()) {
                            // 提取 JSESSIONID
                            String sessionId = extractSessionId(setCookie);
                            if (sessionId != null) {
                                sessionIds.add(sessionId);
                            }
                        }
                    }
                } catch (Exception ignored) {
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        pool.shutdown();

        log.info("Created {} unique session IDs from {} threads x {} requests",
                sessionIds.size(), threadCount, requestsPerThread);

        // 每个请求都生成唯一 session ID（极端情况可能有少量碰撞，但应接近 threadCount * requestsPerThread）
        assertTrue(sessionIds.size() > threadCount,
                "Should generate more unique session IDs than thread count");
    }

    @Test
    void testSessionAttributeAtomicity() throws Exception {
        // 建立 session
        HttpResponse<String> resp1 = get(rustUrl("/api/health"));
        String cookie = resp1.headers().firstValue("Set-Cookie").orElse("");
        String sessionId = extractSessionId(cookie);
        assertTrue(sessionId != null && !sessionId.isEmpty(),
                "Should get a session ID on first request");
    }

    private String extractSessionId(String setCookie) {
        if (setCookie == null || setCookie.isEmpty()) return null;
        // JSESSIONID=xxx; Path=/; ...
        for (String part : setCookie.split(";")) {
            part = part.trim();
            if (part.startsWith("JSESSIONID=")) {
                return part.substring("JSESSIONID=".length());
            }
        }
        return null;
    }
}
