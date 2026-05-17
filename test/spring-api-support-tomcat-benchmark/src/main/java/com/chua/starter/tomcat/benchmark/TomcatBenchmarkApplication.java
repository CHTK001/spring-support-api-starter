package com.chua.starter.tomcat.benchmark;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Tomcat 压测对比服务 — 与 Rust 服务器相同端点。
 * 默认端口 18082。
 */
@SpringBootApplication
@RestController
public class TomcatBenchmarkApplication {

    private final List<Map<String, Object>> users = new java.util.ArrayList<>();

    public TomcatBenchmarkApplication() {
        addUser("张三", "zhangsan@example.com");
        addUser("李四", "lisi@example.com");
        addUser("yemen", "yemen@example.com");
    }

    public static void main(String[] args) {
        SpringApplication.run(TomcatBenchmarkApplication.class, args);
    }

    @GetMapping("/")
    public Map<String, Object> index() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("server", "Tomcat");
        info.put("framework", "Spring Boot 3.5.9");
        info.put("time", LocalDateTime.now().toString());
        info.put("endpoints", List.of(
                "GET  /                - 服务信息",
                "GET  /api/users       - 用户列表",
                "GET  /api/users/{id}  - 用户详情",
                "POST /api/users       - 创建用户",
                "GET  /api/echo        - 请求回显",
                "GET  /api/delay/{ms}  - 延时模拟",
                "GET  /api/health      - 健康检查"
        ));
        return info;
    }

    @GetMapping("/api/users")
    public List<Map<String, Object>> listUsers() {
        return users;
    }

    @GetMapping("/api/users/{id}")
    public ResponseEntity<Map<String, Object>> getUser(@PathVariable int id) {
        if (id < 0 || id >= users.size()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(users.get(id));
    }

    @PostMapping("/api/users")
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody Map<String, String> body) {
        String name = body.getOrDefault("name", "unknown");
        String email = body.getOrDefault("email", "");
        Map<String, Object> user = addUser(name, email);
        return ResponseEntity.status(201).body(user);
    }

    @GetMapping("/api/echo")
    public Map<String, Object> echo(
            @RequestParam(required = false) String q,
            @RequestHeader(value = "X-Request-Id", required = false) String requestId) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("query", q);
        result.put("requestId", requestId);
        result.put("server", "tomcat");
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    @GetMapping("/api/health")
    public Map<String, String> health() {
        Map<String, String> status = new LinkedHashMap<>();
        status.put("status", "UP");
        status.put("server", "tomcat");
        return status;
    }

    @GetMapping("/api/delay/{ms}")
    public Map<String, Object> delay(@PathVariable long ms) throws InterruptedException {
        long sleep = Math.min(ms, 5000);
        Thread.sleep(sleep);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("delayed", sleep);
        result.put("server", "tomcat");
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    private Map<String, Object> addUser(String name, String email) {
        Map<String, Object> user = new LinkedHashMap<>();
        user.put("id", users.size());
        user.put("name", name);
        user.put("email", email);
        user.put("createdAt", LocalDateTime.now().toString());
        users.add(user);
        return user;
    }
}
