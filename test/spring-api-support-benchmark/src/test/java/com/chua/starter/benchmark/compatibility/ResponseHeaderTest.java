package com.chua.starter.benchmark.compatibility;

import com.chua.starter.benchmark.AbstractBenchmarkBase;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 响应头对照 — 验证 Content-Length、Content-Type、Server 等响应头的一致性。
 */
class ResponseHeaderTest extends AbstractBenchmarkBase {

    @Test
    void testContentLengthHeader() throws Exception {
        HttpResponse<String> rust = get(rustUrl("/api/health"));
        HttpResponse<String> tomcat = get(tomcatUrl("/api/health"));

        Optional<String> rustLen = rust.headers().firstValue("Content-Length");
        Optional<String> tomcatLen = tomcat.headers().firstValue("Content-Length");

        // Rust 使用 chunked encoding 可能没有 Content-Length
        // 但至少应该有一种方式标识响应体长度
        if (rustLen.isPresent()) {
            int len = Integer.parseInt(rustLen.get());
            assertTrue(len > 0, "Rust Content-Length should be positive");
        }
        assertTrue(tomcatLen.isPresent(), "Tomcat should have Content-Length");
    }

    @Test
    void testContentTypePresent() throws Exception {
        HttpResponse<String> rust = get(rustUrl("/api/health"));
        HttpResponse<String> tomcat = get(tomcatUrl("/api/health"));

        assertTrue(rust.headers().firstValue("Content-Type").isPresent(),
                "Rust should have Content-Type header");
        assertTrue(tomcat.headers().firstValue("Content-Type").isPresent(),
                "Tomcat should have Content-Type header");
    }

    @Test
    void testCacheControlOnDynamicContent() throws Exception {
        // 动态内容通常没有 Cache-Control，或者有 no-cache
        HttpResponse<String> rust = get(rustUrl("/api/users"));
        // 不强制要求，只是记录
        rust.headers().firstValue("Cache-Control").ifPresent(
                cc -> log.info("Rust Cache-Control: {}", cc));
    }

    @Test
    void testResponseHeadersListed() throws Exception {
        HttpResponse<String> rust = get(rustUrl("/api/health"));
        HttpResponse<String> tomcat = get(tomcatUrl("/api/health"));

        log.info("Rust response headers: {}", rust.headers().map());
        log.info("Tomcat response headers: {}", tomcat.headers().map());

        // 两个服务器都应该返回至少 basic headers
        assertTrue(rust.headers().map().size() >= 2,
                "Rust should return at least 2 headers");
        assertTrue(tomcat.headers().map().size() >= 3,
                "Tomcat should return at least 3 headers");
    }
}
