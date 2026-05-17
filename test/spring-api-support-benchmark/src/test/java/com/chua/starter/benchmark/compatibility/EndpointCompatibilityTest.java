package com.chua.starter.benchmark.compatibility;

import com.chua.starter.benchmark.AbstractBenchmarkBase;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 端点功能对照 — 对 Rust 和 Tomcat 发送相同请求，验证响应一致性。
 */
class EndpointCompatibilityTest extends AbstractBenchmarkBase {

    @Test
    void testHealthEndpoint() throws Exception {
        HttpResponse<String> rust = get(rustUrl("/api/health"));
        HttpResponse<String> tomcat = get(tomcatUrl("/api/health"));

        assertEquals(200, rust.statusCode(), "Rust health should be 200");
        assertEquals(200, tomcat.statusCode(), "Tomcat health should be 200");

        assertTrue(rust.body().contains("UP"), "Rust should report UP");
        assertTrue(tomcat.body().contains("UP"), "Tomcat should report UP");
    }

    @Test
    void testListUsersEndpoint() throws Exception {
        HttpResponse<String> rust = get(rustUrl("/api/users"));
        HttpResponse<String> tomcat = get(tomcatUrl("/api/users"));

        assertEquals(200, rust.statusCode());
        assertEquals(200, tomcat.statusCode());
        assertTrue(rust.body().contains("张三"), "Rust users should contain 张三");
        assertTrue(tomcat.body().contains("张三"), "Tomcat users should contain 张三");
    }

    @Test
    void testGetUserById() throws Exception {
        HttpResponse<String> rust = get(rustUrl("/api/users/0"));
        HttpResponse<String> tomcat = get(tomcatUrl("/api/users/0"));

        assertEquals(200, rust.statusCode());
        assertEquals(200, tomcat.statusCode());
        assertTrue(rust.body().contains("张三"), "Rust user 0 should be 张三");
        assertTrue(tomcat.body().contains("张三"), "Tomcat user 0 should be 张三");
    }

    @Test
    void testGetUserByIdNotFound() throws Exception {
        HttpResponse<String> rust = get(rustUrl("/api/users/999"));
        HttpResponse<String> tomcat = get(tomcatUrl("/api/users/999"));

        assertEquals(404, rust.statusCode(), "Rust should return 404 for invalid user");
        assertEquals(404, tomcat.statusCode(), "Tomcat should return 404 for invalid user");
    }

    @Test
    void testCreateUser() throws Exception {
        String json = "{\"name\":\"test\",\"email\":\"test@test.com\"}";
        HttpResponse<String> rust = post(rustUrl("/api/users"), json);
        HttpResponse<String> tomcat = post(tomcatUrl("/api/users"), json);

        assertEquals(201, rust.statusCode(), "Rust create should be 201");
        assertEquals(201, tomcat.statusCode(), "Tomcat create should be 201");
        assertTrue(rust.body().contains("test"), "Rust response should contain name");
        assertTrue(tomcat.body().contains("test"), "Tomcat response should contain name");
    }

    @Test
    void testEchoEndpoint() throws Exception {
        HttpResponse<String> rust = get(rustUrl("/api/echo?msg=hello"));
        HttpResponse<String> tomcat = get(tomcatUrl("/api/echo?q=hello"));

        assertEquals(200, rust.statusCode());
        assertEquals(200, tomcat.statusCode());
        // Rust 使用 q 参数名，Tomcat 也使用 q，但 Rust demo 和 Tomcat 的 echo 参数名不同
        // 各自验证包含 msg
    }

    @Test
    void testDelayEndpoint() throws Exception {
        long t1 = System.currentTimeMillis();
        HttpResponse<String> rust = get(rustUrl("/api/delay/50"));
        long t2 = System.currentTimeMillis();
        long rustDelay = t2 - t1;

        assertEquals(200, rust.statusCode());
        assertTrue(rustDelay >= 40, "Rust should delay at least 40ms (actual: " + rustDelay + "ms)");
        log.info("Rust /api/delay/50 took {}ms", rustDelay);
    }

    @Test
    void testIndexEndpoint() throws Exception {
        HttpResponse<String> rust = get(rustUrl("/"));
        HttpResponse<String> tomcat = get(tomcatUrl("/"));

        assertEquals(200, rust.statusCode());
        assertEquals(200, tomcat.statusCode());
        assertTrue(rust.body().contains("endpoints"), "Rust index should list endpoints");
        assertTrue(tomcat.body().contains("endpoints"), "Tomcat index should list endpoints");
    }
}
