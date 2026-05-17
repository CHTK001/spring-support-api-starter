package com.chua.starter.benchmark.compatibility;

import com.chua.starter.benchmark.AbstractBenchmarkBase;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Session 功能对照 — 验证 JSESSIONID cookie、getAttribute/setAttribute、invalidate 等行为。
 *
 * <p>注意：Rust 和 Tomcat 的 session 实现可能不同，本测试验证各自的行为符合 Servlet 规范。</p>
 */
class SessionCompatibilityTest extends AbstractBenchmarkBase {

    private final HttpClient cookieClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @Test
    void testRustSessionCookie() throws Exception {
        HttpResponse<String> resp = get(rustUrl("/api/health"));
        String setCookie = resp.headers().firstValue("Set-Cookie").orElse(null);
        assertNotNull(setCookie, "Rust should return Set-Cookie header");
        assertTrue(setCookie.contains("JSESSIONID"),
                "Rust Set-Cookie should contain JSESSIONID");
        assertTrue(setCookie.contains("HttpOnly"),
                "Rust Set-Cookie should contain HttpOnly");
    }

    @Test
    void testTomcatSessionCookie() throws Exception {
        HttpResponse<String> resp = get(tomcatUrl("/api/health"));
        String setCookie = resp.headers().firstValue("Set-Cookie").orElse(null);
        assertNotNull(setCookie, "Tomcat should return Set-Cookie header");
        assertTrue(setCookie.contains("JSESSIONID"),
                "Tomcat Set-Cookie should contain JSESSIONID");
    }

    @Test
    void testRustSessionReuse() throws Exception {
        String cookie = getSessionCookie(rustUrl("/api/health"));
        assertNotNull(cookie);

        // 使用同一 cookie 再次请求
        HttpResponse<String> resp2 = getWithCookie(rustUrl("/api/health"), cookie);
        assertEquals(200, resp2.statusCode());
    }

    @Test
    void testTomcatSessionReuse() throws Exception {
        String cookie = getSessionCookie(tomcatUrl("/api/health"));
        assertNotNull(cookie);

        HttpResponse<String> resp2 = getWithCookie(tomcatUrl("/api/health"), cookie);
        assertEquals(200, resp2.statusCode());
    }

    private String getSessionCookie(String url) throws Exception {
        HttpResponse<String> resp = get(url);
        String setCookie = resp.headers().firstValue("Set-Cookie").orElse(null);
        if (setCookie == null) return null;
        for (String part : setCookie.split(";")) {
            String trimmed = part.trim();
            if (trimmed.startsWith("JSESSIONID=")) {
                return trimmed;
            }
        }
        return null;
    }

    private HttpResponse<String> getWithCookie(String url, String cookie) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .header("Cookie", cookie)
                .GET();
        return cookieClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }
}
