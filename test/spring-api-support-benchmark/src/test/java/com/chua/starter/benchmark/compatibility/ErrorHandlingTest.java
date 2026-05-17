package com.chua.starter.benchmark.compatibility;

import com.chua.starter.benchmark.AbstractBenchmarkBase;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 错误处理对照 — 验证 400/404/405/500 状态码和错误页面行为。
 */
class ErrorHandlingTest extends AbstractBenchmarkBase {

    @Test
    void testNotFound() throws Exception {
        HttpResponse<String> rust = get(rustUrl("/api/nonexistent"));
        HttpResponse<String> tomcat = get(tomcatUrl("/api/nonexistent"));

        assertEquals(404, rust.statusCode(), "Rust should return 404 for unknown path");
        assertEquals(404, tomcat.statusCode(), "Tomcat should return 404 for unknown path");
    }

    @Test
    void testUserNotFound() throws Exception {
        HttpResponse<String> rust = get(rustUrl("/api/users/999"));
        HttpResponse<String> tomcat = get(tomcatUrl("/api/users/999"));

        assertEquals(404, rust.statusCode(), "Rust should return 404 for user not found");
        assertEquals(404, tomcat.statusCode(), "Tomcat should return 404 for user not found");
    }

    @Test
    void testMethodNotAllowed() throws Exception {
        // POST to GET-only endpoint
        HttpResponse<String> rust = request(rustUrl("/api/health"), "POST", null);
        HttpResponse<String> tomcat = request(tomcatUrl("/api/health"), "POST", null);

        // 允许 405 或 404（不同框架处理方式不同）
        assertTrue(rust.statusCode() == 405 || rust.statusCode() == 404,
                "Rust should return 405/404 for wrong method, got: " + rust.statusCode());
        assertTrue(tomcat.statusCode() == 405 || tomcat.statusCode() == 404,
                "Tomcat should return 405/404 for wrong method, got: " + tomcat.statusCode());
    }

    @Test
    void testErrorResponseBody() throws Exception {
        HttpResponse<String> rust = get(rustUrl("/api/nonexistent"));
        // 错误响应应该包含有意义的 body
        assertTrue(rust.body() != null && !rust.body().isEmpty(),
                "Rust 404 should have a non-empty body");
    }
}
