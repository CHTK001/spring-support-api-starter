package com.chua.starter.benchmark.compatibility;

import com.chua.starter.benchmark.AbstractBenchmarkBase;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 内容类型对照 — 验证 Content-Type 处理一致性。
 */
class ContentTypeCompatibilityTest extends AbstractBenchmarkBase {

    @Test
    void testRustJsonContentType() throws Exception {
        HttpResponse<String> resp = get(rustUrl("/api/health"));
        String ct = resp.headers().firstValue("Content-Type").orElse("");
        assertTrue(ct.contains("application/json") || ct.contains("json"),
                "Rust health should return JSON content type, got: " + ct);
    }

    @Test
    void testTomcatJsonContentType() throws Exception {
        HttpResponse<String> resp = get(tomcatUrl("/api/health"));
        String ct = resp.headers().firstValue("Content-Type").orElse("");
        assertTrue(ct.contains("application/json"),
                "Tomcat health should return application/json, got: " + ct);
    }

    @Test
    void testRustPostJsonContentType() throws Exception {
        HttpResponse<String> resp = post(rustUrl("/api/users"), "{\"name\":\"t\"}");
        String ct = resp.headers().firstValue("Content-Type").orElse("");
        assertTrue(ct.contains("json"),
                "Rust POST response should contain json in Content-Type, got: " + ct);
    }

    @Test
    void testRustErrorContentType() throws Exception {
        // 404 页面
        HttpResponse<String> resp = get(rustUrl("/api/nonexistent"));
        String ct = resp.headers().firstValue("Content-Type").orElse("");
        // 允许 text/html 或 application/json
        assertTrue(ct.contains("html") || ct.contains("json"),
                "Rust 404 should return html or json, got: " + ct);
    }
}
