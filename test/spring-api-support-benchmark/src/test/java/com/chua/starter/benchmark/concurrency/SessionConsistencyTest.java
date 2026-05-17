package com.chua.starter.benchmark.concurrency;

import com.chua.starter.benchmark.AbstractBenchmarkBase;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 同一 Session 连续请求，验证属性在不同请求间的一致性。
 */
class SessionConsistencyTest extends AbstractBenchmarkBase {

    private final HttpClient cookieClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @Test
    void testSessionAttributePersistence() throws Exception {
        // 1. 首次请求 — 获取 session cookie
        HttpResponse<String> resp1 = getWithCookie(rustUrl("/api/health"), null);
        String cookie = extractSetCookie(resp1);
        assertTrue(cookie != null && !cookie.isEmpty(),
                "First request should return a session cookie");

        // 2. 第二次请求 — 携带 cookie
        HttpResponse<String> resp2 = getWithCookie(rustUrl("/api/health"), cookie);
        assertEquals(200, resp2.statusCode(),
                "Second request with cookie should succeed");

        // 3. 第三次请求 — 同一 cookie
        HttpResponse<String> resp3 = getWithCookie(rustUrl("/api/echo?msg=hello"), cookie);
        assertEquals(200, resp3.statusCode(),
                "Third request with same cookie should succeed");
        assertTrue(resp3.body().contains("hello"),
                "Echo response should contain the sent message");
    }

    private HttpResponse<String> getWithCookie(String url, String cookie) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .GET();
        if (cookie != null) {
            builder.header("Cookie", cookie);
        }
        return cookieClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private String extractSetCookie(HttpResponse<String> resp) {
        return resp.headers().firstValue("Set-Cookie").orElse(null);
    }
}
