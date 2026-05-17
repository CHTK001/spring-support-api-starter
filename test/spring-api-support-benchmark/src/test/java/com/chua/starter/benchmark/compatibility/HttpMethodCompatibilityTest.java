package com.chua.starter.benchmark.compatibility;

import com.chua.starter.benchmark.AbstractBenchmarkBase;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * HTTP 方法支持对照测试 — 验证 Rust 和 Tomcat 对 GET/POST/PUT/DELETE/HEAD 的支持一致性。
 */
class HttpMethodCompatibilityTest extends AbstractBenchmarkBase {

    @Test
    void testHttpGet() throws Exception {
        assertEquals(200, get(rustUrl("/api/health")).statusCode());
        assertEquals(200, get(tomcatUrl("/api/health")).statusCode());
    }

    @Test
    void testHttpPost() throws Exception {
        String json = "{\"name\":\"post-test\"}";
        assertEquals(201, post(rustUrl("/api/users"), json).statusCode());
        assertEquals(201, post(tomcatUrl("/api/users"), json).statusCode());
    }

    @Test
    void testHttpPut() throws Exception {
        // 两个应用都不支持 PUT，应该返回 405 或 404
        int rustStatus = request(rustUrl("/api/users"), "PUT", "{}").statusCode();
        int tomcatStatus = request(tomcatUrl("/api/users"), "PUT", "{}").statusCode();
        // 只要不是 500 即可（允许 404/405）
        assertTrue(rustStatus < 500, "Rust PUT should not return 500, got: " + rustStatus);
        assertTrue(tomcatStatus < 500, "Tomcat PUT should not return 500, got: " + tomcatStatus);
    }

    @Test
    void testHttpDelete() throws Exception {
        int rustStatus = request(rustUrl("/api/users/0"), "DELETE", null).statusCode();
        int tomcatStatus = request(tomcatUrl("/api/users/0"), "DELETE", null).statusCode();
        assertTrue(rustStatus < 500, "Rust DELETE should not return 500, got: " + rustStatus);
        assertTrue(tomcatStatus < 500, "Tomcat DELETE should not return 500, got: " + tomcatStatus);
    }

    @Test
    void testInvalidMethod() throws Exception {
        int rustStatus = request(rustUrl("/api/health"), "PATCH", null).statusCode();
        int tomcatStatus = request(tomcatUrl("/api/health"), "PATCH", null).statusCode();
        // 允许 404/405
        assertTrue(rustStatus < 500, "Rust PATCH should not return 500, got: " + rustStatus);
        assertTrue(tomcatStatus < 500, "Tomcat PATCH should not return 500, got: " + tomcatStatus);
    }
}
